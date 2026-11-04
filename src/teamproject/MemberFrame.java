package teamproject;


// Swing 화면 관련 클래스
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

// 데이터베이스 관련 클래스
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;

public class MemberFrame extends JFrame {
	
	// 현재 데이터베이스 연결 객체
	private Connection conn;

	// 현재 조회 중인 팀 번호
	private int teamId;

	// 팀원 등록 버튼
	private JButton addButton; //팀원 추가 버튼
	private JButton updateButton; //팀원 수정 버튼
	private JButton deleteButton; // 팀원 삭제 버튼

    // 팀원 목록을 표시할 표
    private JTable memberTable;

    // 표의 데이터를 관리할 모델
    private DefaultTableModel tableModel; //유의 : taskFrame의 tableModel처럼 동작

    // 팀원 관리 화면 생성
    public MemberFrame(Connection conn, int teamId) throws SQLException {
    	
    	// 전달받은 DB 연결과 팀 번호 저장
    	this.conn = conn;
    	this.teamId = teamId;

        // 창 제목 설정
        setTitle("팀프로젝트 팀원 관리");

        // 창 크기 설정
        setSize(800, 500);

        // 창 닫을 때 프로그램 종료
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 화면 중앙에 창 배치
        setLocationRelativeTo(null);

        // 표의 열 이름 설정
        String[] columns = {
            "회원 번호", "아이디", "이름",
            "학번", "역할", "가입일"
        };

        // 표 데이터 모델 생성
        tableModel = new DefaultTableModel(columns, 0);

        // 표 화면 생성
        memberTable = new JTable(tableModel);

        // 표에 스크롤 기능 추가
        JScrollPane scrollPane = new JScrollPane(memberTable);

        // 팀원 등록 버튼 생성
        addButton = new JButton("팀원 등록");

        // 버튼 클릭 시 등록 기능 실행
        addButton.addActionListener(e -> registerMember());

        // 표를 화면 가운데에 배치
        add(scrollPane, BorderLayout.CENTER);

        // 버튼들을 담을 패널 생성
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // 팀원 수정 버튼 생성
        updateButton = new JButton("팀원 수정");

        // 수정 버튼 클릭 시 수정 기능 실행
        updateButton.addActionListener(e -> updateMember());
        
        // 팀원 삭제 버튼 생성
        deleteButton = new JButton("팀원 삭제");

        // 삭제 버튼 클릭 시 삭제 기능 실행
        deleteButton.addActionListener(e -> deleteMember());

        // 등록 버튼과 수정 버튼을 패널에 추가
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // 버튼 패널을 화면 아래에 배치
        add(buttonPanel, BorderLayout.SOUTH);

        // DB에서 팀원 목록을 가져와 표에 표시
        MemberDAO.loadMembers(conn, tableModel, teamId);
    }

    // 팀원 등록 버튼을 눌렀을 때 실행되는 메서드
    private void registerMember() {

        // 입력창에 사용할 텍스트 필드 생성
        JTextField loginIdField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField nameField = new JTextField();

        // 팀원 역할을 선택하는 콤보박스
        String[] roles = {"조원", "부조장", "팀장"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        // 입력 항목을 배치할 패널 생성
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        // 입력 항목과 입력창을 패널에 추가
        panel.add(new JLabel("아이디(학번):"));
        panel.add(loginIdField);

        panel.add(new JLabel("비밀번호:"));
        panel.add(passwordField);

        panel.add(new JLabel("이름:"));
        panel.add(nameField);
        
        // 역할 선택 항목 추가
        panel.add(new JLabel("역할:"));
        panel.add(roleBox);

        // 팀원 정보 입력창 표시
        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "팀원 등록",
                JOptionPane.OK_CANCEL_OPTION
        );

        // 취소 버튼을 누르면 메서드 종료
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // 입력창에서 입력한 값 가져오기
        String loginId = loginIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String name = nameField.getText().trim();
        String role = (String) roleBox.getSelectedItem();

        // 필수 입력 항목이 비어 있는지 확인
        if (loginId.isEmpty() || password.isEmpty()
                || name.isEmpty()) {

            JOptionPane.showMessageDialog(this, "모든 항목을 입력하세요.");
            return;
        }

        try {

            // DAO를 호출하여 DB에 팀원 등록
            MemberDAO.insertMember(
                    conn, teamId, loginId,
                    password, name, role
            );

            // 등록 성공 메시지 표시
            JOptionPane.showMessageDialog(this, "팀원 등록 성공!");

            // 팀원 목록 새로고침
            MemberDAO.loadMembers(conn, tableModel, teamId);

        } catch (SQLException e) {

            // 등록 중 오류가 발생하면 오류 메시지 표시
            JOptionPane.showMessageDialog(
                    this,
                    "팀원 등록 실패: " + e.getMessage()
            );
        }
    }

