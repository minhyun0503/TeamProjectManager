package teamproject.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_connection {//mySQL 연결 담당 클래스

    // MySQL 접속 정보
    private static final String URL = //URL
            "jdbc:mysql://mysql-3b00e226-minhyun030503-11b1.b.aivencloud.com:22196/team_project?sslMode=REQUIRED"
            // [시간대] Aiven DB 서버는 UTC(한국보다 9시간 느림)로 동작한다.
            //  - connectionTimeZone=UTC : DB에 저장된 시각(NOW(), CURRENT_TIMESTAMP)을 UTC로 보고 읽는다.
            //  - forceConnectionTimeZoneToSession=true : 접속할 때 DB 세션 시간대도 UTC로 맞춰서 항상 같은 기준을 쓴다.
            //  → 읽어 온 시각은 Main에서 정한 한국시간(Asia/Seoul)으로 자동 변환되어 화면에 표시된다.
            //    (DATE 칸인 마감일 · 일정 날짜는 변환되지 않고 그대로 유지된다)
            + "&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true";

    private static final String USER = "avnadmin";

    // 데이터베이스 연결 메서드
    public static Connection getConnection(String password)//비밀번호를 매개변수로 전달받고, MySQL연결 객체 반환 메서드
            throws SQLException {//SQL관련 오류 발생 시 호출한 쪽(MAIN)으로 전달

        return DriverManager.getConnection(URL, USER, password); //JDBC를 사용해 데이터베이스 연결 생성후 호출한 쪽(MAIN)으로 반환
    }
}
