# 윤*훈 · 소스별 브리핑

담당: 업무 관리·상태별 보드

PPT 원래 담당: 업무 관리 / 추가 담당: 내 업무 필터·상태별 카드·메모에서 업무 생성·원문 연결

[코드와 설명 함께 보기](03_task.html) · [PDF](../../output/pdf/member_briefings/03_task.pdf)

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
| JCheckBox | 선택 여부를 켜거나 끄는 확인칸입니다. |
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
| LocalDate | 연·월·일로 된 하루를 나타냅니다. |

## 예제 값으로 실행 따라가기

1. 업무 번호 8의 상태가 진행 중이면 reload의 바깥 반복문에서 진행 중 칸을 처리할 때만 안쪽 if를 통과합니다.
2. 완료 버튼에서 move가 호출되고 findSelectedCard가 선택한 8번 카드 객체를 반환합니다.
3. TaskDAO.updateTaskStatus가 해당 팀의 8번 업무를 바꿉니다. 다시 조회하면 같은 업무가 완료 칸에 보입니다.

## 1. TaskFrame.java

[src/teamproject/task/TaskFrame.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L1)

업무 표와 등록·수정·삭제·완료 버튼을 구성하는 기존 관리 화면입니다.

먼저 볼 변수·인수: taskTable은 표시용 표, tableModel은 행 자료, teamId는 선택 팀, progressPanel은 이*주 담당의 진행률 패널입니다.

실행 흐름: 생성자 → reload → 표 표시 / 버튼 → 선택 번호 → 입력창·검사 → TaskDAO → reload → 진행률

실행 예: 새 업무는 과거 마감일을 막지만, 수정에서는 기존 지연 업무를 편집할 수 있게 허용합니다. 팀에서 나간 담당자는 새 담당자를 직접 고르게 합니다.

### 모듈·필드: 화면과 데이터 구분 · 1~49줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L1) · 실행·읽기 시점: 파일 상단

1. JFrame은 창, JTable은 표, DefaultTableModel은 표의 행 자료입니다. java.awt는 배치와 여백, java.time은 날짜 처리를 맡습니다.
2. 연결·팀 번호를 필드에 두어 모든 버튼이 같은 팀을 처리합니다. 다른 담당자의 MemberFrame·InlineProgressPanel은 호출하는 경계만 설명하면 됩니다.

### 생성자 앞부분: 창과 표 설정 · 50~70줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L50) · 실행·읽기 시점: MainFrame이 업무 화면을 만들면

1. conn·teamId를 저장하고 창 속성과 다섯 기본 열을 준비합니다.
2. new DefaultTableModel 뒤의 중괄호는 이름 없는 하위 클래스를 만드는 익명 클래스입니다. isCellEditable을 재정의하여 false를 반환하므로 표를 직접 고칠 수 없습니다.

관련 선언: TaskFrame (50줄), isCellEditable (66줄)

> **익명 클래스**: 클래스 이름을 따로 짓지 않고 new 뒤의 본문에서 필요한 동작을 재정의하여 객체를 만드는 문법입니다.

### 생성자 중간: 표와 더블클릭 처리 · 71~92줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L71) · 실행·읽기 시점: 표 생성 후 사용자의 클릭을 기다림

1. 모델을 JTable에 연결하고 JScrollPane에 넣습니다.
2. MouseAdapter는 필요한 마우스 동작만 작성하게 하는 클래스입니다. mouseClicked에서 getClickCount()==2인지 검사해 현재 팀 MemberFrame을 엽니다. 이 동작은 업무 수정이 아니라 팀원 창 열기입니다.

관련 선언: mouseClicked (79줄)

### 생성자 후반: 버튼·원문·진행률 연결 · 93~143줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L93) · 실행·읽기 시점: 표 아래 기능을 준비할 때

