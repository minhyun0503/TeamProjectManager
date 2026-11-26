package teamproject.common;

// 로그인한 사용자 정보를 프로그램 전체에서 함께 쓰기 위한 클래스
// - 변수가 static이라서 객체를 만들지 않고 LoginUser.getName() 처럼 바로 쓸 수 있다.
// - 메모장 · 포스트잇 · 문서에서 "누가 썼는지" 기록할 때 사용한다.
public class LoginUser {

    // 현재는 1번 팀으로 고정 (팀 선택 기능은 추후 추가)
    public static final int TEAM_ID = 1;

    private static int userId = 0;      // users 표의 user_id (0이면 로그인 전)
    private static String name = "";    // users 표의 name

    // 로그인 성공 시 LoginFrame에서 호출
    public static void login(int id, String userName) {
        userId = id;
        name = userName;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getName() {
        return name;
    }
}
