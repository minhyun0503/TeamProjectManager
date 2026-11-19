# 장*민 · 소스별 브리핑

담당: 공통 실행·DB

PPT 원래 담당: DB·JDBC·공통 시스템 / 추가 담당: 독립 데모 시작·확장 스키마·소속 검사·활동 기록

[코드와 설명 함께 보기](01_common.html) · [PDF](../../output/pdf/member_briefings/01_common.pdf)

각 파일을 열고 아래 순서로 위에서 아래로 설명합니다. 실행은 선언 순서가 아니라 호출·이벤트 순서를 따릅니다.

## 모듈·문법 먼저 확인

소스는 위에서 아래로 읽되 실행은 호출과 이벤트를 따릅니다. 생성자는 new로 객체를 만들 때, 버튼에 연결한 코드는 클릭할 때 실행됩니다.

private은 클래스 내부에서 사용, public은 다른 클래스에서도 호출, static은 객체 생성 없이 클래스 이름으로 호출, final 필드는 대입 후 다시 다른 값을 넣지 않는다는 뜻입니다.

this는 현재 객체, null은 가리키는 객체가 없음입니다. <자료형>은 목록 등에 넣을 값의 종류, 조건 ? A : B는 참일 때 A·거짓일 때 B를 고르는 문법입니다.

e -> 동작은 이벤트가 생겼을 때 실행할 짧은 함수를 연결하는 문법입니다. return은 현재 메서드를 끝내고, throw는 오류를 호출한 곳으로 전달합니다.

java.desktop은 Swing·화면 배치, java.sql은 DB 통신 모듈입니다. JList·Connection은 그 안의 클래스입니다. java.util·java.time은 java.base 안의 패키지입니다.

| 주요 클래스 | 역할 |
|---|---|
| JPanel | 창 안에서 관련 화면 요소를 묶는 영역입니다. |
| JLabel | 안내 문구나 제목을 보여 주는 글자 표시입니다. |
| JPasswordField | 입력 글자가 화면에서 가려지는 비밀번호 칸입니다. |
| JTextField | 한 줄 입력칸입니다. |
| JOptionPane | 입력·확인·안내창을 간단히 만듭니다. |
| Connection | 열린 DB 연결을 나타냅니다. |
| PreparedStatement | SQL과 입력값을 따로 전달해 조회·변경합니다. |
| ResultSet | DB 조회 결과입니다. next()로 다음 결과 행에 이동합니다. |
| SwingWorker | DB 배경 작업과 완료 후 화면 처리를 나눕니다. |

## 1. Main.java