1. 등록·수정·삭제·완료 버튼을 각각의 메서드에 연결합니다. 원본 메모 버튼은 선택 업무 번호로 TaskDAO.sourceNote를 읽어 안내창에 표시합니다.
2. reload로 업무를 읽은 뒤 InlineProgressPanel.attach로 다른 담당자의 진행률을 위에 붙입니다. 첫 reload 시 progressPanel이 아직 없을 수 있어 null 검사를 사용합니다.
3. 모델 변경을 감지하는 리스너가 열 너비 조절을 예약합니다. 리스너는 사용자의 클릭이나 자료 변경이 발생했을 때 실행할 코드를 등록하는 방식입니다.

> **리스너**: 클릭·선택·자료 변경 같은 일이 발생하면 실행할 코드를 미리 연결하는 방식입니다.

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### fitColumns: 가장 넓은 내용 찾기 · 144~172줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L144) · 실행·읽기 시점: 첫 표시 또는 표 자료가 바뀌면

1. 바깥 반복문은 열, 안쪽 반복문은 그 열의 모든 행을 검사합니다. 머리글 너비로 시작해 각 셀의 표시 너비와 비교합니다.
2. Math.max로 최대 너비를 남기고 여백 20을 더해 열에 적용합니다. 행 수×열 수만큼 확인하는 구조이며 DB 조회는 하지 않습니다.

관련 선언: fitColumns (146줄)

### reload: 업무와 진행률 갱신 · 173~178줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L173) · 실행·읽기 시점: 탭 진입·업무 변경 성공 뒤

1. TaskDAO.loadTasks로 표를 다시 채웁니다.
2. 진행률 패널이 이미 생성돼 있으면 그 패널의 reload도 부릅니다. 업무 표와 진행률이 같은 최신 자료를 읽게 연결하는 지점입니다.

관련 선언: reload (174줄)

### getSelectedTaskId: 화면 행에서 실제 번호 · 179~192줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L179) · 실행·읽기 시점: 수정·삭제·완료·원문 버튼 처리 시작

1. 선택 행이 -1이면 선택 없음이므로 -1을 반환합니다.
2. 선택 행의 0번째 열에 보관한 task_id를 읽어 정수로 반환합니다. 화면의 행 번호를 업무 번호로 사용하지 않습니다.

관련 선언: getSelectedTaskId (180줄)

### registerTask: 새 업무 저장 · 193~223줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L193) · 실행·읽기 시점: 업무 등록 버튼 클릭

1. showTaskInputDialog에 old=null을 보내 빈 입력창을 엽니다. 취소 결과 null이면 종료합니다.
2. 검사된 TaskInfo에서 제목·설명·담당자·상태·마감일을 꺼내 insertTask에 전달합니다. 성공 안내 후 reload, SQL 오류면 실패 안내입니다.

관련 선언: registerTask (194줄)

### updateTask: 최신 내용을 읽어 수정 · 224~277줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L224) · 실행·읽기 시점: 업무 수정 버튼 클릭

1. 선택 번호가 없으면 중단합니다. findTask로 표에 없는 설명·담당자 번호까지 최신 상태로 조회합니다.
2. 없으면 이미 삭제됐다고 알리고 새로 읽습니다. 있으면 기존 값을 채운 입력창을 열고, 취소가 아니면 updateTask를 호출합니다.
3. 수정된 행이 있으면 성공, 없으면 사라진 업무로 안내합니다. 결과와 관계없이 정상 조회 경로에서는 reload를 실행합니다.

관련 선언: updateTask (225줄)

### deleteTask: 삭제 확인과 결과 · 278~311줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L278) · 실행·읽기 시점: 업무 삭제 버튼 클릭

1. 실제 업무 번호를 얻고 예/아니오 확인창으로 실수를 막습니다. 아니오면 DB 호출 전에 종료합니다.
2. DAO의 true/false 결과에 따라 삭제 완료 또는 이미 삭제됨을 안내하고 표·진행률을 갱신합니다.

관련 선언: deleteTask (279줄)