    // 선택한 팀원의 정보를 수정하는 메서드
    private void updateMember() {

        // 표에서 선택한 행 번호 가져오기
        int selectedRow = memberTable.getSelectedRow();

        // 선택한 팀원이 없으면 안내 메시지 표시
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수정할 팀원을 선택하세요.");
            return;
        }

        // 표에서 선택한 팀원의 회원 번호 가져오기
        int userId = Integer.parseInt(
        	    tableModel.getValueAt(selectedRow, 0).toString()
        	);

        // 기존 이름, 학번, 역할 가져오기
        String oldName = tableModel.getValueAt(selectedRow, 2).toString();
        String oldStudentNo = tableModel.getValueAt(selectedRow, 3).toString();
        String oldRole = tableModel.getValueAt(selectedRow, 4).toString();

        // 기존 정보를 입력창에 표시할 텍스트 필드 생성
        JTextField nameField = new JTextField(oldName);
        JTextField studentNoField = new JTextField(oldStudentNo);

        // 역할 선택 상자 생성
        String[] roles = {"조원", "부조장", "팀장"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        // 기존 역할을 선택된 값으로 설정
        roleBox.setSelectedItem(oldRole);

        // 입력 항목을 배치할 패널 생성
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        // 이름 입력 항목 추가
        panel.add(new JLabel("이름:"));
        panel.add(nameField);

        // 역할 선택 항목 추가
        panel.add(new JLabel("역할:"));
        panel.add(roleBox);

        // 수정 입력창 표시
        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "팀원 정보 수정",
                JOptionPane.OK_CANCEL_OPTION
        );

        // 취소 버튼을 누르면 종료
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // 수정한 정보 가져오기
        String name = nameField.getText().trim();
        String role = (String) roleBox.getSelectedItem();
        String studentNo = studentNoField.getText().trim();

        // 필수 항목이 비어 있는지 확인
        if (name.isEmpty() || studentNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 항목을 입력하세요.");
            return;
        }

        try {

            // DAO를 호출하여 DB 정보 수정
            MemberDAO.updateMember(
                    conn, teamId, userId,
                    name, studentNo, role
            );

            // 수정 성공 메시지 표시
            JOptionPane.showMessageDialog(this, "팀원 정보 수정 성공!");

            // 팀원 목록 새로고침
            MemberDAO.loadMembers(conn, tableModel, teamId);

        } catch (SQLException e) {

            // 수정 중 오류 발생 시 메시지 표시
            JOptionPane.showMessageDialog(
                    this,
                    "팀원 수정 실패: " + e.getMessage()
            );
        }
    }
 // 팀원 삭제 기능
    private void deleteMember() {

        // 선택한 행 확인
        int selectedRow = memberTable.getSelectedRow();

        // 선택한 팀원이 없으면 안내
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 팀원을 선택하세요.");
            return;
        }

        // 선택한 팀원의 회원 번호 가져오기
        int userId = Integer.parseInt(
        	    tableModel.getValueAt(selectedRow, 0).toString()
        	);

        // 삭제 여부 확인
        int result = JOptionPane.showConfirmDialog(
            this,
            "선택한 팀원을 삭제하시겠습니까?",
            "팀원 삭제",
            JOptionPane.YES_NO_OPTION
        );

        // 아니오를 누르면 종료
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // DB에서 팀원 삭제
            MemberDAO.deleteMember(conn, teamId, userId);

            // 삭제 완료 안내
            JOptionPane.showMessageDialog(this, "팀원이 삭제되었습니다.");

            // 팀원 목록 새로고침
            MemberDAO.loadMembers(conn, tableModel, teamId);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "삭제 오류: " + e.getMessage());
        }
    }
}

