package teamproject.resource;

// 브리핑 담당: 신*준 | 파일별 설명: docs/06_resource.md

import teamproject.common.ActivityDAO;

import java.awt.BorderLayout;
import java.sql.Connection;
import javax.swing.*;

public class ActivityPanel extends JPanel {
  private final Connection conn;
  private final int teamId;
  private final DefaultListModel<String> rows = new DefaultListModel<>();

  public ActivityPanel(Connection conn, int teamId) {
    this.conn = conn;
    this.teamId = teamId;
    setLayout(new BorderLayout());
    add(new JScrollPane(new JList<>(rows)), BorderLayout.CENTER);
    JButton refresh = new JButton("최근 활동 새로고침");
    refresh.addActionListener(e -> reload());
    add(refresh, BorderLayout.NORTH);
    reload();
  }

  public void reload() {
    try {
      rows.clear();
      for (String row : ActivityDAO.recent(conn, teamId)) rows.addElement(row);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "활동 조회 실패: " + e.getMessage());
    }
  }
}
