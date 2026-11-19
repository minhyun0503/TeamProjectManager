# 대학생 팀프로젝트 관리 프로그램 (자바프로그래밍 3조)

팀프로젝트의 업무 분담, 일정, 자료를 한곳에서 관리하는 Java Swing + JDBC + MySQL 데스크톱 프로그램입니다.

## 기술 스택
- Java (Swing GUI), JavaSE-25
- MySQL 8 (Aiven 무료 클라우드 DB, SSL 필수)
- JDBC (MySQL Connector/J)
- 구조: Java GUI → DAO(표마다 하나) → JDBC → 클라우드 MySQL

## 실행 방법
같은 클라우드 DB를 사용합니다. MySQL을 따로 설치할 필요는 없습니다. 기존 DB에는 아래 자료 링크 마이그레이션을 먼저 적용하고, `schema.sql` 전체를 다시 실행하지 마세요.

1. GitHub Desktop에서 이 레포를 Clone 합니다. (`File` → `Clone repository`)
2. Eclipse에서 프로젝트를 가져옵니다. (`File` → `Import` → `Existing Projects into Workspace` → 클론한 폴더 선택)
3. MySQL Connector/J(JDBC 드라이버)는 `lib` 폴더에 포함되어 있어서 따로 추가할 필요가 없습니다.
   - 빨간 오류가 보이면 프로젝트 우클릭 → `Refresh`(F5) 후 `Project` → `Clean`을 해보세요.
4. DB 관리자가 기존 DB에 `db/migrate_materials_url.sql`을 한 번 적용합니다. `url` 컬럼이 이미 있어도 재실행할 수 있습니다.
5. `src/teamproject/common/Main.java`를 실행하고, 비밀번호 창에 DB 비밀번호를 입력합니다.
6. 프로그램에 로그인합니다. 소속 팀이 하나면 바로 열리고, 여러 개면 관리할 팀을 선택합니다. 소속 팀이 없으면 안내 후 로그인 화면에 머뭅니다.
   - DB 비밀번호는 공개 저장소에 기록하지 말고 팀 내부의 안전한 경로로 공유합니다.

※ Aiven 무료 서비스는 오래 사용하지 않으면 자동으로 꺼질 수 있습니다. 연결이 안 되면 팀장에게 알려주세요. (발표 전 Running 상태 확인)

## 테스트 계정 (프로그램 로그인용)
| 이름 | 아이디(학번) | 비밀번호 |
|---|---|---|
| 장*민 | 비공개 | 비공개 |
| 이*찬 | 비공개 | 비공개 |
| 이*주 | 비공개 | 비공개 |
| 윤*훈 | 비공개 | 비공개 |
| 신*준 | 비공개 | 비공개 |

시연용 계정의 학번과 비밀번호는 공개하지 않습니다. 실제 로그인 정보는 팀 내부에서 별도로 안내합니다.
현재 로그인 비밀번호는 DB에 평문으로 저장되어 있어 추후 암호화가 필요합니다.
DB 비밀번호(접속용)와 위 로그인 비밀번호는 서로 다른 것입니다.

## 데이터베이스 구조 (`team_project`)
| 표 | 설명 |
|---|---|
| `users` | 사용자 (로그인 아이디 = 학번) |
| `teams` | 팀(프로젝트) |
| `team_members` | 팀 소속과 역할 |
| `tasks` | 업무 (담당자 `assignee_id` → `users`, 마감일, 상태) |
| `schedules` | 일정 |
| `materials` | 자료(링크) |

- 진행률은 별도 표 없이 tasks의 완료 개수 / 전체 개수로 계산합니다.
- `db/schema.sql`은 DB 구조 참고용입니다. (클라우드에 이미 반영되어 있으니 다시 실행하지 마세요.)

## 파일 구조와 담당 패키지

조원별 담당 기능을 기준으로 5개 패키지로 나눴습니다. 각 패키지에는 해당 기능의 화면과 DAO를 함께 둡니다.

| 담당 | 패키지 | 파일 |
|---|---|---|
| 장*민 — DB 및 공통 시스템 | `teamproject.common` | `Main.java`, `DB_connection.java`, `TeamDAO.java` |
| 이*찬 — 로그인 및 팀원 관리 | `teamproject.member` | `LoginFrame.java`, `UserDAO.java`, `MemberDAO.java`, `MemberFrame.java` |
| 윤*훈 — 업무 관리 | `teamproject.task` | `TaskDAO.java`, `TaskFrame.java` |
| 이*주 — 일정 및 진행률 | `teamproject.schedule` | `InlineProgressDAO.java`, `InlineProgressData.java`, `InlineProgressPanel.java` |
| 신*준 — 자료 및 대시보드 | `teamproject.resource` | `MainFrame.java`, `DashboardPanel.java`, `MaterialDAO.java`, `MaterialPanel.java` |

