# 이*주 · 소스별 브리핑

담당: 진행률·일정 달력

PPT 원래 담당: 진행률·일정 / 추가 담당: 기간 겹침 달력·일정 관리·업무 마감일 표시

[코드와 설명 함께 보기](04_schedule.html) · [PDF](../../output/pdf/member_briefings/04_schedule.pdf)

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
| JTable | 행과 열로 자료를 보여 주는 표입니다. |
| DefaultTableModel | JTable이 표시할 열 이름과 행 자료를 보관합니다. |
| JTextField | 한 줄 입력칸입니다. |
| JTextArea | 여러 줄의 입력·표시 영역입니다. |
| JComboBox | 목록을 펼쳐 하나를 고르는 선택칸입니다. |
| JProgressBar | 진행 수치를 막대와 글자로 표시합니다. |
| JScrollPane | 긴 표·목록·본문에 스크롤을 붙입니다. |
| JOptionPane | 입력·확인·안내창을 간단히 만듭니다. |
| JTabbedPane | 여러 화면 중 하나를 탭으로 선택합니다. |
| Connection | 열린 DB 연결을 나타냅니다. |
| PreparedStatement | SQL과 입력값을 따로 전달해 조회·변경합니다. |
| ResultSet | DB 조회 결과입니다. next()로 다음 결과 행에 이동합니다. |
| LocalDate | 연·월·일로 된 하루를 나타냅니다. |
| YearMonth | 연·월과 그 달의 첫날·말일을 계산합니다. |

## 예제 값으로 실행 따라가기

1. 달 첫날이 목요일이면 getDayOfWeek().getValue()가 4이므로 빈 칸을 3개 넣습니다.
2. drawCalendar가 1일 칸을 만들며 호출한 dayText는 그날이 별도 일정 기간 안인지, 업무 마감일과 같은지 각각 검사합니다.
3. 동시에 두 조건을 만족하는 자료가 있으면 한 칸에 둘 다 표시됩니다. fillScheduleTable은 별도 일정 행만 채웁니다.

## 1. InlineProgressData.java

[src/teamproject/schedule/InlineProgressData.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L1)

DB에서 읽은 사람·업무를 담고 진행률과 마감 안내를 계산합니다.

먼저 볼 변수·인수: people은 Person 목록, tasks는 계산용 Task 목록입니다. userId=0은 팀 전체 계산을 뜻하는 내부 약속입니다.

실행 흐름: InlineProgressDAO가 목록 채움 → getTotalCount·getCompletedCount → getPercent / getDeadline

실행 예: 업무 3개 중 1개 완료면 1×100.0÷3을 반올림해 33%입니다. 업무가 0개일 때는 나누지 않고 0%를 반환합니다.

### 모듈과 Person 선언 · 1~16줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L1) · 실행·읽기 시점: 파일 상단

1. ArrayList는 여러 항목을 순서대로 보관합니다. LocalDate·ChronoUnit은 날짜와 날짜 사이의 일수를 계산합니다.
2. 이 파일은 SQL을 실행하거나 화면을 만들지 않습니다. 받은 자료로 계산만 하므로 예제 값으로 결과를 확인하기 쉽습니다.

### Person: 생성자·값 반환·표시 · 17~36줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L17) · 실행·읽기 시점: 진행률 조회가 사람 한 명을 만들 때

1. 생성자는 회원 번호·이름을 저장합니다. getUserId·getName은 각 값을 반환합니다.
2. toString은 이름 선택칸에 “이름 (회원 번호)”를 표시할 문장입니다. 동명이인이어도 번호로 구별됩니다.

관련 선언: Person (17줄), getUserId (22줄), getName (26줄), toString (32줄)

### Task: 진행률 계산용 자료 · 37~61줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L37) · 실행·읽기 시점: DAO가 업무 한 건을 읽으면

1. 생성자는 담당자 번호·상태·마감일만 보관합니다. getAssigneeId·getStatus·getDueDate는 그 값을 반환합니다.
2. 업무 제목·설명은 계산에 필요하지 않아 이 작은 클래스에 넣지 않습니다. 업무 관리의 TaskInfo와 목적이 다릅니다.

관련 선언: Task (43줄), getAssigneeId (49줄), getStatus (53줄), getDueDate (57줄)

### 목록과 getPeople·getTasks · 62~73줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L62) · 실행·읽기 시점: DAO가 자료를 채우거나 화면이 읽을 때

