# 장*민 — DB·공통 시스템 브리핑

[먼저 읽는 쉬운 설명](easy/common.md) → 이 문서 → [실제 코드 색인](07_code_index.md) 순서로 확인하세요.

담당 범위는 `common` 패키지의 Main, DB_connection, SchemaSetup, TeamDAO, ActivityDAO와 schema.sql입니다. **어떤 화면이 열려도 같은 사용자·팀 데이터를 사용하도록 기반을 제공하는 역할**입니다. ActivityPanel 화면은 신*준 담당이며 기록 저장·조회 DAO는 이 문서의 담당입니다.

## 시작 전에 알아둘 값

| 값 | 예 | 의미·보관 위치 |
|---|---|---|
| JDBC URL | jdbc:h2:file:… 또는 jdbc:mysql:… | 접속할 DB의 주소. DB_connection 정적 필드 |
| DB 사용자·비밀번호 | sa / 데모 빈 값 | DB 연결용. 프로그램 로그인 계정과 다름 |
| 로그인 userId | 10 | 사용자를 구분. UserDAO.User 객체 |
| 선택한 teamId | 7 또는 42 | 현재 관리할 프로젝트. TeamDAO.Team 객체 |
| Connection | conn | DB와 통신하는 JDBC 연결 객체. 화면 생성자에 전달 |

`static` 필드는 DB 접속 설정을 프로그램 한 프로세스에서 공유합니다. 사용자·팀은 임의 전역 숫자로 고정하지 않고 로그인 결과 객체를 통해 넘깁니다. 여러 PC에서는 각 프로세스가 자기 Connection을 갖고 같은 MySQL 데이터를 봅니다.

## Main.java: 실행 흐름

1. `main`은 `SwingUtilities.invokeLater(Main::start)`를 호출합니다. `Main::start`는 start 메서드를 나중에 실행할 작업으로 넘기는 문법입니다.
2. EDT가 실행 방식 선택창을 엽니다. 취소하면 종료합니다.
3. 데모면 사용자 홈 폴더를 만들고 H2 파일 경로를 정합니다. MySQL이면 전용 DB 주소·사용자·비밀번호를 읽습니다.
4. `DB_connection.configure`로 접속 값을 기억합니다.
5. 시작 대기창을 띄운 뒤 `SwingWorker`를 실행합니다. `doInBackground`에서 연결·스키마 준비, `done`에서 결과를 꺼내 로그인 화면을 엽니다.
6. 준비 중 실패하면 연결을 닫고 오류를 표시합니다. DAO 안에서 화면을 만들지 않습니다.

왜 SwingWorker인가: DB 준비 때문에 EDT가 기다리면 창을 그리거나 버튼 이벤트를 처리하지 못합니다. 배경 스레드는 오래 걸릴 작업, EDT는 Swing 화면 변경을 맡습니다. `get()`은 done 시점에 이미 끝난 배경 작업의 결과 또는 예외를 받습니다.

## DB_connection.java: 드라이버와 JDBC

`configure`는 값을 저장할 뿐 연결하지 않습니다. `open`이 `DriverManager.getConnection(url,user,password)`을 호출할 때 실제 연결됩니다. DriverManager는 클래스패스의 JDBC 드라이버에서 URL에 맞는 구현을 사용합니다. 프로그램은 Connection 인터페이스를 사용하므로 H2와 MySQL 분기가 DAO마다 필요하지 않습니다.

자동 메모 갱신은 `open`으로 별도 읽기 연결을 만들고 try-with-resources로 닫습니다. 일반 화면이 사용하는 conn을 배경 스레드가 동시에 사용하면 트랜잭션 상태가 섞일 수 있어 분리했습니다.

## SchemaSetup.java: 초기화와 데모 데이터

`initialize`는 클래스 리소스로 schema.sql을 읽습니다. UTF-8로 문자열을 만들고, SQL 주석을 제거하고, 세미콜론 단위로 나눠 비어 있지 않은 명령을 Statement로 실행합니다. 이 파일은 프로그램이 제공하는 고정 DDL이므로 사용자 입력을 붙이지 않습니다. 이 간단한 분리기는 임의의 SQL 덤프·프로시저를 해석하는 범용 파서가 아닙니다.

`CREATE TABLE IF NOT EXISTS`이므로 같은 스키마를 다시 준비해도 표가 중복 생성되지 않습니다. **기존 다른 구조를 자동 변경하는 마이그레이션 기능은 아닙니다.** 전용 새 DB를 사용하는 이유입니다.

