# Codex 인수인계

## 작업 기준과 시작 방법

저장소 `minhyun0503/TeamProjectManager`의 **`feature/feedback-learning-version`** 브랜치를 체크아웃한다. 인수인계 작성 시점의 소스·브리핑 기준 HEAD는 `67bb1e3904f2969239150b7515955adec3c3046f`다. 자바 기초 수준 코드 개선은 `347d21c`, 업무 마감일 달력 반영은 `1beec76`에 있다. 기존 검토는 [PR #2](https://github.com/minhyun0503/TeamProjectManager/pull/2)에 있다.

루트에서 JDK 17을 준비하고 `sh test.sh`로 빌드 및 회귀 검사를 실행한다. 화면이 있으면 `sh run.sh`로 시작해 로컬 데모를 선택한다. 데모 로그인은 `demo1`~`demo5` / `demo1234`다. `demo1`과 `demo2`의 팀 7·42 전환으로 팀 구분을 확인할 수 있다. 데모 DB는 사용자 홈의 `.teamproject-feedback/demo.mv.db`에 저장된다. [실행·Eclipse 설정](README.md)

## 확정한 요구사항과 구현 현황

| 요구사항 | 현재 상태 |
|---|---|
| 대학생 팀플에서 업무 분담·마감·진행률·자료를 한곳에서 관리 | 로그인·팀 선택 뒤 9개 탭. 팀원 정보·역할, 업무·담당자·마감·상태, 개인·전체 진행률, 자료 링크, 대시보드 구현 |
| 교수 피드백의 실시간 메모장 | 공유 메모 CRUD, 3초 폴링(주기적 재조회), 버전 충돌 안내, 내용 이력. 글자 단위 동시 편집은 범위 밖 |
| 노션 참고 | 상태별 업무 보드, 내 업무 필터, 마감일 정렬, 메모에서 업무 만들기와 원문 연결 |
| 패들렛 참고 | 아이디어 카드·링크·댓글·공감/취소, 5초 폴링 및 관련 활동 표시 |
| 업무 마감일 달력 표시 | 별도 일정은 기간 전체, 등록 업무는 마감일 하루에 제목·상태·담당자 표시. 업무 변경·삭제 후 달력 재진입 또는 새로고침으로 반영 |
| 기존 코드·구조 보존 | 원본 브랜치에서 검토 가능. 작업 브랜치는 단일 Eclipse 프로젝트. 선택 팀·로그인 회원 번호로 조회·저장하고 고정 번호 1 문제 수정 |
| 인터페이스까지 배운 학생의 코드 브리핑 | 핵심 자바 파일 7개 가독성 개선. 조원별 HTML·PDF·Markdown 5세트, 6장 요약본, 실제 줄 번호·예제 값·용어 뜻·예상 질문 제공 |

프로그램 흐름은 `Main → DB 준비 → LoginFrame → 팀 선택 → MainFrame → 각 화면 → DAO → DB → 화면 갱신`이다. 스키마는 기존 계정·팀·업무·자료 구조를 바탕으로 확장했다. 로컬 기준은 `src/teamproject/common/schema.sql`, 새 MySQL DB 초기화는 `db/mysql_demo.sql`이다. 이전 자료 링크 열을 읽는 호환 처리가 있다. `CREATE TABLE IF NOT EXISTS`로는 기존 DB 구조가 자동 변환되지 않으므로, 원본 DB를 대상으로 한 마이그레이션을 실행한 상태로 간주하면 안 된다.

## 조원별 담당과 과제 조건

| 조원 | 패키지와 주요 코드 | 브리핑 |
|---|---|---|
| 장*민 | `common`의 Main, DB_connection, SchemaSetup, TeamDAO, ActivityDAO와 모듈·스키마 | [공통·DB](docs/member_briefings/01_common.md) |
| 이*찬 | `member`의 로그인·팀원 화면과 DAO, NotePanel, NoteDAO | [로그인·팀원·메모](docs/member_briefings/02_member.md) |
| 윤*훈 | `task`의 TaskFrame, TaskDAO, 업무 보드·카드·메모에서 업무 등록 | [업무](docs/member_briefings/03_task.md) |
| 이*주 | `schedule`의 InlineProgress 계열 및 ScheduleDAO·SchedulePanel | [진행률·일정](docs/member_briefings/04_schedule.md) |
| 신*준 | `resource`의 MainFrame·대시보드, 자료, 아이디어·댓글·공감, 최근 활동 화면 | [통합·자료·협업](docs/member_briefings/05_resource.md) |

자바프로그래밍 3조 발표자료 기준 기술은 Java GUI·JDBC·SQL·MySQL이다. 발표에서는 각 조원이 **본인 소스파일을 열고 변수·생성자·메서드, 이벤트 시점, 조건과 반복, DB 또는 화면 결과, 다음 호출**을 설명한다. 모듈·클래스 이름은 원래 표기를 사용하고 트랜잭션 등 어려운 용어는 코드 위치에서 풀어 쓴다. 원래 PPT 역할과 추가 기능의 발표 책임은 [파일별 담당 표](docs/ownership.json)와 [코드 색인](docs/07_code_index.md)에 있다. 상기 발표자료에서 확인되지 않은 제출 형식·일정·채점 조건은 미확인이다.

## 미해결·미검증·제안

- **재현된 미해결 오류**: 현재 회귀 검사에서 확인된 것은 없다. 종전의 팀/회원 번호 고정, 달력 마감일 누락, 느린 조회 중 선택 되돌림, 탈퇴 담당자 임의 교체는 수정됐다.
- **실환경 검증**: Java 17에서 H2 회귀 65개와 시작·로그인·팀 선택, 9개 탭, 자동 갱신·선택 유지 화면 검사를 통과했다. 실제 MySQL 서버와 서로 다른 두 PC에서의 연동은 미검증이다. MySQL 테스트가 필요하면 기존 데이터와 분리된 새 DB에서 준비하고 로그인·양쪽 갱신·버전 충돌을 확인한다.
- **발표자료와 구현 차이**: PPT의 팀 생성·초대는 미구현이다. 현재는 만들어진 팀을 선택하고 팀원을 관리한다. 자료는 링크 저장이며 파일 업로드는 미구현이다. PPT의 마감 알림·통계 확장·모바일은 향후 항목이다.
- **추가 기능 후보**: 회의 가능 시간 투표 → 확정 일정 달력 등록이 우선 추천이었다. 발표·제출 체크리스트, 조원별 활동·결과물 링크, 달력 업무 상세·내 업무 필터, 메모 검색, 아이디어에서 업무 만들기도 제안했다. 기능 추가가 결정되기 전에는 현재 구현으로 표시하지 않는다.
- **참고 사례**: [Framadate](https://docs.framasoft.org/en/framadate/prise-en-main.html)의 시간 후보 투표, [GitHub Milestones](https://docs.github.com/en/issues/using-labels-and-milestones-to-track-work/about-milestones)의 단계별 업무 묶음, [Vikunja](https://vikunja.io/help/tasks/)의 체크리스트, [TEAMMATES](https://github.com/TEAMMATES/teammates)의 교육용 피드백.

## 함께 보존한 자료

[개인별 브리핑](docs/member_briefings/README.md) · [6장 요약 PDF](output/pdf/team_project_briefing.pdf) · [전체 흐름](docs/01_whole_flow.md) · [시연과 질문](docs/08_demo_and_questions.md) · [코드 점검](docs/code_review.md)

원본 PPT는 사용자 제공 과제 자료다. 이 문서에는 주제·기술·역할·기능 조건을 옮겼으며 학번이 들어 있는 PPT 원본은 공개 저장소에 추가하지 않았다.
