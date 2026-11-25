# 신*준 · 소스별 브리핑

담당: 통합 화면·자료·아이디어

PPT 원래 담당: 자료·대시보드 / 추가 담당: 통합 탭·아이디어·댓글·공감·최근 활동 화면

[코드와 설명 함께 보기](05_resource.html) · [PDF](../../output/pdf/member_briefings/05_resource.pdf)

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
| JSplitPane | 두 화면 영역 사이의 크기를 조절하게 합니다. |
| JList | 항목을 세로로 표시하고 선택하는 목록입니다. |
| DefaultListModel | JList가 보여 줄 항목 자료를 보관합니다. |
| JTable | 행과 열로 자료를 보여 주는 표입니다. |
| DefaultTableModel | JTable이 표시할 열 이름과 행 자료를 보관합니다. |
| JTextField | 한 줄 입력칸입니다. |
| JTextArea | 여러 줄의 입력·표시 영역입니다. |
| JProgressBar | 진행 수치를 막대와 글자로 표시합니다. |
| JScrollPane | 긴 표·목록·본문에 스크롤을 붙입니다. |
| JOptionPane | 입력·확인·안내창을 간단히 만듭니다. |
| JTabbedPane | 여러 화면 중 하나를 탭으로 선택합니다. |
| Connection | 열린 DB 연결을 나타냅니다. |
| PreparedStatement | SQL과 입력값을 따로 전달해 조회·변경합니다. |
| ResultSet | DB 조회 결과입니다. next()로 다음 결과 행에 이동합니다. |
| LocalDate | 연·월·일로 된 하루를 나타냅니다. |
| Timer | 정해진 간격마다 실행할 화면 동작을 요청합니다. |
| SwingWorker | DB 배경 작업과 완료 후 화면 처리를 나눕니다. |

## 예제 값으로 실행 따라가기

1. MainFrame 생성자가 9개 탭을 조립하고 tabs.addChangeListener(this)로 알림을 등록합니다.
2. 일정 탭을 누르면 Swing이 stateChanged를 부릅니다. selectedTab이 schedules와 같으므로 schedules.reload가 실행됩니다.
3. 아이디어 탭을 누르면 ideas.reload 쪽을 실행합니다. 찾아 읽을 줄은 ChangeListener 선언 → addChangeListener(this) → stateChanged입니다.

## 1. MainFrame.java

