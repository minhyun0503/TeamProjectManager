package teamproject.member;

// 브리핑 담당: 이*찬 | 파일별 설명: docs/03_member.md

import java.awt.BorderLayout;
import java.sql.Connection;
import java.util.List;
import javax.swing.*;
import teamproject.common.DB_connection;
import teamproject.task.TaskFromNoteDialog;

// 화면 목록은 자동 갱신하고, 편집창의 입력은 자동으로 덮어쓰지 않습니다.
public class NotePanel extends JPanel {
  private final Connection conn;
  private final int teamId, userId;
  private final DefaultListModel<NoteDAO.Note> model = new DefaultListModel<>();
  private final JList<NoteDAO.Note> list = new JList<>(model);
  private final JTextArea detail = new JTextArea();
  private final JLabel status = new JLabel("3초 간격 자동 갱신 · 같은 데모 DB로 두 창을 열어 시연하세요.");
  private final Timer timer;
  private boolean loading;

  public NotePanel(Connection conn, int teamId, int userId) {
    this.conn = conn;
    this.teamId = teamId;
    this.userId = userId;
    setLayout(new BorderLayout(6, 6));
    detail.setEditable(false);
    detail.setLineWrap(true);
    detail.setWrapStyleWord(true);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(
        e -> {
          NoteDAO.Note n = list.getSelectedValue();
          detail.setText(n == null ? "" : n.content);
        });
    JSplitPane split =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), new JScrollPane(detail));
    split.setResizeWeight(0.45);
    add(split, BorderLayout.CENTER);
    add(status, BorderLayout.SOUTH);
    JPanel buttons = new JPanel();
    JButton add = new JButton("새 메모"), edit = new JButton("편집"), delete = new JButton("삭제");
    JButton history = new JButton("내용 이력"),
        task = new JButton("업무로 등록"),
        refresh = new JButton("새로고침");
    for (JButton b : new JButton[] {add, edit, delete, history, task, refresh}) buttons.add(b);
    add(buttons, BorderLayout.NORTH);
    add.addActionListener(e -> edit(null));
    edit.addActionListener(
        e -> {
          if (selected() != null) edit(list.getSelectedValue());
        });
    delete.addActionListener(e -> delete());
    history.addActionListener(e -> history());
    task.addActionListener(
        e -> {
          NoteDAO.Note n = selected();
          if (n != null) TaskFromNoteDialog.open(this, conn, teamId, n);
        });
    refresh.addActionListener(e -> reload());
    timer =
        new Timer(
            3000,
            e -> {
              if (isShowing()) reload();
            });
  }

  @Override
  public void addNotify() {
    super.addNotify();
    timer.start();
  }

  @Override
  public void removeNotify() {
    timer.stop();
    super.removeNotify();
  }

  // doInBackground는 SQL만, done은 Swing 화면만 처리합니다.
  public void reload() {
    if (loading) return;
    loading = true;
    new SwingWorker<List<NoteDAO.Note>, Void>() {
      protected List<NoteDAO.Note> doInBackground() throws Exception {
        try (Connection read = DB_connection.open()) {
          return NoteDAO.list(read, teamId);
        }
      }

      protected void done() {
        try {
          List<NoteDAO.Note> notes = get();
          if (!isDisplayable()) return;
          int selectedId = list.getSelectedValue() == null ? -1 : list.getSelectedValue().id;
          model.clear();
          for (NoteDAO.Note n : notes) {
            model.addElement(n);
            if (n.id == selectedId) list.setSelectedValue(n, true);
          }
          status.setText("자동 갱신 완료 · " + notes.size() + "개 메모 · 갱신 간격 3초");
        } catch (Exception e) {
          status.setText("갱신 실패 · 연결을 확인한 뒤 새로고침하세요.");
        } finally {
          loading = false;
        }
      }
    }.execute();
  }

  private NoteDAO.Note selected() {
    NoteDAO.Note n = list.getSelectedValue();
    if (n == null) JOptionPane.showMessageDialog(this, "메모를 선택하세요.");
    return n;
  }

  private void edit(NoteDAO.Note base) {
    JTextField title = new JTextField(base == null ? "" : base.title);
    JTextArea content = new JTextArea(base == null ? "" : base.content, 12, 40);
    content.setLineWrap(true);
    content.setWrapStyleWord(true);
    JPanel form = new JPanel(new BorderLayout(5, 5));
    form.add(title, BorderLayout.NORTH);
    form.add(new JScrollPane(content), BorderLayout.CENTER);
    while (JOptionPane.showConfirmDialog(
            this, form, base == null ? "새 공유 메모" : "공유 메모 편집", JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION) {
      try {
        boolean saved =
            NoteDAO.save(
                conn,
                teamId,
                userId,
                base == null ? 0 : base.id,
                base == null ? 0 : base.version,
                title.getText().trim(),
                content.getText());
        if (saved) {
          reload();
          return;
        }
        JOptionPane.showMessageDialog(
            this,
            "다른 팀원이 먼저 수정했거나 삭제했습니다.\n내 입력을 복사한 뒤 취소하고 최신 메모를 다시 열어 합쳐 주세요.\n현재 입력은 이 편집창에 유지됩니다.");
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "저장 실패: " + e.getMessage());
      }
    }
  }

  private void delete() {
    NoteDAO.Note n = selected();
    if (n == null) return;
    if (JOptionPane.showConfirmDialog(
            this, "선택한 메모와 내용 이력을 삭제할까요?", "메모 삭제", JOptionPane.YES_NO_OPTION)
        != JOptionPane.YES_OPTION) return;
    try {
      if (!NoteDAO.delete(conn, teamId, n.id, n.version))
        JOptionPane.showMessageDialog(this, "메모가 변경되었습니다. 최신 내용을 확인하세요.");
      reload();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  private void history() {
    NoteDAO.Note n = selected();
    if (n == null) return;
    try {
      JTextArea area = new JTextArea(NoteDAO.history(conn, teamId, n.id), 18, 50);
      area.setEditable(false);
      JOptionPane.showMessageDialog(
          this, new JScrollPane(area), "메모 내용 이력", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }
}
