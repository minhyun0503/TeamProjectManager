# 이*찬 · 소스별 브리핑

담당: 로그인·팀원·공유 메모

PPT 원래 담당: 로그인·팀원 관리 / 추가 담당: 공유 메모·자동 갱신·수정 충돌 검사·내용 이력

[코드와 설명 함께 보기](02_member.html) · [PDF](../../output/pdf/member_briefings/02_member.pdf)

각 파일을 열고 아래 순서로 위에서 아래로 설명합니다. 실행은 선언 순서가 아니라 호출·이벤트 순서를 따릅니다.

## 모듈·문법 먼저 확인

소스는 위에서 아래로 읽되 실행은 호출과 이벤트를 따릅니다. 생성자는 new로 객체를 만들 때, 버튼에 연결한 코드는 클릭할 때 실행됩니다.

private은 클래스 내부에서 사용, public은 다른 클래스에서도 호출, static은 객체 생성 없이 클래스 이름으로 호출, final 필드는 대입 후 다시 다른 값을 넣지 않는다는 뜻입니다.

this는 현재 객체, null은 가리키는 객체가 없음입니다. <자료형>은 목록 등에 넣을 값의 종류, 조건 ? A : B는 참일 때 A·거짓일 때 B를 고르는 문법입니다.

implements는 인터페이스가 요구하는 메서드를 작성하겠다는 뜻입니다. Swing에 리스너 객체를 등록하면 버튼이나 탭의 변화가 있을 때 그 메서드가 호출됩니다.

e -> 동작은 이벤트가 생겼을 때 실행할 짧은 함수를 연결하는 문법입니다. return은 현재 메서드를 끝내고, throw는 오류를 호출한 곳으로 전달합니다.

java.desktop은 Swing·화면 배치, java.sql은 DB 통신 모듈입니다. JList·Connection은 그 안의 클래스입니다. java.util·java.time은 java.base 안의 패키지입니다.

| 주요 클래스 | 역할 |
|---|---|
| JFrame | 독립된 프로그램 창을 만듭니다. |
| JPanel | 창 안에서 관련 화면 요소를 묶는 영역입니다. |
| JLabel | 안내 문구나 제목을 보여 주는 글자 표시입니다. |
| JButton | 눌렀을 때 연결된 동작을 실행하는 버튼입니다. |
| JPasswordField | 입력 글자가 화면에서 가려지는 비밀번호 칸입니다. |
| JSplitPane | 두 화면 영역 사이의 크기를 조절하게 합니다. |
| JList | 항목을 세로로 표시하고 선택하는 목록입니다. |
| DefaultListModel | JList가 보여 줄 항목 자료를 보관합니다. |
| JTable | 행과 열로 자료를 보여 주는 표입니다. |
| DefaultTableModel | JTable이 표시할 열 이름과 행 자료를 보관합니다. |
| JTextField | 한 줄 입력칸입니다. |
| JTextArea | 여러 줄의 입력·표시 영역입니다. |
| JComboBox | 목록을 펼쳐 하나를 고르는 선택칸입니다. |
| JScrollPane | 긴 표·목록·본문에 스크롤을 붙입니다. |
| JOptionPane | 입력·확인·안내창을 간단히 만듭니다. |
| Connection | 열린 DB 연결을 나타냅니다. |
| PreparedStatement | SQL과 입력값을 따로 전달해 조회·변경합니다. |
| ResultSet | DB 조회 결과입니다. next()로 다음 결과 행에 이동합니다. |
| Timer | 정해진 간격마다 실행할 화면 동작을 요청합니다. |
| SwingWorker | DB 배경 작업과 완료 후 화면 처리를 나눕니다. |

## 예제 값으로 실행 따라가기

1. 메모 12번의 버전 2를 A와 B가 각각 열어 봅니다. A의 save가 updateNote에서 한 행을 수정하고 버전을 3으로 바꿉니다.
2. A는 버전 3의 이력과 활동을 써서 commit합니다. B는 여전히 버전 2를 보내므로 수정 행이 0이고, save가 false를 반환합니다.
3. 화면은 충돌을 안내하고 편집 입력을 유지합니다. 동시에 목록 조회가 끝나면 updateList가 그 순간의 선택을 기준으로 새 목록을 채웁니다.

