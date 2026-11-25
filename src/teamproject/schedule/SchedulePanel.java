package teamproject.schedule;

// 브리핑 담당: 이*주 | 파일별 설명: docs/05_schedule.md

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SchedulePanel extends JPanel {
  private final Connection conn;
  private final int teamId, userId;
  private YearMonth month = YearMonth.now();
  private List<ScheduleDAO.Schedule> schedules = new ArrayList<>();
  private List<ScheduleDAO.Deadline> deadlines = new ArrayList<>();
  private final JLabel monthLabel = new JLabel();
  private final JPanel calendar = new JPanel(new GridLayout(0, 7, 3, 3));
  private final DefaultTableModel model =
      new DefaultTableModel(new Object[] {"번호", "일정", "시작", "종료", "메모"}, 0) {
        public boolean isCellEditable(int r, int c) {
          return false;
        }
      };
  private final JTable table = new JTable(model);

  public SchedulePanel(Connection conn, int teamId, int userId) {
    this.conn = conn;
    this.teamId = teamId;
    this.userId = userId;
    setLayout(new BorderLayout());
    JPanel bar = new JPanel();
    JButton prev = new JButton("이전 달"), next = new JButton("다음 달"), refresh = new JButton("새로고침");
    bar.add(prev);
    bar.add(monthLabel);
    bar.add(next);
    bar.add(refresh);
    add(bar, BorderLayout.NORTH);
    JTabbedPane views = new JTabbedPane();
    JPanel calendarView = new JPanel(new BorderLayout());
    JPanel weekdays = new JPanel(new GridLayout(1, 7));
    for (String day : new String[] {"월", "화", "수", "목", "금", "토", "일"}) {
      weekdays.add(new JLabel(day, SwingConstants.CENTER));
    }
    weekdays.setPreferredSize(new java.awt.Dimension(0, 30));
    calendarView.add(weekdays, BorderLayout.NORTH);
    calendarView.add(calendar, BorderLayout.CENTER);
    views.addTab("월간 달력", calendarView);
    views.addTab("일정 목록", new JScrollPane(table));
    add(views);
    JPanel buttons = new JPanel();
    JButton add = new JButton("일정 등록"),
        edit = new JButton("선택 일정 수정"),
        delete = new JButton("선택 일정 삭제");
    buttons.add(add);
    buttons.add(edit);
    buttons.add(delete);
    JPanel bottom = new JPanel(new BorderLayout());
    bottom.add(new JLabel("[일정] 별도 일정 · [업무] 마감일 표시 (업무 수정은 업무 관리에서)"), BorderLayout.NORTH);
    bottom.add(buttons, BorderLayout.SOUTH);
    add(bottom, BorderLayout.SOUTH);
    prev.addActionListener(
        e -> {
          month = month.minusMonths(1);
          reload();
        });
    next.addActionListener(
        e -> {
          month = month.plusMonths(1);
          reload();
        });
    refresh.addActionListener(e -> reload());
    add.addActionListener(e -> edit(null));
    edit.addActionListener(
        e -> {
          ScheduleDAO.Schedule s = selected();
          if (s != null) edit(s);
        });
    delete.addActionListener(e -> delete());
    reload();
  }

  public void reload() {
    try {
      // 두 조회가 모두 성공했을 때에만 화면의 데이터를 교체합니다.
      List<ScheduleDAO.Schedule> nextSchedules = ScheduleDAO.list(conn, teamId, month);
      List<ScheduleDAO.Deadline> nextDeadlines = ScheduleDAO.listDeadlines(conn, teamId, month);
      schedules = nextSchedules;
      deadlines = nextDeadlines;
      monthLabel.setText(month.toString());
      drawCalendar();
      fillScheduleTable();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  private void drawCalendar() {
    calendar.removeAll();
    // 월요일은 1, 일요일은 7입니다. 목요일이 첫날이면 앞 세 칸을 비웁니다.
    int emptyCellsBeforeFirstDay = month.atDay(1).getDayOfWeek().getValue() - 1;
    for (int i = 0; i < emptyCellsBeforeFirstDay; i++) {
      calendar.add(new JLabel(""));
    }
    LocalDate today = LocalDate.now();
    for (int day = 1; day <= month.lengthOfMonth(); day++) {
      LocalDate date = month.atDay(day);
      JTextArea cell = new JTextArea(dayText(date, schedules, deadlines));
      cell.setEditable(false);
      cell.setLineWrap(true);
      cell.setWrapStyleWord(true);
      cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      if (date.equals(today)) {
        cell.setBackground(new Color(230, 240, 255));
      }
      calendar.add(new JScrollPane(cell));
    }
    while (calendar.getComponentCount() % 7 != 0) {
      calendar.add(new JLabel(""));
    }
    calendar.revalidate();
    calendar.repaint();
  }

  private void fillScheduleTable() {
    model.setRowCount(0);
    for (ScheduleDAO.Schedule schedule : schedules) {
      model.addRow(
          new Object[] {schedule.id, schedule.title, schedule.start, schedule.end, schedule.memo});
    }
  }

  // 날짜 한 칸에 들어갈 글자를 만듭니다. 조회와 화면 표시를 나눠 검사하기 쉽습니다.
  public static String dayText(
      LocalDate date, List<ScheduleDAO.Schedule> schedules, List<ScheduleDAO.Deadline> deadlines) {
    StringBuilder content = new StringBuilder(date.getDayOfMonth() + "일");
    for (ScheduleDAO.Schedule s : schedules) {
      if (!date.isBefore(s.start) && !date.isAfter(s.end)) {
        content.append("\n[일정] ").append(s.title);
      }
    }
    for (ScheduleDAO.Deadline task : deadlines) {
      if (date.equals(task.due)) {
        content
            .append("\n[업무] ")
            .append(task.title)
            .append(" (")
            .append(task.status)
            .append(" · ")
            .append(task.assignee)
            .append(")");
      }
    }
    return content.toString();
  }

  private ScheduleDAO.Schedule selected() {
    int row = table.getSelectedRow();
    if (row < 0) {
      JOptionPane.showMessageDialog(this, "일정 목록 탭에서 일정을 선택하세요.");
      return null;
    }
    return schedules.get(row);
  }

  private void edit(ScheduleDAO.Schedule s) {
    JTextField title = new JTextField(s == null ? "" : s.title),
        start = new JTextField(s == null ? LocalDate.now().toString() : s.start.toString());
    JTextField end = new JTextField(s == null ? LocalDate.now().toString() : s.end.toString()),
        memo = new JTextField(s == null ? "" : s.memo);
    JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
    form.add(new JLabel("일정명"));
    form.add(title);
    form.add(new JLabel("시작 yyyy-MM-dd"));
    form.add(start);
    form.add(new JLabel("종료 yyyy-MM-dd"));
    form.add(end);
    form.add(new JLabel("메모"));
    form.add(memo);
    while (JOptionPane.showConfirmDialog(this, form, "일정 저장", JOptionPane.OK_CANCEL_OPTION)
        == JOptionPane.OK_OPTION) {
      try {
        boolean saved =
            ScheduleDAO.save(
                conn,
                teamId,
                userId,
                s == null ? 0 : s.id,
                title.getText().trim(),
                LocalDate.parse(start.getText().trim()),
                LocalDate.parse(end.getText().trim()),
                memo.getText());
        if (!saved) JOptionPane.showMessageDialog(this, "이미 삭제된 일정입니다.");
        reload();
        return;
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "입력 또는 저장 오류: " + e.getMessage());
      }
    }
  }

  private void delete() {
    ScheduleDAO.Schedule s = selected();
    if (s == null) return;
    if (JOptionPane.showConfirmDialog(this, "일정을 삭제할까요?", "삭제", JOptionPane.YES_NO_OPTION)
        != JOptionPane.YES_OPTION) return;
    try {
      if (!ScheduleDAO.delete(conn, teamId, s.id))
        JOptionPane.showMessageDialog(this, "이미 삭제된 일정입니다.");
      reload();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }
}
