package teamproject;

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

        public MaterialInfo(int materialId, String title, String description, String url, String createdAt) {
            this.materialId = materialId;
            this.title = title;
            this.description = description;
            this.url = url;
            this.createdAt = createdAt;
        }

        public int getMaterialId() { return materialId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getUrl() { return url; }
        public String getCreatedAt() { return createdAt; }
    }

    // 클라우드 DB(Aiven)에 materials 테이블 및 url 컬럼 자동 보완
    public static void createTableIfNotExist(Connection conn) {
        String sqlCreate = """
                CREATE TABLE IF NOT EXISTS materials (
                    material_id INT AUTO_INCREMENT PRIMARY KEY,
                    team_id INT NOT NULL,
                    title VARCHAR(100) NOT NULL,
                    description TEXT,
                    url VARCHAR(500) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (PreparedStatement pstmt = conn.prepareStatement(sqlCreate)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // 테이블 생성 관련 예외 무시
        }

        // 기존 materials 테이블에 url 컬럼이 없을 경우를 대비해 자동으로 추가
        String sqlAlter = "ALTER TABLE materials ADD COLUMN url VARCHAR(500) NOT NULL";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlAlter)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // 이미 url 컬럼이 존재하는 경우 발생하는 정상 오류는 무시
        }
    }

    public static void loadMaterials(Connection conn, DefaultTableModel tableModel, int teamId) throws SQLException {
        createTableIfNotExist(conn);

        String sql = """
                SELECT material_id, title, description, url, created_at
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

    public static List<MaterialInfo> getRecentMaterials(Connection conn, int teamId, int limit) throws SQLException {
        createTableIfNotExist(conn);

        String sql = """
                SELECT material_id, title, description, url, created_at
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
                    list.add(new MaterialInfo(
                        rs.getInt("material_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("url"),
                        rs.getTimestamp("created_at").toString()
                    ));
                }
            }
        }
        return list;
    }

    public static void insertMaterial(Connection conn, int teamId, String title, String description, String url) throws SQLException {
        createTableIfNotExist(conn);

        String sql = """
                INSERT INTO materials (team_id, title, description, url)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, url);
            pstmt.executeUpdate();
        }
    }

    public static MaterialInfo findMaterial(Connection conn, int materialId) throws SQLException {
        String sql = "SELECT title, description, url, created_at FROM materials WHERE material_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, materialId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new MaterialInfo(
                        materialId,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("url"),
                        rs.getTimestamp("created_at").toString()
                    );
                }
            }
        }
        return null;
    }

    public static boolean updateMaterial(Connection conn, int materialId, String title, String description, String url) throws SQLException {
        String sql = """
                UPDATE materials
                SET title = ?, description = ?, url = ?
                WHERE material_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, url);
            pstmt.setInt(4, materialId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean deleteMaterial(Connection conn, int materialId) throws SQLException {
        String sql = "DELETE FROM materials WHERE material_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, materialId);
            return pstmt.executeUpdate() > 0;
        }
    }
}