- 자바 파일은 `src/teamproject/` 아래의 담당 패키지 폴더에 있습니다. 파일 맨 위의 `package` 선언은 폴더 이름과 일치해야 합니다.
- `MainFrame`은 자료·대시보드 원본에 포함된 통합 화면이므로 같은 담당자의 `resource` 패키지에 둡니다.
- DB 설계 참고 파일은 `db/schema.sql`, JDBC 드라이버는 `lib/mysql-connector-j-26.10.0.jar`, 모듈 설정은 `src/module-info.java`에 있습니다.
- 다른 담당자의 클래스를 사용할 때는 `import`를 추가합니다. 예: 업무 화면에서 진행률 패널을 사용하려면 `import teamproject.schedule.InlineProgressPanel;`을 씁니다.
- `schedule` 패키지는 현재 진행률·마감 안내를 담당합니다. 일정 등록·수정·삭제는 아직 구현되지 않았습니다.

시작 클래스는 **`teamproject.common.Main`**입니다. Eclipse에서 `src/teamproject/common/Main.java`를 우클릭한 뒤 `Run As` → `Java Application`으로 실행합니다.

## 로그인 사용법 (화면 담당자용)
```java
import teamproject.member.UserDAO;

// 아이디/비밀번호가 맞으면 회원 번호와 이름, 틀리면 null 반환
UserDAO.User user = UserDAO.login(conn, loginId, password);
if (user != null) {
    int userId = user.getUserId();
    String name = user.getName();
}
```

## 역할 분담
| 이름 | 담당 |
|---|---|
| 장*민 | DB 설계·JDBC 연결·공통 시스템, 기능 통합, 업무 표 열 너비 자동 조절 |
| 이*찬 | 로그인 및 팀원 관리 |
| 윤*훈 | 업무 관리 |
| 이*주 | 일정 및 진행률 |
| 신*준 | 자료 및 대시보드 |

## 현재 구현 상태
- [x] DB 표 6개 설계 및 클라우드(Aiven) 반영
- [x] 클라우드 MySQL 연결과 로그인
- [x] 팀원 조회·등록·수정·팀 소속 삭제
- [x] 업무 조회·등록·수정·삭제·완료 처리
- [x] 팀 전체·개인별 진행률 표시
- [x] 업무별 마감 안내와 오늘 마감·지연 개수 표시
- [x] 표 데이터 변경 시 열 너비 자동 조절
- [ ] 일정 등록·수정·삭제
- [x] 자료 관리 및 대시보드 코드 통합
- [x] 로그인 사용자 소속 팀 조회·선택 및 모든 화면에 동일한 팀 번호 전달
- [x] 업무·자료의 조회·수정·삭제 시 팀별 조건 적용
- [x] 로그인한 사용자를 자료 업로더로 저장
- [x] 기존 자료 링크를 보존하는 DB 변경 스크립트 준비 (클라우드 적용·확인은 별도 필요)

## 추가된 기능
### 업무 관리 — 윤*훈
- `TaskDAO`: `insertTask`, `updateTask`, `deleteTask`, `updateTaskStatus`, `findTask`, `getTeamMembers` 추가.
- 업무 화면 아래의 등록·수정·삭제·완료 처리 버튼으로 작업합니다.
- 등록·수정 항목: 업무명, 설명, 담당자 이름, 마감일(`yyyy-MM-dd`), 상태(할 일 / 진행 중 / 완료).
- 업무명은 필수이며 100자 이하입니다. 마감일은 필수이고 날짜 형식을 검사합니다.
- 새 업무는 오늘을 포함한 이후 날짜만 마감일로 지정할 수 있습니다. 수정 시에는 과거 날짜도 허용합니다.
- 삭제 전 확인창을 표시하며, SQL 실행은 `PreparedStatement`를 사용합니다.

### 진행률·마감 안내 — 이*주
- `InlineProgressDAO`, `InlineProgressData`, `InlineProgressPanel`로 구성됩니다.
- 진행률은 완료 업무 수 / 전체 업무 수로 계산하며, 업무가 없으면 0%입니다.
- 화면 위쪽에 팀 전체·선택한 개인의 진행률을 표시합니다.
- 업무 표의 마감 안내: D-N / 오늘 마감 / 지연 N일 / 완료.
- 완료되지 않은 업무 중 오늘 마감인 개수와 지연된 개수를 표시합니다.
- 업무 등록·수정·삭제·완료 처리 후 표와 진행률·마감 안내를 함께 갱신합니다. 다른 PC의 변경은 **새로고침** 버튼으로 확인합니다.
- 진행률 기능은 DB를 조회만 하며 데이터를 변경하지 않습니다.