### completeTask: 상태만 완료로 변경 · 312~334줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L312) · 실행·읽기 시점: 완료 처리 버튼 클릭

1. 선택 번호를 검사한 뒤 updateTaskStatus에 STATUS_DONE을 넘깁니다.
2. 전체 내용을 다시 입력받지 않고 상태 열만 바꿉니다. 결과 안내 후 reload합니다.

관련 선언: completeTask (313줄)

### showTaskInputDialog 앞부분: 담당자와 기존 값 · 335~375줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L335) · 실행·읽기 시점: 등록 또는 수정 메서드가 호출

1. 현재 팀의 담당자 목록을 읽습니다. 비어 있으면 업무를 만들 수 없어 null을 반환합니다.
2. 제목·설명·담당자·날짜·상태 입력칸을 준비합니다. old가 있으면 기존 값을 넣습니다.
3. 수정 시 담당자 선택을 먼저 -1로 비우고 회원 번호가 같은 사람을 찾습니다. 팀에서 나간 담당자를 다른 사람으로 자동 교체하지 않기 위한 처리입니다.

관련 선언: showTaskInputDialog (339줄)

### showTaskInputDialog: 입력칸 배치 · 376~387줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L376) · 실행·읽기 시점: 필드들을 만들고 기존 값을 채운 뒤

1. GridBagLayout은 각 칸의 위치·크기 비율을 정할 수 있는 배치 클래스입니다. g는 그 배치 조건입니다.
2. addInputRow를 다섯 번 불러 왼쪽 라벨과 오른쪽 입력칸을 같은 규칙으로 놓습니다. 배치와 입력 검사를 분리해 반복 코드를 줄입니다.

### showTaskInputDialog: 반복 입력 검사와 반환 · 388~450줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L388) · 실행·읽기 시점: 입력창에서 확인 또는 취소를 누르면

1. while(true) 안에서 취소면 null, 제목 누락·100자 초과·날짜 누락이면 continue로 같은 창을 다시 엽니다. continue는 현재 반복의 남은 부분을 건너뛰는 명령입니다.
2. LocalDate.parse로 연-월-일 문자열을 날짜로 바꿉니다. 올바르지 않은 날짜는 예외를 안내한 뒤 다시 입력받습니다. 새 등록만 과거 날짜를 막습니다.
3. 담당자가 선택되지 않았으면 다시 고르게 합니다. 모두 통과하면 TaskInfo로 입력을 묶어 반환합니다. 이 메서드 자체는 DB에 저장하지 않습니다.

### addInputRow: 입력창 한 줄 만들기 · 451~462줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFrame.java#L451) · 실행·읽기 시점: 입력창 배치가 한 줄을 요청하면

1. g.gridy에 행, g.gridx에 열을 지정합니다. 0열에는 라벨, 1열에는 입력 부품을 넣습니다.
2. 입력칸의 weightx를 1로 두어 남는 가로 공간을 쓰게 합니다. 같은 배치 절차를 메서드로 묶은 것입니다.

관련 선언: addInputRow (452줄)

## 2. TaskDAO.java

[src/teamproject/task/TaskDAO.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L1)

업무 조회·저장·상태 변경과 메모에서 업무를 만드는 DB 처리를 맡습니다.

먼저 볼 변수·인수: STATUS_LIST는 상태 문자열의 공통 기준입니다. Assignee는 담당자, TaskInfo는 업무 상세, Card는 보드 표시용 자료입니다.

실행 흐름: 화면 요청 → 팀 조건·담당자 검사 → tasks 변경 / 메모 등록 → tasks·task_notes 함께 저장

실행 예: board의 mine=false이면 현재 팀 전체, true이면 로그인 회원 번호와 담당자 번호가 같은 업무만 읽습니다.

### 상단: 사용하는 모듈 · 1~16줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L1) · 실행·읽기 시점: 파일 처음

