package teamproject.member;

// 브리핑 담당: 이*찬 | 파일별 설명: docs/03_member.md

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO { // users 표 관련 SQL을 담당하는 클래스

  // 이름과 함께 회원 번호를 전달해야 소속 팀과 자료 작성자를 알 수 있습니다.
  public static class User {
    private final int userId;
    private final String name;

    public User(int userId, String name) {
      this.userId = userId;
      this.name = name;
    }

    public int getUserId() {
      return userId;
    }

    public String getName() {
      return name;
    }
  }

  // 로그인 확인 메서드: 성공하면 이름, 실패하면 null 반환
  public static User login(Connection conn, String loginId, String password) throws SQLException {

    // ?는 나중에 값이 채워질 빈자리
    String sql = "SELECT user_id, name FROM users WHERE login_id = ? AND password = ?"; // SQL인젝션 방지

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

      // 첫 번째 ?에 아이디, 두 번째 ?에 비밀번호 채우기
      pstmt.setString(1, loginId);
      pstmt.setString(2, password);

      // SQL 실행 후 결과 받기
      try (ResultSet rs = pstmt.executeQuery()) {

        // 일치하는 줄이 있으면 회원 번호와 이름 반환
        if (rs.next()) {
          return new User(rs.getInt("user_id"), rs.getString("name"));
        }

        // 일치하는 줄이 없으면 로그인 실패
        return null;
      }
    }
  }
}