데모일 때만 `seedDemo`를 호출합니다. users가 비어 있지 않으면 그대로 돌아갑니다. 처음이면 회원 10~50, 팀 7·42, 소속·업무·첫 메모·일정·아이디어를 한 트랜잭션으로 넣습니다. 도중 실패하면 전부 취소합니다. ID가 반드시 1부터 시작한다고 가정하지 않는지 시연할 수 있습니다.

## TeamDAO.java: 소속 팀과 공통 검사

`findTeams(conn,userId)`는 teams와 team_members를 team_id로 JOIN하고 `WHERE m.user_id=?`로 사용자의 소속만 조회합니다. 각 행을 Team 객체에 담아 ArrayList로 돌려줍니다. `Team.toString`은 선택창에 보일 이름입니다.

`requireMember(conn,teamId,userId)`는 두 값이 함께 존재하는지 SELECT 1로 확인합니다. 반환할 상세 정보가 필요하지 않아 이름 전체를 읽지 않습니다. 없으면 SQLException을 던져 호출한 DAO가 저장을 중단합니다. 새 메모·아이디어·댓글·공감·일정 저장이 이 공통 검사를 사용합니다.

## ActivityDAO.java: 기록과 조회

`record`는 팀·사용자·동작을 activity_log에 INSERT합니다. 기록 시각은 DB 기본값입니다. 호출한 DAO가 진행 중인 트랜잭션을 그대로 사용하므로 메모·아이디어 저장과 활동 기록이 함께 commit되거나 함께 rollback됩니다. record 스스로 commit하지 않는 이유입니다.

`recent`는 users와 JOIN해 사람 이름을 얻고 activity_id 내림차순으로 최근 30건을 반환합니다. 같은 시각 기록도 정수 키로 순서가 결정됩니다. 반환값은 화면에 바로 넣을 문자열 목록입니다.

기록 범위는 메모 저장, 일정 저장, 아이디어 저장·댓글·공감입니다. 아직 모든 기존 기능과 삭제를 추적하는 감사 로그는 아닙니다.

## 핵심 알고리즘을 예로 설명

회원 10으로 로그인 → findTeams는 [팀7,팀42] 반환 → 사용자가 팀42 선택 → MainFrame이 42를 모든 화면에 전달합니다. 팀7의 메모를 조회하는 SQL은 실행되지 않습니다. `setInt(1,42)`에서 1은 첫 물음표, 42가 실제 팀 번호입니다.

메모 저장에 활동 기록까지 3개 SQL이 필요하다면, 첫 SQL 성공만으로 성공 안내하지 않습니다. 마지막까지 성공해야 commit합니다. 예외가 발생하면 rollback으로 시작 전 상태로 되돌립니다.

## 브리핑 원고 예시

“제 코드는 실행 방식과 DB 연결을 준비하고, 로그인한 사람이 속한 팀을 조회하는 공통 부분입니다. 화면은 JDBC의 Connection을 받아 기능별 DAO를 호출합니다. 로컬 데모는 H2를 사용하지만 SQL과 JDBC 구조는 MySQL 방식과 같습니다. 새 공유 기능의 저장과 활동 기록은 같은 트랜잭션에서 처리해 부분 저장을 막았습니다.”

## 교수 질문에 답하기

- **DB 로그인과 프로그램 로그인 차이?** DB 로그인은 저장소에 접속하는 권한, 프로그램 로그인은 users에서 사용자를 찾는 기능입니다.
- **왜 TeamDAO를 분리했나?** 로그인 화면과 모든 새 저장 기능이 같은 소속 검사 규칙을 사용하기 때문입니다.
- **왜 ID를 7·42로 시연하나?** 기존의 1 고정 문제가 실제로 사라졌는지 확인하기 위해서입니다.
- **왜 메모마다 새 연결을 여나?** 배경 폴링과 화면의 저장 트랜잭션이 같은 Connection을 공유하지 않도록 하기 위해서입니다.
- **왜 DB에 활동 시간을 저장하나?** 여러 PC의 화면 시각 대신 같은 DB의 저장 시각을 사용하기 위해서입니다.

## 직접 확인할 과제

demo1으로 두 소속을 확인하고 팀42에 들어가 보세요. users가 비어 있지 않으면 데모 초기화가 왜 건너뛰는지 설명하세요. ActivityDAO.record에서 commit을 추가하면 호출한 메모 저장에 어떤 문제가 생길지 예측하세요. 각 메서드의 실제 위치는 [코드 색인](07_code_index.md#common)을 확인하세요.
