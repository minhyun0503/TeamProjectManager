import java.awt.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import teamproject.common.*;
import teamproject.member.*;
import teamproject.resource.*;
import teamproject.task.*;

// 지연된 조회 중 선택이 바뀌는 상황과, 팀에서 나간 담당자의 수정창을 검사합니다.
public class ReviewGuiTest {
  static CountDownLatch entered, released;
  static final String URL = "jdbc:h2:mem:review_gui;MODE=MySQL;DB_CLOSE_DELAY=-1";

  public static void pauseRefresh() throws InterruptedException {
    entered.countDown();
    if (!released.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Test timeout");
  }

  static Object field(Object obj, String name) throws Exception {
    Field f = obj.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f.get(obj);
  }

  static void waitDone(Object panel) throws Exception {
    for (int i = 0; i < 100; i++) {
      final boolean[] busy = {true};
      SwingUtilities.invokeAndWait(
          () -> {
            try {
              busy[0] = (boolean) field(panel, "loading");
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      if (!busy[0]) return;
      Thread.sleep(40);
    }
    throw new AssertionError("Refresh did not finish");
  }

  static void reload(Object panel) {
    if (panel instanceof NotePanel) ((NotePanel) panel).reload();
    else ((IdeaPanel) panel).reload();
  }

  static void verifySelection(Object panel) throws Exception {
    JList<?> list = (JList<?>) field(panel, "list");
    SwingUtilities.invokeAndWait(
        () -> {
          reload(panel);
        });
    waitDone(panel);
    SwingUtilities.invokeAndWait(() -> list.setSelectedIndex(0));
    entered = new CountDownLatch(1);
    released = new CountDownLatch(1);
    DB_connection.configure(URL + ";INIT=CALL PAUSE_REFRESH()", "sa", "");
    SwingUtilities.invokeAndWait(() -> reload(panel));
    if (!entered.await(3, TimeUnit.SECONDS)) throw new AssertionError("Reader was not paused");
    SwingUtilities.invokeAndWait(() -> list.setSelectedIndex(1));
    Object expected = list.getSelectedValue();
    released.countDown();
    waitDone(panel);
    if (list.getSelectedIndex() != 1) throw new AssertionError("Selection reverted during refresh");
    int expectedId =
        expected instanceof NoteDAO.Note
            ? ((NoteDAO.Note) expected).id
            : ((IdeaDAO.Idea) expected).id;
    Object actual = list.getSelectedValue();
    int actualId =
        actual instanceof NoteDAO.Note ? ((NoteDAO.Note) actual).id : ((IdeaDAO.Idea) actual).id;
    if (actualId != expectedId) throw new AssertionError("Wrong item remained selected");
    DB_connection.configure(URL, "sa", "");
  }

  static JComboBox<?> assigneeBox(Container c) {
    for (Component child : c.getComponents()) {
      if (child instanceof JComboBox<?>) {
        JComboBox<?> box = (JComboBox<?>) child;
        if (box.getItemCount() > 0 && box.getItemAt(0) instanceof TaskDAO.Assignee) return box;
      }
      if (child instanceof Container) {
        JComboBox<?> found = assigneeBox((Container) child);
        if (found != null) return found;
      }
    }
    return null;
  }

  public static void main(String[] args) throws Exception {
    DB_connection.configure(URL, "sa", "");
    Connection conn = DB_connection.open();
    SchemaSetup.initialize(conn, true);
    try (Statement s = conn.createStatement()) {
      s.execute("CREATE ALIAS PAUSE_REFRESH FOR \"ReviewGuiTest.pauseRefresh\"");
    }
    NoteDAO.save(conn, 7, 10, 0, 0, "두 번째 메모", "");
    IdeaDAO.save(conn, 7, 10, 0, "두 번째 의견", "", "");
    NotePanel[] notes = new NotePanel[1];
    IdeaPanel[] ideas = new IdeaPanel[1];
    JFrame[] frame = new JFrame[1];
    JTabbedPane[] tabs = new JTabbedPane[1];
    SwingUtilities.invokeAndWait(
        () -> {
          frame[0] = new JFrame();
          tabs[0] = new JTabbedPane();
          notes[0] = new NotePanel(conn, 7, 10);
          ideas[0] = new IdeaPanel(conn, 7, 10);
          tabs[0].addTab("메모", notes[0]);
          tabs[0].addTab("의견", ideas[0]);
          frame[0].add(tabs[0]);
          frame[0].setSize(900, 600);
          frame[0].setVisible(true);
        });
    verifySelection(notes[0]);
    SwingUtilities.invokeAndWait(() -> tabs[0].setSelectedIndex(1));
    verifySelection(ideas[0]);
    MemberDAO.deleteMember(conn, 7, 30);
    int id;
    try (Statement s = conn.createStatement();
        ResultSet r = s.executeQuery("SELECT task_id FROM tasks WHERE assignee_id=30")) {
      r.next();
      id = r.getInt(1);
    }
    TaskDAO.TaskInfo old = TaskDAO.findTask(conn, 7, id);
    TaskFrame[] taskFrame = new TaskFrame[1];
    SwingUtilities.invokeAndWait(
        () -> {
          try {
            taskFrame[0] = new TaskFrame(conn, 7);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    Method edit =
        TaskFrame.class.getDeclaredMethod(
            "showTaskInputDialog", String.class, TaskDAO.TaskInfo.class);
    edit.setAccessible(true);
    AtomicReference<Throwable> error = new AtomicReference<>();
    CountDownLatch finished = new CountDownLatch(1);
    SwingUtilities.invokeLater(
        () -> {
          try {
            edit.invoke(taskFrame[0], "담당자 검사", old);
          } catch (Throwable e) {
            error.set(e);
          } finally {
            finished.countDown();
          }
        });
    final boolean[] checked = {false};
    for (int i = 0; i < 60 && !checked[0]; i++) {
      SwingUtilities.invokeAndWait(
          () -> {
            for (Window w : Window.getWindows())
              if (w instanceof JDialog && w.isShowing()) {
                JComboBox<?> box = assigneeBox(w);
                if (box != null) {
                  if (box.getSelectedIndex() != -1)
                    error.set(new AssertionError("Departed assignee silently replaced"));
                  checked[0] = true;
                  w.dispose();
                }
              }
          });
      Thread.sleep(50);
    }
    if (!checked[0] || !finished.await(3, TimeUnit.SECONDS) || error.get() != null)
      throw new AssertionError("Assignee check failed", error.get());
    SwingUtilities.invokeAndWait(
        () -> {
          for (Window w : Window.getWindows()) w.dispose();
        });
    conn.close();
    System.out.println("PASS: note/idea selection during slow refresh; departed-assignee dialog");
    System.exit(0);
  }
}
