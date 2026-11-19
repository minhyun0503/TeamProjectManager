package teamproject.common;

// 브리핑 담당: 장*민 | 파일별 설명: docs/02_common.md

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 팀의 변경 기록을 시간순으로 조회합니다. 호출한 DAO의 트랜잭션 안에서 사용합니다.
public class ActivityDAO {
  public static void record(Connection conn, int teamId, int userId, String action)
      throws SQLException {
    try (PreparedStatement p =
        conn.prepareStatement("INSERT INTO activity_log (team_id,user_id,action) VALUES (?,?,?)")) {
      p.setInt(1, teamId);
      p.setInt(2, userId);
      p.setString(3, action);
      p.executeUpdate();
    }
  }

  public static List<String> recent(Connection conn, int teamId) throws SQLException {
    List<String> rows = new ArrayList<>();
    String sql =
        "SELECT a.created_at,u.name,a.action FROM activity_log a JOIN users u ON"
            + " a.user_id=u.user_id WHERE a.team_id=? ORDER BY a.activity_id DESC LIMIT 30";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, teamId);
      try (ResultSet rs = p.executeQuery()) {
        while (rs.next())
          rows.add(rs.getTimestamp(1) + " | " + rs.getString(2) + " | " + rs.getString(3));
      }
    }
    return rows;
  }
}
