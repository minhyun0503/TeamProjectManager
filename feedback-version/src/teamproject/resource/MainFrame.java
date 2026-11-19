package teamproject.resource;

// 브리핑 담당: 신*준 | 파일별 설명: docs/06_resource.md

import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import teamproject.common.TeamDAO;
import teamproject.member.MemberFrame;
import teamproject.member.NotePanel;
import teamproject.member.UserDAO;
import teamproject.schedule.SchedulePanel;
import teamproject.task.TaskBoardPanel;
import teamproject.task.TaskFrame;

public class MainFrame extends JFrame {

  public MainFrame(Connection conn, UserDAO.User user, TeamDAO.Team team) {
    int teamId = team.getTeamId();
    setTitle("팀 프로젝트 관리 - " + team.getTeamName() + " / " + user.getName());
    setSize(950, 650);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    DashboardPanel dashboardPanel = new DashboardPanel(conn, teamId);
    tabbedPane.addTab("대시보드", dashboardPanel);

    try {
      TaskFrame taskFrame = new TaskFrame(conn, teamId);
      tabbedPane.addTab("업무 관리", taskFrame.getContentPane());
      tabbedPane.addChangeListener(
          e -> {
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
      tabbedPane.addChangeListener(
          e -> {
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
    tabbedPane.addChangeListener(
        e -> {
          if (tabbedPane.getSelectedComponent() == dashboardPanel) {
            dashboardPanel.reload();
          } else if (tabbedPane.getSelectedComponent() == materialPanel) {
            materialPanel.reload();
          }
        });
    TaskBoardPanel board = new TaskBoardPanel(conn, teamId, user.getUserId());
    NotePanel notes = new NotePanel(conn, teamId, user.getUserId());
    SchedulePanel schedules = new SchedulePanel(conn, teamId, user.getUserId());
    IdeaPanel ideas = new IdeaPanel(conn, teamId, user.getUserId());
    ActivityPanel activity = new ActivityPanel(conn, teamId);
    tabbedPane.addTab("업무 보드 · 내 업무", board);
    tabbedPane.addTab("공유 메모", notes);
    tabbedPane.addTab("일정 · 달력", schedules);
    tabbedPane.addTab("아이디어 · 댓글", ideas);
    tabbedPane.addTab("최근 활동", activity);
    tabbedPane.addChangeListener(
        e -> {
          java.awt.Component selected = tabbedPane.getSelectedComponent();
          if (selected == board) board.reload();
          if (selected == notes) notes.reload();
          if (selected == schedules) schedules.reload();
          if (selected == ideas) ideas.reload();
          if (selected == activity) activity.reload();
        });
    add(tabbedPane, BorderLayout.CENTER);
  }
}
