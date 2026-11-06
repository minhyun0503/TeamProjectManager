package teamproject;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

// [이동주 담당 3] 계산한 값을 기존 업무 화면에 보여줍니다.
// ActionListener: 버튼 클릭과 이름 선택을 처리합니다.
// 업무 변경 후에는 새로고침 버튼을 눌러 진행률과 마감 안내를 갱신합니다.
public class InlineProgressPanel extends JPanel
        implements ActionListener {

    private Connection conn;
    private int teamId;
    private DefaultTableModel taskModel;
    private InlineProgressData data;
    private JComboBox<InlineProgressData.Person> personBox;
    private JProgressBar teamBar;
    private JProgressBar personBar;
    private JButton refreshButton;
    private JLabel notice;
    private int deadlineColumn;
    // 이름 목록을 채우는 동안 이름 선택 이벤트가 실행되지 않게 하는 표시입니다.
    private boolean updating = false;

    // 팀원 TaskFrame 생성자의 첫 TaskDAO.loadTasks(conn, tableModel); 바로 아래에
    // InlineProgressPanel.attach(this, conn, tableModel, teamId); 를 한 번 추가합니다.
    // 아래 메서드는 기존 업무 화면 위에 내 패널을 붙이는 역할입니다.
    public static void attach(JFrame frame, Connection conn,
            DefaultTableModel taskModel, int teamId) {
        InlineProgressPanel panel = new InlineProgressPanel(conn, taskModel, teamId);
        frame.add(panel, BorderLayout.NORTH);
        frame.revalidate();
    }

    public InlineProgressPanel(Connection conn, DefaultTableModel taskModel, int teamId) {
        this.conn = conn;
        this.taskModel = taskModel;
        this.teamId = teamId;
        setLayout(new BorderLayout(8, 4));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // 1. 화면 부품을 만듭니다. 진행률 막대의 범위는 0~100입니다.
        personBox = new JComboBox<InlineProgressData.Person>();
        teamBar = new JProgressBar(0, 100);
        personBar = new JProgressBar(0, 100);
        teamBar.setStringPainted(true);
        personBar.setStringPainted(true);
        refreshButton = new JButton("새로고침");
        notice = new JLabel(" ");

        // 2. 왼쪽은 이름, 가운데는 막대, 오른쪽은 버튼입니다.
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 4));
        labels.add(new JLabel(teamId + "번 팀 전체 진행률"));
        JPanel personal = new JPanel(new BorderLayout(4, 0));
        personal.add(new JLabel("개인"), BorderLayout.WEST);
        personal.add(personBox, BorderLayout.CENTER);
        labels.add(personal);
        JPanel bars = new JPanel(new GridLayout(2, 1, 0, 4));
        bars.add(teamBar);
        bars.add(personBar);
        add(labels, BorderLayout.WEST);
        add(bars, BorderLayout.CENTER);
        add(refreshButton, BorderLayout.EAST);
        add(notice, BorderLayout.SOUTH);

        // 기존 업무 표의 마지막에 마감 안내 열을 하나 추가합니다.
        deadlineColumn = taskModel.getColumnCount();
        taskModel.addColumn("마감 안내");
        personBox.addActionListener(this);
        refreshButton.addActionListener(this);
        reload();
    }

    // 이름을 선택하거나 새로고침 버튼을 눌렀을 때 실행합니다.
    @Override
    public void actionPerformed(ActionEvent event) {
        if (updating) {
            return; // 이름 목록을 채우는 중에는 계산하지 않습니다.
        }
        if (event.getSource() == personBox) {
            showProgress();
        } else if (event.getSource() == refreshButton) {
            try {
                // 기존 업무 조회 함수를 그대로 사용합니다.
                // 업무 표를 채운 다음 내 진행률과 마감 안내도 다시 계산합니다.
                TaskDAO.loadTasks(conn, taskModel);
                reload();
            } catch (SQLException ex) {
                notice.setText("조회 실패 · 기존 값은 최신이 아닐 수 있음");
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    // DB에서 최신 업무를 읽고, 이름 목록과 진행률을 다시 표시합니다.
    private void reload() {
        updating = true;
        try {
            int selectedId = -1;
            InlineProgressData.Person selected;
            selected = (InlineProgressData.Person) personBox.getSelectedItem();
            if (selected != null) {
                selectedId = selected.getUserId();
            }
            data = InlineProgressDAO.load(conn, teamId);
            personBox.removeAllItems();
            for (int i = 0; i < data.getPeople().size(); i++) {
                InlineProgressData.Person person = data.getPeople().get(i);
                personBox.addItem(person);
                if (person.getUserId() == selectedId) {
                    personBox.setSelectedItem(person);
                }
            }
            showDeadlines();
            showProgress();
        } catch (SQLException ex) {
            notice.setText("진행률 조회 실패 · 새로고침 필요");
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } finally {
            updating = false; // 오류가 나더라도 갱신 중 표시를 해제합니다.
        }
    }

    private void showProgress() {
        if (data == null) {
            return;
        }
        setBar(teamBar, 0); // 0은 팀 전체를 뜻합니다.
        int userId = -1;
        InlineProgressData.Person selected;
        selected = (InlineProgressData.Person) personBox.getSelectedItem();
        if (selected != null) {
            userId = selected.getUserId();
        }
        setBar(personBar, userId);

        int todayCount = 0;
        int lateCount = 0;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < data.getTasks().size(); i++) {
            InlineProgressData.Task task = data.getTasks().get(i);
            if (!"완료".equals(task.getStatus()) && task.getDueDate() != null) {
                if (task.getDueDate().equals(today)) {
                    todayCount++;
                } else if (task.getDueDate().isBefore(today)) {
                    lateCount++;
                }
            }
        }
        notice.setText(teamId + "번 팀 · 오늘 마감 " + todayCount + "개 / 지연 "
                + lateCount + "개   |   업무 변경 후 새로고침을 눌러 주세요");
    }

    private void setBar(JProgressBar bar, int userId) {
        int total = data.getTotalCount(userId);
        int completed = data.getCompletedCount(userId);
        int percent = data.getPercent(userId);
        bar.setValue(percent);
        if (total == 0) {
            bar.setString("0% (업무 없음)");
        } else {
            bar.setString(percent + "% (" + completed + "/" + total + "개 완료)");
        }
    }

    private void showDeadlines() {
        // 원래 표 순서: 0 번호, 1 업무명, 2 담당자, 3 마감일, 4 상태.
        for (int row = 0; row < taskModel.getRowCount(); row++) {
            Object dateValue = taskModel.getValueAt(row, 3);
            String status = String.valueOf(taskModel.getValueAt(row, 4));
            LocalDate dueDate = null;
            String message;
            try {
                if (dateValue != null) {
                    dueDate = LocalDate.parse(dateValue.toString());
                }
                message = InlineProgressData.getDeadline(status, dueDate, LocalDate.now());
            } catch (DateTimeParseException ex) {
                message = "날짜 확인 필요";
            }
            taskModel.setValueAt(message, row, deadlineColumn);
        }
    }
}
