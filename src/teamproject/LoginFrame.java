package teamproject;

import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame { // 로그인 창 (JFrame을 상속)

    private final Connection conn;    // Main에서 전달받은 DB 연결
    private JTextField idField;       // 아이디(학번) 입력칸
    private JPasswordField pwField;   // 비밀번호 입력칸 (입력 글자가 가려짐)

    public LoginFrame(Connection conn) {
        this.conn = conn; // 전달받은 연결을 이 클래스의 필드에 저장

        setTitle("로그인");
        setSize(320, 190);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // X 버튼을 누르면 프로그램 종료
        setLocationRelativeTo(null);                     // 창을 화면 가운데에 배치

        // 3줄 2칸짜리 격자 패널 (가로 간격 5, 세로 간격 5)
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // 가장자리 여백

        idField = new JTextField();
        pwField = new JPasswordField();
        JButton loginButton = new JButton("로그인");

        panel.add(new JLabel("아이디(학번)"));
        panel.add(idField);
        panel.add(new JLabel("비밀번호"));
        panel.add(pwField);
        panel.add(new JLabel(""));   // 빈 칸 (버튼을 오른쪽에 두려고)
        panel.add(loginButton);

        add(panel); // 창에 패널 추가

        // 버튼을 누르면 doLogin() 실행
        loginButton.addActionListener(e -> doLogin());
        // 비밀번호 칸에서 Enter를 눌러도 doLogin() 실행
        pwField.addActionListener(e -> doLogin());
    }

    // 로그인 처리: 입력값 확인 → DB 조회 → 결과에 따라 화면 이동
    private void doLogin() {
        String loginId = idField.getText().trim(); // 앞뒤 공백 제거

        char[] pwChars = pwField.getPassword();
        String loginPw = new String(pwChars);      // 문자열로 변환
        Arrays.fill(pwChars, '\0');                // 보안을 위해 배열을 지움

        // 빈 칸이면 안내 후 종료
        if (loginId.isEmpty() || loginPw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.");
            return;
        }

        try {
            // UserDAO.login: 맞으면 이름, 틀리면 null
            String name = UserDAO.login(conn, loginId, loginPw);

            if (name != null) {
                JOptionPane.showMessageDialog(this, name + "님 환영합니다!");
                new TaskFrame(conn).setVisible(true); // 업무 화면 열기
                dispose();                            // 로그인 창 닫기
            } else {
                JOptionPane.showMessageDialog(this,
                        "아이디 또는 비밀번호가 틀렸습니다.",
                        "로그인 실패", JOptionPane.ERROR_MESSAGE);
                pwField.setText(""); // 비밀번호 칸 비우기
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "DB 오류: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}