[src/teamproject/resource/MainFrame.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MainFrame.java#L1)

각 팀원의 화면을 9개 탭으로 모으고 같은 회원·팀 정보를 전달합니다.

먼저 볼 변수·인수: conn은 공통 연결, user는 로그인 회원, team은 고른 팀입니다. teamId를 꺼내 모든 화면의 조회 기준으로 넘깁니다.

실행 흐름: LoginFrame → MainFrame 생성 → addChangeListener(this) → stateChanged → 선택된 화면 reload

실행 예: TaskFrame·MemberFrame의 창 전체를 또 보여 주지 않고 getContentPane으로 안쪽 내용을 가져와 탭에 붙입니다.

### 모듈과 통합 책임 · 1~33줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MainFrame.java#L1) · 실행·읽기 시점: 파일 상단

1. JTabbedPane은 여러 화면을 탭으로 선택하게 하는 클래스입니다. 각 팀원의 화면 클래스를 import하여 조립합니다.
2. 이 파일의 책임은 화면 연결입니다. 업무 SQL이나 메모 버전 검사 자체는 각 담당자의 DAO에 있습니다.
3. MainFrame은 JFrame을 상속하고 ChangeListener를 구현합니다. ChangeListener는 변경 알림을 받을 때 stateChanged를 제공하겠다는 약속입니다. 각 탭 객체를 필드에 저장해 생성 이후에도 비교할 수 있습니다.

> **ChangeListener**: 탭처럼 값이나 선택이 바뀌었을 때 stateChanged를 호출받기 위해 구현하는 인터페이스입니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### 생성자에서 9개 탭 만들기 · 34~65줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MainFrame.java#L34) · 실행·읽기 시점: 로그인과 팀 선택을 마치면

1. 받은 사용자·팀 객체에서 실제 번호를 꺼내 각 화면에 보냅니다.
2. 기존 업무·팀원 창은 addTaskTab과 addMemberTab으로 붙입니다. 나머지 패널은 tabs.addTab에 넘깁니다.
3. 탭을 모두 만들고 addChangeListener(this)를 실행합니다. 등록된 MainFrame이 이후 탭 변경 알림을 받습니다.

관련 선언: MainFrame (34줄)

### 업무 화면 생성과 실패 처리 · 66~76줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MainFrame.java#L66) · 실행·읽기 시점: 생성자가 업무 탭을 추가할 때

1. TaskFrame을 만든 뒤 getContentPane으로 창 안쪽의 내용만 꺼내 탭에 붙입니다.
2. DB 오류가 나면 안내하고 null을 반환합니다. 이후 탭 전환에서도 null 여부를 먼저 확인합니다.

관련 선언: addTaskTab (66줄)

### 팀원 화면 생성과 실패 처리 · 77~88줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MainFrame.java#L77) · 실행·읽기 시점: 생성자가 팀원 탭을 추가할 때

1. MemberFrame의 창 안쪽을 팀원 관리 탭에 붙이고, 나중에 reload할 수 있도록 생성한 객체를 반환합니다.
2. 생성에 실패하면 메시지를 보여 주고 null을 반환합니다.

관련 선언: addMemberTab (77줄)

### 선택한 탭 하나 새로고침 · 89~116줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MainFrame.java#L89) · 실행·읽기 시점: 사용자가 다른 탭으로 바꾸면

1. tabs.getSelectedComponent로 현재 보이는 화면을 얻습니다.
2. if와 else if를 따라 필드에 보관한 화면 객체와 비교합니다. 일치한 화면의 reload만 호출합니다.
3. 업무 또는 팀원 조회에서 SQLException이 나면 현재 탭 이름을 붙여 안내합니다.

관련 선언: stateChanged (89줄)

## 2. DashboardPanel.java

[src/teamproject/resource/DashboardPanel.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/DashboardPanel.java#L1)

진행률·오늘 마감·지연 업무와 최근 자료를 한 화면에 요약합니다.

먼저 볼 변수·인수: overallProgressBar는 전체 막대, statusSummaryLabel은 개수 안내, 두 SummaryModel은 업무·자료 표입니다.

실행 흐름: 생성자 → reload → 진행률 DAO → 개수 계산 → 업무 SQL → 최근 자료 DAO

실행 예: 최근 자료는 최대 5개입니다. 업무 표는 마감일 순으로 읽되 현재 SQL에 개수 제한이나 미완료 필터는 없어 현재 팀 전체 업무를 보여 줍니다.

### 모듈과 필드 · 1~36줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/DashboardPanel.java#L1) · 실행·읽기 시점: 파일 상단

1. JProgressBar·JLabel·JTable은 막대·안내·표 표시, LocalDate는 오늘·과거 비교에 사용합니다.
2. 진행률 계산은 이*주 담당 InlineProgressDAO·Data를 사용하고 자료 조회는 같은 담당의 MaterialDAO를 재사용합니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### 생성자: 제목·막대·읽기 전용 두 표 · 37~106줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/DashboardPanel.java#L37) · 실행·읽기 시점: MainFrame이 첫 탭을 만들면

1. 상단 제목과 새로고침 버튼을 놓고, 가운데에 진행률 카드와 업무·자료 표 두 개를 배치합니다.
2. 두 모델의 isCellEditable은 각각 false를 반환합니다. 요약 표는 직접 편집하지 않고 담당 관리 탭에서 수정합니다.
3. 버튼은 reload에 연결하고 생성자 끝에서도 한 번 호출해 첫 자료를 채웁니다.

관련 선언: DashboardPanel (37줄), isCellEditable (76줄), isCellEditable (89줄)

### reload 앞부분: 진행률과 마감 개수 · 107~136줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/DashboardPanel.java#L107) · 실행·읽기 시점: 탭 진입·새로고침

1. 진행률 DAO의 data에서 userId=0 기준 전체·완료·퍼센트를 구해 막대와 글자에 넣습니다.
2. 업무 목록을 돌면서 완료가 아니고 날짜가 있을 때만 오늘 마감과 과거 지연을 셉니다.
3. String.format의 %d 자리에 계산한 정수들을 차례로 넣어 안내 문장을 만듭니다.

관련 선언: reload (107줄)

### reload 후반: 업무·최근 자료 표 · 137~178줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/DashboardPanel.java#L137) · 실행·읽기 시점: 진행률 계산 뒤

1. 현재 팀 업무를 마감일 오름차순으로 읽습니다. LEFT JOIN과 COALESCE로 이름이 없는 경우도 표시할 수 있게 합니다.
2. null 제목·상태는 빈 글자, 날짜가 없으면 안내 문장으로 바꿔 표에 넣습니다. 이 화면에서는 새 업무를 만들지 않습니다.
3. MaterialDAO.getRecentMaterials에 5를 넘겨 최신 자료를 읽고 두 번째 표에 채웁니다. 전체 과정 오류는 catch가 안내합니다.

> **LEFT JOIN**: 오른쪽 표에 연결할 행이 없어도 왼쪽 표의 행은 남겨서 읽는 연결 방식입니다.

> **COALESCE**: 주어진 값 중 null이 아닌 첫 값을 고르는 SQL 함수입니다.

## 3. MaterialPanel.java

[src/teamproject/resource/MaterialPanel.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L1)

자료 링크 목록과 등록·수정·삭제·브라우저 열기를 담당합니다.

먼저 볼 변수·인수: materialTable은 표시 표, tableModel은 행 자료, userId는 자료를 등록하는 현재 회원 번호입니다.

실행 흐름: 생성자 → reload / 버튼 → 입력·검사 → MaterialDAO → reload / 링크 열기 → Desktop.browse

실행 예: 자료는 실제 파일이 아닌 제목·설명·웹 주소를 저장합니다. 더블클릭도 링크 열기와 같은 메서드를 부릅니다.

### 모듈과 필드 · 1~31줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L1) · 실행·읽기 시점: 파일 상단

1. JPanel 안에 JTable과 버튼을 둡니다. Desktop은 기본 브라우저 실행, URI는 웹 주소 객체를 만드는 데 사용합니다.
2. GridBagLayout·Constraints는 입력창의 왼쪽 라벨과 오른쪽 입력칸 배치를 정합니다.

### 생성자: 표·버튼·더블클릭 · 32~82줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L32) · 실행·읽기 시점: MainFrame이 자료 탭을 만들면

1. 연결·팀·로그인 회원 번호를 저장하고 번호·자료명·설명·주소·등록일 열을 만듭니다. isCellEditable을 false로 재정의해 직접 수정을 막습니다.
2. 각 버튼에 등록·수정·삭제·링크 메서드를 연결합니다. 익명 MouseAdapter의 mouseClicked는 두 번 클릭했을 때 openSelectedUrl을 부릅니다.
3. 마지막 reload가 DB 자료를 채웁니다. 이벤트를 연결할 때 등록·삭제가 즉시 실행되는 것은 아닙니다.

관련 선언: MaterialPanel (32줄), isCellEditable (43줄), mouseClicked (73줄)

### reload: 자료 표 재조회 · 83~90줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L83) · 실행·읽기 시점: 탭 진입·자료 변경 성공 뒤

1. 현재 팀·모델을 MaterialDAO.loadMaterials로 넘깁니다.
2. 조회 실패를 안내하며 여기서는 다른 팀의 자료를 따로 조회하지 않습니다.

관련 선언: reload (83줄)

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### getSelectedMaterialId: 실제 번호 · 91~96줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L91) · 실행·읽기 시점: 수정·삭제의 시작

1. 선택 행이 없으면 -1을 반환합니다.
2. 선택된 행의 0번째 열에 보관한 material_id를 읽습니다. 행 순서가 바뀌어도 실제 저장 번호를 대상으로 삼습니다.

관련 선언: getSelectedMaterialId (91줄)

### registerMaterial: 새 링크 등록 · 97~110줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L97) · 실행·읽기 시점: 자료 등록 버튼 클릭

1. old=null로 빈 입력창을 열고 취소 결과 null이면 종료합니다.
2. 검사된 제목·설명·주소와 현재 userId를 DAO에 넘겨 등록합니다. 성공 후 안내·reload, DB 오류면 실패 안내입니다.

관련 선언: registerMaterial (97줄)

### updateMaterial: 조회 후 수정 · 111~143줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L111) · 실행·읽기 시점: 자료 수정 버튼 클릭

1. 선택 번호를 검사하고 findMaterial로 현재 DB 값을 가져옵니다. 없으면 안내 후 목록을 다시 읽습니다.
2. 기존 값을 넣은 입력창 결과를 DAO에 넘기며, 현재 팀·실제 자료 번호로 수정합니다. 결과를 알린 뒤 reload합니다.

관련 선언: updateMaterial (111줄)

### deleteMaterial: 삭제 확인 · 144~164줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L144) · 실행·읽기 시점: 자료 삭제 버튼 클릭

1. 선택 번호가 없으면 종료하고, 확인창에서 예를 눌렀을 때만 DELETE를 요청합니다.
2. 삭제 여부를 안내하고 표를 다시 읽습니다. 자료 행을 지우는 것이며 웹사이트의 원본 자료를 삭제하는 동작은 아닙니다.

관련 선언: deleteMaterial (144줄)

### openSelectedUrl: 브라우저 실행 · 165~193줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L165) · 실행·읽기 시점: 링크 버튼 또는 더블클릭

1. 선택 행의 3번째 열에서 주소를 읽고 null·공백을 검사합니다. http://·https://가 없으면 https://를 앞에 붙입니다.
2. 컴퓨터가 Desktop의 BROWSE 기능을 지원하면 URI로 바꿔 기본 브라우저에 넘깁니다. 지원하지 않거나 주소 오류면 안내합니다.
3. 이 메서드는 주소를 여는 것뿐이므로 DB에 내용을 저장하거나 링크 유효성을 원격 검사하지 않습니다.

관련 선언: openSelectedUrl (165줄)

### showMaterialDialog: 등록·수정 공용 입력창 · 194~253줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialPanel.java#L194) · 실행·읽기 시점: 등록 또는 수정 메서드가 호출

1. old가 있으면 기존 제목·설명·주소를 채웁니다. 같은 배치 조건으로 세 항목을 만듭니다.
2. while에서 취소는 null, 제목 누락·제목 100자 초과·주소 500자 초과·주소 누락은 continue로 다시 입력받습니다.
3. 통과하면 MaterialInfo를 반환합니다. 새 자료의 번호는 0이고 등록일은 DB 저장 시 정하므로 여기서는 null입니다.

관련 선언: showMaterialDialog (194줄)

## 4. MaterialDAO.java

[src/teamproject/resource/MaterialDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L1)

현재 팀 자료의 SQL과 최근 자료 조회를 모아 둡니다.

먼저 볼 변수·인수: MaterialInfo는 번호·제목·설명·주소·등록일을 보관합니다. uploaderId는 로그인한 등록자 번호, limit은 최대 조회 개수입니다.

실행 흐름: 자료 화면/대시보드 → 조회·변경 메서드 → 현재 팀 SQL → 자료 객체·표 또는 결과 반환

실행 예: COALESCE(NULLIF(url,''),link_url)은 현재 주소가 빈 문자열 또는 null이면 이전 link_url을 대신 읽는 식입니다.

### 모듈과 MaterialInfo 필드 · 1~21줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L1) · 실행·읽기 시점: 파일 상단

1. java.sql로 DB 작업을 하고 List·ArrayList로 최근 자료 목록을 반환합니다.
2. MaterialInfo는 화면 입력 또는 조회 결과를 넘기는 자료형입니다. DB 연결을 자체 보관하지 않습니다.

### MaterialInfo 생성자와 다섯 get 메서드 · 22~51줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L22) · 실행·읽기 시점: 자료 한 건을 묶을 때

1. 생성자가 번호·제목·설명·주소·등록일을 저장합니다.
2. getMaterialId·getTitle·getDescription·getUrl·getCreatedAt은 해당 필드를 반환합니다. DB 조회를 새로 하는 메서드가 아닙니다.

관련 선언: MaterialInfo (22줄), getMaterialId (31줄), getTitle (35줄), getDescription (39줄), getUrl (43줄), getCreatedAt (47줄)

### loadMaterials: 이전 주소도 호환하여 조회 · 52~82줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L52) · 실행·읽기 시점: 자료 표가 reload하면

1. 현재 팀 자료를 큰 자료 번호부터 읽습니다. NULLIF(url,'')은 빈 주소를 null로 보고, COALESCE는 남아 있는 주소를 선택합니다.
2. 기존 모델 행을 비우고 결과마다 Object[]를 addRow합니다. 이 과정에서 표를 만들거나 DB 구조를 변경하지 않습니다.

관련 선언: loadMaterials (54줄)

> **COALESCE**: 주어진 값 중 null이 아닌 첫 값을 고르는 SQL 함수입니다.

> **NULLIF**: 두 값이 같으면 null로 바꿉니다. 이 프로젝트는 빈 주소를 없는 값으로 취급할 때 사용합니다.

### getRecentMaterials: 개수 제한 목록 · 83~112줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L83) · 실행·읽기 시점: 대시보드가 최근 5개를 요청

1. 현재 팀·limit을 ?에 넣고 자료 번호 내림차순으로 읽습니다. LIMIT은 결과의 최대 개수를 제한합니다.
2. 행마다 MaterialInfo를 만들어 목록으로 반환합니다. 모델을 직접 받지 않아 자료 표 외의 화면에서도 재사용할 수 있습니다.

관련 선언: getRecentMaterials (83줄)

### insertMaterial: 소속 확인과 등록자 저장 · 113~139줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L113) · 실행·읽기 시점: 자료 등록 입력 검사를 통과하면

1. team_members에서 현재 팀과 uploaderId가 맞는지 확인합니다. 없으면 예외로 등록을 중단합니다.
2. 제목·설명·주소와 함께 실제 팀·등록자 번호를 INSERT합니다. 로그인 회원 번호를 화면에서 넘겨받아 기록합니다.

관련 선언: insertMaterial (113줄)

### findMaterial: 한 건 또는 null · 140~161줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L140) · 실행·읽기 시점: 자료 수정 전에 호출

1. 자료 번호와 팀 번호를 함께 조건으로 사용합니다. 조회 주소에는 이전 주소 대체식도 적용합니다.
2. 있으면 MaterialInfo로 반환하고 없으면 null입니다. 화면은 이 결과로 이미 삭제됐는지를 판단합니다.

관련 선언: findMaterial (140줄)

### updateMaterial: 현재 팀 자료 수정 · 162~181줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L162) · 실행·읽기 시점: 수정 입력 검사를 통과하면

1. 제목·설명·주소를 바꾸되 WHERE에 자료 번호와 현재 팀을 함께 지정합니다.
2. 영향 행 수가 0보다 크면 true입니다. 이 SQL에는 등록자 본인만 수정하도록 제한하는 조건은 없습니다. 아이디어의 본인 글 제한과 구별합니다.

관련 선언: updateMaterial (162줄)

### deleteMaterial: 현재 팀 자료 삭제 · 182~191줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/MaterialDAO.java#L182) · 실행·읽기 시점: 삭제 확인 뒤

1. 자료 번호·팀 번호가 일치하는 행을 삭제합니다.
2. 삭제 행이 있으면 true입니다. 이 역시 본인 등록 자료만 지우는 조건이 아닌 현재 팀 자료 조건입니다.

관련 선언: deleteMaterial (182줄)

## 5. IdeaPanel.java

[src/teamproject/resource/IdeaPanel.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L1)

아이디어 카드·본문·댓글·공감과 5초 자동 갱신을 묶은 화면입니다.

먼저 볼 변수·인수: model·list는 게시물 목록, detail·comments는 본문·댓글 표시, input은 새 댓글 입력입니다. timer는 주기, loading은 중복 조회 방지입니다.

실행 흐름: 타이머·탭 → reload → IdeaListWorker.doInBackground → done → updateList

실행 예: 내 글 수정은 화면에서 작성자 번호를 검사하고 DAO의 UPDATE 조건에서도 다시 확인합니다. 공감은 같은 버튼을 누르면 취소됩니다.

### 모듈과 필드 · 1~23줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L1) · 실행·읽기 시점: 파일 상단

1. JList는 목록 화면, DefaultListModel은 그 자료입니다. JTextArea는 여러 줄 표시, JTextField는 댓글 한 줄 입력입니다.
2. SwingWorker는 DB 조회를 다른 실행 흐름으로 나누고 Timer는 5초 간격을 정합니다. Desktop·URI는 참고 링크를 엽니다.

> **SwingWorker**: 느린 작업은 doInBackground에서, 끝난 뒤 화면 변경은 done에서 처리하도록 나누는 클래스입니다.

### 생성자: 카드·본문·댓글·이벤트 · 24~85줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L24) · 실행·읽기 시점: MainFrame이 아이디어 탭을 만들면

1. 본문·댓글을 읽기 전용으로 정하고 IdeaCardRenderer로 목록 한 항목의 모양을 정합니다. 목록 선택이 확정되면 showDetail을 실행합니다.
2. JSplitPane은 두 영역 사이 크기를 사용자가 조절하게 하는 클래스입니다. 왼쪽 목록, 오른쪽 본문·댓글·입력칸으로 나눕니다.
3. 등록·수정·삭제·공감·댓글·링크·새로고침을 연결합니다. 댓글 버튼과 입력칸 Enter는 같은 comment를 부릅니다. Timer는 보일 때만 5초 간격으로 reload를 요청합니다.

관련 선언: IdeaPanel (24줄)

### addNotify·removeNotify: 타이머 관리 · 86~97줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L86) · 실행·읽기 시점: 화면에 붙거나 떨어질 때

1. addNotify는 부모 처리 후 타이머 시작, removeNotify는 타이머 정지 후 부모 처리를 합니다.
2. 보이지 않는 탭에서는 isShowing 조건으로 조회를 생략합니다. 글자 하나씩 동시 편집하는 통신이 아니라 주기적으로 다시 읽는 폴링 방식입니다.

관련 선언: addNotify (87줄), removeNotify (93줄)

> **폴링**: 정해진 시간마다 DB를 다시 조회하여 변경된 내용을 확인하는 방식입니다.

### 갱신 요청과 중복 검사 · 98~105줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L98) · 실행·읽기 시점: 탭을 열거나 타이머가 갱신을 요청하면

1. loading이 true이면 이미 조회 중이므로 return합니다. 처음 요청이면 true로 바꿉니다.
2. IdeaListWorker 객체를 만들고 execute로 조회를 시작합니다. 클래스가 하는 일은 아래에서 이어서 봅니다.

관련 선언: reload (98줄)

### 조회 작업 클래스 · 106~114줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L106) · 실행·읽기 시점: execute로 작업이 시작되면

1. SwingWorker를 상속합니다. List<IdeaDAO.Idea>는 조회할 결과형이고 Void는 도중 결과를 쓰지 않는다는 뜻입니다.
2. doInBackground는 새 연결로 IdeaDAO.list를 호출합니다. try 괄호에서 만든 연결은 블록이 끝날 때 자동으로 닫힙니다.

관련 선언: doInBackground (108줄)

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### 조회 결과 받기 · 115~128줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L115) · 실행·읽기 시점: Swing이 화면 실행 흐름에서 호출

1. get으로 조회 결과를 받은 뒤 패널이 화면에 붙어 있으면 updateList에 넘깁니다.
2. 실패는 상태 라벨에 표시합니다. finally에서 loading을 false로 바꿔 다음 조회를 받을 수 있게 합니다.

관련 선언: done (115줄)

### 모델을 바꾸고 선택 복원 · 129~145줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L129) · 실행·읽기 시점: done이 새 자료를 넘기면

1. 모델을 비우기 직전에 사용자가 보고 있던 항목을 기억합니다. 조회 도중 다른 항목을 골랐으면 그 항목이 기준입니다.
2. 새 항목을 하나씩 목록 모델에 넣고 같은 번호가 있으면 itemToSelect에 저장합니다. 전부 채운 뒤 그 항목을 선택합니다.
3. 선택한 항목이 삭제되었다면 일치하는 번호가 없어 선택을 복원하지 않습니다.

관련 선언: updateList (129줄)

### selected: 게시물 선택 확인 · 146~151줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L146) · 실행·읽기 시점: 수정·삭제·공감·댓글·링크 시작

1. 선택된 Idea를 반환하며 없으면 안내합니다.
2. 버튼 메서드는 null을 받으면 더 진행하지 않습니다. DB 번호가 아닌 목록 위치로 게시물을 결정하지 않습니다.

관련 선언: selected (146줄)

### showDetail: 선택 본문과 댓글 · 152~175줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L152) · 실행·읽기 시점: 목록 선택이 바뀌거나 댓글 등록 후

1. 선택이 없으면 본문·댓글 표시를 비우고 종료합니다.
2. 있으면 제목·작성자·링크·공감 수·본문을 표시하고, IdeaDAO.comments의 문자열 목록을 빈 줄로 이어 댓글 영역에 넣습니다. 이 댓글 조회는 현재 코드에서 화면 실행 흐름에서 수행합니다.

관련 선언: showDetail (152줄)

### edit: 본인 글만 수정·등록 · 176~212줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L176) · 실행·읽기 시점: 아이디어 등록 또는 내 글 수정

1. 기존 글이고 authorId가 로그인 userId와 다르면 안내 후 중단합니다. 새 글은 null로 시작합니다.
2. 제목·선택 링크·본문을 입력받고 확인하는 동안 save를 호출합니다. 제목·링크 규칙 위반이면 안내 후 입력창을 유지합니다.
3. false는 삭제되었거나 권한 조건이 안 맞았다는 뜻입니다. 정상 호출 후 목록을 다시 읽고 입력창을 종료합니다.

관련 선언: edit (176줄)

### delete: 본인 글과 관련 자료 삭제 · 213~226줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L213) · 실행·읽기 시점: 내 글 삭제 버튼 클릭

1. 선택과 확인창을 거친 뒤 팀·회원·게시물 번호를 DAO로 보냅니다.
2. DAO의 작성자 조건이 맞아야 삭제됩니다. 실패하면 본인 글만 삭제 가능하다고 안내하고 목록을 새로 읽습니다. 댓글·공감은 외래키 연쇄 삭제 규칙으로 정리됩니다.

관련 선언: delete (213줄)

> **외래키**: 다른 표의 기본키를 참조하여 존재하는 회원·팀·게시물 등을 연결하는 DB 규칙입니다.

### like: 공감 또는 취소 요청 · 227~237줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L227) · 실행·읽기 시점: 공감/취소 버튼 클릭

1. 선택 게시물 번호·현재 팀·로그인 회원으로 toggleLike를 호출합니다.
2. 추가인지 취소인지 DB의 기존 행으로 판단한 뒤 reload로 카드 개수를 다시 받습니다.

관련 선언: like (227줄)

### comment: 새 댓글 등록 · 238~250줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L238) · 실행·읽기 시점: 댓글 버튼 또는 Enter

1. 현재 선택 글과 입력 문자열을 DAO로 넘깁니다.
2. 성공하면 입력칸을 비우고 showDetail로 댓글 내용을, reload로 카드의 댓글 개수를 갱신합니다. 오류면 입력을 비우기 전에 catch로 이동해 내용이 남습니다.

관련 선언: comment (238줄)

### openLink: 참고 주소 열기 · 251~269줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaPanel.java#L251) · 실행·읽기 시점: 링크 열기 버튼 클릭

1. 선택 글에 주소가 없으면 안내합니다. 브라우저 기능을 지원하지 않으면 직접 열 주소를 보여 줍니다.
2. 지원하면 URI.create로 주소 객체를 만들고 기본 브라우저에 넘깁니다. 주소 형식 규칙은 저장할 때 DAO에서 검사합니다.

관련 선언: openLink (251줄)

## 6. IdeaDAO.java

[src/teamproject/resource/IdeaDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L1)

아이디어 본문·댓글·공감의 조회와 변경을 처리합니다.

먼저 볼 변수·인수: Idea는 게시물 번호·작성자·제목·본문·링크와 댓글·공감 개수입니다. deleted는 공감 삭제 SQL의 영향 행 수입니다.

실행 흐름: 목록 → 본문·개수 묶음 / 저장·댓글·공감 → 소속·대상 검사 → 변경·활동 기록 → commit

실행 예: 공감 행 (글 3,회원 10)이 있으면 삭제하여 취소합니다. 없으면 삭제 행 수가 0이므로 새 행을 넣습니다.

### 모듈과 Idea 필드 · 1~16줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L1) · 실행·읽기 시점: 소스 상단

1. DB와 List를 사용하며 TeamDAO는 소속 검사, ActivityDAO는 활동 기록을 맡습니다.
2. Idea는 화면이 아니라 자료 객체입니다. 댓글·공감 수는 별도 표의 행을 세어 조회할 때 채웁니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### Idea 생성자·toString · 17~40줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L17) · 실행·읽기 시점: 조회 결과 한 건을 만들 때

1. 생성자가 게시물·작성자 정보와 개수들을 필드에 보관합니다.
2. toString은 제목·이름·댓글·공감을 합친 기본 표시 문장입니다. 실제 카드 화면은 IdeaCardRenderer가 이 필드를 읽어 표시합니다.

관련 선언: Idea (17줄), toString (36줄)

### list: 게시물과 두 개수 조회 · 41~66줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L41) · 실행·읽기 시점: 목록 갱신 요청

1. 현재 팀 게시물을 최신 번호부터 읽고 작성자의 이름을 JOIN합니다.
2. SQL 안의 SELECT COUNT(*) 두 개는 각 게시물의 댓글·공감 행 수를 셉니다. 이처럼 SQL 안에서 하는 작은 조회를 하위 질의라고 합니다.
3. 결과를 Idea에 묶어 목록으로 반환합니다. 게시물 자체에 공감 숫자를 직접 더해 저장하는 방식은 아닙니다.

관련 선언: list (41줄)

> **JOIN**: 관련 번호가 같은 여러 표의 행을 이어 필요한 값을 함께 읽는 SQL 기능입니다.

### save: 등록 또는 본인 글 수정 · 67~102줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L67) · 실행·읽기 시점: 입력창 확인 시

1. 제목 1~100자·링크 최대 500자와 http://·https:// 시작 규칙을 검사합니다. 빈 링크는 허용합니다. 소속도 확인합니다.
2. id=0이면 삽입, 아니면 현재 팀·작성자 userId·글 번호가 모두 맞는 글만 수정합니다. 조건의 작성자가 본인 글 제한을 실제 DB에서도 적용합니다.
3. 변경이 있으면 활동을 기록하고 commit합니다. 트랜잭션은 변경·활동이 같이 확정되거나 취소되는 묶음입니다. 실패 시 rollback, finally는 자동 저장 복구입니다.

관련 선언: save (67줄)

> **rollback**: 아직 확정하지 않은 현재 트랜잭션의 변경을 취소합니다.

> **commit**: 현재 트랜잭션의 변경을 DB에 최종 확정합니다.

> **자동 저장**: JDBC의 autoCommit 설정입니다. true면 SQL 변경마다 확정하고 false면 commit·rollback으로 묶음을 마칩니다.

> **트랜잭션**: 여러 DB 변경을 한 묶음으로 처리해 모두 확정하거나, 오류가 나면 묶음 전체를 취소하는 처리입니다.

### delete: 본인 글만 삭제 · 103~113줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L103) · 실행·읽기 시점: 화면의 삭제 확인 뒤

1. 게시물 번호·팀 번호·작성자 번호가 모두 맞는 행만 지웁니다.
2. 삭제된 행이 있으면 true입니다. 댓글·공감 행은 ON DELETE CASCADE로 함께 지워집니다. 이는 부모 게시물을 지울 때 관련 자식 행도 지우는 DB 규칙입니다.

관련 선언: delete (103줄)

> **ON DELETE CASCADE**: 참조한 부모 행을 삭제하면 관련 자식 행도 DB가 함께 삭제하도록 정한 규칙입니다.

### requireIdea: 현재 팀의 대상인지 검사 · 114~124줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L114) · 실행·읽기 시점: 댓글·공감 변경 안에서 호출

1. 아이디어 번호와 현재 팀 번호로 존재를 확인합니다.
2. 결과가 없으면 예외를 내어 다른 팀의 글에 반응을 저장하는 것을 막습니다. private 메서드로 이 DAO 내부에서 재사용합니다.

관련 선언: requireIdea (114줄)

### comments: 댓글 내용 목록 · 125~141줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L125) · 실행·읽기 시점: 선택 글의 상세 화면이 요청

1. 댓글·아이디어·작성자 표를 연결하고 현재 팀·글 번호로 제한합니다. 댓글 번호 오름차순으로 읽습니다.
2. 각 댓글을 이름·시각·본문 문장으로 만들고 목록으로 반환합니다. 화면의 String.join이 댓글 사이에 빈 줄을 넣습니다.

관련 선언: comments (125줄)

### comment: 댓글과 활동 한 묶음 · 142~166줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L142) · 실행·읽기 시점: 댓글 버튼 또는 Enter

1. 댓글이 공백이거나 1000자를 넘으면 중단하고, 회원 소속을 확인합니다.
2. 자동 저장을 끄고 requireIdea로 대상 글을 확인한 다음 댓글과 활동을 INSERT합니다. 둘 다 성공하면 commit, 오류면 rollback 후 화면에 전달합니다.
3. finally에서 자동 저장을 true로 복구합니다. 작성자 번호는 로그인 회원 번호를 사용합니다.

관련 선언: comment (142줄)

### toggleLike: 기존 행 삭제 또는 새 삽입 · 167~197줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaDAO.java#L167) · 실행·읽기 시점: 공감/취소 버튼 클릭

1. 소속·게시물 팀을 검사하고 현재 글·회원 조합의 공감 행을 먼저 삭제합니다. executeUpdate 결과를 deleted에 담습니다.
2. deleted=0이면 원래 공감이 없었으므로 INSERT하여 공감합니다. 1이면 삭제가 된 것이므로 취소이며 추가하지 않습니다. 이런 켜기/끄기 동작을 토글이라고 합니다.
3. 활동 문장도 공감/취소에 맞게 기록하고 트랜잭션을 확정합니다. 복합 기본키가 동일 글·회원의 중복 행을 막습니다.

관련 선언: toggleLike (168줄)

> **토글**: 같은 동작을 반복하면 켜기와 끄기가 번갈아 일어나는 방식입니다.

## 7. IdeaCardRenderer.java

[src/teamproject/resource/IdeaCardRenderer.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaCardRenderer.java#L1)

아이디어 한 건을 제목·작성자·댓글과 공감 수 카드로 표시합니다.

먼저 볼 변수·인수: title·author·counts는 라벨입니다. selected는 항목의 선택 여부이며 입력된 idea에서 표시값을 꺼냅니다.

실행 흐름: IdeaPanel.setCellRenderer → Swing의 표시 요청 → 세 라벨과 색 설정 → this 반환

실행 예: 새 공감 수는 DAO의 다음 조회에서 받습니다. 이 클래스는 화면을 그리며 공감 수를 계산·저장하지 않습니다.

### 모듈과 표시 규약 · 1~13줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaCardRenderer.java#L1) · 실행·읽기 시점: 소스 상단

1. JPanel을 상속하고 ListCellRenderer<Idea>를 구현합니다. 목록의 자료를 어떻게 그릴지 정하는 규약입니다.
2. Color는 선택색, Component는 반환할 화면 부품의 공통 형식입니다.

### 생성자: 3줄 카드 · 14~24줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaCardRenderer.java#L14) · 실행·읽기 시점: 아이디어 화면이 표시 객체를 만들면

1. GridLayout(3,1)에 제목·작성자·개수 라벨을 차례로 넣습니다.
2. 선 테두리와 안쪽 여백을 겹쳐 적용합니다. 이 설정은 카드 기본 모양입니다.

관련 선언: IdeaCardRenderer (14줄)

### getListCellRendererComponent: 값 채우기 · 25~42줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/IdeaCardRenderer.java#L25) · 실행·읽기 시점: 각 아이디어를 화면에 그릴 때

1. 전달된 idea의 제목·작성자·댓글 수·공감 수를 라벨에 표시합니다.
2. selected이면 선택색, 아니면 기본색을 사용하고 return this로 카드 패널을 반환합니다. 목록 위치 index·focus는 이 코드에서 별도 처리하지 않습니다.

관련 선언: getListCellRendererComponent (26줄)

## 8. ActivityPanel.java

[src/teamproject/resource/ActivityPanel.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/ActivityPanel.java#L1)

기록된 최근 활동을 선택 가능한 문장 목록으로 보여 줍니다.

먼저 볼 변수·인수: rows는 String 목록을 가진 모델, conn·teamId는 현재 팀 조회에 사용하는 값입니다.

실행 흐름: 생성자/탭 진입/버튼 → reload → ActivityDAO.recent → rows.addElement

실행 예: 최근 활동은 최대 30개입니다. 메모 저장·일정 저장·아이디어 저장·댓글·공감 등 DAO에서 기록한 동작만 나타납니다.

### 모듈과 필드 · 1~15줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/ActivityPanel.java#L1) · 실행·읽기 시점: 파일 상단

1. JList는 항목 목록, DefaultListModel<String>은 문자열만 담는 목록 자료입니다.
2. ActivityDAO는 장*민 담당 공통 파일입니다. 이 화면은 조회한 문장을 표시하는 역할을 맡습니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### 생성자: 목록·버튼 배치 · 16~26줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/ActivityPanel.java#L16) · 실행·읽기 시점: MainFrame이 최근 활동 탭을 만들면

1. 연결·팀 번호를 저장하고 rows를 JList에 연결합니다. JScrollPane으로 긴 목록에 스크롤을 붙입니다.
2. 새로고침 버튼을 reload에 연결하고 처음에도 reload하여 자료를 표시합니다.

관련 선언: ActivityPanel (16줄)

### reload: 최근 문장으로 교체 · 27~35줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/resource/ActivityPanel.java#L27) · 실행·읽기 시점: 탭 진입 또는 버튼 클릭

1. rows.clear로 화면 자료만 비운 뒤 DAO가 반환한 문자열을 순서대로 addElement합니다.
2. DB 활동을 삭제하는 동작은 아닙니다. DAO에서 정한 최신순·최대 개수 기준을 그대로 표시합니다.

관련 선언: reload (27줄)

## 마지막 시연·질문

9개 탭에 같은 팀 번호가 전달됨을 설명하고, 자료 링크·아이디어 작성·댓글·공감 취소·최근 활동까지 시연합니다.

**JList의 카드는 어디에서 만들어지나요?**

자료는 IdeaDAO, 목록은 IdeaPanel, 한 항목의 모양은 IdeaCardRenderer가 맡습니다.

**공감 수를 직접 1씩 더하나요?**

공감 행을 추가·삭제하고 목록 조회 시 COUNT로 셉니다. 같은 글·회원 조합은 중복 저장하지 못합니다.

**자료도 아이디어처럼 본인 것만 수정하나요?**

현재 자료는 같은 팀 조건, 아이디어는 같은 팀과 작성자 본인 조건을 사용합니다.

소스 기준 커밋: 347d21cb4d48ef80c333af9ae4da02395f618765. 줄 번호는 이 기준의 실제 파일과 일치합니다.
