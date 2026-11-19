package teamproject.common;

// 브리핑 담당: 장*민 | 파일별 설명: docs/02_common.md

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// 원본과 같은 JDBC 연결 방식입니다. 실행할 때 정한 접속 정보를 메모리에만 둡니다.
public class DB_connection {
  private static String url;
  private static String user;
  private static String password;

  public static void configure(String jdbcUrl, String username, String dbPassword) {
    url = jdbcUrl;
    user = username;
    password = dbPassword;
  }

  // 일반 화면용 연결과 자동 갱신 작업용 연결을 따로 열어 서로 방해하지 않게 합니다.
  public static Connection open() throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }
}
