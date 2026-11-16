package teamproject;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {

    private Connection conn;
    private int teamId;

    private JProgressBar overallProgressBar;
    private JLabel statusSummaryLabel;
    private DefaultTableModel taskSummaryModel;
    private DefaultTableModel materialSummaryModel;
    private JButton refreshButton;

    public DashboardPanel(Connection conn, int teamId) {
        this.conn = conn;
        this.teamId = teamId;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("프로젝트 종합 대시보드 (팀 " + teamId + ")", SwingConstants.LEFT);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        refreshButton = new JButton("새로고침");
        refreshButton.addActionListener(e -> reload());

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(refreshButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel progressCard = new JPanel(new GridLayout(2, 1, 5, 5));
        progressCard.setBorder(BorderFactory.createTitledBorder("전체 프로젝트 진행 현황"));

        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        statusSummaryLabel = new JLabel("현황 정보를 계산 중입니다...", SwingConstants.CENTER);
        statusSummaryLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

        progressCard.add(overallProgressBar);
        progressCard.add(statusSummaryLabel);
        centerPanel.add(progressCard, BorderLayout.NORTH);

        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        String[] taskCols = {"업무명", "담당자", "마감일", "상태"};
        taskSummaryModel = new DefaultTableModel(taskCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable taskTable = new JTable(taskSummaryModel);
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setBorder(BorderFactory.createTitledBorder("주요 / 마감 임박 업무 목록"));
        taskPanel.add(new JScrollPane(taskTable), BorderLayout.CENTER);

        String[] matCols = {"자료명", "URL 링크", "등록일"};
        materialSummaryModel = new DefaultTableModel(matCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable matTable = new JTable(materialSummaryModel);
        JPanel matPanel = new JPanel(new BorderLayout());
        matPanel.setBorder(BorderFactory.createTitledBorder("최근 공유 자료 목록"));
        matPanel.add(new JScrollPane(matTable), BorderLayout.CENTER);

        tablesPanel.add(taskPanel);
        tablesPanel.add(matPanel);

        centerPanel.add(tablesPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        reload();
    }

    public void reload() {
        try {
            // 1. 진행률 및 마감/지연 업무 계산 (InlineProgressDAO 활용)
            InlineProgressData progressData = InlineProgressDAO.load(conn, teamId);
            int percent = progressData.getPercent(0);
            int completed = progressData.getCompletedCount(0);
            int total = progressData.getTotalCount(0);

            overallProgressBar.setValue(percent);
            overallProgressBar.setString(percent + "% (" + completed + " / " + total + " 개 완료)");

            int todayCount = 0;
            int lateCount = 0;
            LocalDate today = LocalDate.now();

            for (InlineProgressData.Task t : progressData.getTasks()) {
                if (!"완료".equals(t.getStatus()) && t.getDueDate() != null) {
                    if (t.getDueDate().equals(today)) {
                        todayCount++;
                    } else if (t.getDueDate().isBefore(today)) {
                        lateCount++;
                    }
                }
            }

            statusSummaryLabel.setText(String.format("총 업무: %d개  |  완료: %d개  |  오늘 마감: %d개  |  지연 업무: %d개",
                    total, completed, todayCount, lateCount));

            // 2. 대시보드 업무 표 데이터 직접 조회 (오류 방지 안전 처리)
            taskSummaryModel.setRowCount(0);
            String taskSql = """
                    SELECT t.title, COALESCE(u.name, '담당자 없음') AS name, t.due_date, t.status
                    FROM tasks t
                    LEFT JOIN users u ON t.assignee_id = u.user_id
                    WHERE t.team_id = ?
                    ORDER BY t.due_date ASC, t.task_id DESC
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(taskSql)) {
                pstmt.setInt(1, teamId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String title = rs.getString("title");
                        String name = rs.getString("name");
                        java.sql.Date dueDate = rs.getDate("due_date");
                        String status = rs.getString("status");

                        taskSummaryModel.addRow(new Object[]{
                            title != null ? title : "",
                            name != null ? name : "",
                            dueDate != null ? dueDate.toString() : "마감일 없음",
                            status != null ? status : ""
                        });
                    }
                }
            }

            // 3. 최근 자료 목록 세팅 (MaterialDAO 활용)
            materialSummaryModel.setRowCount(0);
            List<MaterialDAO.MaterialInfo> materials = MaterialDAO.getRecentMaterials(conn, teamId, 5);
            for (MaterialDAO.MaterialInfo m : materials) {
                materialSummaryModel.addRow(new Object[]{m.getTitle(), m.getUrl(), m.getCreatedAt()});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "대시보드 새로고침 실패: " + e.getMessage());
        }
    }
}