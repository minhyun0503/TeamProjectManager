package teamproject.resource;

// 브리핑 담당: 신*준 | 파일별 설명: docs/06_resource.md

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import teamproject.common.ActivityDAO;
import teamproject.common.TeamDAO;

// 신*준 담당 추가: 게시물, 댓글, 사용자별 공감 데이터를 분리해 관리합니다.
public class IdeaDAO {
  public static class Idea {
    public final int id, authorId, comments, likes;
    public final String title, content, link, author;

    public Idea(
        int id,
        int authorId,
        String title,
        String content,
        String link,
        String author,
        int comments,
        int likes) {
      this.id = id;
      this.authorId = authorId;
      this.title = title;
      this.content = content;
      this.link = link;
      this.author = author;
      this.comments = comments;
      this.likes = likes;
    }

    public String toString() {
      return title + " · " + author + " · 댓글 " + comments + " · 공감 " + likes;
    }
  }

  public static List<Idea> list(Connection conn, int teamId) throws SQLException {
    List<Idea> rows = new ArrayList<>();
    String sql =
        "SELECT i.*,u.name,(SELECT COUNT(*) FROM idea_comments c WHERE c.idea_id=i.idea_id) AS"
            + " comments,(SELECT COUNT(*) FROM idea_reactions r WHERE r.idea_id=i.idea_id) AS likes"
            + " FROM ideas i JOIN users u ON i.author_id=u.user_id WHERE i.team_id=? ORDER BY"
            + " i.idea_id DESC";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, teamId);
      try (ResultSet r = p.executeQuery()) {
        while (r.next())
          rows.add(
              new Idea(
                  r.getInt("idea_id"),
                  r.getInt("author_id"),
                  r.getString("title"),
                  r.getString("content"),
                  r.getString("link_url"),
                  r.getString("name"),
                  r.getInt("comments"),
                  r.getInt("likes")));
      }
    }
    return rows;
  }

  public static boolean save(
      Connection conn, int teamId, int userId, int id, String title, String content, String link)
      throws SQLException {
    if (title.isBlank() || title.length() > 100 || link.length() > 500)
      throw new SQLException("제목은 1~100자, 링크는 500자 이하로 입력하세요.");
    if (!link.isBlank() && !link.startsWith("https://") && !link.startsWith("http://"))
      throw new SQLException("링크는 http:// 또는 https://로 시작해야 합니다.");
    TeamDAO.requireMember(conn, teamId, userId);
    conn.setAutoCommit(false);
    try {
      String sql =
          id == 0
              ? "INSERT INTO ideas (title,content,link_url,team_id,author_id) VALUES (?,?,?,?,?)"
              : "UPDATE ideas SET title=?,content=?,link_url=? WHERE team_id=? AND author_id=? AND"
                  + " idea_id=?";
      int count;
      try (PreparedStatement p = conn.prepareStatement(sql)) {
        p.setString(1, title);
        p.setString(2, content);
        p.setString(3, link);
        p.setInt(4, teamId);
        p.setInt(5, userId);
        if (id != 0) p.setInt(6, id);
        count = p.executeUpdate();
      }
      if (count > 0) ActivityDAO.record(conn, teamId, userId, "아이디어 저장: " + title);
      conn.commit();
      return count > 0;
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    } finally {
      conn.setAutoCommit(true);
    }
  }

  public static boolean delete(Connection conn, int teamId, int userId, int id)
      throws SQLException {
    try (PreparedStatement p =
        conn.prepareStatement("DELETE FROM ideas WHERE idea_id=? AND team_id=? AND author_id=?")) {
      p.setInt(1, id);
      p.setInt(2, teamId);
      p.setInt(3, userId);
      return p.executeUpdate() > 0;
    }
  }

  private static void requireIdea(Connection conn, int teamId, int id) throws SQLException {
    try (PreparedStatement p =
        conn.prepareStatement("SELECT 1 FROM ideas WHERE idea_id=? AND team_id=?")) {
      p.setInt(1, id);
      p.setInt(2, teamId);
      try (ResultSet r = p.executeQuery()) {
        if (!r.next()) throw new SQLException("현재 팀의 게시물이 아닙니다.");
      }
    }
  }

  public static List<String> comments(Connection conn, int teamId, int id) throws SQLException {
    List<String> rows = new ArrayList<>();
    String sql =
        "SELECT u.name,c.content,c.created_at FROM idea_comments c JOIN ideas i ON"
            + " c.idea_id=i.idea_id JOIN users u ON c.author_id=u.user_id WHERE i.team_id=? AND"
            + " i.idea_id=? ORDER BY c.comment_id";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, teamId);
      p.setInt(2, id);
      try (ResultSet r = p.executeQuery()) {
        while (r.next())
          rows.add(r.getString(1) + " [" + r.getTimestamp(3) + "]\n" + r.getString(2));
      }
    }
    return rows;
  }

  public static void comment(Connection conn, int teamId, int userId, int id, String content)
      throws SQLException {
    if (content.isBlank() || content.length() > 1000) throw new SQLException("댓글은 1~1000자로 입력하세요.");
    TeamDAO.requireMember(conn, teamId, userId);
    conn.setAutoCommit(false);
    try {
      requireIdea(conn, teamId, id);
      try (PreparedStatement p =
          conn.prepareStatement(
              "INSERT INTO idea_comments (idea_id,author_id,content) VALUES (?,?,?)")) {
        p.setInt(1, id);
        p.setInt(2, userId);
        p.setString(3, content);
        p.executeUpdate();
      }
      ActivityDAO.record(conn, teamId, userId, "아이디어 #" + id + "에 댓글 작성");
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    } finally {
      conn.setAutoCommit(true);
    }
  }

  // 기존 공감을 먼저 지워 보고, 지운 행이 없을 때만 새 공감을 넣습니다.
  public static void toggleLike(Connection conn, int teamId, int userId, int id)
      throws SQLException {
    TeamDAO.requireMember(conn, teamId, userId);
    conn.setAutoCommit(false);
    try {
      requireIdea(conn, teamId, id);
      int deleted;
      try (PreparedStatement p =
          conn.prepareStatement("DELETE FROM idea_reactions WHERE idea_id=? AND user_id=?")) {
        p.setInt(1, id);
        p.setInt(2, userId);
        deleted = p.executeUpdate();
      }
      if (deleted == 0)
        try (PreparedStatement p =
            conn.prepareStatement("INSERT INTO idea_reactions (idea_id,user_id) VALUES (?,?)")) {
          p.setInt(1, id);
          p.setInt(2, userId);
          p.executeUpdate();
        }
      ActivityDAO.record(conn, teamId, userId, "아이디어 #" + id + (deleted == 0 ? "에 공감" : " 공감 취소"));
      conn.commit();
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    } finally {
      conn.setAutoCommit(true);
    }
  }
}