[src/teamproject/common/Main.java](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/Main.java#L1)

프로그램의 시작점. 실행 방식을 고르고 DB 준비가 끝나면 로그인 창을 엽니다.

먼저 볼 변수·인수: args는 시작 인수, mode는 실행 방식(0=로컬 데모), demo는 데모 자료를 넣을지 나타내는 참·거짓 값입니다.

실행 흐름: main → start → DB_connection.configure → doInBackground → SchemaSetup.initialize → done → LoginFrame

실행 예: 로컬 데모를 고르면 사용자 홈 폴더에 파일 DB를 준비합니다. 이미 저장된 데이터는 다음 실행에도 남습니다.

### 상단 선언과 모듈 · 1~10줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/Main.java#L1) · 실행·읽기 시점: 파일을 처음 열 때

1. package는 이 파일이 속한 묶음입니다. Files·Path는 폴더와 경로, Connection은 DB 연결, javax.swing은 화면을 만드는 클래스입니다.
2. import는 필요한 클래스의 짧은 이름을 쓰게 하는 선언입니다. 가져온 것만으로 DB 연결이나 창 생성이 실행되지는 않습니다.

### main: 가장 먼저 실행 · 11~16줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/Main.java#L11) · 실행·읽기 시점: 자바가 프로그램을 시작할 때

1. static 메서드여서 Main 객체를 따로 만들지 않고 실행됩니다.
2. SwingUtilities.invokeLater(Main::start)는 화면을 다루는 전용 실행 흐름인 EDT에 start 실행을 맡깁니다. Main::start는 실행할 메서드를 가리킵니다.

관련 선언: main (12줄)

> **EDT**: Swing 화면과 사용자 입력 처리를 담당하는 전용 실행 흐름입니다. 오래 걸리는 작업을 여기서 계속하면 화면 반응이 늦어집니다.

### start 앞부분: 실행 방식 선택 · 17~59줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/Main.java#L17) · 실행·읽기 시점: main이 시작 작업을 맡기면

1. 선택창의 결과가 음수이면 창을 닫았다는 뜻이므로 return으로 종료합니다.
2. mode가 0이면 홈 폴더에 저장 위치를 만들고 H2 파일 DB 주소를 configure에 전달합니다. AUTO_SERVER는 같은 컴퓨터의 여러 실행에서 파일 DB를 함께 열도록 하는 H2 설정입니다.
3. MySQL이면 주소·DB 계정·DB 비밀번호를 받습니다. 이 비밀번호는 프로그램의 demo1234 로그인 비밀번호와 구분합니다. 취소하거나 원본 DB 이름을 입력하면 진행하지 않습니다.

관련 선언: start (17줄)

### 대기창과 SwingWorker 생성 · 60~70줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/Main.java#L60) · 실행·읽기 시점: DB 주소 설정을 끝내면

1. 작업 중임을 보여 주는 JDialog를 열고, X 버튼으로 준비 중인 창을 없애지 못하게 합니다.
2. SwingWorker는 시간이 걸리는 일을 화면 처리와 나누는 클래스입니다. execute()가 아래의 두 메서드를 정해진 순서로 실행시킵니다.

> **SwingWorker**: 느린 작업은 doInBackground에서, 끝난 뒤 화면 변경은 done에서 처리하도록 나누는 클래스입니다.

### doInBackground: 연결과 DB 준비 · 71~81줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/Main.java#L71) · 실행·읽기 시점: SwingWorker가 별도의 작업 흐름에서 호출

1. open으로 DB 연결을 만들고 initialize에 연결과 demo 여부를 넘깁니다.
2. 정상 완료하면 Connection을 반환합니다. 준비 중 오류가 나면 연결을 닫고 throw로 오류를 위쪽 호출자에게 전달합니다.

관련 선언: doInBackground (71줄)

### done: 준비 결과로 로그인 열기 · 82~95줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/Main.java#L82) · 실행·읽기 시점: 배경 작업이 끝나면 EDT에서 호출

1. 대기창을 닫고 get()으로 앞 메서드가 반환한 Connection을 받습니다. 배경 작업의 오류도 get()에서 확인합니다.
2. 그 연결을 LoginFrame 생성자에 전달하고 setVisible(true)로 창을 보여 줍니다. 바깥 catch는 시작 설정 과정의 오류를 안내합니다.

관련 선언: done (82줄)

## 2. DB_connection.java

[src/teamproject/common/DB_connection.java](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/DB_connection.java#L1)

접속 정보를 보관하고 필요할 때 새 DB 연결을 만드는 공통 파일입니다.

먼저 볼 변수·인수: url은 DB 위치, user·password는 DB 접속 계정입니다. static 필드이므로 모든 화면이 같은 설정을 사용합니다.

실행 흐름: DB_connection.configure → 저장된 접속 정보 → 각 DAO/자동 갱신 작업에서 open

실행 예: 메모 자동 갱신은 open을 다시 호출해 별도 Connection을 사용합니다. 연결 객체가 달라도 DB 주소가 같으면 같은 저장소를 봅니다.

### 모듈과 필드 · 1~14줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/DB_connection.java#L1) · 실행·읽기 시점: 클래스를 읽기 시작할 때

1. java.sql 모듈의 Connection·DriverManager·SQLException을 사용합니다.
2. 접속 문자열 3개를 private static 필드로 둡니다. private은 이 클래스 안에서만 직접 읽고 바꿀 수 있다는 뜻입니다.

### configure: 설정 저장 · 15~20줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/DB_connection.java#L15) · 실행·읽기 시점: Main에서 실행 방식을 선택한 뒤 호출

1. 받은 jdbcUrl·username·dbPassword를 필드에 대입합니다.
2. 이 단계는 값만 보관합니다. 실제 연결 시도는 open에서 합니다.

관련 선언: configure (15줄)

### open: 새 연결 반환 · 21~25줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/DB_connection.java#L21) · 실행·읽기 시점: 시작 준비 또는 자동 조회가 DB 연결을 요청할 때

1. DriverManager.getConnection이 주소에 맞는 JDBC 드라이버를 통해 연결합니다. JDBC는 자바가 DB와 통신할 때 사용하는 공통 방식입니다.
2. Connection을 호출한 곳에 돌려줍니다. 연결 실패는 SQLException으로 전달합니다. 여기서는 SQL을 실행하지 않습니다.

관련 선언: open (22줄)

## 3. SchemaSetup.java

[src/teamproject/common/SchemaSetup.java](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/SchemaSetup.java#L1)

필요한 표를 준비하고 처음 실행한 로컬 데모에만 가상 자료를 넣습니다.

먼저 볼 변수·인수: conn은 이미 열린 연결, demo는 데모 자료 삽입 여부입니다. in은 schema.sql 파일을 읽는 통로입니다.

실행 흐름: initialize → schema.sql 읽기·실행 → demo이면 seedDemo → 성공하면 Main으로 복귀

실행 예: users가 비어 있으면 회원 10·20·30·40·50과 팀 7·42를 만듭니다. 1로 고정해서 쓰는 구조가 아님을 확인할 수 있습니다.

### 모듈과 클래스 역할 · 1~12줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/SchemaSetup.java#L1) · 실행·읽기 시점: 상단부터 읽을 때

1. InputStream은 파일 내용을 읽고 StandardCharsets.UTF_8은 한글을 올바르게 해석합니다.
2. Statement는 이 파일에 고정된 SQL을 실행합니다. 사용자 입력을 SQL에 넣는 DAO에서는 PreparedStatement를 사용합니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### initialize: 표 구조 준비 · 13~26줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/SchemaSetup.java#L13) · 실행·읽기 시점: Main의 doInBackground에서 호출

1. 같은 패키지의 schema.sql을 자원 파일로 읽습니다. 없으면 즉시 오류를 발생시킵니다.
2. 현재 스키마 파일의 한 줄 주석을 제거한 뒤 세미콜론으로 나눕니다. 비어 있지 않은 명령을 for 반복문으로 실행합니다. 이 분리 방식은 현재처럼 단순한 SQL 파일을 전제로 합니다.
3. try 괄호 안에서 만든 파일·실행 도구는 블록을 벗어나면 자동으로 닫힙니다. demo가 true일 때만 seedDemo를 부릅니다.

관련 선언: initialize (13줄)

### seedDemo 앞부분: 중복 자료 방지 · 27~33줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/SchemaSetup.java#L27) · 실행·읽기 시점: 로컬 데모 표 구조를 만든 뒤

1. users 행 수를 읽고 rs.next()로 첫 결과로 이동합니다. 값이 0이 아니면 return하여 기존 자료를 유지합니다.
2. setAutoCommit(false)는 SQL마다 바로 저장하던 방식을 잠시 끄는 것입니다. 여러 삽입을 트랜잭션으로 묶습니다.

관련 선언: seedDemo (27줄)

> **트랜잭션**: 여러 DB 변경을 한 묶음으로 처리해 모두 확정하거나, 오류가 나면 묶음 전체를 취소하는 처리입니다.

### 가상 자료 삽입과 성공·실패 처리 · 34~72줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/SchemaSetup.java#L34) · 실행·읽기 시점: 아직 회원이 없을 때만

1. 회원 → 팀 → 팀 소속 → 업무 → 메모 → 메모 이력 → 일정 → 아이디어 순으로 넣습니다. 참조할 회원·팀이 먼저 있어야 하기 때문입니다.
2. 트랜잭션은 여러 DB 변경을 한 묶음으로 처리하는 방식입니다. 모두 성공하면 commit으로 확정하고, 하나라도 실패하면 rollback으로 이 묶음을 취소합니다.
3. finally의 setAutoCommit(true)는 성공 여부와 관계없이 자동 저장 방식을 되돌립니다. 실제 학생 정보가 아닌 가상 자료를 넣는 부분입니다.

> **rollback**: 아직 확정하지 않은 현재 트랜잭션의 변경을 취소합니다.

> **commit**: 현재 트랜잭션의 변경을 DB에 최종 확정합니다.

> **자동 저장**: JDBC의 autoCommit 설정입니다. true면 SQL 변경마다 확정하고 false면 commit·rollback으로 묶음을 마칩니다.

## 4. TeamDAO.java

[src/teamproject/common/TeamDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/TeamDAO.java#L1)

로그인 회원의 소속 팀 목록을 찾고 저장 요청의 소속을 검사합니다.

먼저 볼 변수·인수: Team에는 teamId(실제 팀 번호)와 teamName(표시 이름)을 보관합니다. findTeams의 userId는 로그인 결과에서 받습니다.

실행 흐름: LoginFrame → findTeams → Team 선택 / 각 저장 DAO → requireMember

실행 예: demo1은 팀 7·42를 선택할 수 있습니다. 선택창에서 보이는 문자열과 DB 조건에 쓰는 번호는 구분됩니다.

### 모듈과 Team 자료형 · 1~17줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/TeamDAO.java#L1) · 실행·읽기 시점: 파일을 처음 열 때

1. List는 여러 팀을 보관하는 목록의 형식이고 ArrayList는 실제로 항목을 넣는 목록 클래스입니다.
2. static 내부 클래스 Team은 팀 한 건을 묶어 전달합니다. final 필드는 생성할 때 받은 값을 이후 다시 대입하지 않습니다.

### Team 생성자와 값 반환 메서드 · 18~36줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/TeamDAO.java#L18) · 실행·읽기 시점: 조회한 팀 한 건을 객체로 만들 때

1. 생성자는 번호와 이름을 this.teamId·this.teamName에 보관합니다. this는 지금 만들어진 객체를 뜻합니다.
2. getTeamId는 팀 번호, getTeamName은 팀 이름을 반환합니다. toString은 선택창에 표시할 “팀 이름 (팀 번호)” 문자열입니다. 세 메서드는 DB를 변경하지 않습니다.

관련 선언: Team (18줄), getTeamId (23줄), getTeamName (27줄), toString (32줄)

### findTeams: 소속 팀 조회 · 37~57줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/TeamDAO.java#L37) · 실행·읽기 시점: 로그인 성공 후 회원 번호를 받으면

1. teams와 team_members를 team_id가 같은 행끼리 JOIN합니다. JOIN은 여러 표의 관련 행을 이어서 읽는 SQL 기능입니다.
2. WHERE의 ?에 로그인한 userId를 넣고 executeQuery로 조회합니다. setInt(1, userId)의 1은 첫 번째 ?의 위치입니다.
3. while(rs.next())로 모든 결과를 하나씩 Team으로 만들어 목록에 넣고 반환합니다. 결과가 없으면 빈 목록입니다.

관련 선언: findTeams (37줄)

> **JOIN**: 관련 번호가 같은 여러 표의 행을 이어 필요한 값을 함께 읽는 SQL 기능입니다.

### requireMember: 현재 팀 소속 검사 · 58~69줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/TeamDAO.java#L58) · 실행·읽기 시점: 메모·일정·아이디어 저장 등이 요청되면

1. 팀 번호와 회원 번호 두 조건으로 team_members를 찾습니다. SELECT 1은 전체 정보를 가져오는 대신 존재 여부만 확인하려는 것입니다.
2. 한 행도 없으면 SQLException을 발생시켜 저장을 중단합니다. 행이 있으면 별도 반환값 없이 정상 종료합니다.

관련 선언: requireMember (59줄)

## 5. ActivityDAO.java

[src/teamproject/common/ActivityDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/ActivityDAO.java#L1)

메모·일정·아이디어 관련 활동 문장을 저장하고 최근 30개를 조회합니다.

먼저 볼 변수·인수: record의 action은 기록할 문장입니다. conn을 호출자에게서 받으므로 원래 변경과 같은 트랜잭션에 참여할 수 있습니다.

실행 흐름: 각 기능 DAO → record / ActivityPanel → recent → 화면 목록

실행 예: 메모 저장과 “공유 메모 저장: 제목” 활동 기록을 같은 연결로 저장합니다. record 자체는 commit을 하지 않습니다.

### 모듈과 책임 범위 · 1~10줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/ActivityDAO.java#L1) · 실행·읽기 시점: 파일을 열면

1. java.sql로 DB 작업을 하고 List·ArrayList로 최근 활동 문장들을 반환합니다.
2. 최근 활동은 지정한 동작만 기록합니다. 모든 업무·팀원 변경이나 삭제를 전부 추적하는 기능은 아닙니다.

### record: 활동 한 건 저장 · 11~21줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/ActivityDAO.java#L11) · 실행·읽기 시점: 호출한 DAO가 변경을 처리하는 도중

1. teamId·userId·action을 첫째부터 셋째 ?에 채워 activity_log에 삽입합니다.
2. executeUpdate로 한 행을 추가합니다. commit·rollback은 이 메서드를 부른 DAO가 결정하므로 원래 변경과 함께 성공·취소할 수 있습니다.

관련 선언: record (11줄)

> **rollback**: 아직 확정하지 않은 현재 트랜잭션의 변경을 취소합니다.

> **commit**: 현재 트랜잭션의 변경을 DB에 최종 확정합니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### recent: 최근 목록 조회 · 22~36줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/ActivityDAO.java#L22) · 실행·읽기 시점: 최근 활동 화면에서 새로고침하면

1. activity_log와 users를 이어 사용자 이름까지 읽고 현재 팀으로 제한합니다.
2. activity_id 내림차순은 큰 번호부터, LIMIT 30은 최대 30행입니다. 각 행을 “시간 | 이름 | 활동” 문자열로 만들어 목록으로 돌려줍니다.

관련 선언: recent (22줄)

## 6. module-info.java

[src/module-info.java](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/module-info.java#L1)

프로그램이 사용하는 자바 모듈을 선언합니다.

먼저 볼 변수·인수: TeamProjectManagerFeedback은 이 프로젝트의 모듈 이름입니다. requires는 다른 모듈의 기능이 필요하다는 선언입니다.

실행 흐름: 컴파일·실행 준비 → 모듈의 사용 가능 범위 확인

실행 예: JList를 사용하려면 java.desktop, Connection을 사용하려면 java.sql이 필요합니다. H2·MySQL 드라이버 JAR도 별도로 있어야 DB에 연결됩니다.

### 모듈 선언 전체 · 1~6줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/module-info.java#L1) · 실행·읽기 시점: Eclipse에서 module-info.java를 열 때

1. java.sql은 JDBC의 Connection·PreparedStatement·ResultSet 등을 제공합니다.
2. java.desktop은 Swing 화면과 AWT의 배치·색·브라우저 연동 등을 제공합니다. JList는 모듈 자체가 아니라 java.desktop 안의 클래스입니다.
3. requires는 창을 띄우거나 DB를 여는 실행문이 아닙니다. 사용 가능한 기능 묶음을 선언하는 부분입니다.

## 7. schema.sql

[src/teamproject/common/schema.sql](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/schema.sql#L1)

각 기능이 함께 쓰는 DB 표와 연결 관계를 정의합니다.

먼저 볼 변수·인수: PRIMARY KEY는 행을 구별하는 기준, FOREIGN KEY는 다른 표의 행을 가리키는 번호입니다. AUTO_INCREMENT는 새 번호 자동 생성입니다.

실행 흐름: 회원 → 팀·팀 소속 → 업무·일정·자료 → 메모·업무 연결 → 아이디어·반응 → 활동

실행 예: 회원 정보는 users에 한 번 보관하고 팀별 역할은 team_members에 둡니다. 한 회원이 팀 7과 팀 42에서 다른 역할을 가질 수 있습니다.

### users: 사용자 계정 · 1~10줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/schema.sql#L1) · 실행·읽기 시점: 첫 표부터 읽기

1. user_id는 내부 회원 번호, login_id는 로그인 문자열, student_no는 별도 학번입니다. 같은 값이라고 가정하지 않습니다.
2. NOT NULL은 필수값, UNIQUE는 중복 금지입니다. CREATE TABLE IF NOT EXISTS는 없는 표만 만들며 기존 표를 자동으로 변환하지 않습니다.

### teams와 team_members: 팀과 소속 · 11~31줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/schema.sql#L11) · 실행·읽기 시점: 계정 표 다음

1. teams는 팀 자체를 저장하고 leader_id로 회원을 참조합니다. team_members는 한 회원이 어느 팀에 속하는지와 역할을 저장합니다.
2. 소속의 기본키는 (team_id,user_id) 두 값의 조합입니다. 같은 팀·회원 조합을 두 번 넣을 수 없지만 같은 회원이 여러 팀에 속할 수 있습니다.

### tasks와 schedules: 업무·별도 일정 · 32~56줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/schema.sql#L32) · 실행·읽기 시점: 팀 구조 다음

1. tasks는 담당자·상태·하나의 마감일을 저장합니다. schedules는 시작일·종료일을 가진 별도 일정입니다.
2. 달력은 두 표를 함께 조회합니다. 업무를 일정 표에 복사하지 않으므로 마감일을 바꿔도 옛 일정이 남지 않습니다.

### materials: 자료 링크 · 57~71줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/schema.sql#L57) · 실행·읽기 시점: 자료 관리 기능의 저장 구조

1. 자료 번호·소속 팀·제목·설명·주소·등록자를 저장합니다. 파일 자체를 업로드하는 표는 아닙니다.
2. url은 현재 주소, link_url은 이전 주소를 보존하는 열입니다. MaterialDAO가 현재 주소가 없을 때 이전 주소를 읽습니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### notes·note_history·task_notes: 메모와 연결 · 72~93줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/schema.sql#L72) · 실행·읽기 시점: 공유 메모 관련 표

1. notes.version은 수정할 때 1씩 늘립니다. 화면에서 읽은 버전과 DB 버전이 같은 경우만 수정하여 다른 사람의 변경을 덮어쓰지 않습니다.
2. note_history는 (메모 번호,버전)마다 당시 내용을 저장합니다. task_notes는 생성된 업무와 원문 메모의 번호를 연결합니다.
3. ON DELETE CASCADE는 부모 행을 지울 때 관련 자식 행도 DB가 함께 지우는 규칙입니다. 메모 삭제 시 이력·연결은 지워져도 이미 생성된 업무 행은 유지됩니다.

> **ON DELETE CASCADE**: 참조한 부모 행을 삭제하면 관련 자식 행도 DB가 함께 삭제하도록 정한 규칙입니다.

### ideas·댓글·공감·활동: 협업 자료 · 94~121줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/1beec761ec6fac0d1a6b4eb4e19694740fb5e583/src/teamproject/common/schema.sql#L94) · 실행·읽기 시점: 마지막 표들

1. ideas에는 본문, idea_comments에는 댓글, idea_reactions에는 공감한 사람을 각각 저장합니다.
2. 공감의 기본키가 (idea_id,user_id)이므로 한 사람이 같은 글에 중복 공감을 남기지 못합니다. activity_log는 동작 문장과 작성 시각을 저장합니다.

## 마지막 시연·질문

Main을 실행해 로컬 데모를 고르고, DB 준비가 끝나 로그인 창이 열리는 지점까지 설명합니다.

**setInt(1, teamId)의 1은 무엇인가요?**

첫 번째 ?의 위치입니다. 실제 팀 번호는 teamId 변수의 값입니다.

**트랜잭션이 필요한 이유는?**

계정·소속처럼 같이 성공해야 하는 여러 저장을 한 묶음으로 확정하거나 취소하기 위해서입니다.

**schema.sql을 실행하면 기존 표 구조도 바뀌나요?**

아니요. IF NOT EXISTS는 없는 표만 만듭니다. 기존 구조 변경은 별도 SQL이 필요합니다.

소스 기준 커밋: 1beec761ec6fac0d1a6b4eb4e19694740fb5e583. 줄 번호는 이 기준의 실제 파일과 일치합니다.
