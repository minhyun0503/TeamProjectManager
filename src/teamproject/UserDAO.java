package teamproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO { // users 표 관련 SQL을 담당하는 클래스

    // 로그인 확인 메서드: 성공하면 이름, 실패하면 null 반환
    public static String login(Connection conn, String loginId, String password)
            throws SQLException {

        // ?는 나중에 값이 채워질 빈자리
        String sql = "SELECT name FROM users WHERE login_id = ? AND password = ?"; //SQL인젝션 방지

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 첫 번째 ?에 아이디, 두 번째 ?에 비밀번호 채우기
            pstmt.setString(1, loginId);
            pstmt.setString(2, password);

            // SQL 실행 후 결과 받기
            try (ResultSet rs = pstmt.executeQuery()) {

                // 일치하는 줄이 있으면 이름 반환
                if (rs.next()) {
                    return rs.getString("name");
                }

                // 일치하는 줄이 없으면 로그인 실패
                return null;
            }
        }
    }

    // 아이디(학번)로 사용자 번호(user_id)를 찾는 메서드. 없으면 0 반환
    // 로그인 성공 후 "누가 로그인했는지"를 LoginUser에 저장할 때 사용한다.
    public static int findUserId(Connection conn, String loginId) throws SQLException {

        String sql = "SELECT user_id FROM users WHERE login_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loginId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
                return 0;
            }
        }
    }
}