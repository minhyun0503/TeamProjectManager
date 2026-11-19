package teamproject.resource;

// 브리핑 담당: 신*준 | 파일별 설명: docs/06_resource.md

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.net.URI;
import java.sql.Connection;
import java.util.List;
import javax.swing.*;
import teamproject.common.DB_connection;

public class IdeaPanel extends JPanel {
  private final Connection conn;
  private final int teamId, userId;
  private final DefaultListModel<IdeaDAO.Idea> model = new DefaultListModel<>();
  private final JList<IdeaDAO.Idea> list = new JList<>(model);
  private final JTextArea detail = new JTextArea(), comments = new JTextArea();
  private final JTextField input = new JTextField();
  private final JLabel status = new JLabel("5초 간격 갱신 · 공감은 한 사람당 한 번, 다시 누르면 취소");
  private final Timer timer;
  private boolean loading;

  public IdeaPanel(Connection conn, int teamId, int userId) {
    this.conn = conn;
    this.teamId = teamId;
    this.userId = userId;
    setLayout(new BorderLayout(5, 5));
    detail.setEditable(false);
    detail.setLineWrap(true);
    detail.setWrapStyleWord(true);
    comments.setEditable(false);
    comments.setLineWrap(true);
    comments.setWrapStyleWord(true);
    list.setCellRenderer(new IdeaCardRenderer());
    list.setFixedCellHeight(95);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(
        e -> {
          if (!e.getValueIsAdjusting()) showDetail();
        });
    JPanel right = new JPanel(new BorderLayout());
    JSplitPane texts =
        new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, new JScrollPane(detail), new JScrollPane(comments));
    texts.setResizeWeight(0.5);
    right.add(texts);
    JButton comment = new JButton("댓글 등록");
    JPanel bottom = new JPanel(new BorderLayout());
    bottom.add(input);
    bottom.add(comment, BorderLayout.EAST);
    right.add(bottom, BorderLayout.SOUTH);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), right);
    split.setResizeWeight(0.4);
    add(split);
    JPanel buttons = new JPanel();
    JButton add = new JButton("아이디어 등록"),
        edit = new JButton("내 글 수정"),
        delete = new JButton("내 글 삭제");
    JButton like = new JButton("공감 / 취소"),
        link = new JButton("링크 열기"),
        refresh = new JButton("새로고침");
    for (JButton b : new JButton[] {add, edit, delete, like, link, refresh}) buttons.add(b);
    add(buttons, BorderLayout.NORTH);
    add(status, BorderLayout.SOUTH);
    add.addActionListener(e -> edit(null));
    edit.addActionListener(
        e -> {
          IdeaDAO.Idea idea = selected();
          if (idea != null) edit(idea);
        });
    delete.addActionListener(e -> delete());
    like.addActionListener(e -> like());
    comment.addActionListener(e -> comment());
    input.addActionListener(e -> comment());
    link.addActionListener(e -> openLink());
    refresh.addActionListener(e -> reload());
    timer =
        new Timer(
            5000,
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

  public void reload() {
    if (loading) return;
    loading = true;
    new SwingWorker<List<IdeaDAO.Idea>, Void>() {
      protected List<IdeaDAO.Idea> doInBackground() throws Exception {
        try (Connection read = DB_connection.open()) {
          return IdeaDAO.list(read, teamId);
        }
      }

      protected void done() {
        try {
          List<IdeaDAO.Idea> ideas = get();
          if (!isDisplayable()) return;
          int selectedId = list.getSelectedValue() == null ? -1 : list.getSelectedValue().id;
          model.clear();
          for (IdeaDAO.Idea idea : ideas) {
            model.addElement(idea);
            if (idea.id == selectedId) list.setSelectedValue(idea, true);
          }
          status.setText("자동 갱신 완료 · " + ideas.size() + "개 아이디어 · 공감은 한 사람당 한 번");
        } catch (Exception e) {
          status.setText("갱신 실패 · 연결을 확인한 뒤 새로고침하세요.");
        } finally {
          loading = false;
        }
      }
    }.execute();
  }

  private IdeaDAO.Idea selected() {
    IdeaDAO.Idea idea = list.getSelectedValue();
    if (idea == null) JOptionPane.showMessageDialog(this, "아이디어를 선택하세요.");
    return idea;
  }

  private void showDetail() {
    IdeaDAO.Idea idea = list.getSelectedValue();
    if (idea == null) {
      detail.setText("");
      comments.setText("");
      return;
    }
    detail.setText(
        idea.title
            + "\n작성자: "
            + idea.author
            + "\n링크: "
            + idea.link
            + "\n공감: "
            + idea.likes
            + "\n\n"
            + idea.content);
    try {
      comments.setText(String.join("\n\n", IdeaDAO.comments(conn, teamId, idea.id)));
    } catch (Exception e) {
      comments.setText("댓글 조회 실패: " + e.getMessage());
    }
  }

  private void edit(IdeaDAO.Idea idea) {
    if (idea != null && idea.authorId != userId) {
      JOptionPane.showMessageDialog(this, "본인이 작성한 글만 수정할 수 있습니다.");
      return;
    }
    JTextField title = new JTextField(idea == null ? "" : idea.title),
        link = new JTextField(idea == null ? "" : idea.link);
    JTextArea content = new JTextArea(idea == null ? "" : idea.content, 10, 40);
    content.setLineWrap(true);
    content.setWrapStyleWord(true);
    JPanel form = new JPanel(new BorderLayout(4, 4));
    JPanel top = new JPanel(new java.awt.GridLayout(0, 1));
    top.add(new JLabel("제목"));
    top.add(title);
    top.add(new JLabel("참고 링크 (선택)"));
    top.add(link);
    form.add(top, BorderLayout.NORTH);
    form.add(new JScrollPane(content));
    while (JOptionPane.showConfirmDialog(this, form, "아이디어 저장", JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION) {
      try {
        if (!IdeaDAO.save(
            conn,
            teamId,
            userId,
            idea == null ? 0 : idea.id,
            title.getText().trim(),
            content.getText(),
            link.getText().trim())) JOptionPane.showMessageDialog(this, "이미 삭제됐거나 수정 권한이 없습니다.");
        reload();
        return;
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage());
      }
    }
  }

  private void delete() {
    IdeaDAO.Idea idea = selected();
    if (idea == null) return;
    if (JOptionPane.showConfirmDialog(this, "글과 댓글·공감을 삭제할까요?", "삭제", JOptionPane.YES_NO_OPTION)
        != JOptionPane.YES_OPTION) return;
    try {
      if (!IdeaDAO.delete(conn, teamId, userId, idea.id))
        JOptionPane.showMessageDialog(this, "본인이 작성한 글만 삭제할 수 있습니다.");
      reload();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  private void like() {
    IdeaDAO.Idea idea = selected();
    if (idea == null) return;
    try {
      IdeaDAO.toggleLike(conn, teamId, userId, idea.id);
      reload();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  private void comment() {
    IdeaDAO.Idea idea = selected();
    if (idea == null) return;
    try {
      IdeaDAO.comment(conn, teamId, userId, idea.id, input.getText().trim());
      input.setText("");
      showDetail();
      reload();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  private void openLink() {
    IdeaDAO.Idea idea = selected();
    if (idea == null) return;
    try {
      if (idea.link == null || idea.link.isBlank()) {
        JOptionPane.showMessageDialog(this, "첨부한 링크가 없습니다.");
        return;
      }
      if (!Desktop.isDesktopSupported()
          || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        JOptionPane.showMessageDialog(this, "브라우저에서 직접 열어 주세요: " + idea.link);
        return;
      }
      Desktop.getDesktop().browse(URI.create(idea.link));
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "링크 열기 실패: " + e.getMessage());
    }
  }
}
