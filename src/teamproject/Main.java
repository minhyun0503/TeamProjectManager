package teamproject;

import java.sql.Connection; //DB연결을 나타내는 인터페이스
import java.sql.DriverManager; 
import java.sql.SQLException;
import javax.swing.JOptionPane;

import java.util.Arrays; //자바 배열관련
import javax.swing.JPasswordField;//입력한 문자를 화면에서 가려주는 비밀번호 입력 컴포넌트

public class Main {

    // MySQL 접속 정보
    public static void main(String[] args) {
    	
    	JPasswordField passwordField = new JPasswordField(); //비밀번호 입력칸 객체 생성
    	 
    	 int result = JOptionPane.showConfirmDialog(
                 null,
                 passwordField,
                 "MySQL 비밀번호 입력",
                 JOptionPane.OK_CANCEL_OPTION,
                 JOptionPane.PLAIN_MESSAGE //팝업 창 구성요소 5가지
         );
    	 
    	 // 사용자가 취소한 경우 프로그램 종료
         if (result != JOptionPane.OK_OPTION) {
             return;
         }
    	 
    	  char[] passwordChars = passwordField.getPassword();
          String password = new String(passwordChars); //문자열로 변환
          Arrays.fill(passwordChars, '\0'); //보안을 위해 사용이끝난 passwordChars 문자배열을 널 문자로 덮어씀.
    	 


        try (Connection conn = DB_connection.getConnection(password)) {//DB_connection 클래스의 정적메서드 호출

            System.out.println("MySQL 연결 성공!");
            
            System.out.println("===== 업무 목록 =====");
            TaskDAO.showTasks(conn); //TaskDao 클래스 참고
            
            System.out.println("===== 로그인 테스트 =====");
            
            // 테스트 1: 맞는 아이디와 비밀번호
            String name1 = UserDAO.login(conn, "20253289", "1234");
            if (name1 != null) {
                System.out.println("로그인 성공: " + name1);
            } else {
                System.out.println("로그인 실패");
            }

            //업무관리 GUI 생성자 실행
            TaskFrame frame = new TaskFrame();
            frame.setVisible(true); //창 띄우는 코드

        } catch (SQLException e) {//예외 반환 시 

            System.out.println("MySQL 연결 실패!");
            System.out.println(e.getMessage());

        } finally {
            password = null; //password 문자열의 참조 해제
        }
    }
}