1. 사람 목록과 업무 목록을 빈 ArrayList로 시작합니다. 두 get 메서드는 해당 목록 객체 자체를 돌려줍니다.
2. DAO는 getPeople().add·getTasks().add로 목록을 채웁니다. 새 복사 목록을 반환하는 구조가 아닙니다.

관련 선언: getPeople (66줄), getTasks (70줄)

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### getTotalCount: 전체·개인 업무 수 · 74~86줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L74) · 실행·읽기 시점: 막대의 전체 개수가 필요할 때

1. total=0에서 모든 업무를 반복합니다. userId가 0이거나 업무 담당자가 요청한 번호와 같으면 1을 더합니다.
2. ||는 둘 중 하나라도 참이면 통과하는 “또는” 조건입니다. 반복 종료 후 total을 반환합니다.

관련 선언: getTotalCount (76줄)

### getCompletedCount: 완료한 개수 · 87~99줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L87) · 실행·읽기 시점: 진행률 계산에서 호출

1. 전체·개인 조건으로 대상 업무를 좁힌 다음 상태가 “완료”인지 한 번 더 검사합니다.
2. 두 조건을 만족할 때만 completed를 늘립니다. 문자열 내용 비교는 ==가 아니라 equals를 사용합니다.

관련 선언: getCompletedCount (87줄)

### getPercent: 0건 처리와 반올림 · 100~110줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L100) · 실행·읽기 시점: 진행률 막대 값을 구할 때

1. 앞의 두 메서드로 전체·완료 개수를 얻습니다. total=0이면 0을 반환해 0으로 나누지 않습니다.
2. 100.0을 써서 소수 계산을 유지하고 Math.round로 가장 가까운 정수로 반올림한 뒤 int로 반환합니다.

관련 선언: getPercent (100줄)

### getDeadline: 순서가 중요한 날짜 분기 · 111~129줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressData.java#L111) · 실행·읽기 시점: 마감 안내 문구를 만들 때

1. 완료이면 날짜가 지나도 “완료”, 날짜가 없으면 “마감일 없음”을 먼저 반환합니다.
2. 그 외에는 today에서 dueDate까지 일수를 구합니다. 음수면 지연, 0이면 오늘 마감, 양수면 D-일수입니다. 오늘도 인수로 받아 같은 예제 날짜로 반복 확인할 수 있습니다.

관련 선언: getDeadline (112줄)

## 2. InlineProgressDAO.java

