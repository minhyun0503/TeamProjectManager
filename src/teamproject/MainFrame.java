package teamproject;

import java.awt.BorderLayout;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

// 로그인 후 열리는 통합 화면. 기능별 화면을 탭으로 모아 보여준다.
public class MainFrame extends JFrame {

    private Connection conn;
    private int teamId = LoginUser.TEAM_ID; // 현재는 1번 팀 고정

    public MainFrame(Connection conn) {
        this.conn = conn;

        setTitle("팀 프로젝트 통합 관리 시스템  -  " + LoginUser.getName() + "님");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 협업 기능(메모장 · 포스트잇 · 문서)용 표가 없으면 만든다
        try {
            CollabSchema.prepare(conn);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "협업 기능 표를 준비하지 못했습니다.\n메모장 · 아이디어 보드 · 문서 탭이 동작하지 않을 수 있습니다.\n" + e.getMessage(),
                    "DB 준비 오류", JOptionPane.WARNING_MESSAGE);
        }

        JTabbedPane tabbedPane = new JTabbedPane();

        // ── 프로젝트 현황 ──
        DashboardPanel dashboardPanel = new DashboardPanel(conn, teamId);
        tabbedPane.addTab("대시보드", dashboardPanel);

        // ── 업무 ──
        try {
            TaskFrame taskFrame = new TaskFrame(conn);
            tabbedPane.addTab("업무 관리", taskFrame.getContentPane());
        } catch (Exception e) {
            e.printStackTrace();
        }

        KanbanPanel kanbanPanel = new KanbanPanel(conn, teamId);
        tabbedPane.addTab("칸반 보드", kanbanPanel);

        SchedulePanel schedulePanel = new SchedulePanel(conn, teamId);
        tabbedPane.addTab("일정", schedulePanel);

        // ── 팀 · 자료 ──
        try {
            MemberFrame memberFrame = new MemberFrame(conn, teamId);
            tabbedPane.addTab("팀원 관리", memberFrame.getContentPane());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MaterialPanel materialPanel = new MaterialPanel(conn, teamId);
        tabbedPane.addTab("자료 관리", materialPanel);

        // ── 협업 (실시간 메모장 · 패들렛 · 노션 스타일) ──
        MemoPanel memoPanel = new MemoPanel(conn, teamId);
        tabbedPane.addTab("팀 메모장", memoPanel);

        BoardPanel boardPanel = new BoardPanel(conn, teamId);
        tabbedPane.addTab("아이디어 보드", boardPanel);

        PagePanel pagePanel = new PagePanel(conn, teamId);
        tabbedPane.addTab("문서", pagePanel);

        // 탭을 바꿀 때마다 그 화면을 최신 내용으로 다시 읽는다
        // (탭 번호 대신 화면 객체로 비교해서, 탭 순서를 바꿔도 안전하다)
        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();

            if (selected == dashboardPanel) {
                dashboardPanel.reload();
            } else if (selected == kanbanPanel) {
                kanbanPanel.reload();
            } else if (selected == schedulePanel) {
                schedulePanel.reload();
            } else if (selected == materialPanel) {
                materialPanel.reload();
            } else if (selected == memoPanel) {
                memoPanel.reload();
            } else if (selected == boardPanel) {
                boardPanel.reload();
            } else if (selected == pagePanel) {
                pagePanel.reload();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }
}
