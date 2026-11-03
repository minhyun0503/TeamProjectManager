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
}