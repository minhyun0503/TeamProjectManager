# PROJECT_SYNC.md — 코드 ↔ 설명자료 연결 파일

> 이 파일은 **Claude Code(코딩)** 와 **Claude 채팅(설명·시각자료)** 이 함께 보는 공용 요약이다.
> 원본: GitHub 레포 루트 `TeamProjectManager/PROJECT_SYNC.md`
> 사본: Claude 프로젝트 "자바프로그래밍" 문서 `PROJECT_SYNC.md`
> 어디서 고치든 **버전을 +1** 하고, 다른 쪽에 최신본을 덮어쓴다. 번호가 높은 쪽이 최신이다.

| 항목 | 값 |
|---|---|
| 버전 | v2 |
| 마지막 수정 | 2026-11-26 |
| 마지막 수정한 곳 | CODE |

---

## 1. 프로젝트 개요
- 주제: 대학생 팀프로젝트 관리 프로그램 (자바프로그래밍 3조)
- 구조: Java Swing GUI → DAO(표마다 하나) → JDBC → 클라우드 MySQL(Aiven, SSL)
- 개발 환경: Eclipse 2023-12, Java 17, UTF-8 / JDBC 드라이버는 `lib/`에 포함
- 레포: minhyun0503/TeamProjectManager, 기본 브랜치 `main`
- **DB 접속 정보·비밀번호·학번은 이 파일에 쓰지 않는다.** (README와 같은 규칙)

## 2. 역할 분담
| 담당 | 패키지 | 범위 |
|---|---|---|
| 장*민 | common, collab | DB 설계·JDBC 연결·공통 시스템, 기능 통합, 협업 기능(칸반·일정·메모장·아이디어 보드·문서) |
| 이*찬 | login, member | 로그인, 팀원 관리 |
| 윤*훈 | task | 업무 관리 |
| 이*주 | progress | 일정 및 진행률 |
| 신*준 | material, dashboard | 자료 및 대시보드 |

## 3. DB 테이블 (DB: `team_project`, 9개)
| 테이블 | 용도 | 주요 컬럼 |
|---|---|---|
| users | 사용자 (로그인 아이디 = 학번) | user_id, login_id, password, name, student_no |
| teams | 팀(프로젝트) | team_id, team_name, description, leader_id |
| team_members | 팀 소속·역할 | team_id, user_id, role(기본 '조원') |
| tasks | 업무 | task_id, team_id, title, description, assignee_id → users, status('할 일'/'진행 중'/'완료'), due_date |
| schedules | 일정 | schedule_id, team_id, title, start_date, end_date, memo |
| materials | 자료(링크) | material_id, team_id, title, url, description, uploader_id(NULL 허용) |
| team_memo | 팀 공동 메모 (팀당 1개) | team_id, content, version(저장 충돌 확인용), updated_by |
| board_posts | 아이디어 보드 포스트잇 | post_id, team_id, user_id, content, color, likes |
| pages | 문서 페이지 | page_id, team_id, title, content, updated_by |

- 진행률은 별도 표 없이 tasks의 완료 수 / 전체 수로 계산.
- team_memo·board_posts·pages는 프로그램 시작 시 `CollabSchema`가 없으면 자동 생성.
- materials는 처음 설계(link_url, 필수 uploader_id)와 코드가 달라서 `CollabSchema`가 url 칸 추가·uploader_id NULL 허용·link_url → url 복사를 해 줌.

