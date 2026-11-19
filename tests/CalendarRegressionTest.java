import java.sql.*;
import java.time.*;
import java.util.List;
import teamproject.common.*;
import teamproject.schedule.*;
import teamproject.task.*;

public class CalendarRegressionTest {
  private static int checks;

  private static void check(boolean ok, String name) {
    checks++;
    if (!ok) throw new AssertionError(name);
  }

  private static int taskId(Connection conn, String title) throws SQLException {
    try (PreparedStatement p = conn.prepareStatement("SELECT task_id FROM tasks WHERE title=?")) {
      p.setString(1, title);
      try (ResultSet r = p.executeQuery()) {
        r.next();
        return r.getInt(1);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    DB_connection.configure("jdbc:h2:mem:calendar_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    try (Connection conn = DB_connection.open()) {
      SchemaSetup.initialize(conn, true);
      YearMonth feb = YearMonth.of(2028, 2), march = feb.plusMonths(1);
      LocalDate first = feb.atDay(1), last = feb.atEndOfMonth();
      check(last.getDayOfMonth() == 29, "Leap-year fixture");
      TaskDAO.insertTask(conn, 7, "월초 업무", "", 30, "할 일", first);
      TaskDAO.insertTask(conn, 7, "월말 업무", "", 30, "진행 중", last);
      TaskDAO.insertTask(conn, 7, "같은 날 업무", "", 20, "할 일", last);
      TaskDAO.insertTask(conn, 42, "다른 팀 업무", "", 20, "할 일", last);
      TaskDAO.insertTask(conn, 7, "다음 달 업무", "", 30, "할 일", march.atDay(1));
      List<ScheduleDAO.Deadline> deadlines = ScheduleDAO.listDeadlines(conn, 7, feb);
      check(deadlines.size() == 3, "Month and team filters");
      check(
          SchedulePanel.dayText(first, List.of(), deadlines).contains("[업무] 월초 업무"),
          "First day included");
      String lastText = SchedulePanel.dayText(last, List.of(), deadlines);
      check(
          lastText.contains("월말 업무") && lastText.contains("같은 날 업무"), "Multiple tasks on leap day");
      check(
          !lastText.contains("다른 팀 업무") && !lastText.contains("다음 달 업무"),
          "Other team/month absent");
      check(lastText.contains("진행 중 · 데모팀원3"), "Status and assignee shown");
      ScheduleDAO.save(conn, 7, 10, 0, "별도 일정", last.minusDays(1), march.atDay(2), "");
      List<ScheduleDAO.Schedule> schedules = ScheduleDAO.list(conn, 7, feb);
      String mixed = SchedulePanel.dayText(last, schedules, deadlines);
      check(
          mixed.contains("[일정] 별도 일정") && mixed.contains("[업무] 월말 업무"),
          "Schedule and task coexist");
      int id = taskId(conn, "월말 업무");
      TaskDAO.updateTaskStatus(conn, 7, id, "완료");
      check(
          SchedulePanel.dayText(last, schedules, ScheduleDAO.listDeadlines(conn, 7, feb))
              .contains("완료 · 데모팀원3"),
          "Completed tasks remain labeled");
      TaskDAO.updateTask(conn, 7, id, "이동한 업무", "", 30, "완료", march.atDay(1));
      check(
          ScheduleDAO.listDeadlines(conn, 7, feb).size() == 2,
          "Old due date disappears after editing");
      check(
          SchedulePanel.dayText(
                  march.atDay(1),
                  ScheduleDAO.list(conn, 7, march),
                  ScheduleDAO.listDeadlines(conn, 7, march))
              .contains("이동한 업무"),
          "New due date appears next month");
      TaskDAO.deleteTask(conn, 7, id);
      check(
          !SchedulePanel.dayText(
                  march.atDay(1), List.of(), ScheduleDAO.listDeadlines(conn, 7, march))
              .contains("이동한 업무"),
          "Deleted task disappears");
      try (Statement s = conn.createStatement();
          ResultSet r = s.executeQuery("SELECT COUNT(*) FROM schedules WHERE title<>'팀원 코드 브리핑'")) {
        r.next();
        check(r.getInt(1) == 1, "Tasks were not copied into schedules");
      }
      check(ScheduleDAO.listDeadlines(conn, 42, feb).size() == 1, "Second team has own deadlines");
    }
    System.out.println("PASS: " + checks + " calendar regression checks");
  }
}