[src/teamproject/schedule/InlineProgressDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressDAO.java#L1)

현재 팀의 사람·업무를 조회하여 진행률 계산용 자료를 채웁니다.

먼저 볼 변수·인수: data는 반환할 InlineProgressData, found는 업무 담당자가 사람 목록에 이미 있는지 나타내는 표시입니다.

실행 흐름: load → 현재 팀원 목록 → 현재 팀 업무 → 빠진 담당자 보충 → data 반환

실행 예: 팀에서 나간 사람이 업무 담당자로 남아 있으면 진행률 조회 대상에 보충합니다. 업무를 현재 팀원에게 임의로 옮기지는 않습니다.

### 모듈과 책임 · 1~14줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressDAO.java#L1) · 실행·읽기 시점: 파일 상단

1. Connection·PreparedStatement·ResultSet으로 조회하고 Date를 LocalDate로 바꿉니다.
2. 이 DAO는 자료를 읽기만 하며 INSERT·UPDATE·DELETE를 하지 않습니다. 공유 conn도 닫지 않습니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### load 앞부분: 현재 소속 팀원 · 15~36줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressDAO.java#L15) · 실행·읽기 시점: 진행률 패널·대시보드가 최신 자료를 요청

1. 빈 data를 만들고 team_members와 users를 회원 번호로 JOIN합니다. 현재 팀에 속한 사람만 읽습니다.
2. 결과를 한 행씩 Person으로 만들어 data.getPeople()에 추가합니다. try 안의 조회 도구와 결과는 자동으로 닫힙니다.

관련 선언: load (15줄)

> **JOIN**: 관련 번호가 같은 여러 표의 행을 이어 필요한 값을 함께 읽는 SQL 기능입니다.

### load 후반: 업무와 담당자 보충 · 37~76줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressDAO.java#L37) · 실행·읽기 시점: 사람 목록을 채운 뒤

1. 현재 팀의 모든 업무를 읽습니다. LEFT JOIN은 담당자 이름을 못 찾아도 업무 행은 남기는 연결 방식입니다.
2. DB 날짜가 null이 아닐 때만 toLocalDate로 변환하고 Task를 목록에 넣습니다.
3. 각 담당자 번호가 people에 있는지 반복해 검사합니다. 없으면 이름 또는 “담당자 정보 없음”으로 Person을 보충합니다. 끝나면 두 목록이 채워진 data를 반환합니다.

> **LEFT JOIN**: 오른쪽 표에 연결할 행이 없어도 왼쪽 표의 행은 남겨서 읽는 연결 방식입니다.

## 3. InlineProgressPanel.java

[src/teamproject/schedule/InlineProgressPanel.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L1)

기존 업무 창 위에 진행률 막대를 붙이고 표의 마감 안내 열을 채웁니다.

먼저 볼 변수·인수: personBox는 개인 선택칸, teamBar·personBar는 막대, taskModel은 윤*훈 담당 표의 모델입니다. updating은 목록 갱신 중 이벤트를 막는 표시입니다.

실행 흐름: TaskFrame.attach → 생성자 → reload → DAO.load → showDeadlines·showProgress → setBar

실행 예: 이름 목록을 다시 넣는 중에도 선택 이벤트가 생길 수 있습니다. updating=true 동안은 그 이벤트를 처리하지 않아 불필요한 재계산을 막습니다.

### 모듈과 필드 · 1~41줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L1) · 실행·읽기 시점: 상단부터 읽기

1. JProgressBar는 0~100 같은 수치를 막대와 글자로 표시하는 클래스입니다. ActionListener는 버튼·선택 동작을 받을 메서드 규약입니다.
2. taskModel을 공유하므로 새 표를 따로 만들지 않고 기존 업무 표의 마지막 열에 마감 안내를 넣을 수 있습니다.

### attach: 다른 창에 패널 붙이기 · 42~52줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L42) · 실행·읽기 시점: TaskFrame 생성자가 한 번 호출

1. 새 InlineProgressPanel을 만든 뒤 전달받은 frame의 위쪽에 추가합니다.
2. revalidate는 배치가 바뀌었음을 화면에 알립니다. 생성한 panel을 반환하여 TaskFrame이 이후 reload를 호출할 수 있게 합니다.

관련 선언: attach (45줄)

### 생성자: 막대·선택칸·추가 열 · 53~91줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L53) · 실행·읽기 시점: attach가 새 객체를 만들면

1. 연결·모델·팀을 저장하고 개인 선택칸, 전체·개인 막대, 새로고침 버튼과 안내 라벨을 배치합니다.
2. 막대 범위를 0~100으로 만들고 글자 표시를 켭니다. 기존 열 개수를 deadlineColumn에 기억한 뒤 “마감 안내” 열을 추가합니다.
3. 이름 선택과 버튼 이벤트를 this에 연결합니다. this는 ActionListener를 구현한 현재 패널이며 actionPerformed가 실행됩니다. 마지막에 reload합니다.

관련 선언: InlineProgressPanel (53줄)

### actionPerformed: 동작의 출처 구분 · 92~112줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L92) · 실행·읽기 시점: 선택칸 또는 버튼 동작이 발생하면

1. updating이면 바로 종료합니다. 이벤트의 출처가 personBox이면 이미 읽은 자료로 showProgress만 실행합니다.
2. 새로고침 버튼이면 TaskDAO로 업무 표를 다시 읽고, reload로 진행률과 마감 안내도 최신으로 바꿉니다. 조회 오류는 안내합니다.

관련 선언: actionPerformed (94줄)

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### reload: 선택 유지·자료 재조회 · 113~141줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L113) · 실행·읽기 시점: 첫 표시·업무 변경·새로고침

1. updating을 true로 바꾸고 이전에 고른 사람의 실제 회원 번호를 기억합니다.
2. DAO에서 새 data를 읽고 이름 목록을 다시 채우면서 같은 번호가 있으면 다시 선택합니다. 그다음 마감 안내와 막대를 계산합니다.
3. 성공·실패와 관계없이 finally에서 updating=false로 복구합니다. 단순히 처음 사람으로 강제 고정하지 않습니다.

관련 선언: reload (114줄)

### showProgress: 팀·개인과 오늘·지연 · 142~176줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L142) · 실행·읽기 시점: 자료를 읽거나 사람 선택을 바꾸면

1. 자료가 없으면 종료합니다. 팀 막대에는 userId=0, 개인 막대에는 선택 번호 또는 -1을 넘겨 setBar를 부릅니다.
2. 모든 업무를 돌며 완료가 아니고 날짜가 있는 경우만 확인합니다. 오늘과 같으면 todayCount, 오늘보다 전이면 lateCount를 늘립니다.
3. 안내 라벨에 현재 팀의 오늘 마감·지연 개수를 표시합니다. 이 개수는 선택한 개인이 아닌 팀 전체 기준입니다.

관련 선언: showProgress (142줄)

### setBar: 수치를 화면 막대로 · 177~188줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L177) · 실행·읽기 시점: showProgress가 막대와 회원 번호를 넘기면

1. data의 전체 개수·완료 개수·진행률을 구해 setValue로 막대에 넣습니다.
2. 전체가 0이면 “업무 없음”, 아니면 퍼센트와 완료/전체 개수를 글자로 보여 줍니다.

관련 선언: setBar (177줄)

### showDeadlines: 표 각 행의 마감 문장 · 189~207줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/InlineProgressPanel.java#L189) · 실행·읽기 시점: 최신 표와 data를 읽은 뒤

1. 각 행의 3번째 열(마감일)과 4번째 열(상태)을 읽고 날짜로 바꿉니다. 열 번호는 0부터 시작합니다.
2. getDeadline 결과를 추가한 deadlineColumn에 넣습니다. 잘못된 날짜 문자열이면 “날짜 확인 필요”로 표시합니다. 모델만 바꾸며 DB에 안내 문장을 저장하지 않습니다.

관련 선언: showDeadlines (189줄)

## 4. ScheduleDAO.java

[src/teamproject/schedule/ScheduleDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/ScheduleDAO.java#L1)

별도 일정과 업무 마감일을 각각 읽고 별도 일정의 저장·삭제를 담당합니다.

먼저 볼 변수·인수: Schedule은 기간이 있는 일정, Deadline은 업무 마감일 한 건입니다. month는 표시할 연·월, id=0이면 새 일정입니다.

실행 흐름: 일정 list + 업무 listDeadlines → 달력 / 일정 입력 → save → 활동 함께 확정

실행 예: 10월 30일~11월 2일 일정은 11월의 말일보다 먼저 시작하고 첫날보다 늦게 끝나므로 11월 조회에도 포함됩니다.

### 모듈·Schedule 생성자 · 1~28줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/ScheduleDAO.java#L1) · 실행·읽기 시점: 파일 상단

1. LocalDate는 하루, YearMonth는 연·월을 나타냅니다. List는 조회 결과 여러 건을 보관합니다.
2. Schedule 생성자는 일정 번호·제목·시작일·종료일·메모를 저장합니다. 기간 비교와 화면 표시에서 이 필드를 직접 읽습니다.

관련 선언: Schedule (20줄)

### list: 표시 월과 겹치는 일정 · 29~52줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/ScheduleDAO.java#L29) · 실행·읽기 시점: 달력 또는 일정 목록이 조회하면

1. 조건은 “시작일≤이번 달 말일”과 “종료일≥이번 달 첫날”입니다. 두 조건을 모두 만족하면 겹치는 기간이 있습니다.
2. 첫 ?는 팀, 둘째는 달 말일, 셋째는 달 첫날입니다. 결과를 Schedule 객체들로 만들어 반환합니다. 시작일이 이번 달인지 만으로 제한하지 않습니다.

관련 선언: list (29줄)

### Deadline 생성자: 업무 표시 자료 · 53~67줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/ScheduleDAO.java#L53) · 실행·읽기 시점: 업무 마감일을 객체로 묶을 때

1. 업무 번호·제목·담당자 이름·상태·마감일을 저장합니다.
2. 별도 일정처럼 시작·종료 기간이 없고 마감일 하루에만 표시하기 위한 자료형입니다.

관련 선언: Deadline (59줄)

### listDeadlines: 이번 달 업무 원본 조회 · 68~95줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/ScheduleDAO.java#L68) · 실행·읽기 시점: SchedulePanel.reload에서 호출

1. 현재 팀 업무 중 마감일이 이번 달 첫날 이상·말일 이하인 행을 읽습니다. 양쪽 경계 날짜를 모두 포함합니다.
2. LEFT JOIN으로 담당자 이름을 붙이고 이름이 없으면 COALESCE로 “담당자 없음”을 사용합니다. COALESCE는 null이 아닌 값을 앞에서부터 고르는 SQL 함수입니다.
3. 마감일·업무 번호 순으로 Deadline 목록을 만듭니다. 완료 업무도 제외하지 않습니다. schedules에 INSERT하지 않아 중복 일정이 생기지 않습니다.

관련 선언: listDeadlines (68줄)

> **LEFT JOIN**: 오른쪽 표에 연결할 행이 없어도 왼쪽 표의 행은 남겨서 읽는 연결 방식입니다.

> **COALESCE**: 주어진 값 중 null이 아닌 첫 값을 고르는 SQL 함수입니다.

### save: 입력 검사와 일정 저장 · 96~136줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/ScheduleDAO.java#L96) · 실행·읽기 시점: 일정 등록·수정 확인 버튼 클릭

1. 제목 1~100자와 종료일이 시작일보다 이르지 않은지 검사합니다. 같은 날짜로 시작·종료해도 됩니다. 현재 팀 소속도 확인합니다.
2. id=0이면 INSERT, 아니면 팀 번호·일정 번호로 UPDATE를 고릅니다. 공통 값들을 채우고 수정일 때만 여섯째 ?에 일정 번호를 넣습니다.
3. 변경 행이 있으면 활동 기록을 남기고 commit합니다. 트랜잭션은 일정 변경과 활동 기록을 함께 확정·취소하는 묶음입니다. 오류면 rollback, finally에서 자동 저장을 복구합니다.

관련 선언: save (96줄)

> **rollback**: 아직 확정하지 않은 현재 트랜잭션의 변경을 취소합니다.

> **commit**: 현재 트랜잭션의 변경을 DB에 최종 확정합니다.

> **자동 저장**: JDBC의 autoCommit 설정입니다. true면 SQL 변경마다 확정하고 false면 commit·rollback으로 묶음을 마칩니다.

> **트랜잭션**: 여러 DB 변경을 한 묶음으로 처리해 모두 확정하거나, 오류가 나면 묶음 전체를 취소하는 처리입니다.

### delete: 별도 일정 삭제 · 137~145줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/ScheduleDAO.java#L137) · 실행·읽기 시점: 일정 삭제 확인 뒤

1. 현재 팀과 실제 일정 번호가 일치하는 schedules 행만 삭제합니다.
2. 삭제 행이 있으면 true입니다. 이 메서드는 업무를 삭제하지 않으며, 업무 마감일 변경은 업무 관리 기능에서 수행합니다.

관련 선언: delete (137줄)

## 5. SchedulePanel.java

[src/teamproject/schedule/SchedulePanel.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L1)

월간 달력과 별도 일정 목록을 만들고 날짜별 내용을 배치합니다.

먼저 볼 변수·인수: month는 현재 표시 월, schedules·deadlines는 두 조회 결과입니다. calendar는 7열 패널, model·table은 별도 일정 표입니다.

실행 흐름: 생성자 → reload → 두 목록 조회 → 날짜 칸 반복 → dayText / 일정 버튼 → edit·delete

실행 예: 어떤 달의 첫날이 목요일이면 offset=3이므로 월·화·수 세 칸을 비운 뒤 1일부터 넣습니다. 같은 날에 일정과 여러 업무를 함께 표시할 수 있습니다.

### 모듈·필드·isCellEditable · 1~31줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L1) · 실행·읽기 시점: 파일 상단

1. YearMonth로 이전·다음 달과 말일을 계산합니다. GridLayout(0,7)은 필요한 만큼 행이 늘어나는 7열 배치입니다.
2. isCellEditable이 false인 익명 모델을 써서 일정 표의 직접 수정을 막습니다. 실제 수정은 입력창과 DAO를 거칩니다.

관련 선언: isCellEditable (26줄)

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### 생성자: 월 이동·두 보기·버튼 · 32~87줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L32) · 실행·읽기 시점: MainFrame이 달력 탭을 만들면

1. 위에는 이전·다음·새로고침, 가운데에는 월간 달력과 별도 일정 목록 탭, 아래에는 일정 버튼과 표시 설명을 만듭니다.
2. 이전·다음은 month를 한 달 바꾼 뒤 reload합니다. 등록은 edit(null), 수정은 선택한 Schedule을 전달하며 삭제는 delete에 연결합니다.
3. JTabbedPane은 화면을 탭으로 고르는 클래스입니다. 달력의 업무 항목은 별도 일정 표의 수정·삭제 대상에 넣지 않습니다.

관련 선언: SchedulePanel (32줄)

### 달력과 표에 필요한 자료 조회 · 88~102줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L88) · 실행·읽기 시점: 월을 바꾸거나 달력 탭에 들어가면

1. ScheduleDAO에서 이번 달 일정 목록과 업무 마감 목록을 읽습니다. 두 조회가 모두 끝나면 화면에 쓰는 필드를 교체합니다.
2. 월 표시를 바꾸고 drawCalendar와 fillScheduleTable을 차례로 호출합니다.

관련 선언: reload (88줄)

### 요일부터 맞춰 달력 그리기 · 103~129줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L103) · 실행·읽기 시점: 새 목록으로 달력을 그릴 때

1. 첫날의 요일에서 1을 빼 앞에 넣을 빈 칸 수를 계산합니다. 첫날부터 말일까지 반복하고 dayText로 날짜 칸의 내용을 얻습니다.
2. 오늘은 배경색을 다르게 칠합니다. 끝에 빈 칸을 채워 전체 칸 수를 7의 배수로 맞춥니다.
3. revalidate는 위치를 다시 맞추고 repaint는 화면을 다시 그리도록 요청하는 메서드입니다.

관련 선언: drawCalendar (103줄)

### 별도 일정 목록 표 채우기 · 130~137줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L130) · 실행·읽기 시점: 달력의 일정 목록 탭을 갱신할 때

1. 기존 행을 비우고 schedules의 값으로 행을 채웁니다. 여기에는 등록·수정할 수 있는 별도 일정만 표시합니다.

관련 선언: fillScheduleTable (130줄)

### dayText: 한 날짜의 문장 만들기 · 138~161줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L138) · 실행·읽기 시점: 각 날짜 칸을 만들 때

1. 날짜를 먼저 적고 모든 별도 일정을 검사합니다. 시작일보다 전도 아니고 종료일보다 뒤도 아니면 그 날짜가 기간 안이므로 [일정] 제목을 붙입니다.
2. 모든 업무를 검사해 due가 해당 날짜와 같을 때 [업무] 제목·상태·담당자를 붙입니다. 여러 건이면 계속 덧붙입니다.
3. 새 DB 조회 없이 전달받은 두 목록만 사용합니다. 최대 31일×두 목록 길이만큼 비교하는 단순 반복 구조입니다.

관련 선언: dayText (139줄)

### selected: 별도 일정 선택 · 162~170줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L162) · 실행·읽기 시점: 수정·삭제 버튼 클릭

1. 일정 목록 탭의 선택 행이 없으면 안내하고 null을 반환합니다.
2. 행이 있으면 표와 같은 순서인 schedules의 객체를 반환합니다. 달력 칸을 클릭해서 일정을 선택하는 기능은 현재 없습니다.

관련 선언: selected (162줄)

### edit: 등록·수정 입력과 재시도 · 171~206줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L171) · 실행·읽기 시점: 일정 등록 또는 선택 일정 수정

1. 새 일정이면 오늘 날짜를 기본값으로, 수정이면 기존 제목·기간·메모를 넣습니다.
2. 확인 시 날짜 문자열을 LocalDate로 바꾸고 id=0 또는 실제 번호로 save를 호출합니다. 날짜 형식·기간 오류면 입력창을 유지해 다시 고치게 합니다.
3. 정상 처리 후 reload하고 종료합니다. 수정 대상이 이미 없으면 안내합니다. 종료일은 시작일과 같아도 허용됩니다.

관련 선언: edit (171줄)

### delete: 확인 후 일정 삭제 · 207~220줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/schedule/SchedulePanel.java#L207) · 실행·읽기 시점: 선택 일정 삭제 버튼 클릭

1. 선택 객체를 얻고 확인창에서 예를 누른 경우만 DAO로 보냅니다.
2. 실제 일정 번호와 팀 번호로 지우고 목록을 갱신합니다. 해당 날짜의 업무 마감일은 업무 표에서 읽기 때문에 이 삭제의 영향을 받지 않습니다.

관련 선언: delete (207줄)

## 마지막 시연·질문

진행률의 0건·완료·지연 예제를 설명한 후, 여러 달에 걸친 일정과 업무 마감일이 함께 보이는 달력을 확인합니다.

**왜 100이 아니라 100.0인가요?**

소수점 계산을 유지하고 마지막에 반올림하기 위해서입니다.

**이번 달 밖에서 시작한 일정은 어떻게 찾나요?**

시작≤이번 달 말일, 종료≥이번 달 첫날 두 조건으로 겹침을 검사합니다.

**업무를 일정 표에 따로 저장하나요?**

아니요. tasks의 마감일을 직접 읽어 표시하므로 수정·삭제 후 재조회하면 그대로 반영됩니다.

소스 기준 커밋: 347d21cb4d48ef80c333af9ae4da02395f618765. 줄 번호는 이 기준의 실제 파일과 일치합니다.