1. java.sql은 조회·변경, LocalDate는 마감일, List·ArrayList는 목록, DefaultTableModel은 표에 행을 넣는 데 사용합니다.
2. 모든 조회·수정에 전달받은 teamId를 넣습니다. setInt(1,teamId)의 1은 팀 번호가 아닌 SQL 물음표 위치입니다.

### showTasks: 콘솔 조회 · 17~45줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L17) · 실행·읽기 시점: 콘솔 출력 확인용으로 직접 호출할 때

1. 업무와 담당자 이름을 JOIN하고 현재 팀 조건으로 읽습니다.
2. while 결과를 돌며 System.out.println으로 출력합니다. 현재 통합 화면의 표를 채우는 경로는 아래 loadTasks이며 이 메서드는 별도 콘솔용입니다.

관련 선언: showTasks (18줄)

> **JOIN**: 관련 번호가 같은 여러 표의 행을 이어 필요한 값을 함께 읽는 SQL 기능입니다.

### loadTasks: 업무 표 채우기 · 46~74줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L46) · 실행·읽기 시점: TaskFrame 또는 진행률 새로고침이 요청

1. 현재 팀 업무를 실제 업무 번호 순으로 읽습니다.
2. 모델의 행만 비우고 결과 한 건마다 번호·제목·이름·마감일·상태를 Object[]로 만들어 추가합니다. 진행률이 추가한 열 구조는 유지됩니다.

관련 선언: loadTasks (47줄)

### 상태 상수·Assignee 자료형 · 75~114줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L75) · 실행·읽기 시점: 조회 메서드 뒤 선언부

1. static final 문자열 세 개로 “할 일·진행 중·완료” 표기를 한곳에 둡니다. 상수는 여러 파일에서 같은 값을 쓰기 위한 기준입니다.
2. Assignee 생성자는 회원 번호와 이름을 저장합니다. getUserId는 DB에 넣을 번호, getName은 이름, toString은 선택칸에 보일 이름을 반환합니다.

관련 선언: Assignee (95줄), getUserId (100줄), getName (104줄), toString (110줄)

### TaskInfo: 업무 상세를 한 묶음으로 · 115~164줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L115) · 실행·읽기 시점: 조회 결과 또는 입력값을 전달할 때

1. 생성자가 업무 번호·제목·설명·담당자 번호·상태·마감일을 저장합니다. 등록 전의 taskId=0은 아직 번호를 받지 않았다는 표시입니다.
2. getTaskId·getTitle·getDescription·getAssigneeId·getStatus·getDueDate는 이름에 해당하는 필드를 그대로 돌려줍니다. SQL 실행이나 화면 변경은 하지 않습니다.

관련 선언: TaskInfo (125줄), getTaskId (140줄), getTitle (144줄), getDescription (148줄), getAssigneeId (152줄), getStatus (156줄), getDueDate (160줄)

### getTeamMembers: 담당자 선택 목록 · 165~197줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L165) · 실행·읽기 시점: 업무 입력창을 열 때

1. team_members와 users를 연결하고 현재 팀 조건으로 회원 번호·이름을 읽습니다.
2. 각 행을 Assignee로 묶어 목록으로 반환합니다. 같은 이름이 있어도 번호가 함께 보관돼 구별할 수 있습니다.

관련 선언: getTeamMembers (166줄)

### findTask: 업무 한 건 상세 · 198~236줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L198) · 실행·읽기 시점: 업무 수정 전에 호출

1. 실제 업무 번호와 현재 팀 번호 두 조건으로 조회합니다. 없으면 null입니다.
2. 있으면 상세 자료 TaskInfo를 반환합니다. DB의 java.sql.Date를 toLocalDate로 자바 날짜로 바꿔 입력창에 전달합니다.

관련 선언: findTask (199줄)

### insertTask: 새 업무 삽입 · 237~271줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L237) · 실행·읽기 시점: 새 등록 입력 검사를 통과하면

