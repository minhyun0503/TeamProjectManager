package teamproject.common;

// 브리핑 담당: 장*민 | 파일별 설명: docs/02_common.md

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 로그인한 사용자가 소속된 팀만 선택 목록에 표시합니다.
public class TeamDAO {
  public static class Team {
    private final int teamId;
    private final String teamName;

    public Team(int teamId, String teamName) {
      this.teamId = teamId;
      this.teamName = teamName;
    }

    public int getTeamId() {
      return teamId;
    }

    public String getTeamName() {
      return teamName;
    }

    @Override
    public String toString() {
      return teamName + " (팀 " + teamId + ")";
    }
  }

  public static List<Team> findTeams(Connection conn, int userId) throws SQLException {
    String sql =
        """
        SELECT t.team_id, t.team_name
        FROM teams t
        JOIN team_members m ON t.team_id = m.team_id
        WHERE m.user_id = ?
        ORDER BY t.team_id
        """;
    List<Team> teams = new ArrayList<>();
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          teams.add(new Team(rs.getInt("team_id"), rs.getString("team_name")));
        }
      }
    }
    return teams;
  }

  // 공통 검사: 화면에서 넘어온 사용자와 팀이 실제 소속 관계인지 확인합니다.
  public static void requireMember(Connection conn, int teamId, int userId) throws SQLException {
    try (PreparedStatement p =
        conn.prepareStatement("SELECT 1 FROM team_members WHERE team_id=? AND user_id=?")) {
      p.setInt(1, teamId);
      p.setInt(2, userId);
      try (ResultSet rs = p.executeQuery()) {
        if (!rs.next()) throw new SQLException("현재 팀에 소속되어 있지 않습니다.");
      }
    }
  }
}
