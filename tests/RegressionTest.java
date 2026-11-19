import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableModel;
import teamproject.common.TeamDAO;
import teamproject.member.MemberDAO;
import teamproject.member.UserDAO;
import teamproject.resource.MaterialDAO;
import teamproject.schedule.InlineProgressDAO;
import teamproject.schedule.InlineProgressData;
import teamproject.task.TaskDAO;

// 실제 클라우드 DB 대신 H2의 MySQL 호환 모드에서 DAO를 검증합니다.
public class RegressionTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:regression;MODE=MySQL;DB_CLOSE_DELAY=-1")) {
            createSchema(conn);
            seed(conn);
            testLoginAndTeams(conn);
            testMembers(conn);
            testTasksAndProgress(conn);
            testMaterials(conn);
            testLegacyLinks(conn);
        }
        System.out.println("PASS: " + checks + " regression checks");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void sql(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) { stmt.execute(sql); }
    }

    private static int number(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void expectSqlFailure(SqlAction action, String message) throws Exception {
        try { action.run(); } catch (SQLException expected) { check(true, message); return; }
        check(false, message);
    }

    private interface SqlAction { void run() throws Exception; }

    private static void createSchema(Connection conn) throws Exception {
        String schema = Files.readString(Path.of("db/schema.sql"));
        Matcher matcher = Pattern.compile("CREATE TABLE \\w+ \\(.*?\\);", Pattern.DOTALL).matcher(schema);
        int tables = 0;
        while (matcher.find()) { sql(conn, matcher.group()); tables++; }
        check(tables == 6, "Repository schema must create all six tables");
    }

    private static void seed(Connection conn) throws SQLException {
        sql(conn, "INSERT INTO users (user_id, login_id, password, name, student_no) VALUES "
                + "(10,'demo-a','pw','User A','A'),(20,'demo-b','pw','User B','B'),"
                + "(30,'demo-c','pw','User C','C'),(40,'no-team','pw','No Team','D')");
        // 팀 번호 1을 사용하지 않고 여러 팀의 분리와 선택을 확인합니다.
        sql(conn, "INSERT INTO teams (team_id,team_name,leader_id) VALUES (7,'Team Seven',10),(42,'Team Forty Two',20)");
        sql(conn, "INSERT INTO team_members (team_id,user_id) VALUES (7,10),(7,30),(42,10),(42,20)");
    }

    private static void testLoginAndTeams(Connection conn) throws Exception {
        UserDAO.User user = UserDAO.login(conn, "demo-b", "pw");
        check(user != null && user.getUserId() == 20 && "User B".equals(user.getName()), "Login must retain actual user ID");
        check(UserDAO.login(conn, "demo-b", "wrong") == null, "Wrong password must fail");
        check(UserDAO.login(conn, "' OR 1=1 --", "pw") == null, "Login parameters must not become SQL");
        check(TeamDAO.findTeams(conn, 10).size() == 2, "A user can select between two joined teams");
        check(TeamDAO.findTeams(conn, 20).size() == 1 && TeamDAO.findTeams(conn, 20).get(0).getTeamId() == 42,
                "A user in team 42 must not be sent to team 1");
        check(TeamDAO.findTeams(conn, 40).isEmpty(), "A user without a team must have no fallback team");
    }

    private static void testMembers(Connection conn) throws Exception {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"id","login","name","student","role","joined"}, 0);
        MemberDAO.loadMembers(conn, model, 42);
        check(model.getRowCount() == 2, "Member list must be filtered by team");
        MemberDAO.insertMember(conn, 42, "new-member", "pw", "New Member", "조원");
        int newId = UserDAO.login(conn, "new-member", "pw").getUserId();
        check(number(conn, "SELECT COUNT(*) FROM team_members WHERE team_id=42 AND user_id=" + newId) == 1,
                "Member registration must use selected team");
        MemberDAO.updateMember(conn, 42, newId, "Renamed", "NEW", "부조장");
        check(number(conn, "SELECT COUNT(*) FROM users WHERE user_id=" + newId + " AND name='Renamed' AND student_no='NEW'") == 1,
                "Member profile changes must persist");
        expectSqlFailure(() -> MemberDAO.updateMember(conn, 42, 30, "Wrong", "X", "조원"), "Cannot edit another team's member");
        check("User C".equals(UserDAO.login(conn, "demo-c", "pw").getName()), "Rejected edit must not change user profile");
        int users = number(conn, "SELECT COUNT(*) FROM users");
        expectSqlFailure(() -> MemberDAO.insertMember(conn, 999, "bad-team", "pw", "Bad", "조원"), "Invalid team insertion must roll back");
        check(number(conn, "SELECT COUNT(*) FROM users") == users && conn.getAutoCommit(), "Rollback must leave no orphan user and restore autocommit");
        MemberDAO.deleteMember(conn, 7, newId);
        check(TeamDAO.findTeams(conn, newId).size() == 1, "Deleting in another team must not remove membership");
        MemberDAO.deleteMember(conn, 42, newId);
        check(TeamDAO.findTeams(conn, newId).isEmpty() && UserDAO.login(conn, "new-member", "pw") != null,
                "Deleting membership must preserve login account");
    }

    private static void testTasksAndProgress(Connection conn) throws Exception {
        LocalDate due = LocalDate.now().plusDays(3);
        TaskDAO.insertTask(conn, 7, "Seven task", "", 30, "할 일", due);
        TaskDAO.insertTask(conn, 42, "Forty two task", "", 20, "완료", due);
        int task7 = number(conn, "SELECT task_id FROM tasks WHERE team_id=7");
        int task42 = number(conn, "SELECT task_id FROM tasks WHERE team_id=42");
        DefaultTableModel model = new DefaultTableModel(new Object[]{"id","title","assignee","due","status","deadline"}, 0);
        TaskDAO.loadTasks(conn, model, 42);
        check(model.getRowCount() == 1 && ((Number) model.getValueAt(0,0)).intValue() == task42, "Task list must contain selected team's rows only");
        check(model.getColumnCount() == 6, "Refreshing tasks must preserve the deadline column");
        check(TaskDAO.findTask(conn, 7, task42) == null, "Task detail must be team-scoped");
        check(!TaskDAO.updateTask(conn, 7, task42, "Wrong", "", 10, "할 일", due), "Cannot update a different team's task");
        check(!TaskDAO.updateTaskStatus(conn, 7, task42, "할 일"), "Cannot complete a different team's task");
        check(!TaskDAO.deleteTask(conn, 7, task42), "Cannot delete a different team's task");
        expectSqlFailure(() -> TaskDAO.insertTask(conn, 7, "Wrong assignee", "", 20, "할 일", due), "Cannot assign a task to another team's member");
        check(TaskDAO.updateTask(conn, 7, task7, "Edited", "New details", 30, "진행 중", due), "Own-team task can be edited");
        check(TaskDAO.updateTaskStatus(conn, 7, task7, "완료"), "Own-team task can be completed");
        check("Edited".equals(TaskDAO.findTask(conn, 7, task7).getTitle()), "Updated task content must persist");
        InlineProgressData progress = InlineProgressDAO.load(conn, 42);
        check(progress.getTotalCount(0) == 1 && progress.getPercent(0) == 100, "Team progress must exclude other teams' tasks");
        check(progress.getTotalCount(30) == 0, "Other team's assignee must not affect personal progress");
        LocalDate today = LocalDate.now();
        check("오늘 마감".equals(InlineProgressData.getDeadline("할 일", today, today)), "Due-today label");
        check("지연 2일".equals(InlineProgressData.getDeadline("할 일", today.minusDays(2), today)), "Overdue label");
        check("완료".equals(InlineProgressData.getDeadline("완료", today.minusDays(2), today)), "Completed work must not count as overdue");
        check(TaskDAO.deleteTask(conn, 7, task7) && TaskDAO.findTask(conn, 7, task7) == null, "Own-team task can be deleted");
    }

    private static void testMaterials(Connection conn) throws Exception {
        MaterialDAO.insertMaterial(conn, 42, 20, "New resource", "Details", "https://example.com/new");
        int id = number(conn, "SELECT MAX(material_id) FROM materials");
        check(number(conn, "SELECT uploader_id FROM materials WHERE material_id=" + id) == 20, "Actual logged-in uploader ID must be stored");
        check("https://example.com/new".equals(MaterialDAO.findMaterial(conn, 42, id).getUrl()), "Resource URL must round-trip");
        expectSqlFailure(() -> MaterialDAO.insertMaterial(conn, 7, 20, "Wrong uploader", "", "https://example.com"), "Uploader must belong to selected team");
        check(MaterialDAO.findMaterial(conn, 7, id) == null, "Resource detail must be team-scoped");
        check(!MaterialDAO.updateMaterial(conn, 7, id, "Wrong", "", "https://example.com/wrong"), "Cannot edit another team's resource");
        check(!MaterialDAO.deleteMaterial(conn, 7, id), "Cannot delete another team's resource");
        check(MaterialDAO.updateMaterial(conn, 42, id, "Edited resource", "", "https://example.com/edited"), "Own-team resource can be edited");
        check(MaterialDAO.getRecentMaterials(conn, 7, 5).isEmpty(), "Recent resources must exclude other teams");
        check(MaterialDAO.getRecentMaterials(conn, 42, 1).size() == 1, "Recent resources must respect limit");
        check(MaterialDAO.deleteMaterial(conn, 42, id), "Own-team resource can be deleted");
    }

    private static void testLegacyLinks(Connection conn) throws Exception {
        sql(conn, "INSERT INTO materials (team_id,title,link_url,uploader_id) VALUES (7,'Legacy','https://example.com/legacy',10)");
        int legacy = number(conn, "SELECT MAX(material_id) FROM materials");
        check("https://example.com/legacy".equals(MaterialDAO.findMaterial(conn, 7, legacy).getUrl()), "Legacy link_url must remain readable");
        sql(conn, "INSERT INTO materials (team_id,title,link_url,url,uploader_id) VALUES (7,'Already new','https://example.com/old','https://example.com/keep',10)");
        int keep = number(conn, "SELECT MAX(material_id) FROM materials");
        String migration = Files.readString(Path.of("db/migrate_materials_url.sql"));
        Matcher matcher = Pattern.compile("UPDATE materials SET url = link_url.*?;", Pattern.DOTALL).matcher(migration);
        check(matcher.find(), "Migration must include backfill statement");
        sql(conn, matcher.group());
        sql(conn, matcher.group());
        check("https://example.com/legacy".equals(MaterialDAO.findMaterial(conn, 7, legacy).getUrl()), "Repeated backfill must preserve legacy links");
        check("https://example.com/keep".equals(MaterialDAO.findMaterial(conn, 7, keep).getUrl()), "Backfill must not overwrite existing new URLs");
        DefaultTableModel model = new DefaultTableModel(new Object[]{"id","title","description","url","created"}, 0);
        MaterialDAO.loadMaterials(conn, model, 7);
        check(model.getRowCount() == 2, "Legacy and new materials must be listed together");
        check(number(conn, "SELECT COUNT(*) FROM materials WHERE uploader_id=10") == 2, "Migration must retain uploader IDs");
    }
}
