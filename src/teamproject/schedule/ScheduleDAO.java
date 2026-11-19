package teamproject.schedule;

// 브리핑 담당: 이*주 | 파일별 설명: docs/05_schedule.md

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import teamproject.common.ActivityDAO;
import teamproject.common.TeamDAO;

// 이*주 담당 추가: 기간이 이번 달과 겹치는 일정도 빠짐없이 조회합니다.
public class ScheduleDAO {
  public static class Schedule {
    public final int id;
    public final String title, memo;
    public final LocalDate start, end;

    public Schedule(int id, String title, LocalDate start, LocalDate end, String memo) {
      this.id = id;
      this.title = title;
      this.start = start;
      this.end = end;
      this.memo = memo;
    }
  }

  public static List<Schedule> list(Connection conn, int teamId, YearMonth month)
      throws SQLException {
    List<Schedule> rows = new ArrayList<>();
    String sql =
        "SELECT * FROM schedules WHERE team_id=? AND start_date<=? AND end_date>=? ORDER BY"
            + " start_date,schedule_id";
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, teamId);
      p.setDate(2, Date.valueOf(month.atEndOfMonth()));
      p.setDate(3, Date.valueOf(month.atDay(1)));
      try (ResultSet r = p.executeQuery()) {
        while (r.next())
          rows.add(
              new Schedule(
                  r.getInt("schedule_id"),
                  r.getString("title"),
                  r.getDate("start_date").toLocalDate(),
                  r.getDate("end_date").toLocalDate(),
                  r.getString("memo")));
      }
    }
    return rows;
  }

  // 업무를 일정 표에 복사하지 않고, 이번 달의 마감일을 직접 읽습니다.
  public static class Deadline {
    public final int taskId;
    public final String title, assignee, status;
    public final LocalDate due;

    public Deadline(int taskId, String title, String assignee, String status, LocalDate due) {
      this.taskId = taskId;
      this.title = title;
      this.assignee = assignee;
      this.status = status;
      this.due = due;
    }
  }

  public static List<Deadline> listDeadlines(Connection conn, int teamId, YearMonth month)
      throws SQLException {
    List<Deadline> rows = new ArrayList<>();
    String sql =
        """
        SELECT t.task_id, t.title, COALESCE(u.name, '담당자 없음') AS assignee, t.status, t.due_date
        FROM tasks t LEFT JOIN users u ON t.assignee_id = u.user_id
        WHERE t.team_id = ? AND t.due_date >= ? AND t.due_date <= ?
        ORDER BY t.due_date, t.task_id
        """;
    try (PreparedStatement p = conn.prepareStatement(sql)) {
      p.setInt(1, teamId);
      p.setDate(2, Date.valueOf(month.atDay(1)));
      p.setDate(3, Date.valueOf(month.atEndOfMonth()));
      try (ResultSet r = p.executeQuery()) {
        while (r.next())
          rows.add(
              new Deadline(
                  r.getInt("task_id"),
                  r.getString("title"),
                  r.getString("assignee"),
                  r.getString("status"),
                  r.getDate("due_date").toLocalDate()));
      }
    }
    return rows;
  }

  public static boolean save(
      Connection conn,
      int teamId,
      int userId,
      int id,
      String title,
      LocalDate start,
      LocalDate end,
      String memo)
      throws SQLException {
    if (title.isBlank() || title.length() > 100 || end.isBefore(start))
      throw new SQLException("제목은 1~100자, 종료일은 시작일 이후로 입력하세요.");
    TeamDAO.requireMember(conn, teamId, userId);
    conn.setAutoCommit(false);
    try {
      String sql =
          id == 0
              ? "INSERT INTO schedules (title,start_date,end_date,memo,team_id) VALUES (?,?,?,?,?)"
              : "UPDATE schedules SET title=?,start_date=?,end_date=?,memo=? WHERE team_id=? AND"
                  + " schedule_id=?";
      int count;
      try (PreparedStatement p = conn.prepareStatement(sql)) {
        p.setString(1, title);
        p.setDate(2, Date.valueOf(start));
        p.setDate(3, Date.valueOf(end));
        p.setString(4, memo);
        p.setInt(5, teamId);
        if (id != 0) p.setInt(6, id);
        count = p.executeUpdate();
      }
      if (count > 0) ActivityDAO.record(conn, teamId, userId, "일정 저장: " + title);
      conn.commit();
      return count > 0;
    } catch (SQLException e) {
      conn.rollback();
      throw e;
    } finally {
      conn.setAutoCommit(true);
    }
  }

  public static boolean delete(Connection conn, int teamId, int id) throws SQLException {
    try (PreparedStatement p =
        conn.prepareStatement("DELETE FROM schedules WHERE team_id=? AND schedule_id=?")) {
      p.setInt(1, teamId);
      p.setInt(2, id);
      return p.executeUpdate() > 0;
    }
  }
}