## 4. 클래스 목록
| 패키지 | 클래스 | 역할 | 담당 | 상태 |
|---|---|---|---|---|
| common | Main | 프로그램 시작, DB 비밀번호 입력 후 연결, 로그인 창 실행 | 장*민 | 완료 |
| common | MainFrame | 탭 9개 통합 화면, 탭 전환 시 다시 조회 | 장*민 | 완료 |
| common | DB_connection | 클라우드 MySQL 연결 | 장*민 | 완료 |
| common | LoginUser | 로그인한 사람 번호·이름 보관 (작성자 기록용) | 장*민 | 완료 |
| common | CollabSchema | 협업 표 자동 생성, 자료 표 보정 | 장*민 | 완료 |
| collab | KanbanDAO / KanbanPanel | 칸반 보드 (할 일/진행 중/완료, 단계 이동) | 장*민 | 완료 |
| collab | ScheduleDAO / SchedulePanel | 일정 CRUD, D-day 표시 | 장*민 | 완료 |
| collab | MemoDAO / MemoPanel | 실시간 팀 메모장 (3초 폴링, version으로 저장 충돌 처리) | 장*민 | 완료 |
| collab | BoardDAO / BoardPanel | 아이디어 보드 (포스트잇, 색, 좋아요, 5초 새로고침) | 장*민 | 완료 |
| collab | PageDAO / PagePanel | 문서 페이지 (목록 + 편집, 서식 선택) | 장*민 | 완료 |
| login | LoginFrame | 로그인 화면 → 성공 시 MainFrame | 이*찬 | 완료 |
| login | UserDAO | 로그인 확인, user_id 조회 | 이*찬 | 완료 |
| member | MemberDAO / MemberFrame | 팀원 조회·등록·수정·팀 소속 삭제 | 이*찬 | 완료 |
| task | TaskDAO / TaskFrame | 업무 CRUD·상태 변경, 열 너비 자동 조절(fitColumns) | 윤*훈 (열 너비: 장*민) | 완료 |
| progress | InlineProgressDAO / Data / Panel | 전체·개인 진행률, 마감 안내(D-N/오늘 마감/지연) | 이*주 | 완료 |
| material | MaterialDAO / MaterialPanel | 자료 CRUD, 링크 열기 | 신*준 | 완료 |
| dashboard | DashboardPanel | 진행률, 업무 목록, 최근 자료 5건 | 신*준 | 완료 |

## 5. 실행 흐름
1. `Main` — DB 비밀번호 입력 → `DB_connection`으로 연결 (연결 `conn` 하나를 모든 화면이 같이 씀)
2. `LoginFrame` — `UserDAO.login`으로 users 확인 → 성공하면 `LoginUser`에 번호·이름 저장
3. `MainFrame` — `CollabSchema`로 협업 표 준비 → 탭 9개 표시
   (대시보드 / 업무 관리 / 칸반 보드 / 일정 / 팀원 관리 / 자료 관리 / 팀 메모장 / 아이디어 보드 / 문서)
4. 각 탭: Panel(화면) → DAO(SQL, PreparedStatement) → MySQL 표
5. 실시간 기능은 몇 초마다 DB를 다시 확인하는 폴링 방식 (보고 있는 탭만)

## 6. 현재 진행 상황
- 완료: 위 4번 표의 모든 기능 (README "현재 구현 상태" 기준)
- 남은 한계:
  - 팀 번호가 1번으로 고정, 업무 목록에 팀별 조건 없음
  - 로그인 비밀번호 평문 저장 (암호화 필요)
  - 팀원 수정창에 학번 변경 칸 없음, 역할 바꿔도 teams.leader_id 안 바뀜
- 확인 필요: `claude/pensive-dirac-pwf0gf` 브랜치에 main에 없는 커밋 1개 ("시간을 한국시간으로 표시하고 새로고침 오류 수정")

---

## 7. 변경 요청 큐

### 7-1. 코드 → 설명자료 (Claude Code가 적고, 채팅이 반영 후 [x])
- [ ] 2026-11-26 · 발표자료(3조 최종본)는 DB 6개·기본 기능 기준 → 협업 기능 5개(칸반·일정·메모장·아이디어 보드·문서)와 테이블 9개, 탭 9개 구조 반영 필요
- [ ] 2026-11-26 · 역할 분담 슬라이드의 이*주/신*준 담당을 위 2번 표대로 맞추기

### 7-2. 설명자료 → 코드 (채팅이 적고, Claude Code가 반영 후 [x])
- (없음)

반영이 끝난 항목은 `[x]`로 바꾸고, 쌓이면 8번 변경 기록으로 옮긴다.

---

## 8. 변경 기록 (최신이 위)
| 버전 | 날짜 | 수정한 곳 | 내용 |
|---|---|---|---|
| v2 | 2026-11-26 | CODE | 레포 코드·README 기준으로 3~6번 채움, 7-1 큐 2건 추가 |
| v1 | 2026-11-26 | CHAT | 파일 최초 작성 |
