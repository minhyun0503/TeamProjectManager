package teamproject;

import java.awt.BorderLayout;
import java.sql.Connection;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

    private Connection conn;
    private int teamId = 1;

    public MainFrame(Connection conn) {
        this.conn = conn;

        setTitle("팀 프로젝트 통합 관리 시스템");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        DashboardPanel dashboardPanel = new DashboardPanel(conn, teamId);
        tabbedPane.addTab("대시보드", dashboardPanel);

        try {
            TaskFrame taskFrame = new TaskFrame(conn);
            tabbedPane.addTab("업무 관리", taskFrame.getContentPane());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MemberFrame memberFrame = new MemberFrame(conn, teamId);
            tabbedPane.addTab("팀원 관리", memberFrame.getContentPane());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MaterialPanel materialPanel = new MaterialPanel(conn, teamId);
        tabbedPane.addTab("자료 관리", materialPanel);

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                dashboardPanel.reload();
            } else if (selectedIndex == 3) {
                materialPanel.reload();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }
}