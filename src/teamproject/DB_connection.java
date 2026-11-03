package teamproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_connection {//mySQL 연결 담당 클래스

    // MySQL 접속 정보
    private static final String URL = //URL
            "jdbc:mysql://127.0.0.1:3306/team_project";

    private static final String USER = "root";

    // 데이터베이스 연결 메서드
    public static Connection getConnection(String password)//비밀번호를 매개변수로 전달받고, MySQL연결 객체 반환 메서드
            throws SQLException {//SQL관련 오류 발생 시 호출한 쪽(MAIN)으로 전달

        return DriverManager.getConnection(URL, USER, password); //JDBC를 사용해 데이터베이스 연결 생성후 호출한 쪽(MAIN)으로 반환
    }
}
