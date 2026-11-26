package teamproject;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

// 팀 일정 화면 (중간발표 · 회의 · 마감 같은 팀 일정을 등록하고 D-day를 보여줌)
public class SchedulePanel extends JPanel {

    private final Connection conn;
    private final int teamId;

    private DefaultTableModel tableModel;
    private JTable scheduleTable;

    public SchedulePanel(Connection conn, int teamId) {
        this.conn = conn;
        this.teamId = teamId;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("팀 일정  ·  시작일 순서로 보여주고 남은 날짜(D-day)를 계산합니다");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        add(titleLabel, BorderLayout.NORTH);

        // 표: 칸을 직접 고칠 수 없게(isCellEditable false) 만든다
        String[] columns = {"번호", "일정명", "시작일", "종료일", "남은 날짜", "메모"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scheduleTable = new JTable(tableModel);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.setRowHeight(24);
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(40);   // 번호는 좁게
        scheduleTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // 일정명은 넓게
        scheduleTable.getColumnModel().getColumn(5).setPreferredWidth(250);  // 메모도 넓게
        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("일정 등록");
        JButton editButton = new JButton("일정 수정");
        JButton deleteButton = new JButton("일정 삭제");
        JButton refreshButton = new JButton("새로고침");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addSchedule());
        editButton.addActionListener(e -> editSchedule());
        deleteButton.addActionListener(e -> deleteSchedule());
        refreshButton.addActionListener(e -> reload());

        reload();
    }

    // 표 다시 채우기 (탭을 열 때 MainFrame에서도 호출)
    public void reload() {
        try {
            ScheduleDAO.loadSchedules(conn, tableModel, teamId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "일정 불러오기 실패: " + e.getMessage());
        }
    }

    // [일정 등록]
    private void addSchedule() {
        // 기본값: 오늘 하루짜리 일정
        LocalDate today = LocalDate.now();
        ScheduleDAO.ScheduleInfo input = showDialog("일정 등록",
                new ScheduleDAO.ScheduleInfo(0, "", today, today, ""));
        if (input == null) {
            return;
        }

        try {
            ScheduleDAO.insertSchedule(conn, teamId, input.getTitle(),
                    input.getStartDate(), input.getEndDate(), input.getMemo());
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "등록 실패: " + e.getMessage());
        }
    }

    // [일정 수정]
    private void editSchedule() {
        int scheduleId = getSelectedId();
        if (scheduleId == 0) {
            return;
        }

        try {
            ScheduleDAO.ScheduleInfo old = ScheduleDAO.findSchedule(conn, scheduleId);
            if (old == null) {
                JOptionPane.showMessageDialog(this, "이미 삭제된 일정입니다.");
                reload();
                return;
            }

            ScheduleDAO.ScheduleInfo input = showDialog("일정 수정", old);
            if (input == null) {
                return;
            }

            ScheduleDAO.updateSchedule(conn, scheduleId, input.getTitle(),
                    input.getStartDate(), input.getEndDate(), input.getMemo());
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "수정 실패: " + e.getMessage());
        }
    }

    // [일정 삭제]
    private void deleteSchedule() {
        int scheduleId = getSelectedId();
        if (scheduleId == 0) {
            return;
        }

        int row = scheduleTable.getSelectedRow();
        String title = tableModel.getValueAt(row, 1).toString();
        int answer = JOptionPane.showConfirmDialog(this, "\"" + title + "\" 일정을 삭제할까요?",
                "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ScheduleDAO.deleteSchedule(conn, scheduleId);
            reload();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "삭제 실패: " + e.getMessage());
        }
    }

    // 선택한 줄의 일정 번호. 선택이 없으면 안내 후 0
    private int getSelectedId() {
        int row = scheduleTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "표에서 일정을 먼저 선택하세요.");
            return 0;
        }
        return (int) tableModel.getValueAt(row, 0);
    }

    // 입력창을 띄워서 값을 받는다. 취소하면 null
    // 잘못 입력하면 안내하고 입력창을 다시 띄운다 (입력한 값은 그대로 유지)
    private ScheduleDAO.ScheduleInfo showDialog(String dialogTitle, ScheduleDAO.ScheduleInfo old) {
        JTextField titleField = new JTextField(old.getTitle(), 20);
        JTextField startField = new JTextField(old.getStartDate().toString());
        JTextField endField = new JTextField(old.getEndDate().toString());
        JTextField memoField = new JTextField(old.getMemo() == null ? "" : old.getMemo());

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("일정명"));
        panel.add(titleField);
        panel.add(new JLabel("시작일 (yyyy-MM-dd)"));
        panel.add(startField);
        panel.add(new JLabel("종료일 (yyyy-MM-dd)"));
        panel.add(endField);
        panel.add(new JLabel("메모"));
        panel.add(memoField);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel, dialogTitle,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null; // 취소
            }

            String title = titleField.getText().trim();
            if (title.isEmpty() || title.length() > 100) {
                JOptionPane.showMessageDialog(this, "일정명은 1~100자로 입력하세요.");
                continue; // 다시 입력
            }

            LocalDate start;
            LocalDate end;
            try {
                // LocalDate.parse: "2026-11-26" 형식 글자를 날짜로 바꾼다 (형식이 틀리면 예외)
                start = LocalDate.parse(startField.getText().trim());
                end = LocalDate.parse(endField.getText().trim());
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "날짜는 2026-11-26 형식으로 입력하세요.");
                continue;
            }

            if (end.isBefore(start)) {
                JOptionPane.showMessageDialog(this, "종료일은 시작일과 같거나 뒤여야 합니다.");
                continue;
            }

            return new ScheduleDAO.ScheduleInfo(old.getScheduleId(), title, start, end,
                    memoField.getText().trim());
        }
    }
}
