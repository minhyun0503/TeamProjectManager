package teamproject.resource;

import teamproject.common.TeamDAO;
import teamproject.member.MemberFrame;
import teamproject.member.UserDAO;
import teamproject.task.TaskFrame;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {

    public MainFrame(Connection conn, UserDAO.User user, TeamDAO.Team team) {
        int teamId = team.getTeamId();
        setTitle("팀 프로젝트 관리 - " + team.getTeamName() + " / " + user.getName());
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        DashboardPanel dashboardPanel = new DashboardPanel(conn, teamId);
        tabbedPane.addTab("대시보드", dashboardPanel);

        try {
            TaskFrame taskFrame = new TaskFrame(conn, teamId);
            tabbedPane.addTab("업무 관리", taskFrame.getContentPane());
            tabbedPane.addChangeListener(e -> {
                if (tabbedPane.getSelectedComponent() == taskFrame.getContentPane()) {
                    try {
                        taskFrame.reload();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "업무 조회 실패: " + ex.getMessage());
                    }
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "업무 화면 생성 실패: " + e.getMessage());
        }

        try {
            MemberFrame memberFrame = new MemberFrame(conn, teamId);
            tabbedPane.addTab("팀원 관리", memberFrame.getContentPane());
            tabbedPane.addChangeListener(e -> {
                if (tabbedPane.getSelectedComponent() == memberFrame.getContentPane()) {
                    try {
                        memberFrame.reload();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "팀원 조회 실패: " + ex.getMessage());
                    }
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "팀원 화면 생성 실패: " + e.getMessage());
        }

        MaterialPanel materialPanel = new MaterialPanel(conn, teamId, user.getUserId());
        tabbedPane.addTab("자료 관리", materialPanel);
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == dashboardPanel) {
                dashboardPanel.reload();
            } else if (tabbedPane.getSelectedComponent() == materialPanel) {
                materialPanel.reload();
            }
        });
        add(tabbedPane, BorderLayout.CENTER);
    }
}
