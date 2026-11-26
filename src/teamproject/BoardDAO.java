package teamproject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// 포스트잇 보드(board_posts 표)를 다루는 클래스
public class BoardDAO {

    // 포스트잇 색 이름 (화면의 선택 목록과 DB 저장값으로 함께 사용)
    public static final String[] COLORS = {"노랑", "분홍", "하늘", "연두"};

    // 포스트잇 한 장의 정보를 담는 상자
    public static class Post {
        private final int postId;
        private final int userId;        // 작성자 번호 (본인 글인지 확인용)
        private final String writerName; // 작성자 이름
        private final String content;
        private final String color;
        private final int likes;         // 좋아요 수
        private final Timestamp createdAt;

        public Post(int postId, int userId, String writerName, String content,
                    String color, int likes, Timestamp createdAt) {
            this.postId = postId;
            this.userId = userId;
            this.writerName = writerName;
            this.content = content;
            this.color = color;
            this.likes = likes;
            this.createdAt = createdAt;
        }

        public int getPostId() { return postId; }
        public int getUserId() { return userId; }
        public String getWriterName() { return writerName; }
        public String getContent() { return content; }
        public String getColor() { return color; }
        public int getLikes() { return likes; }
        public Timestamp getCreatedAt() { return createdAt; }
    }

    // 팀의 포스트잇 목록 (최신 글이 먼저)
    public static List<Post> loadPosts(Connection conn, int teamId) throws SQLException {

        String sql = "SELECT p.post_id, p.user_id, u.name, p.content, p.color, p.likes, p.created_at"
                + " FROM board_posts p"
                + " JOIN users u ON p.user_id = u.user_id"
                + " WHERE p.team_id = ?"
                + " ORDER BY p.post_id DESC";

        List<Post> posts = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(new Post(
                            rs.getInt("post_id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("content"),
                            rs.getString("color"),
                            rs.getInt("likes"),
                            rs.getTimestamp("created_at")));
                }
            }
        }
        return posts;
    }

    // 새 포스트잇 붙이기
    public static void insertPost(Connection conn, int teamId, int userId,
                                  String content, String color) throws SQLException {

        String sql = "INSERT INTO board_posts (team_id, user_id, content, color) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teamId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, content);
            pstmt.setString(4, color);
            pstmt.executeUpdate();
        }
    }

    // 좋아요 +1 (DB에서 바로 1을 더하므로 여러 명이 동시에 눌러도 숫자가 맞는다)
    public static void likePost(Connection conn, int postId) throws SQLException {

        String sql = "UPDATE board_posts SET likes = likes + 1 WHERE post_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.executeUpdate();
        }
    }

    // 포스트잇 떼기: 작성자 본인만 지울 수 있도록 user_id도 함께 확인한다
    public static boolean deletePost(Connection conn, int postId, int userId) throws SQLException {

        String sql = "DELETE FROM board_posts WHERE post_id = ? AND user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
