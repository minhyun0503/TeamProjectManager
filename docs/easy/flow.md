# 모두가 알아야 할 전체 흐름

## 프로그램을 켜면 무슨 일이 일어날까?

| 순서 | 실제 파일 | 하는 일 |
|---|---|---|
| 1 | Main.java | 로컬 데모인지 MySQL 연결인지 선택받음 |
| 2 | DB_connection.java | 선택한 DB에 연결 |
| 3 | SchemaSetup.java | 필요한 테이블을 준비하고 첫 데모 데이터를 넣음 |
| 4 | LoginFrame.java | 아이디와 비밀번호 입력받음 |
| 5 | UserDAO.java | 계정이 맞는지 조회하고 회원 번호·이름 반환 |
| 6 | TeamDAO.java | 그 회원이 속한 팀 조회 |
| 7 | MainFrame.java | 선택한 팀의 업무·메모 등 9개 탭 표시 |

demo1로 로그인하면 회원 번호 10을 얻습니다. 회원10은 팀7과 팀42에 소속돼 있으므로 팀을 고릅니다. 팀7을 고르면 각 화면이 같은 teamId=7을 사용합니다.

## 예제: 업무 등록 버튼 한 번 따라가기

사용자가 업무명에 “자료 조사”, 담당자에 데모팀원3, 마감일에 올바른 날짜를 입력했다고 생각해 봅시다.

1. **TaskFrame의 버튼 이벤트**가 registerTask를 부릅니다.
2. **showTaskInputDialog**가 업무명·담당자·마감일·상태를 받습니다.
3. 필수 항목과 날짜를 검사합니다. 잘못 입력했다면 DB에 보내기 전에 안내합니다.
4. **TaskDAO.insertTask**에 선택한 팀 번호와 입력값을 보냅니다.
5. DAO는 담당자가 이 팀에 있는지 확인하고 tasks에 새 행을 넣습니다.
6. 저장이 성공하면 **TaskFrame.reload**가 업무 표와 진행률을 다시 읽습니다.

다른 기능도 큰 순서는 같습니다. 버튼 이름과 DAO가 사용하는 표가 달라질 뿐입니다.

## SQL의 물음표를 실제 값과 연결하기

아래는 SQL 원리를 설명하기 위해 줄인 예제입니다. 완성된 TaskDAO를 대체하는 코드는 아닙니다.

```java
String sql = "SELECT title FROM tasks WHERE team_id = ?";
PreparedStatement p = conn.prepareStatement(sql);
p.setInt(1, teamId);
ResultSet rs = p.executeQuery();
```

| 코드 | 말로 풀기 |
|---|---|
| String sql = ... | 실행할 조회 문장을 문자열로 보관 |
| conn.prepareStatement(sql) | 이 연결에서 실행할 SQL 준비 |
| p.setInt(1, teamId) | 첫 번째 ?에 현재 팀 번호를 정수로 채움 |
| executeQuery() | 조회를 실행하고 결과를 받음 |

teamId가7이면 “tasks에서 팀7의 업무 제목을 찾아 달라”는 요청입니다. `setInt(1, teamId)`를 보고 “팀1로 고정했다”고 설명하면 안 됩니다.

PreparedStatement는 사용자가 쓴 문자열을 SQL 명령 자체와 구분해 전달하는 데도 도움이 됩니다. 그래서 입력값을 SQL 문자열 뒤에 직접 이어 붙이지 않습니다.

## 조회 결과를 읽는 while문

```java
while (rs.next()) {
    String title = rs.getString("title");
}
```

`next()`는 다음 행이 있는지 확인하면서 그 행으로 이동합니다. 결과가3행이면 본문을3번 실행하고, 없으면0번 실행합니다. 실제 DAO는 읽은 값을 객체나 화면 목록에 넣습니다.

SELECT는 `executeQuery()`로 결과 목록을 받습니다. INSERT·UPDATE·DELETE는 `executeUpdate()`로 처리한 행 수를 받습니다. 메서드 이름이 비슷하므로 구분해서 설명하세요.

## 저장 여러 개를 묶어야 하는 경우

메모 한 번을 저장할 때 메모 본문, 내용 이력, 활동 기록이 함께 저장됩니다. 이 중 본문만 저장되고 이력이 빠지는 상태는 피하고 싶습니다.

| 코드 | 뜻 |
|---|---|
| setAutoCommit(false) | SQL마다 바로 확정하지 않고 묶어서 처리 시작 |
| commit() | 지금까지의 저장을 함께 확정 |
| rollback() | 중간에 실패했으므로 이번 묶음을 취소 |
| finally의 setAutoCommit(true) | 다음 화면 작업을 위해 기본 상태로 복원 |

이 묶음을 **트랜잭션**이라고 합니다. 메모를 업무로 만드는 두 INSERT도 같은 이유로 묶습니다.

## 모듈과 도구는 왜 썼을까?

| 도구 | 맡은 일 | 선택 이유 |
|---|---|---|
| Swing / java.desktop | 창, 버튼, 표, 목록 | 자바 기본 도구로 화면·이벤트를 설명할 수 있음 |
| JDBC / java.sql | 자바 코드와 DB 연결 | H2와 MySQL에 같은 연결·쿼리 구조 사용 |
| H2 드라이버 | 로컬 데모 DB 실행 | 별도 DB 설치 없이 시연 가능 |
| MySQL 드라이버 | 공유 서버에 접속 | 여러 PC가 같은 데이터를 사용할 때 필요 |
| LocalDate·YearMonth | 날짜와 월 계산 | 윤년·월 길이를 직접 하드코딩하지 않아도 됨 |
| ArrayList | 여러 조회 결과 보관 | 순서대로 넣고 반복문으로 읽기 쉬움 |
| Timer·SwingWorker | 정기 조회와 배경 작업 | 정해진 간격에 읽되, 반복 조회 때문에 화면이 멈추는 일을 줄임 |

모듈은 JDK 기능 묶음, 패키지는 우리가 파일을 나눈 공간, JAR는 외부 라이브러리 파일입니다. 서로 같은 말은 아닙니다.

## 자동 갱신을 정확하게 설명하기

공유 메모는3초, 아이디어 목록은5초 간격으로 DB를 다시 읽습니다. 이 방식을 **폴링**이라고 합니다. 저장 즉시 모든 글자가 동시에 바뀌는 공동 편집 방식은 아닙니다.

Timer가 “다시 읽을 시간”을 알리고, SwingWorker의 doInBackground가 별도 연결에서 읽습니다. 읽기가 끝나면 done이 화면 목록을 바꿉니다. Swing 화면을 바꾸는 담당 스레드를 EDT라고 합니다. 별도 연결을 쓰는 이유는 화면의 저장 작업과 배경 읽기가 같은 Connection의 상태를 섞어 쓰지 않게 하기 위해서입니다.