1. requireTeamMember로 담당자가 현재 팀에 속하는지 먼저 검사합니다.
2. 여섯 ?에 팀·제목·설명·담당자·상태·날짜를 순서대로 넣습니다. LocalDate는 Date.valueOf로 DB 날짜 형식에 맞춥니다.
3. executeUpdate로 INSERT를 실행합니다. 이 단일 삽입은 일반 화면 연결의 자동 저장 설정을 사용합니다.

관련 선언: insertTask (238줄)

> **자동 저장**: JDBC의 autoCommit 설정입니다. true면 SQL 변경마다 확정하고 false면 commit·rollback으로 묶음을 마칩니다.

### updateTask: 상세 수정 · 272~308줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L272) · 실행·읽기 시점: 수정창에서 확인한 뒤

1. 새 담당자의 소속을 먼저 확인하고 업무의 다섯 값을 UPDATE합니다.
2. WHERE에 업무 번호·팀 번호를 모두 넣습니다. executeUpdate는 바뀐 행 수를 반환하므로 0보다 크면 true, 대상이 없으면 false입니다.

관련 선언: updateTask (274줄)

### updateTaskStatus: 상태만 바꾸기 · 309~322줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L309) · 실행·읽기 시점: 완료 버튼 또는 보드 상태 변경

1. 변경할 상태와 실제 업무·팀 번호를 SQL에 넣습니다.
2. 수정 결과가 있는지 true/false로 돌려줍니다. 업무명·설명·날짜는 이 SQL에서 바꾸지 않습니다.

관련 선언: updateTaskStatus (310줄)

### deleteTask: 현재 팀 업무 삭제 · 323~334줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L323) · 실행·읽기 시점: 삭제 확인 뒤

1. 업무 번호와 팀 번호가 함께 맞는 tasks 행만 삭제합니다.
2. 행이 삭제되면 true입니다. 해당 업무의 task_notes 연결은 외래키의 연쇄 삭제 규칙으로 지워지며 원문 메모는 남습니다.

관련 선언: deleteTask (324줄)

> **외래키**: 다른 표의 기본키를 참조하여 존재하는 회원·팀·게시물 등을 연결하는 DB 규칙입니다.

### requireTeamMember: 담당자 소속 검사 · 335~346줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L335) · 실행·읽기 시점: 업무 등록·수정·메모 업무 생성 전에

1. team_members에서 현재 팀과 담당자 번호를 함께 찾습니다.
2. 없으면 SQLException으로 중단합니다. 화면의 선택칸만 믿지 않고 DB에서 다시 확인하는 검사입니다.

관련 선언: requireTeamMember (335줄)

### Card 생성자·toString · 347~366줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L347) · 실행·읽기 시점: 상태별 보드의 자료를 만들 때

1. 업무 번호·담당자 번호·제목·이름·상태·마감일을 필드에 담습니다.
2. toString은 제목/이름/날짜를 돌려줍니다. 실제 카드 모양은 TaskCardRenderer가 이 객체를 받아 그립니다.

관련 선언: Card (353줄), toString (362줄)

### board: 전체 업무 또는 내 업무 · 367~391줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L367) · 실행·읽기 시점: 보드의 조회·체크 선택 변경 시

1. 현재 팀과 함께 (?=0 OR 담당자=?) 조건을 사용합니다. mine이 false면 0을 넣어 팀 전체가 통과하고, true면 담당자 일치 조건이 필요해집니다.
2. 마감일·업무 번호 순으로 정렬해 각 결과를 Card에 담아 반환합니다. SQL 조건으로 먼저 줄인 뒤 화면이 상태별로 분류합니다.

관련 선언: board (367줄)

### insertFromNote 앞부분: 메모 확인 · 392~412줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L392) · 실행·읽기 시점: 공유 메모의 업무로 등록에서 요청

1. 담당자 소속을 검사하고 setAutoCommit(false)로 여러 저장을 묶습니다.
2. 선택 메모가 현재 팀에 있는지 조회합니다. 다른 팀 메모이거나 삭제됐다면 예외를 내고 진행하지 않습니다.

