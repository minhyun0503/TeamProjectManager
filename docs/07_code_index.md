# 파일·메서드 코드 색인

각 메서드를 눌러 현재 소스의 선언 위치를 확인할 수 있습니다. 상세 브리핑은 [조원별 자료](member_briefings/README.md)를 참고하세요.

## common

담당 **장*민**

### ActivityDAO.java

[활동 기록 저장·최근 목록 조회](../src/teamproject/common/ActivityDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [record](../src/teamproject/common/ActivityDAO.java#L11) | 현재 트랜잭션에서 활동 한 행을 저장합니다. |
| [recent](../src/teamproject/common/ActivityDAO.java#L22) | 현재 팀의 최근 30개 활동을 읽습니다. |

### DB_connection.java

[DB 접속 설정과 연결 생성](../src/teamproject/common/DB_connection.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [configure](../src/teamproject/common/DB_connection.java#L15) | URL·DB 계정을 메모리에 설정합니다. |
| [open](../src/teamproject/common/DB_connection.java#L22) | 새 JDBC Connection을 반환합니다. |

### Main.java

[진입점·실행 모드와 DB 준비](../src/teamproject/common/Main.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [main](../src/teamproject/common/Main.java#L12) | EDT에 시작 작업을 예약합니다. |
| [start](../src/teamproject/common/Main.java#L17) | 모드를 선택하고 DB 준비 작업 후 로그인 창을 엽니다. |
| [configureDemoDatabase](../src/teamproject/common/Main.java#L47) | 로컬 데모 파일 DB 주소를 설정합니다. |
| [configureMySqlDatabase](../src/teamproject/common/Main.java#L57) | MySQL 접속 정보를 받고 취소 여부를 반환합니다. |
| [showWaitingDialog](../src/teamproject/common/Main.java#L82) | DB 준비 안내창을 보여 주고 창 객체를 돌려줍니다. |
| [DatabaseStartupWorker](../src/teamproject/common/Main.java#L98) | DB 준비에 필요한 모드와 대기창을 받습니다. |
| [doInBackground](../src/teamproject/common/Main.java#L104) | 백그라운드에서 DB 연결·조회만 수행합니다. |
| [done](../src/teamproject/common/Main.java#L116) | 배경 작업의 결과를 받아 EDT에서 화면을 바꿉니다. |

### SchemaSetup.java

[전용 스키마·데모 가상 데이터 준비](../src/teamproject/common/SchemaSetup.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [initialize](../src/teamproject/common/SchemaSetup.java#L13) | 스키마 리소스를 실행하고 필요하면 데모 데이터를 넣습니다. |
| [seedDemo](../src/teamproject/common/SchemaSetup.java#L27) | 처음인 경우만 가상 데이터를 트랜잭션으로 저장합니다. |

### TeamDAO.java

[소속 팀 목록·공통 소속 검사](../src/teamproject/common/TeamDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [Team](../src/teamproject/common/TeamDAO.java#L18) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [getTeamId](../src/teamproject/common/TeamDAO.java#L23) | 객체가 보관한 TeamId 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getTeamName](../src/teamproject/common/TeamDAO.java#L27) | 객체가 보관한 TeamName 값을 반환합니다. DB를 바꾸지 않습니다. |
| [toString](../src/teamproject/common/TeamDAO.java#L32) | 목록·선택창에 보이는 대표 문자열을 반환합니다. |
| [findTeams](../src/teamproject/common/TeamDAO.java#L37) | 회원의 소속 팀을 JOIN으로 조회합니다. |
| [requireMember](../src/teamproject/common/TeamDAO.java#L59) | 팀·회원 소속이 없으면 예외로 저장을 중단합니다. |

## member

담당 **이*찬**

### LoginFrame.java

[로그인 입력·팀 선택](../src/teamproject/member/LoginFrame.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [LoginFrame](../src/teamproject/member/LoginFrame.java#L27) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [doLogin](../src/teamproject/member/LoginFrame.java#L59) | 입력 검사→인증→소속 조회→선택→통합 화면 이동입니다. |

### MemberDAO.java

[팀원 SQL·두 표 변경 트랜잭션](../src/teamproject/member/MemberDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [loadMembers](../src/teamproject/member/MemberDAO.java#L17) | 현재 팀의 사용자·소속 정보를 표에 채웁니다. |
| [insertMember](../src/teamproject/member/MemberDAO.java#L70) | 계정·소속을 함께 저장하고 실패 시 롤백합니다. |
| [updateMember](../src/teamproject/member/MemberDAO.java#L151) | 소속 확인 후 계정 정보·역할을 함께 바꿉니다. |
| [deleteMember](../src/teamproject/member/MemberDAO.java#L232) | 계정은 남기고 해당 팀 소속만 지웁니다. |

### MemberFrame.java

[팀원 표와 CRUD 입력창](../src/teamproject/member/MemberFrame.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [MemberFrame](../src/teamproject/member/MemberFrame.java#L46) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [reload](../src/teamproject/member/MemberFrame.java#L115) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |
| [registerMember](../src/teamproject/member/MemberFrame.java#L120) | 등록 입력창의 값을 검사하고 DAO를 호출합니다. |
| [updateMember](../src/teamproject/member/MemberFrame.java#L188) | 소속 확인 후 계정 정보·역할을 함께 바꿉니다. |
| [deleteMember](../src/teamproject/member/MemberFrame.java#L271) | 계정은 남기고 해당 팀 소속만 지웁니다. |

### NoteDAO.java

[메모 데이터·버전 충돌·내용 이력](../src/teamproject/member/NoteDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [Note](../src/teamproject/member/NoteDAO.java#L17) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [toString](../src/teamproject/member/NoteDAO.java#L34) | 목록·선택창에 보이는 대표 문자열을 반환합니다. |
| [list](../src/teamproject/member/NoteDAO.java#L39) | 현재 팀 목록을 SQL로 조회하여 DTO 목록을 만듭니다. |
| [save](../src/teamproject/member/NoteDAO.java#L63) | 입력·소속 검사 후 등록/수정하고 필요한 이력·활동을 함께 저장합니다. |
| [insertNote](../src/teamproject/member/NoteDAO.java#L99) | 메모를 삽입하고 DB가 발급한 번호를 반환합니다. |
| [updateNote](../src/teamproject/member/NoteDAO.java#L119) | 기존 버전과 일치하는 메모만 수정합니다. |
| [insertHistory](../src/teamproject/member/NoteDAO.java#L139) | 저장한 메모 본문의 버전을 이력 표에 남깁니다. |
| [delete](../src/teamproject/member/NoteDAO.java#L151) | 현재 팀과 식별 조건에 맞는 행만 삭제합니다. |
| [history](../src/teamproject/member/NoteDAO.java#L162) | 현재 팀 메모의 내용 버전을 최신순 문자열로 반환합니다. |

### NotePanel.java

[공유 메모 화면·폴링·편집](../src/teamproject/member/NotePanel.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [NotePanel](../src/teamproject/member/NotePanel.java#L23) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [addNotify](../src/teamproject/member/NotePanel.java#L71) | 패널이 화면에 붙으면 자동 갱신 타이머를 시작합니다. |
| [removeNotify](../src/teamproject/member/NotePanel.java#L77) | 화면에서 떨어지면 타이머를 중지합니다. |
| [reload](../src/teamproject/member/NotePanel.java#L83) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |
| [doInBackground](../src/teamproject/member/NotePanel.java#L93) | 백그라운드에서 DB 연결·조회만 수행합니다. |
| [done](../src/teamproject/member/NotePanel.java#L100) | 배경 작업의 결과를 받아 EDT에서 화면을 바꿉니다. |
| [updateList](../src/teamproject/member/NotePanel.java#L114) | 현재 선택을 기억하고 최신 목록에서 같은 번호를 다시 선택합니다. |
| [selected](../src/teamproject/member/NotePanel.java#L131) | 선택 항목을 반환하고 없으면 안내합니다. |
| [edit](../src/teamproject/member/NotePanel.java#L137) | 입력창에서 값을 읽고 저장 실패 시 입력 수정 기회를 줍니다. |
| [delete](../src/teamproject/member/NotePanel.java#L171) | 현재 팀과 식별 조건에 맞는 행만 삭제합니다. |
| [history](../src/teamproject/member/NotePanel.java#L186) | 현재 팀 메모의 내용 버전을 최신순 문자열로 반환합니다. |

### UserDAO.java

[로그인 인증 결과](../src/teamproject/member/UserDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [User](../src/teamproject/member/UserDAO.java#L17) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [getUserId](../src/teamproject/member/UserDAO.java#L22) | 객체가 보관한 UserId 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getName](../src/teamproject/member/UserDAO.java#L26) | 객체가 보관한 Name 값을 반환합니다. DB를 바꾸지 않습니다. |
| [login](../src/teamproject/member/UserDAO.java#L32) | 아이디·비밀번호가 맞으면 회원 객체, 아니면 null입니다. |

## task

담당 **윤*훈**

### TaskBoardPanel.java

[상태별 업무 분류·내 업무 필터](../src/teamproject/task/TaskBoardPanel.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [TaskBoardPanel](../src/teamproject/task/TaskBoardPanel.java#L20) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [clearOtherSelections](../src/teamproject/task/TaskBoardPanel.java#L63) | 선택하지 않은 상태 칸에서 선택을 해제합니다. |
| [reload](../src/teamproject/task/TaskBoardPanel.java#L71) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |
| [findSelectedCard](../src/teamproject/task/TaskBoardPanel.java#L89) | 세 상태 목록을 확인해 선택한 카드를 반환합니다. |
| [move](../src/teamproject/task/TaskBoardPanel.java#L99) | 선택 카드의 상태를 바꾸고 세 칸을 다시 채웁니다. |

### TaskCardRenderer.java

[업무 카드의 표시](../src/teamproject/task/TaskCardRenderer.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [TaskCardRenderer](../src/teamproject/task/TaskCardRenderer.java#L13) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [getListCellRendererComponent](../src/teamproject/task/TaskCardRenderer.java#L25) | DTO의 값을 라벨에 넣고 카드 패널을 반환합니다. |

### TaskDAO.java

[업무 SQL·보드 조회·메모 연결](../src/teamproject/task/TaskDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [showTasks](../src/teamproject/task/TaskDAO.java#L18) | 현재 팀 업무를 콘솔로 출력합니다. |
| [loadTasks](../src/teamproject/task/TaskDAO.java#L47) | 현재 팀 업무를 표 모델에 채웁니다. |
| [Assignee](../src/teamproject/task/TaskDAO.java#L95) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [getUserId](../src/teamproject/task/TaskDAO.java#L100) | 객체가 보관한 UserId 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getName](../src/teamproject/task/TaskDAO.java#L104) | 객체가 보관한 Name 값을 반환합니다. DB를 바꾸지 않습니다. |
| [toString](../src/teamproject/task/TaskDAO.java#L110) | 목록·선택창에 보이는 대표 문자열을 반환합니다. |
| [TaskInfo](../src/teamproject/task/TaskDAO.java#L125) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [getTaskId](../src/teamproject/task/TaskDAO.java#L140) | 객체가 보관한 TaskId 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getTitle](../src/teamproject/task/TaskDAO.java#L144) | 객체가 보관한 Title 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getDescription](../src/teamproject/task/TaskDAO.java#L148) | 객체가 보관한 Description 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getAssigneeId](../src/teamproject/task/TaskDAO.java#L152) | 객체가 보관한 AssigneeId 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getStatus](../src/teamproject/task/TaskDAO.java#L156) | 객체가 보관한 Status 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getDueDate](../src/teamproject/task/TaskDAO.java#L160) | 객체가 보관한 DueDate 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getTeamMembers](../src/teamproject/task/TaskDAO.java#L166) | 현재 팀의 담당자 선택 목록입니다. |
| [findTask](../src/teamproject/task/TaskDAO.java#L199) | 현재 팀의 업무 상세 한 건 또는 null입니다. |
| [insertTask](../src/teamproject/task/TaskDAO.java#L238) | 같은 팀 담당자인지 확인하고 업무를 만듭니다. |
| [updateTask](../src/teamproject/task/TaskDAO.java#L274) | 같은 팀의 업무를 수정하고 영향 행 여부를 반환합니다. |
| [updateTaskStatus](../src/teamproject/task/TaskDAO.java#L310) | 같은 팀 업무의 상태만 바꿉니다. |
| [deleteTask](../src/teamproject/task/TaskDAO.java#L324) | 같은 팀의 업무를 삭제합니다. |
| [requireTeamMember](../src/teamproject/task/TaskDAO.java#L335) | 업무 담당자가 현재 팀에 있는지 검사합니다. |
| [Card](../src/teamproject/task/TaskDAO.java#L353) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [toString](../src/teamproject/task/TaskDAO.java#L362) | 목록·선택창에 보이는 대표 문자열을 반환합니다. |
| [board](../src/teamproject/task/TaskDAO.java#L367) | 현재 팀·선택한 개인 필터로 카드 목록을 반환합니다. |
| [insertFromNote](../src/teamproject/task/TaskDAO.java#L393) | 메모 소속 확인→업무 생성 키→연결 저장→commit입니다. |
| [sourceNote](../src/teamproject/task/TaskDAO.java#L446) | 현재 팀 업무에 연결된 최신 원문 메모입니다. |

### TaskFrame.java

[기존 업무 CRUD·표·진행률 통합](../src/teamproject/task/TaskFrame.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [TaskFrame](../src/teamproject/task/TaskFrame.java#L50) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [isCellEditable](../src/teamproject/task/TaskFrame.java#L66) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [mouseClicked](../src/teamproject/task/TaskFrame.java#L79) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [fitColumns](../src/teamproject/task/TaskFrame.java#L146) | 열별 제목·행의 최대 표시 너비를 찾습니다. |
| [reload](../src/teamproject/task/TaskFrame.java#L174) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |
| [getSelectedTaskId](../src/teamproject/task/TaskFrame.java#L180) | 선택 행의 실제 업무 번호를 읽고 선택이 없으면 -1입니다. |
| [registerTask](../src/teamproject/task/TaskFrame.java#L194) | 입력→등록→표·진행률 갱신입니다. |
| [updateTask](../src/teamproject/task/TaskFrame.java#L225) | 같은 팀의 업무를 수정하고 영향 행 여부를 반환합니다. |
| [deleteTask](../src/teamproject/task/TaskFrame.java#L279) | 같은 팀의 업무를 삭제합니다. |
| [completeTask](../src/teamproject/task/TaskFrame.java#L313) | 선택 업무를 완료로 변경하고 갱신합니다. |
| [showTaskInputDialog](../src/teamproject/task/TaskFrame.java#L339) | 등록·수정 공통 입력창에서 날짜·필수 값을 검사합니다. |
| [addInputRow](../src/teamproject/task/TaskFrame.java#L452) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |

### TaskFromNoteDialog.java

[공유 메모에서 결정 업무 입력](../src/teamproject/task/TaskFromNoteDialog.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [open](../src/teamproject/task/TaskFromNoteDialog.java#L15) | 선택한 메모로 업무 입력창을 열고 업무·연결 저장을 호출합니다. |

## schedule

담당 **이*주**

### InlineProgressDAO.java

[진행률 계산용 데이터 조회](../src/teamproject/schedule/InlineProgressDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [load](../src/teamproject/schedule/InlineProgressDAO.java#L15) | 현재 팀의 사람·업무를 계산용 객체로 읽습니다. |

### InlineProgressData.java

[개수·퍼센트·날짜 계산](../src/teamproject/schedule/InlineProgressData.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [Person](../src/teamproject/schedule/InlineProgressData.java#L17) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [getUserId](../src/teamproject/schedule/InlineProgressData.java#L22) | 객체가 보관한 UserId 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getName](../src/teamproject/schedule/InlineProgressData.java#L26) | 객체가 보관한 Name 값을 반환합니다. DB를 바꾸지 않습니다. |
| [toString](../src/teamproject/schedule/InlineProgressData.java#L32) | 목록·선택창에 보이는 대표 문자열을 반환합니다. |
| [Task](../src/teamproject/schedule/InlineProgressData.java#L43) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [getAssigneeId](../src/teamproject/schedule/InlineProgressData.java#L49) | 객체가 보관한 AssigneeId 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getStatus](../src/teamproject/schedule/InlineProgressData.java#L53) | 객체가 보관한 Status 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getDueDate](../src/teamproject/schedule/InlineProgressData.java#L57) | 객체가 보관한 DueDate 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getPeople](../src/teamproject/schedule/InlineProgressData.java#L66) | 객체가 보관한 People 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getTasks](../src/teamproject/schedule/InlineProgressData.java#L70) | 객체가 보관한 Tasks 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getTotalCount](../src/teamproject/schedule/InlineProgressData.java#L76) | 전체 또는 지정한 개인의 업무 수를 셉니다. |
| [getCompletedCount](../src/teamproject/schedule/InlineProgressData.java#L87) | 개인 조건과 완료 상태가 일치하는 업무를 셉니다. |
| [getPercent](../src/teamproject/schedule/InlineProgressData.java#L100) | 0건 분기 후 완료/전체 비율을 반올림합니다. |
| [getDeadline](../src/teamproject/schedule/InlineProgressData.java#L112) | 완료·날짜 없음·과거·오늘·미래로 나누어 안내합니다. |

### InlineProgressPanel.java

[진행률·마감 안내 표시](../src/teamproject/schedule/InlineProgressPanel.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [attach](../src/teamproject/schedule/InlineProgressPanel.java#L45) | 진행률 패널을 기존 화면 위쪽에 붙여 반환합니다. |
| [InlineProgressPanel](../src/teamproject/schedule/InlineProgressPanel.java#L53) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [actionPerformed](../src/teamproject/schedule/InlineProgressPanel.java#L94) | 사용자 선택과 새로고침 이벤트를 구분합니다. |
| [reload](../src/teamproject/schedule/InlineProgressPanel.java#L114) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |
| [showProgress](../src/teamproject/schedule/InlineProgressPanel.java#L142) | 전체·개인 막대와 오늘/지연 개수를 표시합니다. |
| [setBar](../src/teamproject/schedule/InlineProgressPanel.java#L177) | 특정 사용자의 개수·퍼센트를 막대에 넣습니다. |
| [showDeadlines](../src/teamproject/schedule/InlineProgressPanel.java#L189) | 업무 표의 각 행에 마감 안내를 계산해 넣습니다. |

### ScheduleDAO.java

[일정 SQL·기간 검증](../src/teamproject/schedule/ScheduleDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [Schedule](../src/teamproject/schedule/ScheduleDAO.java#L20) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [list](../src/teamproject/schedule/ScheduleDAO.java#L29) | 현재 팀 목록을 SQL로 조회하여 DTO 목록을 만듭니다. |
| [Deadline](../src/teamproject/schedule/ScheduleDAO.java#L59) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [listDeadlines](../src/teamproject/schedule/ScheduleDAO.java#L68) | 현재 팀·표시 월의 업무 마감일을 원본 업무 표에서 읽습니다. |
| [save](../src/teamproject/schedule/ScheduleDAO.java#L96) | 입력·소속 검사 후 등록/수정하고 필요한 이력·활동을 함께 저장합니다. |
| [delete](../src/teamproject/schedule/ScheduleDAO.java#L137) | 현재 팀과 식별 조건에 맞는 행만 삭제합니다. |

### SchedulePanel.java

[월간 달력·일정 CRUD](../src/teamproject/schedule/SchedulePanel.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [isCellEditable](../src/teamproject/schedule/SchedulePanel.java#L26) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [SchedulePanel](../src/teamproject/schedule/SchedulePanel.java#L32) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [reload](../src/teamproject/schedule/SchedulePanel.java#L88) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |
| [drawCalendar](../src/teamproject/schedule/SchedulePanel.java#L103) | 월의 요일에 맞춰 날짜와 일정·업무 칸을 그립니다. |
| [fillScheduleTable](../src/teamproject/schedule/SchedulePanel.java#L130) | 별도 일정 목록을 표 모델에 채웁니다. |
| [dayText](../src/teamproject/schedule/SchedulePanel.java#L139) | 해당 날짜의 별도 일정과 업무 마감일을 구분해 표시할 글자를 만듭니다. |
| [selected](../src/teamproject/schedule/SchedulePanel.java#L162) | 선택 항목을 반환하고 없으면 안내합니다. |
| [edit](../src/teamproject/schedule/SchedulePanel.java#L171) | 입력창에서 값을 읽고 저장 실패 시 입력 수정 기회를 줍니다. |
| [delete](../src/teamproject/schedule/SchedulePanel.java#L207) | 현재 팀과 식별 조건에 맞는 행만 삭제합니다. |

## resource

담당 **신*준**

### ActivityPanel.java

[최근 활동 목록 화면](../src/teamproject/resource/ActivityPanel.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [ActivityPanel](../src/teamproject/resource/ActivityPanel.java#L16) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [reload](../src/teamproject/resource/ActivityPanel.java#L27) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |

### DashboardPanel.java

[기존 진행률·업무·자료 요약](../src/teamproject/resource/DashboardPanel.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [DashboardPanel](../src/teamproject/resource/DashboardPanel.java#L37) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [isCellEditable](../src/teamproject/resource/DashboardPanel.java#L76) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [isCellEditable](../src/teamproject/resource/DashboardPanel.java#L89) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [reload](../src/teamproject/resource/DashboardPanel.java#L107) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |

### IdeaCardRenderer.java

[아이디어 카드의 표시](../src/teamproject/resource/IdeaCardRenderer.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [IdeaCardRenderer](../src/teamproject/resource/IdeaCardRenderer.java#L14) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [getListCellRendererComponent](../src/teamproject/resource/IdeaCardRenderer.java#L26) | DTO의 값을 라벨에 넣고 카드 패널을 반환합니다. |

### IdeaDAO.java

[게시물·댓글·공감 SQL](../src/teamproject/resource/IdeaDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [Idea](../src/teamproject/resource/IdeaDAO.java#L17) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [toString](../src/teamproject/resource/IdeaDAO.java#L36) | 목록·선택창에 보이는 대표 문자열을 반환합니다. |
| [list](../src/teamproject/resource/IdeaDAO.java#L41) | 현재 팀 목록을 SQL로 조회하여 DTO 목록을 만듭니다. |
| [save](../src/teamproject/resource/IdeaDAO.java#L67) | 입력·소속 검사 후 등록/수정하고 필요한 이력·활동을 함께 저장합니다. |
| [delete](../src/teamproject/resource/IdeaDAO.java#L103) | 현재 팀과 식별 조건에 맞는 행만 삭제합니다. |
| [requireIdea](../src/teamproject/resource/IdeaDAO.java#L114) | 선택 게시물이 현재 팀 소속인지 검사합니다. |
| [comments](../src/teamproject/resource/IdeaDAO.java#L125) | 현재 팀 게시물의 댓글을 순서대로 읽습니다. |
| [comment](../src/teamproject/resource/IdeaDAO.java#L142) | 댓글 입력을 검사하고 댓글·활동을 함께 저장합니다. |
| [toggleLike](../src/teamproject/resource/IdeaDAO.java#L168) | 기존 공감 삭제, 없었다면 등록하는 토글입니다. |

### IdeaPanel.java

[아이디어 카드·댓글 입력·5초 갱신](../src/teamproject/resource/IdeaPanel.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [IdeaPanel](../src/teamproject/resource/IdeaPanel.java#L24) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [addNotify](../src/teamproject/resource/IdeaPanel.java#L87) | 패널이 화면에 붙으면 자동 갱신 타이머를 시작합니다. |
| [removeNotify](../src/teamproject/resource/IdeaPanel.java#L93) | 화면에서 떨어지면 타이머를 중지합니다. |
| [reload](../src/teamproject/resource/IdeaPanel.java#L98) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |
| [doInBackground](../src/teamproject/resource/IdeaPanel.java#L108) | 백그라운드에서 DB 연결·조회만 수행합니다. |
| [done](../src/teamproject/resource/IdeaPanel.java#L115) | 배경 작업의 결과를 받아 EDT에서 화면을 바꿉니다. |
| [updateList](../src/teamproject/resource/IdeaPanel.java#L129) | 현재 선택을 기억하고 최신 목록에서 같은 번호를 다시 선택합니다. |
| [selected](../src/teamproject/resource/IdeaPanel.java#L146) | 선택 항목을 반환하고 없으면 안내합니다. |
| [showDetail](../src/teamproject/resource/IdeaPanel.java#L152) | 선택 게시물과 댓글을 오른쪽에 표시합니다. |
| [edit](../src/teamproject/resource/IdeaPanel.java#L176) | 입력창에서 값을 읽고 저장 실패 시 입력 수정 기회를 줍니다. |
| [delete](../src/teamproject/resource/IdeaPanel.java#L213) | 현재 팀과 식별 조건에 맞는 행만 삭제합니다. |
| [like](../src/teamproject/resource/IdeaPanel.java#L227) | 선택 게시물 공감을 토글하고 목록을 갱신합니다. |
| [comment](../src/teamproject/resource/IdeaPanel.java#L238) | 댓글 입력을 검사하고 댓글·활동을 함께 저장합니다. |
| [openLink](../src/teamproject/resource/IdeaPanel.java#L251) | 허용된 참고 링크를 브라우저로 엽니다. |

### MainFrame.java

[9개 탭 생성·같은 사용자와 팀 전달](../src/teamproject/resource/MainFrame.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [MainFrame](../src/teamproject/resource/MainFrame.java#L34) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [addTaskTab](../src/teamproject/resource/MainFrame.java#L66) | 업무 화면을 만들어 탭에 붙입니다. |
| [addMemberTab](../src/teamproject/resource/MainFrame.java#L77) | 팀원 화면을 만들어 탭에 붙입니다. |
| [stateChanged](../src/teamproject/resource/MainFrame.java#L89) | 탭 선택이 바뀌면 해당 화면만 새로고침합니다. |

### MaterialDAO.java

[자료 링크 SQL·업로더 저장](../src/teamproject/resource/MaterialDAO.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [MaterialInfo](../src/teamproject/resource/MaterialDAO.java#L22) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [getMaterialId](../src/teamproject/resource/MaterialDAO.java#L31) | 객체가 보관한 MaterialId 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getTitle](../src/teamproject/resource/MaterialDAO.java#L35) | 객체가 보관한 Title 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getDescription](../src/teamproject/resource/MaterialDAO.java#L39) | 객체가 보관한 Description 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getUrl](../src/teamproject/resource/MaterialDAO.java#L43) | 객체가 보관한 Url 값을 반환합니다. DB를 바꾸지 않습니다. |
| [getCreatedAt](../src/teamproject/resource/MaterialDAO.java#L47) | 객체가 보관한 CreatedAt 값을 반환합니다. DB를 바꾸지 않습니다. |
| [loadMaterials](../src/teamproject/resource/MaterialDAO.java#L54) | 현재 팀 자료를 표에 넣고 이전 링크 컬럼도 호환합니다. |
| [getRecentMaterials](../src/teamproject/resource/MaterialDAO.java#L83) | 현재 팀 최신 자료를 제한 개수만 반환합니다. |
| [insertMaterial](../src/teamproject/resource/MaterialDAO.java#L113) | 로그인한 업로더와 링크를 현재 팀 자료로 저장합니다. |
| [findMaterial](../src/teamproject/resource/MaterialDAO.java#L140) | 현재 팀 자료 한 건 또는 null을 반환합니다. |
| [updateMaterial](../src/teamproject/resource/MaterialDAO.java#L162) | 같은 팀 자료의 제목·내용·링크를 바꿉니다. |
| [deleteMaterial](../src/teamproject/resource/MaterialDAO.java#L182) | 같은 팀 자료를 삭제합니다. |

### MaterialPanel.java

[자료 표·입력·브라우저 열기](../src/teamproject/resource/MaterialPanel.java)

| 메서드·생성자 | 처리 흐름·역할 |
|---|---|
| [MaterialPanel](../src/teamproject/resource/MaterialPanel.java#L32) | 필드와 화면 부품·이벤트를 초기화하는 생성자입니다. |
| [isCellEditable](../src/teamproject/resource/MaterialPanel.java#L43) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [mouseClicked](../src/teamproject/resource/MaterialPanel.java#L73) | 내부 데이터 객체의 생성 또는 화면 입력 처리를 담당합니다. 본문과 인수를 확인하세요. |
| [reload](../src/teamproject/resource/MaterialPanel.java#L83) | DB 결과를 다시 읽어 해당 화면 모델을 갱신합니다. |
| [getSelectedMaterialId](../src/teamproject/resource/MaterialPanel.java#L91) | 표의 실제 자료 번호를 읽습니다. |
| [registerMaterial](../src/teamproject/resource/MaterialPanel.java#L97) | 자료 입력창→DAO 등록→목록 갱신입니다. |
| [updateMaterial](../src/teamproject/resource/MaterialPanel.java#L111) | 같은 팀 자료의 제목·내용·링크를 바꿉니다. |
| [deleteMaterial](../src/teamproject/resource/MaterialPanel.java#L144) | 같은 팀 자료를 삭제합니다. |
| [openSelectedUrl](../src/teamproject/resource/MaterialPanel.java#L165) | 선택한 주소를 기본 브라우저로 엽니다. |
| [showMaterialDialog](../src/teamproject/resource/MaterialPanel.java#L194) | 자료 제목·주소 필수값과 길이를 검사합니다. |
