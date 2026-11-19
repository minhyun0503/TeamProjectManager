package teamproject.resource;

// 브리핑 담당: 신*준 | 파일별 설명: docs/06_resource.md

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class MaterialDAO {

  public static class MaterialInfo {
    private final int materialId;
    private final String title;
    private final String description;
    private final String url;
    private final String createdAt;

    public MaterialInfo(
        int materialId, String title, String description, String url, String createdAt) {
      this.materialId = materialId;
      this.title = title;
      this.description = description;
      this.url = url;
      this.createdAt = createdAt;
    }

    public int getMaterialId() {
      return materialId;
    }

    public String getTitle() {
      return title;
    }

    public String getDescription() {
      return description;
    }

    public String getUrl() {
      return url;
    }

    public String getCreatedAt() {
      return createdAt;
    }
  }

  // DB 구조는 db/schema.sql 및 기존 DB용 migration SQL로 준비합니다.
  // 조회할 때마다 테이블을 생성하거나 수정하지 않습니다.
  public static void loadMaterials(Connection conn, DefaultTableModel tableModel, int teamId)
      throws SQLException {
    String sql =
        """
SELECT material_id, title, description, COALESCE(NULLIF(url, ''), link_url) AS url, created_at
FROM materials
WHERE team_id = ?
ORDER BY material_id DESC
""";

    tableModel.setRowCount(0);

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, teamId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Object[] row = {
            rs.getInt("material_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("url"),
            rs.getTimestamp("created_at")
          };
          tableModel.addRow(row);
        }
      }
    }
  }

  public static List<MaterialInfo> getRecentMaterials(Connection conn, int teamId, int limit)
      throws SQLException {
    String sql =
        """
SELECT material_id, title, description, COALESCE(NULLIF(url, ''), link_url) AS url, created_at
FROM materials
WHERE team_id = ?
ORDER BY material_id DESC
LIMIT ?
""";

    List<MaterialInfo> list = new ArrayList<>();
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, teamId);
      pstmt.setInt(2, limit);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          list.add(
              new MaterialInfo(
                  rs.getInt("material_id"),
                  rs.getString("title"),
                  rs.getString("description"),
                  rs.getString("url"),
                  String.valueOf(rs.getTimestamp("created_at"))));
        }
      }
    }
    return list;
  }

  public static void insertMaterial(
      Connection conn, int teamId, int uploaderId, String title, String description, String url)
      throws SQLException {
    try (PreparedStatement check =
        conn.prepareStatement("SELECT 1 FROM team_members WHERE team_id = ? AND user_id = ?")) {
      check.setInt(1, teamId);
      check.setInt(2, uploaderId);
      try (ResultSet rs = check.executeQuery()) {
        if (!rs.next()) throw new SQLException("현재 팀에 소속된 사용자만 자료를 등록할 수 있습니다.");
      }
    }
    String sql =
        """
        INSERT INTO materials (team_id, title, description, url, uploader_id)
        VALUES (?, ?, ?, ?, ?)
        """;

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, teamId);
      pstmt.setString(2, title);
      pstmt.setString(3, description);
      pstmt.setString(4, url);
      pstmt.setInt(5, uploaderId);
      pstmt.executeUpdate();
    }
  }

  public static MaterialInfo findMaterial(Connection conn, int teamId, int materialId)
      throws SQLException {
    String sql =
        "SELECT title, description, COALESCE(NULLIF(url, ''), link_url) AS url, created_at FROM"
            + " materials WHERE material_id = ? AND team_id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, materialId);
      pstmt.setInt(2, teamId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new MaterialInfo(
              materialId,
              rs.getString("title"),
              rs.getString("description"),
              rs.getString("url"),
              String.valueOf(rs.getTimestamp("created_at")));
        }
      }
    }
    return null;
  }

  public static boolean updateMaterial(
      Connection conn, int teamId, int materialId, String title, String description, String url)
      throws SQLException {
    String sql =
        """
        UPDATE materials
        SET title = ?, description = ?, url = ?
        WHERE material_id = ? AND team_id = ?
        """;

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, title);
      pstmt.setString(2, description);
      pstmt.setString(3, url);
      pstmt.setInt(4, materialId);
      pstmt.setInt(5, teamId);
      return pstmt.executeUpdate() > 0;
    }
  }

  public static boolean deleteMaterial(Connection conn, int teamId, int materialId)
      throws SQLException {
    String sql = "DELETE FROM materials WHERE material_id = ? AND team_id = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, materialId);
      pstmt.setInt(2, teamId);
      return pstmt.executeUpdate() > 0;
    }
  }
}
