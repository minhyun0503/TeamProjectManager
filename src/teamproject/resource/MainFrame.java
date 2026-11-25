package teamproject.resource;

// 브리핑 담당: 신*준 | 파일별 설명: docs/06_resource.md

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import teamproject.common.TeamDAO;
import teamproject.member.MemberFrame;
import teamproject.member.NotePanel;
import teamproject.member.UserDAO;
import teamproject.schedule.SchedulePanel;
import teamproject.task.TaskBoardPanel;
import teamproject.task.TaskFrame;

public class MainFrame extends JFrame implements ChangeListener {
  private final JTabbedPane tabs = new JTabbedPane();
  private final DashboardPanel dashboard;
  private final TaskFrame tasks;
  private final MemberFrame members;
  private final MaterialPanel materials;
  private final TaskBoardPanel board;
  private final NotePanel notes;
  private final SchedulePanel schedules;
  private final IdeaPanel ideas;
  private final ActivityPanel activity;

  public MainFrame(Connection conn, UserDAO.User user, TeamDAO.Team team) {
    int teamId = team.getTeamId();
    int userId = user.getUserId();
    setTitle("팀 프로젝트 관리 - " + team.getTeamName() + " / " + user.getName());
    setSize(950, 650);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    dashboard = new DashboardPanel(conn, teamId);
    tabs.addTab("대시보드", dashboard);
    tasks = addTaskTab(conn, teamId);
    members = addMemberTab(conn, teamId);
    materials = new MaterialPanel(conn, teamId, userId);
    tabs.addTab("자료 관리", materials);

    board = new TaskBoardPanel(conn, teamId, userId);
    notes = new NotePanel(conn, teamId, userId);
    schedules = new SchedulePanel(conn, teamId, userId);
    ideas = new IdeaPanel(conn, teamId, userId);
    activity = new ActivityPanel(conn, teamId);
    tabs.addTab("업무 보드 · 내 업무", board);
    tabs.addTab("공유 메모", notes);
    tabs.addTab("일정 · 달력", schedules);
    tabs.addTab("아이디어 · 댓글", ideas);
    tabs.addTab("최근 활동", activity);

    // 탭을 모두 만든 뒤 현재 창을 변경 알림을 받을 객체로 등록합니다.
    tabs.addChangeListener(this);
    add(tabs, BorderLayout.CENTER);
  }

  private TaskFrame addTaskTab(Connection conn, int teamId) {
    try {
      TaskFrame frame = new TaskFrame(conn, teamId);
      tabs.addTab("업무 관리", frame.getContentPane());
      return frame;
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(this, "업무 화면 생성 실패: " + e.getMessage());
      return null;
    }
  }

  private MemberFrame addMemberTab(Connection conn, int teamId) {
    try {
      MemberFrame frame = new MemberFrame(conn, teamId);
      tabs.addTab("팀원 관리", frame.getContentPane());
      return frame;
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(this, "팀원 화면 생성 실패: " + e.getMessage());
      return null;
    }
  }

  @Override
  public void stateChanged(ChangeEvent event) {
    Component selectedTab = tabs.getSelectedComponent();
    try {
      if (selectedTab == dashboard) {
        dashboard.reload();
      } else if (tasks != null && selectedTab == tasks.getContentPane()) {
        tasks.reload();
      } else if (members != null && selectedTab == members.getContentPane()) {
        members.reload();
      } else if (selectedTab == materials) {
        materials.reload();
      } else if (selectedTab == board) {
        board.reload();
      } else if (selectedTab == notes) {
        notes.reload();
      } else if (selectedTab == schedules) {
        schedules.reload();
      } else if (selectedTab == ideas) {
        ideas.reload();
      } else if (selectedTab == activity) {
        activity.reload();
      }
    } catch (SQLException e) {
      JOptionPane.showMessageDialog(
          this, tabs.getTitleAt(tabs.getSelectedIndex()) + " 조회 실패: " + e.getMessage());
    }
  }
}