관련 선언: insertFromNote (393줄)

### insertFromNote 후반: 업무·연결 함께 저장 · 413~445줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L413) · 실행·읽기 시점: 메모와 담당자 검사를 통과하면

1. 새 tasks 행을 “할 일”로 저장하고 getGeneratedKeys로 자동 생성된 업무 번호를 받습니다.
2. 그 번호와 원문 메모 번호를 task_notes에 넣습니다. 트랜잭션은 이 두 변경을 한 묶음으로 처리하는 것으로, 둘 다 성공해야 commit하고 오류면 rollback합니다.
3. finally는 자동 저장을 true로 돌립니다. 메모 원문은 남으며 업무 설명에는 생성 시점의 메모 본문이 복사됩니다.

> **getGeneratedKeys**: INSERT 뒤 DB가 자동 생성한 새 번호를 읽는 메서드입니다.

> **rollback**: 아직 확정하지 않은 현재 트랜잭션의 변경을 취소합니다.

> **commit**: 현재 트랜잭션의 변경을 DB에 최종 확정합니다.

> **트랜잭션**: 여러 DB 변경을 한 묶음으로 처리해 모두 확정하거나, 오류가 나면 묶음 전체를 취소하는 처리입니다.

### sourceNote: 연결된 현재 원문 · 446~459줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskDAO.java#L446) · 실행·읽기 시점: 업무의 원본 메모 보기 클릭

1. task_notes로 업무와 메모를 연결하고 업무 팀·메모 팀을 모두 현재 팀으로 제한합니다.
2. 연결이 있으면 현재 메모 제목·본문, 없으면 안내 문장을 반환합니다. 과거 버전의 고정 복사본을 보여 주는 조회는 아닙니다.

관련 선언: sourceNote (446줄)

## 3. TaskBoardPanel.java

