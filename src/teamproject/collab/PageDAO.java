package teamproject.collab;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// 문서 페이지(pages 표)를 다루는 클래스 (노션의 페이지처럼 제목 + 긴 본문)
public class PageDAO {

    // 왼쪽 목록에 보여줄 페이지 (번호 + 제목)
    public static class PageItem {
        private final int pageId;
        private final String title;

        public PageItem(int pageId, String title) {
            this.pageId = pageId;
            this.title = title;
        }

        public int getPageId() { return pageId; }

        // JList는 목록에 toString()의 결과를 보여준다
        @Override
        public String toString() {
            return title;
        }
    }

    // 페이지 하나의 전체 내용
    public static class PageInfo {
        private final int pageId;
        private final String title;
        private final String content;
        private final String updatedByName;
        private final Timestamp updatedAt;

        public PageInfo(int pageId, String title, String content,
                        String updatedByName, Timestamp updatedAt) {
            this.pageId = pageId;
            this.title = title;
            this.content = content;
            this.updatedByName = updatedByName;
            this.updatedAt = updatedAt;
        }

        public int getPageId() { return pageId; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public String getUpdatedByName() { return updatedByName; }
        public Timestamp getUpdatedAt() { return updatedAt; }
    }

    // 팀의 페이지 목록 (최근에 고친 페이지가 위로)
    public static List<PageItem> listPages(Connection conn, int teamId) throws SQLException {

        String sql = "SELECT page_id, title FROM pages WHERE team_id = ? ORDER BY updated_at DESC, page_id DESC";
        List<PageItem> list = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PageItem(rs.getInt("page_id"), rs.getString("title")));
                }
            }
        }
        return list;
    }

    // 페이지 하나 읽기 (없으면 null)
    public static PageInfo findPage(Connection conn, int pageId) throws SQLException {

        String sql = "SELECT p.page_id, p.title, p.content, u.name, p.updated_at"
                + " FROM pages p"
                + " LEFT JOIN users u ON p.updated_by = u.user_id"
                + " WHERE p.page_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String content = rs.getString("content");
                    return new PageInfo(rs.getInt("page_id"), rs.getString("title"),
                            content == null ? "" : content,
                            rs.getString("name"), rs.getTimestamp("updated_at"));
                }
                return null;
            }
        }
    }

    // 새 페이지 만들기. 새로 생긴 page_id를 돌려준다.
    public static int insertPage(Connection conn, int teamId, String title,
                                 String content, int userId) throws SQLException {

        String sql = "INSERT INTO pages (team_id, title, content, updated_by, updated_at)"
                + " VALUES (?, ?, ?, ?, NOW())";

        // RETURN_GENERATED_KEYS: AUTO_INCREMENT로 만들어진 번호를 돌려받겠다는 뜻
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, teamId);
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            setUser(pstmt, 4, userId);
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    // 페이지 저장 (제목 · 본문 · 수정자 · 수정 시각)
    public static boolean updatePage(Connection conn, int pageId, String title,
                                     String content, int userId) throws SQLException {

        String sql = "UPDATE pages SET title = ?, content = ?, updated_by = ?, updated_at = NOW()"
                + " WHERE page_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            setUser(pstmt, 3, userId);
            pstmt.setInt(4, pageId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 페이지 삭제
    public static boolean deletePage(Connection conn, int pageId) throws SQLException {

        String sql = "DELETE FROM pages WHERE page_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pageId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 작성자 번호 넣기: 로그인 정보가 없으면(0) 비워 둔다(NULL)
    private static void setUser(PreparedStatement pstmt, int index, int userId) throws SQLException {
        if (userId > 0) {
            pstmt.setInt(index, userId);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
    }
}
