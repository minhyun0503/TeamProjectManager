package teamproject.member;

// 브리핑 담당: 이*찬 | 파일별 설명: docs/03_member.md

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import teamproject.common.ActivityDAO;
import teamproject.common.TeamDAO;

// 이*찬 담당 추가: 공유 메모의 조회, 버전 검사, 수정 이력을 담당합니다.
public class NoteDAO {
  public static class Note {
    public final int id, version, authorId;
    public final String title, content, editor, updatedAt;

    public Note(
        int id,
        int version,
        int authorId,
        String title,
        String content,
        String editor,
        String updatedAt) {
      this.id = id;
      this.version = version;
      this.authorId = authorId;
      this.title = title;
      this.content = content;
      this.editor = editor;
      this.updatedAt = updatedAt;
    }

    public String toString() {
      return title + " [v" + version + "] · " + editor + " · " + updatedAt;
    }
  }

  public static List<Note> list(Connection conn, int teamId) throws SQLException {
    List<Note> notes = new ArrayList<>();
    String sql =
        "SELECT n.*,u.name FROM notes n JOIN users u ON n.updated_by=u.user_id "
            + "WHERE n.team_id=? ORDER BY n.updated_at DESC,n.note_id DESC";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, teamId);
      try (ResultSet rs = p.executeQuery()) {
        while (rs.next())
          notes.add(
              new Note(
                  rs.getInt("note_id"),
                  rs.getInt("version"),
                  rs.getInt("author_id"),
                  rs.getString("title"),
                  rs.getString("content"),
                  rs.getString("name"),
                  String.valueOf(rs.getTimestamp("updated_at"))));
      }
    }
    return notes;
  }

  // id=0이면 새 메모입니다. 수정이면 화면에서 읽은 version이 DB와 같은지 검사합니다.
  public static boolean save(
      Connection conn, int teamId, int userId, int id, int version, String title, String content)
      throws SQLException {
    if (title.isBlank() || title.length() > 100) {
      throw new SQLException("제목은 1~100자로 입력하세요.");
    }
    TeamDAO.requireMember(conn, teamId, userId);
    conn.setAutoCommit(false);
    try {
      int savedId = id;
      int savedVersion;
      if (id == 0) {
        savedId = insertNote(conn, teamId, userId, title, content);
        savedVersion = 1;
      } else {
        boolean updated = updateNote(conn, teamId, userId, id, version, title, content);
        if (!updated) {
          conn.rollback();
          return false;
        }
        savedVersion = version + 1;
      }

      // 메모 본문, 변경 이력, 활동 기록을 함께 확정합니다.
      insertHistory(conn, savedId, savedVersion, content, userId);
      ActivityDAO.record(conn, teamId, userId, "공유 메모 저장: " + title);
      conn.commit();
      return true;
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    } finally {
      conn.setAutoCommit(true);
    }
  }

  private static int insertNote(
      Connection conn, int teamId, int userId, String title, String content) throws SQLException {
    String sql =
        "INSERT INTO notes (team_id,title,content,author_id,updated_by) VALUES (?,?,?,?,?)";
    try (PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      p.setInt(1, teamId);
      p.setString(2, title);
      p.setString(3, content);
      p.setInt(4, userId);
      p.setInt(5, userId);
      p.executeUpdate();
      try (ResultSet result = p.getGeneratedKeys()) {
        if (result.next()) {
          return result.getInt(1);
        }
        throw new SQLException("새 메모의 번호를 가져오지 못했습니다.");
      }
    }
  }

  private static boolean updateNote(
      Connection conn, int teamId, int userId, int id, int version, String title, String content)
      throws SQLException {
    String sql =
        """
        UPDATE notes
        SET title=?, content=?, updated_by=?, version=version+1, updated_at=CURRENT_TIMESTAMP
        WHERE note_id=? AND team_id=? AND version=?
        """;
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setString(1, title);
      p.setString(2, content);
      p.setInt(3, userId);
      p.setInt(4, id);
      p.setInt(5, teamId);
      p.setInt(6, version);
      return p.executeUpdate() > 0;
    }
  }

  private static void insertHistory(
      Connection conn, int noteId, int version, String content, int userId) throws SQLException {
    String sql = "INSERT INTO note_history (note_id,version,content,editor_id) VALUES (?,?,?,?)";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, noteId);
      p.setInt(2, version);
      p.setString(3, content);
      p.setInt(4, userId);
      p.executeUpdate();
    }
  }

  public static boolean delete(Connection conn, int teamId, int id, int version)
      throws SQLException {
    try (PreparedStatement p =
        conn.prepareStatement("DELETE FROM notes WHERE note_id=? AND team_id=? AND version=?")) {
      p.setInt(1, id);
      p.setInt(2, teamId);
      p.setInt(3, version);
      return p.executeUpdate() > 0;
    }
  }

  public static String history(Connection conn, int teamId, int id) throws SQLException {
    StringBuilder text = new StringBuilder();
    String sql =
        "SELECT h.version,h.content,h.edited_at,u.name FROM note_history h "
            + "JOIN notes n ON h.note_id=n.note_id JOIN users u ON h.editor_id=u.user_id "
            + "WHERE n.team_id=? AND n.note_id=? ORDER BY h.version DESC";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, teamId);
      p.setInt(2, id);
      try (ResultSet rs = p.executeQuery()) {
        while (rs.next())
          text.append("v")
              .append(rs.getInt(1))
              .append(" · ")
              .append(rs.getString(4))
              .append(" · ")
              .append(rs.getTimestamp(3))
              .append("\n")
              .append(rs.getString(2))
              .append("\n\n");
      }
    }
    return text.toString();
  }
}