## 1. LoginFrame.java

[src/teamproject/member/LoginFrame.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/LoginFrame.java#L1)

아이디·비밀번호를 받아 인증하고 소속 팀을 선택하게 하는 창입니다.

먼저 볼 변수·인수: conn은 Main에서 받은 연결, idField·pwField는 입력칸입니다. user는 인증된 회원, team은 선택한 팀 객체입니다.

실행 흐름: 생성자 → 로그인 버튼/Enter → doLogin → UserDAO.login → TeamDAO.findTeams → MainFrame

실행 예: demo1로 로그인한 뒤 팀 42를 고르면 MainFrame에 회원 10과 팀 42가 전달됩니다. 이후 조회에 1을 고정해 넣지 않습니다.

### 상단 선언과 필드 · 1~26줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/LoginFrame.java#L1) · 실행·읽기 시점: 소스 처음

1. JFrame은 독립 창, JTextField는 한 줄 입력칸, JPasswordField는 화면에서 글자를 가리는 비밀번호 입력칸입니다.
2. Connection·Arrays·List는 각각 DB 연결·배열 처리·팀 목록에 사용합니다. 클래스가 JFrame을 상속하여 창의 크기·제목 기능을 사용합니다.

### 생성자: 창 배치와 이벤트 연결 · 27~57줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/LoginFrame.java#L27) · 실행·읽기 시점: Main에서 new LoginFrame(conn)을 실행하면

1. 받은 연결을 this.conn에 보관하고 제목·크기·닫기 동작·화면 중앙 위치를 정합니다.
2. GridLayout(3,2)는 3행 2열 배치입니다. 라벨·입력칸·로그인 버튼을 순서대로 넣습니다.
3. addActionListener는 사용자의 동작에 실행할 코드를 연결합니다. 로그인 버튼과 비밀번호 Enter가 같은 doLogin을 부릅니다.

관련 선언: LoginFrame (27줄)

### doLogin: 입력 검사와 인증 · 58~109줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/LoginFrame.java#L58) · 실행·읽기 시점: 버튼 또는 Enter 입력 시

1. 아이디 앞뒤 공백을 제거합니다. 비밀번호 문자 배열을 문자열로 바꾼 후 Arrays.fill로 원래 배열을 비웁니다. 만들어진 문자열까지 지워지는 것은 아닙니다.
2. 빈 입력이면 안내하고 return합니다. UserDAO.login의 결과가 null이면 인증 실패이므로 비밀번호 칸을 비웁니다.
3. 성공한 userId로 소속 목록을 읽습니다. 0개면 중단, 1개면 그 팀 선택, 여러 개면 선택창을 띄우고 취소 시 중단합니다.
4. 정상 선택되면 MainFrame(conn,user,team)을 보여 주고 dispose로 로그인 창만 닫습니다. DB 오류는 catch에서 안내합니다.

관련 선언: doLogin (59줄)

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

## 2. UserDAO.java

[src/teamproject/member/UserDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/UserDAO.java#L1)

입력한 계정 정보가 맞는지 조회하고 회원 번호·이름을 돌려줍니다.

먼저 볼 변수·인수: User의 userId는 DB 내부 번호, name은 표시 이름입니다. login의 loginId·password는 입력창에서 받은 값입니다.

실행 흐름: LoginFrame.doLogin → login → User 또는 null → 로그인 창의 성공·실패 분기

실행 예: 같은 이름의 사람이 있어도 userId로 구별합니다. 비밀번호 비교는 현재 수업용 코드의 DB 문자열 비교 방식입니다.

### 모듈과 User 필드 · 1~16줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/UserDAO.java#L1) · 실행·읽기 시점: 파일 상단

1. java.sql의 조회 클래스들을 사용합니다. DAO는 DB 읽기·쓰기를 모은 클래스라는 뜻입니다.
2. User는 성공 결과를 한 묶음으로 전달하는 작은 내부 클래스입니다. 화면에서 직접 SQL을 반복 작성하지 않도록 분리했습니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### User 생성자·getUserId·getName · 17~30줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/UserDAO.java#L17) · 실행·읽기 시점: 로그인 행을 찾았을 때

1. 생성자가 회원 번호와 이름을 저장합니다. getUserId는 번호, getName은 이름을 반환합니다.
2. 변경 메서드를 두지 않고 final 필드로 보관하므로 인증 결과를 전달하는 동안 값이 임의로 다시 대입되지 않습니다.

관련 선언: User (17줄), getUserId (22줄), getName (26줄)

### login: 조건에 맞는 사용자 한 명 · 31~56줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/UserDAO.java#L31) · 실행·읽기 시점: LoginFrame이 연결·아이디·비밀번호를 넘기면

1. SQL의 첫 ?에 아이디, 둘째 ?에 비밀번호를 바인딩합니다. 바인딩은 SQL 문장과 입력값을 따로 전달하여 ?를 채우는 방식입니다.
2. executeQuery 결과의 rs.next()가 true이면 회원 번호·이름으로 User를 만듭니다. 없으면 null을 반환합니다.
3. try 괄호의 실행 도구와 결과는 자동으로 닫습니다. 공유 연결 conn은 여기서 닫지 않습니다. throws SQLException은 DB 오류 처리를 호출한 화면에 맡긴다는 뜻입니다.

관련 선언: login (32줄)

> **바인딩**: SQL 문장과 입력값을 따로 전달해 ? 위치를 값으로 채우는 방식입니다.

## 3. MemberFrame.java

[src/teamproject/member/MemberFrame.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberFrame.java#L1)

팀원 목록 표를 만들고 등록·수정·삭제 입력을 처리합니다.

먼저 볼 변수·인수: conn·teamId는 현재 연결·선택 팀, memberTable은 눈에 보이는 표, tableModel은 표의 데이터를 보관합니다.

실행 흐름: 생성자/탭 진입 → loadMembers / 버튼 → 입력 검사 → MemberDAO → loadMembers

실행 예: 표의 0행은 첫 번째 화면 행이지 회원 0번이 아닙니다. 선택한 행의 0번째 열에 보관한 실제 userId를 읽어 처리합니다.

### 모듈과 필드 · 1~45줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberFrame.java#L1) · 실행·읽기 시점: 파일 상단

1. Swing의 JTable·DefaultTableModel·JOptionPane으로 목록과 입력창을 만들고 java.awt의 배치 클래스로 위치를 정합니다.
2. 버튼·표·연결·팀 번호는 여러 메서드가 함께 사용하므로 필드에 보관합니다.

### 생성자: 표와 버튼 준비 · 46~114줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberFrame.java#L46) · 실행·읽기 시점: MainFrame이 팀원 화면을 만들 때

1. 전달받은 연결과 팀 번호를 저장합니다. DISPOSE_ON_CLOSE는 이 창만 닫는 설정입니다.
2. 회원 번호·아이디·이름·학번·역할·가입일 열로 모델을 만들고 JTable과 JScrollPane에 연결합니다.
3. 버튼별로 registerMember·updateMember·deleteMember를 연결하고 마지막에 loadMembers로 자료를 채웁니다. 현재 MemberFrame의 기본 모델은 셀 편집을 막는 재정의를 하지 않아, 표 직접 입력은 화면값만 바꾸며 DB 저장은 되지 않습니다.

관련 선언: MemberFrame (46줄)

### reload: 팀원 표 다시 읽기 · 115~118줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberFrame.java#L115) · 실행·읽기 시점: MainFrame에서 이 탭을 다시 열면

1. MemberDAO.loadMembers에 현재 연결·모델·팀 번호를 전달합니다.
2. 여기서 DB 결과를 표에 다시 채우며, 별도 반환값은 없습니다.

관련 선언: reload (115줄)

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### registerMember: 입력창에서 등록 · 119~186줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberFrame.java#L119) · 실행·읽기 시점: 팀원 등록 버튼 클릭

1. 아이디·비밀번호·이름 입력칸과 역할 JComboBox를 만듭니다. JComboBox는 펼쳐서 하나를 고르는 선택칸입니다.
2. 확인창에서 취소하면 return합니다. 확인이면 값을 읽고 필수 입력이 비었는지 검사합니다.
3. insertMember 호출이 성공해야 성공 안내 후 목록을 다시 조회합니다. 오류가 나면 catch에서 실패 이유를 보여 줍니다. 등록 시 학번은 DAO에서 아이디와 같은 값으로 처음 채웁니다.

관련 선언: registerMember (120줄)

### updateMember: 기존 값을 채워 수정 · 187~269줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberFrame.java#L187) · 실행·읽기 시점: 팀원 수정 버튼 클릭

1. 선택 행이 없으면 안내하고 종료합니다. 선택 행의 실제 회원 번호·이름·학번·역할을 읽습니다. 학번이 null이면 빈 문자열로 바꿉니다.
2. 기존 값을 입력창에 채우고 새 이름·학번의 빈 값 여부를 검사합니다. 취소 시 DB를 바꾸지 않습니다.
3. MemberDAO.updateMember에 팀 번호와 실제 회원 번호를 함께 넘기고, 성공 후 목록을 다시 읽습니다. 여기서 아이디·비밀번호를 수정하는 기능은 없습니다.

관련 선언: updateMember (188줄)

### deleteMember: 현재 팀 소속 해제 · 270~309줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberFrame.java#L270) · 실행·읽기 시점: 팀원 삭제 버튼 클릭

1. 선택한 행의 실제 회원 번호를 가져온 뒤 예/아니오 확인창을 보여 줍니다.
2. 예를 눌렀을 때만 DAO를 호출합니다. DAO는 users 계정이 아닌 현재 팀의 소속만 삭제합니다.
3. 다시 조회하면 소속에서 빠진 사람은 이 팀의 목록에서 사라집니다. 관련 계정과 이미 맡은 업무는 유지됩니다.

관련 선언: deleteMember (271줄)

## 4. MemberDAO.java

[src/teamproject/member/MemberDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberDAO.java#L1)

팀원 표를 조회하고 계정·팀 소속을 DB에 저장합니다.

먼저 볼 변수·인수: 모든 메서드의 conn·teamId는 화면에서 전달합니다. insert는 새 회원을 만들고 update/delete는 실제 userId로 대상을 찾습니다.

실행 흐름: 화면 요청 → 현재 팀 SQL → 계정·소속 변경 묶음 → 성공 시 화면 재조회

실행 예: 계정 삽입은 성공했지만 소속 삽입이 실패하면 rollback으로 새 계정 삽입도 취소합니다.

### 모듈과 책임 · 1~15줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberDAO.java#L1) · 실행·읽기 시점: 파일 상단

1. java.sql로 DB에 요청하고 DefaultTableModel로 조회 결과를 표에 추가합니다.
2. 이 파일은 입력창을 만들지 않습니다. 값의 입력은 MemberFrame, 실제 SQL 처리는 이 파일이 담당합니다.

### loadMembers: 표 한 행씩 채우기 · 16~68줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberDAO.java#L16) · 실행·읽기 시점: 처음 화면 생성·reload·변경 성공 뒤

1. users와 team_members를 회원 번호로 JOIN하고 현재 팀만 조회합니다. 회원 기본 정보와 이 팀에서의 역할을 함께 얻습니다.
2. setRowCount(0)으로 화면 모델을 비운 뒤 while(rs.next())로 결과마다 Object[] 한 행을 만들고 addRow합니다. 모델을 비우는 것은 DB 삭제가 아닙니다.

관련 선언: loadMembers (17줄)

> **JOIN**: 관련 번호가 같은 여러 표의 행을 이어 필요한 값을 함께 읽는 SQL 기능입니다.

### insertMember: 계정 저장과 생성 번호 · 69~119줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberDAO.java#L69) · 실행·읽기 시점: 등록 입력 검사를 통과하면

1. 계정 INSERT와 소속 INSERT 두 SQL을 준비하고 원래 자동 저장 설정을 기억합니다. setAutoCommit(false)로 트랜잭션을 시작합니다.
2. users에 아이디·비밀번호·이름·학번을 넣습니다. 최초 학번은 loginId를 사용합니다. RETURN_GENERATED_KEYS와 getGeneratedKeys로 DB가 새로 만든 user_id를 받습니다.

관련 선언: insertMember (70줄)

> **getGeneratedKeys**: INSERT 뒤 DB가 자동 생성한 새 번호를 읽는 메서드입니다.

> **자동 저장**: JDBC의 autoCommit 설정입니다. true면 SQL 변경마다 확정하고 false면 commit·rollback으로 묶음을 마칩니다.

> **트랜잭션**: 여러 DB 변경을 한 묶음으로 처리해 모두 확정하거나, 오류가 나면 묶음 전체를 취소하는 처리입니다.

### insertMember 후반: 소속 저장·확정 · 120~149줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberDAO.java#L120) · 실행·읽기 시점: 계정 번호를 받았으면

1. 받은 회원 번호를 team_members에 팀 번호·역할과 함께 넣습니다. 두 삽입이 모두 끝나면 commit합니다.
2. 트랜잭션은 여러 변경을 모두 성공하거나 모두 취소하도록 묶는 처리입니다. catch의 rollback은 중간 실패를 취소하고 throw는 화면으로 오류를 전달합니다. finally는 원래 자동 저장 설정을 되돌립니다.

> **rollback**: 아직 확정하지 않은 현재 트랜잭션의 변경을 취소합니다.

> **commit**: 현재 트랜잭션의 변경을 DB에 최종 확정합니다.

### updateMember 앞부분: 소속 확인·SQL 준비 · 150~180줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberDAO.java#L150) · 실행·읽기 시점: 수정 화면이 변경값을 전달하면

1. 팀 번호와 회원 번호로 먼저 소속을 확인합니다. 없으면 예외를 발생시켜 다른 팀의 회원을 수정하지 못하게 합니다.
2. users에는 이름·학번, team_members에는 역할을 바꾸는 SQL을 각각 준비합니다. users의 기본 정보는 다른 소속 팀에서도 공유되는 계정 정보입니다.

관련 선언: updateMember (151줄)

### updateMember 후반: 두 변경 한 묶음 · 181~230줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberDAO.java#L181) · 실행·읽기 시점: 소속 확인을 통과하면

1. 원래 자동 저장 설정을 보관하고 두 UPDATE를 차례로 실행합니다. 각 ?에는 변경값과 실제 대상을 지정하는 번호를 넣습니다.
2. 모두 성공하면 commit, SQL 오류면 rollback 후 재전달, finally에서 원래 설정으로 복원합니다. 소속 검사 자체는 이 트랜잭션을 시작하기 전에 수행됩니다.

### deleteMember: 소속만 삭제 · 231~254줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/MemberDAO.java#L231) · 실행·읽기 시점: 삭제 확인을 통과하면

1. DELETE 대상은 team_members입니다. WHERE의 team_id와 user_id가 모두 일치하는 소속 행만 지웁니다.
2. users 행을 지우지 않으므로 다른 팀 소속과 작성 기록·업무 담당자 정보가 유지됩니다. 이 메서드는 영향 행 수를 반환하지 않습니다.

관련 선언: deleteMember (232줄)

## 5. NotePanel.java

[src/teamproject/member/NotePanel.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L1)

공유 메모 목록·내용·편집창과 3초 자동 갱신을 담당합니다.

먼저 볼 변수·인수: model은 메모 객체 목록, list는 그 목록을 보여 주는 JList, detail은 읽기 전용 본문입니다. loading은 조회가 이미 진행 중인지 나타냅니다.

실행 흐름: 타이머·탭 → reload → NoteListWorker.doInBackground → done → updateList

실행 예: 조회가 느린 동안 다른 메모를 선택해도 done에서 가장 최근 선택 번호를 읽고 복원합니다. 편집 중 글은 별도 입력창에 유지합니다.

### 모듈과 필드: JList와 모델 · 1~22줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L1) · 실행·읽기 시점: 파일 상단

1. JList는 항목을 세로로 보여 주고 하나를 고르는 화면 클래스입니다. DefaultListModel은 그 항목들의 저장 목록입니다.
2. new JList<>(model)은 화면을 모델에 연결합니다. <NoteDAO.Note>는 메모 객체만 넣겠다는 자료형 지정입니다. model.clear는 화면 목록을 비우며 DB 메모를 지우지 않습니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### 생성자: 화면·버튼·타이머 연결 · 23~69줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L23) · 실행·읽기 시점: MainFrame이 메모 패널을 만들면

1. conn·teamId·userId를 보관하고 읽기 전용 본문과 단일 선택 목록을 나란히 배치합니다. 메모 선택 시 선택 객체의 content를 본문에 넣습니다.
2. 새 메모는 edit(null), 편집은 선택 객체로 edit, 삭제·이력·새로고침은 각각의 메서드를 연결합니다. 업무 등록 버튼은 업무 담당 파일 TaskFromNoteDialog.open으로 넘깁니다.
3. Timer(3000,…)는 3초마다 실행할 동작입니다. 패널이 보일 때만 reload를 요청합니다. Timer와 SwingWorker는 역할이 다릅니다: 주기 결정과 DB 배경 조회입니다.

관련 선언: NotePanel (23줄)

> **SwingWorker**: 느린 작업은 doInBackground에서, 끝난 뒤 화면 변경은 done에서 처리하도록 나누는 클래스입니다.

### addNotify·removeNotify: 타이머 수명 · 70~82줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L70) · 실행·읽기 시점: 패널이 화면에 붙거나 떨어질 때 Swing이 호출

1. addNotify는 부모 클래스 동작 후 타이머를 시작합니다. removeNotify는 먼저 타이머를 멈춘 뒤 부모 동작을 실행합니다.
2. @Override는 부모의 메서드를 현재 목적에 맞게 다시 작성했다는 표시입니다. 보이지 않는 탭에서는 isShowing 검사로 불필요한 조회를 줄입니다.

관련 선언: addNotify (71줄), removeNotify (77줄)

### 갱신 요청과 중복 검사 · 83~90줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L83) · 실행·읽기 시점: 탭을 열거나 타이머가 갱신을 요청하면

1. loading이 true이면 이미 조회 중이므로 return합니다. 처음 요청이면 true로 바꿉니다.
2. NoteListWorker 객체를 만들고 execute로 조회를 시작합니다. 클래스가 하는 일은 아래에서 이어서 봅니다.

관련 선언: reload (83줄)

### 조회 작업 클래스 · 91~99줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L91) · 실행·읽기 시점: execute로 작업이 시작되면

1. SwingWorker를 상속합니다. List<NoteDAO.Note>는 조회할 결과형이고 Void는 도중 결과를 쓰지 않는다는 뜻입니다.
2. doInBackground는 새 연결로 NoteDAO.list를 호출합니다. try 괄호에서 만든 연결은 블록이 끝날 때 자동으로 닫힙니다.

관련 선언: doInBackground (93줄)

### 조회 결과 받기 · 100~113줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L100) · 실행·읽기 시점: Swing이 화면 실행 흐름에서 호출

1. get으로 조회 결과를 받은 뒤 패널이 화면에 붙어 있으면 updateList에 넘깁니다.
2. 실패는 상태 라벨에 표시합니다. finally에서 loading을 false로 바꿔 다음 조회를 받을 수 있게 합니다.

관련 선언: done (100줄)

### 모델을 바꾸고 선택 복원 · 114~130줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L114) · 실행·읽기 시점: done이 새 자료를 넘기면

1. 모델을 비우기 직전에 사용자가 보고 있던 항목을 기억합니다. 조회 도중 다른 항목을 골랐으면 그 항목이 기준입니다.
2. 새 항목을 하나씩 목록 모델에 넣고 같은 번호가 있으면 itemToSelect에 저장합니다. 전부 채운 뒤 그 항목을 선택합니다.
3. 선택한 항목이 삭제되었다면 일치하는 번호가 없어 선택을 복원하지 않습니다.

관련 선언: updateList (114줄)

### selected: 선택 여부 공통 검사 · 131~136줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L131) · 실행·읽기 시점: 편집·삭제·이력 등의 버튼을 누르면

1. getSelectedValue가 선택된 메모 객체를 반환합니다. 없으면 null입니다.
2. null일 때 안내를 보여 주고 그대로 반환합니다. 호출한 메서드가 null이면 즉시 종료하도록 구성합니다.

관련 선언: selected (131줄)

### edit: 새 메모와 수정 공용 입력창 · 137~170줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L137) · 실행·읽기 시점: 새 메모 또는 편집 버튼 클릭

1. base가 null이면 빈 입력창, 기존 객체이면 제목·본문을 채웁니다.
2. 확인을 누르는 동안 while로 저장을 시도합니다. 새 메모는 id=0·version=0, 수정은 읽어 둔 번호·버전을 DAO에 전달합니다.
3. 성공하면 목록 갱신 후 종료합니다. false이면 다른 사람의 선행 수정·삭제 가능성을 안내하고 현재 입력을 유지합니다. 오류도 안내 후 같은 창에서 고칠 기회를 줍니다.

관련 선언: edit (137줄)

### delete: 버전을 함께 보내 삭제 · 171~185줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L171) · 실행·읽기 시점: 삭제 확인을 누르면

1. 선택 메모의 번호와 버전을 DAO로 보냅니다. 번호가 같아도 다른 사람이 수정해 버전이 바뀌었으면 삭제되지 않습니다.
2. 실패 여부를 안내하고 최신 목록을 다시 읽습니다. 편집 이력과 업무 연결 행 정리는 DB의 외래키 삭제 규칙이 수행합니다.

관련 선언: delete (171줄)

> **외래키**: 다른 표의 기본키를 참조하여 존재하는 회원·팀·게시물 등을 연결하는 DB 규칙입니다.

### history: 이전 내용 보기 · 186~198줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NotePanel.java#L186) · 실행·읽기 시점: 내용 이력 버튼 클릭

1. 선택 메모의 이력 문자열을 DAO에서 받아 수정 불가 JTextArea에 넣습니다.
2. 스크롤 가능한 안내창으로 보여 줍니다. 이 창은 과거 내용을 표시할 뿐, 특정 버전으로 복원·저장하는 기능은 아닙니다.

관련 선언: history (186줄)

## 6. NoteDAO.java

[src/teamproject/member/NoteDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L1)

메모 조회·저장·버전 검사·내용 이력을 처리합니다.

먼저 볼 변수·인수: Note는 id·version·authorId와 제목·본문·수정자·시각을 묶습니다. save는 새 메모이면 id=0을 받습니다.

실행 흐름: list → Note 목록 / save → 소속 검사 → 새 삽입 또는 버전 UPDATE → 이력·활동 → 확정

실행 예: A와 B가 버전 2를 읽고 A가 먼저 저장하면 DB는 3입니다. B의 “버전 2일 때만 수정”은 0행이 되어 덮어쓰지 않습니다.

### 모듈과 Note 필드 · 1~16줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L1) · 실행·읽기 시점: 소스 상단

1. DB 클래스와 목록 클래스를 쓰고, TeamDAO로 소속을 확인하고 ActivityDAO로 활동을 남깁니다.
2. 여기서 관리하는 Note는 화면이 아닌 자료 묶음입니다. id는 실제 메모 번호, version은 수정 순서를 검사하는 정수입니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### Note 생성자·toString · 17~38줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L17) · 실행·읽기 시점: 조회 결과 한 행을 객체로 만들 때

1. 생성자는 번호·버전·작성자와 본문·수정 정보를 필드에 보관합니다.
2. toString은 JList에 표시할 제목·버전·수정자·시각을 합칩니다. 목록에서 보이는 문자열과 실제 저장 객체는 다릅니다.

관련 선언: Note (17줄), toString (34줄)

### list: 현재 팀의 최신순 메모 · 39~62줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L39) · 실행·읽기 시점: 화면의 배경 조회에서 호출

1. notes와 마지막 수정자의 users 행을 JOIN합니다. 현재 팀 조건 뒤 수정 시각·메모 번호 내림차순으로 정렬합니다.
2. 모든 결과를 Note로 만들어 List에 넣습니다. 본문도 같이 읽으므로 메모를 고를 때 이미 받은 content를 표시할 수 있습니다.

관련 선언: list (39줄)

> **JOIN**: 관련 번호가 같은 여러 표의 행을 이어 필요한 값을 함께 읽는 SQL 기능입니다.

### 저장 순서와 트랜잭션 · 63~98줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L63) · 실행·읽기 시점: 편집창에서 저장하면

1. 제목과 팀 소속을 검사한 뒤 자동 확정을 끕니다. id가 0이면 insertNote를 불러 새 번호와 버전 1을 사용합니다.
2. 기존 메모이면 updateNote를 호출합니다. 수정한 행이 없으면 rollback 후 false를 반환합니다. 성공한 수정의 새 버전은 기존 버전+1입니다.
3. 메모·이력·활동 기록을 같은 Connection으로 처리합니다. 모두 끝나면 commit으로 확정합니다. 예외가 나면 rollback으로 취소하고 finally에서 자동 확정을 켭니다.

관련 선언: save (63줄)

> **rollback**: 아직 확정하지 않은 현재 트랜잭션의 변경을 취소합니다.

> **commit**: 현재 트랜잭션의 변경을 DB에 최종 확정합니다.

### 새 메모 등록과 번호 반환 · 99~118줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L99) · 실행·읽기 시점: save가 새 메모를 처리할 때

1. 팀 번호·제목·본문·작성자·수정자를 SQL의 물음표 순서로 넣습니다.
2. getGeneratedKeys로 새 메모의 번호를 돌려줍니다. 번호를 읽을 수 없으면 SQLException을 던져 save에서 작업을 취소합니다.

관련 선언: insertNote (99줄)

> **getGeneratedKeys**: INSERT 뒤 DB가 자동 생성한 새 번호를 읽는 메서드입니다.

### 번호·팀·버전으로 기존 메모 수정 · 119~138줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L119) · 실행·읽기 시점: save가 기존 메모를 처리할 때

1. WHERE에서 메모 번호·팀 번호·화면에서 읽은 버전을 동시에 확인합니다. 일치하면 본문을 수정하고 DB의 버전을 1 올립니다.
2. executeUpdate 값이 0이면 수정한 행이 없다는 뜻입니다. 다른 사람이 먼저 저장했거나 삭제했을 때 save가 false로 알려줍니다.

관련 선언: updateNote (119줄)

### 본문의 새 버전 기록 · 139~150줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L139) · 실행·읽기 시점: 새 메모 또는 수정이 성공하면

1. save가 전달한 실제 번호와 새 버전, 본문, 편집자 번호를 note_history에 저장합니다.
2. 저장 확정은 호출한 save 메서드에서 합니다. 같은 연결로 본문과 이력을 묶어 처리할 수 있습니다.

관련 선언: insertHistory (139줄)

### delete: 확인한 버전만 삭제 · 151~161줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L151) · 실행·읽기 시점: 화면에서 삭제를 확정하면

1. 메모 번호·팀·버전이 모두 맞는 행만 지우고 영향 행이 있으면 true입니다.
2. 버전이 달라졌거나 이미 없으면 false입니다. 실제 이력 삭제는 schema.sql의 ON DELETE CASCADE 규칙으로 연결됩니다.

관련 선언: delete (151줄)

> **ON DELETE CASCADE**: 참조한 부모 행을 삭제하면 관련 자식 행도 DB가 함께 삭제하도록 정한 규칙입니다.

### history: 내용 이력 문자열 만들기 · 162~186줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/member/NoteDAO.java#L162) · 실행·읽기 시점: 내용 이력 버튼이 요청하면

1. 이력·원본 메모·수정자를 연결하고 현재 팀과 메모 번호로 제한합니다.
2. 버전이 큰 순으로 반복해서 “버전·수정자·시각”과 본문을 StringBuilder에 덧붙입니다. StringBuilder는 문장을 반복해서 조립하는 클래스입니다.

관련 선언: history (162줄)

## 마지막 시연·질문

로그인·팀 선택 후 팀원을 등록하고 메모를 편집합니다. 두 창에서 같은 메모를 수정해 버전 충돌을 설명합니다.

**JList와 DefaultListModel은 어떻게 다른가요?**

JList는 항목을 보여 주는 화면이고 모델은 그 항목 자료를 보관합니다. 모델을 비워도 DB가 삭제되지는 않습니다.

**다른 사람이 먼저 메모를 수정하면?**

UPDATE의 버전 조건이 맞지 않아 0행이 됩니다. DAO는 false, 화면은 입력을 유지하며 충돌을 안내합니다.

**팀원 삭제는 계정 삭제인가요?**

현재 팀의 team_members 행만 삭제합니다. users 계정과 다른 팀 소속은 유지합니다.

소스 기준 커밋: 347d21cb4d48ef80c333af9ae4da02395f618765. 줄 번호는 이 기준의 실제 파일과 일치합니다.
