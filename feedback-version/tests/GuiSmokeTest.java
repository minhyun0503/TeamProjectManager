import java.awt.*;
import java.nio.file.*;
import java.sql.Connection;
import javax.imageio.ImageIO;
import javax.swing.*;
import teamproject.common.*;
import teamproject.member.*;
import teamproject.resource.*;

// 디스플레이가 있는 환경에서 별도로 실행하는 화면 통합 검사입니다.
public class GuiSmokeTest {
  static MainFrame frame;
  static JTabbedPane tabs;

  public static void main(String[] args) throws Exception {
    DB_connection.configure("jdbc:h2:mem:gui_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    Connection conn = DB_connection.open();
    SchemaSetup.initialize(conn, true);
    SwingUtilities.invokeAndWait(
        () -> {
          frame =
              new MainFrame(conn, new UserDAO.User(10, "데모팀원1"), new TeamDAO.Team(7, "시연 프로젝트"));
          frame.setSize(1250, 850);
          frame.setLocation(30, 30);
          frame.setVisible(true);
          tabs = (JTabbedPane) frame.getContentPane().getComponent(0);
          if (tabs.getTabCount() != 9) throw new AssertionError("Expected nine feature tabs");
        });
    Path dir = Path.of(args.length == 0 ? "docs/screenshots" : args[0]);
    Files.createDirectories(dir);
    for (int i = 0; i < tabs.getTabCount(); i++) {
      final int index = i;
      SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(index));
      Thread.sleep(900);
      if (i == 5 || i == 7) {
        try (Connection other = DB_connection.open()) {
          if (i == 5) NoteDAO.save(other, 7, 20, 0, 0, "다른 창에서 등록한 메모", "자동 갱신으로 보이는 내용입니다.");
          else
            IdeaDAO.save(other, 7, 20, 0, "다른 창의 아이디어", "의견을 댓글로 남겨 주세요.", "https://example.com");
        }
        Thread.sleep(i == 5 ? 3500 : 5500);
        SwingUtilities.invokeAndWait(
            () -> {
              JList<?> list = findList(tabs.getSelectedComponent());
              if (list == null || list.getModel().getSize() < 2)
                throw new AssertionError(
                    "Automatic refresh did not show another connection's data");
              list.setSelectedIndex(0);
            });
      }
      for (Window w : Window.getWindows())
        if (w instanceof JDialog && w.isVisible())
          throw new AssertionError("Unexpected dialog while opening tab " + i);
      if (i == 4 || i == 5 || i == 6 || i == 7) {
        final int n = i;
        SwingUtilities.invokeAndWait(
            () -> {
              try {
                java.awt.image.BufferedImage image =
                    new Robot()
                        .createScreenCapture(
                            new Rectangle(frame.getLocationOnScreen(), frame.getSize()));
                ImageIO.write(image, "png", dir.resolve("tab-" + n + ".png").toFile());
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
      }
    }
    SwingUtilities.invokeAndWait(
        () -> {
          for (Window w : Window.getWindows()) w.dispose();
        });
    conn.close();
    System.out.println("PASS: nine GUI tabs and automatic note/idea refresh");
    System.exit(0);
  }

  private static JList<?> findList(Component root) {
    if (root instanceof JList<?>) return (JList<?>) root;
    if (root instanceof Container) {
      for (Component child : ((Container) root).getComponents()) {
        JList<?> result = findList(child);
        if (result != null) return result;
      }
    }
    return null;
  }
}
