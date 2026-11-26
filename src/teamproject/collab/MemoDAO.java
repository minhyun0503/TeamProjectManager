package teamproject.collab;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

// 팀 공동 메모장(team_memo 표)을 읽고 저장하는 클래스
public class MemoDAO {

    // 메모 한 개의 정보를 담는 상자
    public static class MemoInfo {
        private final String content;       // 메모 내용
        private final int version;          // 저장할 때마다 1씩 올라가는 번호
        private final String updatedByName; // 마지막으로 저장한 사람 이름 (없으면 null)
        private final Timestamp updatedAt;  // 마지막 저장 시각 (없으면 null)

        public MemoInfo(String content, int version, String updatedByName, Timestamp updatedAt) {
            this.content = content;
            this.version = version;
            this.updatedByName = updatedByName;
            this.updatedAt = updatedAt;
        }

        public String getContent() { return content; }
        public int getVersion() { return version; }
        public String getUpdatedByName() { return updatedByName; }
        public Timestamp getUpdatedAt() { return updatedAt; }
    }

    // 팀 메모를 읽어 온다. 아직 메모가 없으면 빈 메모를 하나 만든다.
    public static MemoInfo loadMemo(Connection conn, int teamId) throws SQLException {

        // users와 LEFT JOIN: 아직 아무도 저장하지 않았으면(updated_by가 NULL) 이름도 NULL
        String sql = "SELECT m.content, m.version, u.name, m.updated_at"
                + " FROM team_memo m"
                + " LEFT JOIN users u ON m.updated_by = u.user_id"
                + " WHERE m.team_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String content = rs.getString("content");
                    if (content == null) {
                        content = "";
                    }
                    return new MemoInfo(content, rs.getInt("version"),
                            rs.getString("name"), rs.getTimestamp("updated_at"));
                }
            }
        }

        // 메모가 없으면 빈 메모를 만든다.
        // INSERT IGNORE: 두 사람이 동시에 만들려고 해도 오류 없이 하나만 생긴다.
        String insertSql = "INSERT IGNORE INTO team_memo (team_id, content, version) VALUES (?, '', 0)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setInt(1, teamId);
            pstmt.executeUpdate();
        }
        return new MemoInfo("", 0, null, null);
    }

    // 메모 저장. 내가 불러온 뒤로 아무도 저장하지 않았을 때만 성공(true)한다.
    // - WHERE version = ? : 내가 알고 있는 번호와 DB 번호가 같을 때만 바꾼다.
    // - 다른 사람이 먼저 저장했다면 번호가 이미 올라가 있어서 바뀌는 줄이 0개 → false
    public static boolean saveMemo(Connection conn, int teamId, String content,
                                   int myVersion, int userId) throws SQLException {

        String sql = "UPDATE team_memo"
                + " SET content = ?, version = version + 1, updated_by = ?, updated_at = NOW()"
                + " WHERE team_id = ? AND version = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, content);
            if (userId > 0) {
                pstmt.setInt(2, userId);
            } else {
                pstmt.setNull(2, Types.INTEGER); // 로그인 정보가 없으면 작성자를 비워 둔다
            }
            pstmt.setInt(3, teamId);
            pstmt.setInt(4, myVersion);
            return pstmt.executeUpdate() == 1;
        }
    }
}
