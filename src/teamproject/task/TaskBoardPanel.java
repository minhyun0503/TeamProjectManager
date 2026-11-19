package teamproject.task;

// 브리핑 담당: 윤*훈 | 파일별 설명: docs/04_task.md

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

// 윤*훈 담당 추가: 같은 업무 데이터를 상태별 세 칸에 분류해 보여줍니다.
public class TaskBoardPanel extends JPanel {
  private final Connection conn;
  private final int teamId, userId;
  private final JCheckBox mine = new JCheckBox("내 업무만 보기");
  private final List<DefaultListModel<TaskDAO.Card>> models = new ArrayList<>();
  private final List<JList<TaskDAO.Card>> lists = new ArrayList<>();

  public TaskBoardPanel(Connection conn, int teamId, int userId) {
    this.conn = conn;
    this.teamId = teamId;
    this.userId = userId;
    setLayout(new BorderLayout(6, 6));
    JPanel top = new JPanel();
    JButton refresh = new JButton("새로고침");
    top.add(mine);
    top.add(refresh);
    add(top, BorderLayout.NORTH);
    JPanel columns = new JPanel(new GridLayout(1, 3, 6, 6));
    for (String status : TaskDAO.STATUS_LIST) {
      DefaultListModel<TaskDAO.Card> model = new DefaultListModel<>();
      JList<TaskDAO.Card> list = new JList<>(model);
      list.setCellRenderer(new TaskCardRenderer());
      list.setFixedCellHeight(95);
      models.add(model);
      lists.add(list);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener(
          e -> {
            if (!e.getValueIsAdjusting() && list.getSelectedValue() != null)
              for (JList<TaskDAO.Card> other : lists) if (other != list) other.clearSelection();
          });
      JPanel column = new JPanel(new BorderLayout());
      column.setBorder(BorderFactory.createTitledBorder(status));
      column.add(new JScrollPane(list));
      columns.add(column);
    }
    add(columns, BorderLayout.CENTER);
    JPanel buttons = new JPanel();
    for (String status : TaskDAO.STATUS_LIST) {
      JButton move = new JButton(status + " 상태로 변경");
      move.addActionListener(e -> move(status));
      buttons.add(move);
    }
    add(buttons, BorderLayout.SOUTH);
    mine.addActionListener(e -> reload());
    refresh.addActionListener(e -> reload());
    reload();
  }

  public void reload() {
    try {
      for (DefaultListModel<TaskDAO.Card> model : models) model.clear();
      for (TaskDAO.Card card : TaskDAO.board(conn, teamId, userId, mine.isSelected())) {
        for (int i = 0; i < TaskDAO.STATUS_LIST.length; i++)
          if (TaskDAO.STATUS_LIST[i].equals(card.status)) models.get(i).addElement(card);
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  private void move(String status) {
    TaskDAO.Card selected = null;
    for (JList<TaskDAO.Card> list : lists)
      if (list.getSelectedValue() != null) selected = list.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(this, "변경할 업무 카드를 선택하세요.");
      return;
    }
    try {
      if (!TaskDAO.updateTaskStatus(conn, teamId, selected.id, status))
        JOptionPane.showMessageDialog(this, "이미 삭제된 업무입니다.");
      reload();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }
}
