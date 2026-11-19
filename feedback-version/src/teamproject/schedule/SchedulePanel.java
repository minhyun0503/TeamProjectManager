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
    add(buttons, BorderLayout.SOUTH);
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
      schedules = ScheduleDAO.list(conn, teamId, month);
      monthLabel.setText(month.toString());
      model.setRowCount(0);
      calendar.removeAll();

      int offset = month.atDay(1).getDayOfWeek().getValue() - 1;
      for (int i = 0; i < offset; i++) calendar.add(new JLabel(""));
      for (int day = 1; day <= month.lengthOfMonth(); day++) {
        LocalDate date = month.atDay(day);
        StringBuilder content = new StringBuilder(day + "일");
        for (ScheduleDAO.Schedule s : schedules)
          if (!date.isBefore(s.start) && !date.isAfter(s.end)) content.append("\n").append(s.title);
        JTextArea cell = new JTextArea(content.toString());
        cell.setEditable(false);
        cell.setLineWrap(true);
        cell.setWrapStyleWord(true);
        cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        if (date.equals(LocalDate.now())) cell.setBackground(new Color(230, 240, 255));
        calendar.add(new JScrollPane(cell));
      }
      while (calendar.getComponentCount() % 7 != 0) calendar.add(new JLabel(""));
      for (ScheduleDAO.Schedule s : schedules)
        model.addRow(new Object[] {s.id, s.title, s.start, s.end, s.memo});
      calendar.revalidate();
      calendar.repaint();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
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