### 기능 통합·열 너비 자동 조절 — 장*민
- `TaskFrame`에 진행률 패널 연결과 열 너비 기능을 추가했습니다. 업무 표는 로그인 후 선택한 팀을 기준으로 조회합니다.
- `InlineProgressPanel.attach(this, conn, tableModel, teamId)`로 진행률 패널을 연결합니다.
- `fitColumns()`는 각 열의 제목과 모든 행에서 가장 넓은 내용의 너비에 여백 20px을 더해 선호 너비를 설정합니다.
- 첫 화면 표시와 표 데이터 변경 시 열 너비를 다시 계산합니다. 변경 감지는 `TableModelListener`, 화면 갱신 예약은 `SwingUtilities.invokeLater`를 사용합니다.

### 자료 관리·대시보드 — 신*준
- 자료·대시보드의 기존 화면 구성을 유지하면서 팀 선택과 로그인 사용자 정보 전달을 연결했습니다.
- `MainFrame`에서 대시보드 / 업무 관리 / 팀원 관리 / 자료 관리 탭을 제공합니다.
- 대시보드는 전체 진행률, 오늘 마감·지연 업무 수, 팀 업무 목록, 최근 자료 5건을 표시합니다. 업무 표는 마감일순으로 선택한 팀의 전체 업무를 표시하며 임박 업무만 걸러내지는 않습니다.
- 자료 등록·수정·삭제와 링크 열기를 지원하며, 자료 표를 더블클릭해도 링크가 열립니다.
- 대시보드와 자료 관리 탭으로 이동하면 해당 화면을 다시 조회합니다.
- 로그인 후 소속 팀을 선택하여 `MainFrame`으로 이동합니다. 업무·팀원·자료·대시보드가 같은 팀 번호를 사용하며 탭 전환 시 해당 목록을 갱신합니다.
- 자료 링크는 `url`을 우선 읽고, 값이 비어 있으면 이전 `link_url`을 읽습니다. 자료 등록 시 로그인한 회원 번호를 필수 `uploader_id`에 저장합니다.
- 조회 시 테이블을 자동 생성·변경하던 코드는 제거했습니다. DB 변경은 아래 스크립트로 명시적으로 적용합니다.

## 기존 DB의 자료 링크 변경
- `db/migrate_materials_url.sql`은 `url VARCHAR(500)` 컬럼이 없으면 추가하고, 새 링크가 비어 있는 자료에 한해 `link_url`을 복사합니다.
- `link_url`과 기존 자료·업로더·외래키는 유지하며, 이미 저장된 새 `url`은 덮어쓰지 않습니다. `uploader_id`의 필수 조건도 유지합니다.
- 기존 DB에 `db/schema.sql` 전체를 다시 실행하지 마세요. 이 파일은 새 DB를 만들 때 사용할 구조·가상 샘플 참고 자료입니다.
- 실제 클라우드 DB의 스크립트 적용과 실행 확인은 별도로 필요합니다.


## 현재 사용 범위
- 로그인 → 통합 화면의 각 탭으로 이동합니다. 업무 표 더블클릭으로 별도 팀원 관리 화면을 여는 기존 동작도 유지합니다.
- 로그인 사용자의 소속 팀을 `TeamDAO.findTeams(conn, userId)`로 조회합니다. 팀을 선택하면 업무·팀원·진행률·자료·대시보드가 모두 그 팀을 사용합니다.
- 업무와 자료의 목록·상세 조회·수정·삭제에 팀 번호 조건을 적용합니다. 업무 담당자와 자료 업로더는 해당 팀 소속인지 확인합니다.
- 팀원 삭제는 팀 소속만 제거하며 사용자 계정은 유지합니다.
- 팀원 수정창에서 이름·학번·역할을 변경할 수 있습니다. 별도 팀원 창을 닫아도 프로그램 전체가 종료되지 않습니다.
- 팀 생성·기존 계정 초대와 로그인 중 팀 전환은 아직 제공하지 않습니다. 다른 팀을 선택하려면 프로그램에 다시 로그인합니다.
- 팀원 역할을 바꿔도 `teams.leader_id`는 자동 변경되지 않습니다.

## 참고
- 실행 흐름: `Main`(DB 연결) → `LoginFrame`(로그인 및 소속 팀 선택) → `MainFrame`(선택한 팀의 통합 화면)
- DB 연결(`conn`)은 `Main`에서 만들어 화면끼리 넘겨 가며 하나만 사용합니다.
- 새 화면에서 DB가 필요하면 생성자에서 `Connection conn`을 전달받아 사용하세요.


## 검증
- Java 25 소스/타깃 전체 컴파일을 확인했습니다.
- `tests/RegressionTest.java`는 H2의 MySQL 호환 모드에서 로그인·소속 팀, 팀별 업무·자료 분리, 팀원 CRUD·등록 실패 시 롤백, 진행률, 업로더 저장, 기존 링크 보존을 검증합니다.
- 실행 방법과 범위는 `tests/README.md`를 참고하세요. 이 테스트는 실제 MySQL 서버나 GUI 조작 검증을 대신하지 않습니다.
