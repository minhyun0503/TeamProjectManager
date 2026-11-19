import java.sql.*;
import java.time.*;
import teamproject.common.*;
import teamproject.member.*;
import teamproject.resource.*;
import teamproject.schedule.*;
import teamproject.task.*;

// 실제 수업용 데이터는 사용하지 않습니다. 두 연결을 열어 동시 수정도 검사합니다.
public class FeedbackRegressionTest {
  private static int checks;

  private static void check(boolean result, String why) {
    checks++;
    if (!result) throw new AssertionError(why);
  }

  private interface Work {
    void run() throws Exception;
  }

  private static void fails(Work work, String why) throws Exception {
    try {
      work.run();
    } catch (SQLException expected) {
      check(true, why);
      return;
    }
    check(false, why);
  }

  private static int number(Connection c, String sql) throws Exception {
    try (Statement p = c.createStatement();
        ResultSet r = p.executeQuery(sql)) {
      r.next();
      return r.getInt(1);
    }
  }

  public static void main(String[] args) throws Exception {
    DB_connection.configure("jdbc:h2:mem:feedback_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    try (Connection a = DB_connection.open();
        Connection b = DB_connection.open()) {
      SchemaSetup.initialize(a, true);
      SchemaSetup.initialize(a, true);
      check(number(a, "SELECT COUNT(*) FROM users") == 5, "Restart must not duplicate demo users");
      check(TeamDAO.findTeams(a, 10).size() == 2, "Multiple team memberships");
      check(UserDAO.login(a, "demo2", "demo1234").getUserId() == 20, "Actual login ID");
      check(UserDAO.login(a, "demo2", "wrong") == null, "Bad password rejected");
      check(NoteDAO.list(a, 42).isEmpty(), "Notes team separation");
      NoteDAO.Note first = NoteDAO.list(a, 7).get(0);
      check(
          NoteDAO.save(a, 7, 20, first.id, first.version, "Changed by B", "Content B"),
          "First writer succeeds");
      check(
          !NoteDAO.save(b, 7, 10, first.id, first.version, "Stale A", "Must not overwrite"),
          "Stale writer rejected");
      NoteDAO.Note current = NoteDAO.list(b, 7).get(0);
      check(
          current.version == first.version + 1 && current.content.equals("Content B"),
          "New connection sees committed latest version");
      check(NoteDAO.history(a, 7, first.id).contains("Content B"), "Version snapshot persisted");
      check(
          !NoteDAO.save(a, 42, 10, first.id, current.version, "Wrong team", "X"),
          "Wrong team cannot update note");
      check(!NoteDAO.delete(a, 42, first.id, current.version), "Wrong team cannot delete note");
      check(!NoteDAO.delete(a, 7, first.id, first.version), "Stale note deletion rejected");
      fails(
          () -> NoteDAO.save(a, 42, 30, 0, 0, "Wrong member", "X"), "Nonmember cannot create note");
      fails(() -> NoteDAO.save(a, 7, 10, 0, 0, "", "X"), "Empty note title rejected");
      int tasks = number(a, "SELECT COUNT(*) FROM tasks");
      TaskDAO.insertFromNote(a, 7, first.id, "Decision task", current.content, 30, LocalDate.now());
      int linked = number(a, "SELECT MAX(task_id) FROM tasks");
      check(
          number(a, "SELECT COUNT(*) FROM tasks") == tasks + 1,
          "Meeting decision becomes one task");
      check(
          TaskDAO.sourceNote(a, 7, linked).contains("Content B"), "Task retains source note link");
      check(TaskDAO.sourceNote(a, 42, linked).equals("연결된 메모가 없습니다."), "Source note team guard");
      fails(
          () -> TaskDAO.insertFromNote(a, 42, first.id, "Wrong note", "", 20, LocalDate.now()),
          "Other team's note cannot be linked");
      check(
          number(a, "SELECT COUNT(*) FROM tasks") == tasks + 1 && a.getAutoCommit(),
          "Failed link rolls back task and restores autocommit");
      check(TaskDAO.board(a, 7, 30, true).size() == 2, "My tasks filters by logged-in assignee");
      check(TaskDAO.board(a, 42, 10, false).size() == 1, "Board separates teams");
      check(TaskDAO.updateTaskStatus(a, 7, linked, "완료"), "Board status persists");
      check(
          InlineProgressDAO.load(a, 7).getCompletedCount(0) == 1,
          "Board and progress use same tasks");
      try (Statement cleanup = a.createStatement()) {
        cleanup.executeUpdate("DELETE FROM schedules");
      }
      LocalDate start = LocalDate.of(2026, 10, 30), end = LocalDate.of(2026, 11, 2);
      check(ScheduleDAO.save(a, 7, 10, 0, "Cross-month", start, end, ""), "Valid schedule saved");
      check(
          ScheduleDAO.list(a, 7, YearMonth.of(2026, 11)).size() == 1,
          "Schedule crossing month is visible");
      check(
          ScheduleDAO.list(a, 7, YearMonth.of(2026, 12)).isEmpty(),
          "Nonoverlapping month excluded");
      check(ScheduleDAO.list(a, 42, YearMonth.of(2026, 11)).isEmpty(), "Schedule team separation");
      int schedule = number(a, "SELECT MAX(schedule_id) FROM schedules");
      check(
          !ScheduleDAO.save(a, 42, 10, schedule, "Wrong", start, end, ""),
          "Other team cannot edit schedule");
      fails(
          () -> ScheduleDAO.save(a, 7, 10, 0, "Bad dates", end, start, ""),
          "Reversed dates rejected");
      check(!ScheduleDAO.delete(a, 42, schedule), "Other team cannot delete schedule");
      check(ScheduleDAO.delete(a, 7, schedule), "Own schedule deletion");
      check(
          IdeaDAO.save(a, 7, 10, 0, "Proposal", "Description", "https://example.com"),
          "Idea saved");
      int idea = number(a, "SELECT MAX(idea_id) FROM ideas");
      check(IdeaDAO.list(a, 42).isEmpty(), "Idea board team separation");
      check(!IdeaDAO.save(a, 7, 20, idea, "Other author", "", ""), "Only author can edit idea");
      check(!IdeaDAO.delete(a, 7, 20, idea), "Only author can delete idea");
      fails(
          () -> IdeaDAO.save(a, 7, 10, 0, "Bad link", "", "javascript:alert(1)"),
          "Invalid link scheme rejected");
      IdeaDAO.comment(a, 7, 20, idea, "Good idea");
      check(IdeaDAO.comments(a, 7, idea).get(0).contains("Good idea"), "Comment round trip");
      check(IdeaDAO.comments(a, 42, idea).isEmpty(), "Comments remain in their team");
      fails(() -> IdeaDAO.comment(a, 42, 20, idea, "Wrong"), "Cross-team comment rejected");
      fails(() -> IdeaDAO.comment(a, 7, 20, idea, ""), "Empty comment rejected");
      IdeaDAO.toggleLike(a, 7, 20, idea);
      check(IdeaDAO.list(a, 7).get(0).likes == 1, "First like counted");
      IdeaDAO.toggleLike(b, 7, 20, idea);
      check(IdeaDAO.list(a, 7).get(0).likes == 0, "Same user's second click removes like");
      IdeaDAO.toggleLike(a, 7, 20, idea);
      IdeaDAO.toggleLike(a, 7, 30, idea);
      check(IdeaDAO.list(a, 7).get(0).likes == 2, "Different users counted separately");
      fails(() -> IdeaDAO.toggleLike(a, 42, 20, idea), "Cross-team like rejected");
      check(!ActivityDAO.recent(a, 7).isEmpty(), "Changes appear in recent activities");
      check(
          ActivityDAO.recent(a, 42).isEmpty(),
          "Activity team separation and rejected operation rollback");
      check(IdeaDAO.delete(a, 7, 10, idea), "Author deletion succeeds");
      check(
          number(a, "SELECT COUNT(*) FROM idea_comments") == 0
              && number(a, "SELECT COUNT(*) FROM idea_reactions") == 0,
          "Post deletion cascades comments and reactions");
      MaterialDAO.insertMaterial(a, 7, 20, "Resource", "", "https://example.com");
      check(
          number(a, "SELECT uploader_id FROM materials") == 20,
          "Existing material feature retains actual uploader");
      check(
          NoteDAO.delete(a, 7, first.id, current.version),
          "Current-version note deletion succeeds");
      check(
          number(a, "SELECT COUNT(*) FROM task_notes") == 0
              && TaskDAO.findTask(a, 7, linked) != null,
          "Deleting source note leaves created task intact");
      check(number(a, "SELECT COUNT(*) FROM note_history") == 0, "Deleted note history cascades");
    }
    System.out.println("PASS: " + checks + " feedback regression checks");
  }
}