[src/teamproject/task/TaskBoardPanel.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskBoardPanel.java#L1)

같은 업무를 상태별 세 목록과 내 업무 필터로 보여 줍니다.

먼저 볼 변수·인수: models는 상태별 데이터 목록 3개, lists는 화면 JList 3개입니다. mine은 내 업무만 보기 체크칸입니다.

실행 흐름: 생성자 → reload → TaskDAO.board → 상태별 모델 분류 / 상태 버튼 → move → UPDATE → reload

실행 예: 업무 “발표 준비”의 상태를 진행 중으로 바꾸면 새로운 업무를 만드는 것이 아니라 기존 행이 수정되고 가운데 목록에 다시 표시됩니다.

### 모듈과 필드 · 1~19줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskBoardPanel.java#L1) · 실행·읽기 시점: 파일 상단

1. JList는 선택 가능한 목록, DefaultListModel은 그 목록의 자료, JCheckBox는 켜고 끄는 선택칸입니다.
2. List<DefaultListModel<…>>은 세 목록의 모델을 차례로 담는 바깥 목록입니다. STATUS_LIST의 순서와 모델 순서를 맞춥니다.

### 생성자: 상태별 세 칸·버튼 만들기 · 20~62줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskBoardPanel.java#L20) · 실행·읽기 시점: MainFrame이 업무 보드 탭을 만들면

1. 상태 배열을 돌며 모델·JList·테두리가 있는 열을 세 개 만듭니다. TaskCardRenderer를 지정해 항목이 카드처럼 보이게 합니다.
2. 한 목록에서 고르면 다른 목록의 선택을 해제합니다. 한 번에 하나의 업무만 대상으로 삼기 위한 처리입니다.
3. 상태 변경 버튼은 move(status), 체크·새로고침은 reload에 연결합니다. 마지막 reload로 첫 목록을 채웁니다.

관련 선언: TaskBoardPanel (20줄)

### 다른 상태 칸의 선택 해제 · 63~70줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskBoardPanel.java#L63) · 실행·읽기 시점: 사용자가 업무 카드를 선택했을 때

1. 선택이 끝난 목록을 인수로 받고 lists의 나머지 목록을 반복하며 선택을 지웁니다.
2. 현재 목록인지 객체를 비교합니다. 선택한 카드의 업무 데이터는 이 과정에서 바뀌지 않습니다.

관련 선언: clearOtherSelections (63줄)

### 상태 칸마다 업무 카드 채우기 · 71~88줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskBoardPanel.java#L71) · 실행·읽기 시점: 탭에 들어오거나 상태·필터가 바뀌면

1. TaskDAO.board에서 현재 팀의 목록을 한 번 읽습니다. 조회에 성공하면 각 모델을 비웁니다.
2. 바깥 반복문은 상태 칸을 고르고, 안쪽 반복문은 업무를 하나씩 확인합니다. 카드의 상태와 칸의 상태가 같으면 그 모델에 넣습니다.

관련 선언: reload (71줄)

> **DAO**: DB 조회·저장 코드를 모은 클래스입니다. 화면 입력과 SQL 처리를 나누기 위해 사용합니다.

### 세 목록에서 선택한 카드 찾기 · 89~98줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskBoardPanel.java#L89) · 실행·읽기 시점: 상태 변경 버튼이 move를 부를 때

1. 세 JList를 반복하며 getSelectedValue 결과를 확인합니다. 카드가 있으면 그 값을 바로 반환합니다.
2. 어느 목록에도 선택이 없으면 null로 돌려주므로 move가 안내할 수 있습니다.

관련 선언: findSelectedCard (89줄)

### 선택 업무의 상태 변경 · 99~113줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskBoardPanel.java#L99) · 실행·읽기 시점: 상태 변경 버튼을 누르면

1. findSelectedCard에서 null이 나오면 안내 후 멈춥니다.
2. 카드가 있으면 번호·현재 팀·새 상태를 TaskDAO.updateTaskStatus에 넘깁니다. reload를 부르면 바뀐 상태 칸에 카드가 표시됩니다.

관련 선언: move (99줄)

## 4. TaskCardRenderer.java

[src/teamproject/task/TaskCardRenderer.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskCardRenderer.java#L1)

업무 객체 한 개를 세 줄짜리 카드 모양으로 그리는 표시 전용 클래스입니다.

먼저 볼 변수·인수: title·person·due는 라벨 3개입니다. card는 표시할 업무, selected는 그 항목이 선택됐는지 나타냅니다.

실행 흐름: TaskBoardPanel.setCellRenderer → 목록이 그릴 때 getListCellRendererComponent → 패널 반환

실행 예: 선택한 카드는 목록의 선택 배경·글자색을 사용합니다. 여기에서 상태를 바꾸거나 DB를 저장하지 않습니다.

### 모듈과 클래스 선언 · 1~12줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskCardRenderer.java#L1) · 실행·읽기 시점: 파일 처음

1. ListCellRenderer<Card>는 목록 항목을 어떤 화면 모양으로 그릴지 정하는 규약입니다. implements는 그 규약의 메서드를 구현한다는 뜻입니다.
2. JPanel을 상속해 여러 라벨을 한 카드 안에 배치합니다. Color·Component는 색과 반환할 화면 부품 형식입니다.

### 생성자: 카드 기본 모양 · 13~23줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskCardRenderer.java#L13) · 실행·읽기 시점: 보드에서 표시 클래스를 생성하면

1. 3행 1열 배치에 제목·담당자·날짜 라벨을 넣습니다.
2. 테두리와 안쪽 여백을 한 번 설정합니다. 새 DB 자료를 조회하는 부분은 없습니다.

관련 선언: TaskCardRenderer (13줄)

### getListCellRendererComponent: 값과 선택색 · 24~41줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskCardRenderer.java#L24) · 실행·읽기 시점: Swing이 각 항목을 화면에 그릴 때

1. 전달받은 card의 제목·이름·날짜를 라벨에 넣습니다. index는 목록 위치, selected는 선택 여부이며 focus는 현재 코드에서 사용하지 않습니다.
2. 조건 연산자 selected ? A : B로 선택색 또는 기본색을 고르고 모든 라벨에 적용합니다. return this로 지금 구성한 카드 패널을 돌려줍니다. 같은 표시 객체를 항목마다 재사용합니다.

관련 선언: getListCellRendererComponent (25줄)

## 5. TaskFromNoteDialog.java

[src/teamproject/task/TaskFromNoteDialog.java](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFromNoteDialog.java#L1)

선택한 공유 메모로부터 업무 제목·담당자·마감일을 입력받습니다.

먼저 볼 변수·인수: parent는 안내창 기준 화면, note는 선택 메모 객체, members는 현재 팀 담당자 목록입니다.

실행 흐름: NotePanel의 업무로 등록 → open → 입력 검사 → TaskDAO.insertFromNote → 완료 안내

실행 예: 기본 제목은 메모 제목, 기본 마감일은 내일입니다. 업무 저장과 원문 번호 연결은 TaskDAO에서 함께 처리합니다.

### 모듈과 역할 · 1~14줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFromNoteDialog.java#L1) · 실행·읽기 시점: 파일 상단

1. JOptionPane은 간단한 입력·안내창, JComboBox는 담당자 선택칸, LocalDate는 날짜 변환·비교에 씁니다.
2. 이 클래스는 입력창만 담당합니다. 화면 구성과 DB 트랜잭션을 분리합니다.

> **트랜잭션**: 여러 DB 변경을 한 묶음으로 처리해 모두 확정하거나, 오류가 나면 묶음 전체를 취소하는 처리입니다.

### open 앞부분: 목록과 기본 입력값 · 15~32줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFromNoteDialog.java#L15) · 실행·읽기 시점: NotePanel에서 선택 메모를 전달하면

1. 현재 팀 담당자를 조회하고 비어 있으면 중단합니다.
2. 선택 메모 제목과 내일 날짜를 입력칸 기본값으로 넣고 담당자 선택칸을 구성합니다. 메모 본문은 업무 설명으로 전달할 자료입니다.

관련 선언: open (15줄)

### open 후반: 검사·반복·DAO 호출 · 33~56줄

[코드 위치](https://github.com/minhyun0503/TeamProjectManager/blob/347d21cb4d48ef80c333af9ae4da02395f618765/src/teamproject/task/TaskFromNoteDialog.java#L33) · 실행·읽기 시점: 입력창 확인 버튼 클릭

1. 날짜를 LocalDate로 바꾸고 제목의 빈 값·100자 초과·과거 날짜를 검사합니다. 잘못됐으면 같은 창을 다시 보여 줍니다.
2. 선택 담당자의 회원 번호를 꺼내 메모 번호·제목·본문·날짜와 함께 insertFromNote에 전달합니다. 성공하면 안내 후 return, 취소하면 while을 벗어납니다.
3. 등록 실패를 안내한 경우 입력 내용은 유지됩니다. 다른 업무 탭은 그 탭을 다시 열 때 최신 원본을 조회합니다.

## 마지막 시연·질문

업무를 등록하고 상태별 보드에서 진행 중으로 바꿉니다. 메모에서 만든 업무를 골라 원본 메모 조회까지 설명합니다.

**왜 화면의 행 번호로 삭제하지 않나요?**

행은 표시 위치이고 task_id가 저장된 업무의 고유 번호이기 때문입니다.

**상태를 바꾸면 업무가 하나 더 생기나요?**

아니요. 기존 tasks 행의 status를 바꾸고 다른 상태 목록에 표시합니다.

**업무와 메모 연결 중 하나만 저장되면?**

같은 트랜잭션이므로 오류 시 둘 다 취소합니다. 생성된 업무 번호로 연결해야 합니다.

소스 기준 커밋: 347d21cb4d48ef80c333af9ae4da02395f618765. 줄 번호는 이 기준의 실제 파일과 일치합니다.
