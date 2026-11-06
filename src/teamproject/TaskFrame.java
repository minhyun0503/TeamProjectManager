package teamproject;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import javax.swing.SwingUtilities;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;

// [업무 관리 추가] 등록 · 수정 · 삭제 화면에서 사용하는 클래스
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TaskFrame extends JFrame { //업무 관리 화면(GUI) 담당 클래스 | extends 키워드 :  JFrame의 기능 상속

    // taskTable과 tableModel을 잘구분해서 코드 이해하기.
    private JTable taskTable; //데이터를 화면에 어떻게 표로 보여주는지, 창
    private DefaultTableModel tableModel; //어떤 데이터를 담고 있는지, 데이터

    // [업무 관리 추가] 등록·수정·삭제 메서드에서도 DB가 필요해서 생성자에서 받은 연결을 필드에 저장
    private Connection conn;

    // [업무 관리 추가] 현재 팀 번호. 지금은 MemberFrame 호출부와 같이 1번 팀으로 고정.
    // 팀 선택 기능이 완성되면 생성자로 전달받도록 바꾼다.
    private int teamId = 1;
	
    // 업무 관리 화면 생성
    public TaskFrame(Connection conn) throws SQLException{

        this.conn = conn; // [업무 관리 추가] 전달받은 DB 연결 저장

        setTitle("팀프로젝트 관리 프로그램"); //프로그램 창 제목 지정
        setSize(800, 500); // 창 크기 : 800x500
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //창 닫을 때 프로그램 종료
        setLocationRelativeTo(null); //화면 중앙에 창 배치

    
    
        // 표의 열 이름 설정
        String[] columns = {
        		"번호", "업무명", "담당자", "마감일", "상태"
        };
    
        tableModel = new DefaultTableModel(columns, 0) {//열 이름을 표시하고, 기본 데이터는 0
            @Override
            public boolean isCellEditable(int row, int column) {//사용자가 표의 셀을 직접 수정못하게 하는 코드
                return false;
            }
        };

        taskTable = new JTable(tableModel); //화면에 보이는 표를 생성 (이 시점까지 표안에 데이터 x)
        
        JScrollPane scrollPane = new JScrollPane(taskTable); //taskTable을 스크롤 가능한 영역 안에 추가
        
     // 업무 목록 더블클릭 시 팀원 관리창 열기
        taskTable.addMouseListener(new java.awt.event.MouseAdapter() {//더블 클릭 시 실행할 동작 등록하는 메서드
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {//클릭이 2번???
                    try {
                        System.out.println("더블클릭 감지");
                        MemberFrame frame = new MemberFrame(conn, 1);//조회된 업무현황 표를 더블 클릭 시 해당 팀원을 표시(현재는 1번팀으로만 고정값)
                        frame.setVisible(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(TaskFrame.this, ex.getMessage());
                    }
                }
            }
        });

        add(scrollPane, BorderLayout.CENTER); //Swing 창 가운데에 스크롤 가능한 표 배치

        // [업무 관리 추가] 화면 아래에 업무 등록 · 수정 · 삭제 · 완료 처리 버튼 배치
        JButton addButton = new JButton("업무 등록");
        JButton updateButton = new JButton("업무 수정");
        JButton deleteButton = new JButton("업무 삭제");
        JButton completeButton = new JButton("완료 처리");

        // 버튼을 누르면 해당 기능 실행
        addButton.addActionListener(e -> registerTask());
        updateButton.addActionListener(e -> updateTask());
        deleteButton.addActionListener(e -> deleteTask());
        completeButton.addActionListener(e -> completeTask());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(completeButton);

        add(buttonPanel, BorderLayout.SOUTH); // 버튼 패널을 화면 아래에 배치
        TaskDAO.loadTasks(conn, tableModel); //taskDAO클래스에서 tableModel(표)에 데이터 채워넣는 단계
        InlineProgressPanel.attach(this, conn, tableModel, teamId); // [이동주 추가] 진행률 연결

        // [열 너비 추가] 표 내용이 바뀔 때마다(조회, 등록, 수정 등) 열 너비를 글자 길이에 맞춘다
        tableModel.addTableModelListener(e -> SwingUtilities.invokeLater(this::fitColumns));
        fitColumns(); // 처음 화면을 열 때도 한 번 맞춘다
    }

    // ================================================================
    // [업무 관리 추가] 업무 등록 · 수정 · 삭제 · 완료 처리
    // ================================================================

    // [열 너비 추가] 각 열의 너비를 안에 들어 있는 글자(열 이름 포함) 중 가장 긴 것에 맞춘다.
    // 업무명이나 담당자 이름이 길어도 잘리지 않고, 짧은 열(번호 등)은 좁게 유지된다.
    private void fitColumns() {

        for (int col = 0; col < taskTable.getColumnCount(); col++) {

            TableColumn column = taskTable.getColumnModel().getColumn(col);

            // 1. 열 이름(머리글)의 너비로 시작
            TableCellRenderer headerRenderer = taskTable.getTableHeader().getDefaultRenderer();
            int width = headerRenderer.getTableCellRendererComponent(
                    taskTable, column.getHeaderValue(), false, false, -1, col)
                    .getPreferredSize().width;

            // 2. 모든 행의 글자 너비 중 가장 큰 값을 찾는다
            for (int row = 0; row < taskTable.getRowCount(); row++) {
                TableCellRenderer renderer = taskTable.getCellRenderer(row, col);
                int cellWidth = taskTable.prepareRenderer(renderer, row, col)
                        .getPreferredSize().width;
                width = Math.max(width, cellWidth);
            }

            // 3. 좌우 여백 20픽셀을 더해서 열 너비로 지정
            column.setPreferredWidth(width + 20);
        }
    }

    // 표에서 선택한 업무의 번호(task_id)를 돌려준다. 선택한 행이 없으면 -1
    private int getSelectedTaskId() {

        // 표에서 선택한 행 번호 가져오기 (선택이 없으면 -1)
        int selectedRow = taskTable.getSelectedRow();

        if (selectedRow == -1) {
            return -1;
        }

        // 표의 0번 칸(번호)이 업무 번호(task_id)이다
        return Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
    }

    // 업무 등록 버튼을 눌렀을 때 실행되는 메서드
    private void registerTask() {

        try {
            // 빈 입력창을 띄워서 새 업무 정보를 입력받는다 (취소하면 null)
            TaskDAO.TaskInfo input = showTaskInputDialog("업무 등록", null);

            if (input == null) {
                return;
            }

            // DAO를 호출하여 DB에 업무 등록
            TaskDAO.insertTask(
                    conn, teamId,
                    input.getTitle(), input.getDescription(),
                    input.getAssigneeId(), input.getStatus(), input.getDueDate()
            );

            JOptionPane.showMessageDialog(this, "업무 등록 성공!");

            // 업무 목록 새로고침
            TaskDAO.loadTasks(conn, tableModel);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "업무 등록 실패: " + e.getMessage());
        }
    }

    // 선택한 업무의 내용을 수정하는 메서드
    private void updateTask() {

        int taskId = getSelectedTaskId();

        // 선택한 업무가 없으면 안내 메시지 표시
        if (taskId == -1) {
            JOptionPane.showMessageDialog(this, "수정할 업무를 선택하세요.");
            return;
        }

        try {
            // 선택한 업무의 현재 내용을 DB에서 조회 (설명, 담당자 번호는 표에 없어서 따로 조회)
            TaskDAO.TaskInfo old = TaskDAO.findTask(conn, taskId);

            if (old == null) {
                JOptionPane.showMessageDialog(this, "이미 삭제된 업무입니다.");
                TaskDAO.loadTasks(conn, tableModel);
                return;
            }

            // 기존 내용이 채워진 입력창을 띄운다 (취소하면 null)
            TaskDAO.TaskInfo input = showTaskInputDialog("업무 수정", old);

            if (input == null) {
                return;
            }

            // DAO를 호출하여 DB 정보 수정
            boolean updated = TaskDAO.updateTask(
                    conn, taskId,
                    input.getTitle(), input.getDescription(),
                    input.getAssigneeId(), input.getStatus(), input.getDueDate()
            );

            if (updated) {
                JOptionPane.showMessageDialog(this, "업무 수정 성공!");
            } else {
                JOptionPane.showMessageDialog(this, "이미 삭제되었거나 존재하지 않는 업무입니다.");
            }

            // 업무 목록 새로고침
            TaskDAO.loadTasks(conn, tableModel);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "업무 수정 실패: " + e.getMessage());
        }
    }

    // 선택한 업무를 삭제하는 메서드
    private void deleteTask() {

        int taskId = getSelectedTaskId();

        // 선택한 업무가 없으면 안내
        if (taskId == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 업무를 선택하세요.");
            return;
        }

        // 실수로 지우는 것을 막기 위한 삭제 확인창
        int result = JOptionPane.showConfirmDialog(
                this,
                "선택한 업무를 삭제하시겠습니까?",
                "업무 삭제",
                JOptionPane.YES_NO_OPTION
        );

        // 아니오를 누르면 종료
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // DB에서 업무 삭제
            TaskDAO.deleteTask(conn, taskId);

            JOptionPane.showMessageDialog(this, "업무가 삭제되었습니다.");

            // 업무 목록 새로고침
            TaskDAO.loadTasks(conn, tableModel);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "삭제 오류: " + e.getMessage());
        }
    }

    // 선택한 업무를 한 번에 '완료' 상태로 바꾸는 메서드
    private void completeTask() {

        int taskId = getSelectedTaskId();

        if (taskId == -1) {
            JOptionPane.showMessageDialog(this, "완료 처리할 업무를 선택하세요.");
            return;
        }

        try {
            // 상태 컬럼만 '완료'로 변경
            TaskDAO.updateTaskStatus(conn, taskId, TaskDAO.STATUS_DONE);

            JOptionPane.showMessageDialog(this, "완료 처리되었습니다.");

            // 업무 목록 새로고침
            TaskDAO.loadTasks(conn, tableModel);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "완료 처리 오류: " + e.getMessage());
        }
    }

    // 업무 등록 / 수정에서 함께 사용하는 입력창.
    //  - old가 null이면 등록(빈 입력창), old가 있으면 수정(기존 내용이 채워진 입력창)
    //  - 입력값이 올바르면 TaskInfo로 돌려주고, 취소하면 null을 돌려준다.
    //  - 입력값이 잘못되면 안내 후 같은 입력창을 다시 띄운다 (입력했던 내용은 그대로 남아 있음)
    private TaskDAO.TaskInfo showTaskInputDialog(String dialogTitle, TaskDAO.TaskInfo old)
            throws SQLException {

        // 담당자 선택 목록: 현재 팀의 팀원들
        List<TaskDAO.Assignee> members = TaskDAO.getTeamMembers(conn, teamId);

        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이 팀에 등록된 팀원이 없어 담당자를 지정할 수 없습니다.");
            return null;
        }

        // ----- 입력창에 사용할 컴포넌트 생성 -----
        JTextField titleField = new JTextField(20);                       // 업무명
        JTextArea descArea = new JTextArea(3, 20);                        // 설명 (여러 줄)
        JComboBox<TaskDAO.Assignee> assigneeBox =                         // 담당자 (이름으로 선택)
                new JComboBox<>(members.toArray(new TaskDAO.Assignee[0]));
        JTextField dueField = new JTextField(10);                         // 마감일 (예: 2026-12-31)
        JComboBox<String> statusBox = new JComboBox<>(TaskDAO.STATUS_LIST); // 상태

        // 수정이면 기존 내용을 입력창에 채워 넣는다
        if (old != null) {
            titleField.setText(old.getTitle());
            descArea.setText(old.getDescription() == null ? "" : old.getDescription());
            dueField.setText(old.getDueDate().toString()); // yyyy-MM-dd 형식으로 표시
            statusBox.setSelectedItem(old.getStatus());

            // 콤보박스 항목 중 회원 번호가 같은 팀원을 찾아 선택 상태로 만든다
            for (TaskDAO.Assignee a : members) {
                if (a.getUserId() == old.getAssigneeId()) {
                    assigneeBox.setSelectedItem(a);
                    break;
                }
            }
        }

        // ----- 입력 항목을 배치할 패널 -----
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 3, 3, 3);
        g.fill = GridBagConstraints.HORIZONTAL;

        addInputRow(panel, g, 0, "업무명 *", titleField);
        addInputRow(panel, g, 1, "설명", new javax.swing.JScrollPane(descArea));
        addInputRow(panel, g, 2, "담당자 *", assigneeBox);
        addInputRow(panel, g, 3, "마감일 * (yyyy-MM-dd)", dueField);
        addInputRow(panel, g, 4, "상태", statusBox);

        // ----- 입력창을 띄우고, 입력값이 올바를 때까지 반복 -----
        while (true) {

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    dialogTitle,
                    JOptionPane.OK_CANCEL_OPTION
            );

            // 취소 버튼을 누르면 null 반환
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }

            // 입력값 가져오기
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();
            String dueText = dueField.getText().trim();

            // 업무명 검사 (DB 컬럼이 VARCHAR(100)이라 길이 제한)
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "업무명을 입력하세요.");
                continue; // 입력창 다시 띄우기
            }
            if (title.length() > 100) {
                JOptionPane.showMessageDialog(this, "업무명은 100자 이하로 입력하세요.");
                continue;
            }

            // 마감일 검사 (tasks.due_date가 NOT NULL이라 필수)
            if (dueText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "마감일을 입력하세요.");
                continue;
            }

            LocalDate dueDate;
            try {
                dueDate = LocalDate.parse(dueText); // yyyy-MM-dd 형식만 허용
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "마감일 형식이 올바르지 않습니다. 예) 2026-12-31");
                continue;
            }

            // 새로 등록하는 업무의 마감일이 과거이면 막는다 (수정은 허용)
            if (old == null && dueDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "마감일은 오늘 이후로 설정하세요.");
                continue;
            }

            // 모든 검사를 통과하면 입력한 내용을 TaskInfo로 묶어서 반환
            TaskDAO.Assignee assignee = (TaskDAO.Assignee) assigneeBox.getSelectedItem();

            return new TaskDAO.TaskInfo(
                    old == null ? 0 : old.getTaskId(),
                    title,
                    description,
                    assignee.getUserId(),
                    (String) statusBox.getSelectedItem(),
                    dueDate
            );
        }
    }

    // 입력창의 한 줄(왼쪽 항목 이름 + 오른쪽 입력칸)을 추가하는 도우미 메서드
    private void addInputRow(JPanel panel, GridBagConstraints g, int row,
                             String label, java.awt.Component field) {
        g.gridy = row;
        g.gridx = 0; g.weightx = 0; panel.add(new JLabel(label), g); // 왼쪽: 항목 이름
        g.gridx = 1; g.weightx = 1; panel.add(field, g);             // 오른쪽: 입력칸
    }
}
