# DAO 회귀 검증

실제 클라우드 DB에 접속하지 않고 H2의 MySQL 호환 모드를 사용합니다. `db/schema.sql`의 6개 CREATE TABLE 문을 읽고, 팀 번호 **7·42**로 가상 데이터를 구성합니다. 코드의 사용자·팀 ID 전달과 팀별 데이터 분리를 검증합니다.

검증 대상:
- 로그인 사용자 ID 보존, 잘못된 비밀번호·SQL 문자열 입력, 소속 팀 0·1·여러 개.
- 팀원 조회·등록·수정·소속 삭제, 다른 팀 사용자 수정 방지, 등록 실패 시 롤백.
- 업무 조회·상세·수정·삭제·완료 처리의 팀별 분리, 다른 팀 담당자 지정 방지.
- 팀 전체·개인 진행률 및 마감 안내.
- 자료 업로더 ID 저장, 팀별 자료 CRUD·최근 자료, 기존 링크 조회 및 반복 복사 시 새 URL 보존.

## 실행

먼저 JDK 25 및 H2 2.4.240 JAR를 준비합니다. H2는 테스트 전용이며 프로그램 실행에는 필요하지 않습니다. H2 JAR는 [Maven Central](https://repo.maven.apache.org/maven2/com/h2database/h2/2.4.240/h2-2.4.240.jar)에서 받을 수 있습니다.

Eclipse에서 프로젝트를 빌드하여 `bin` 폴더를 생성한 뒤 저장소 루트에서 실행합니다.

Windows (H2 JAR 경로는 받은 위치로 변경):

```bat
javac -encoding UTF-8 -cp bin -d test-bin tests/RegressionTest.java
java -Djava.awt.headless=true -cp "bin;test-bin;C:\path\h2-2.4.240.jar" RegressionTest
```

macOS/Linux:

```sh
javac -encoding UTF-8 -cp bin -d test-bin tests/RegressionTest.java
java -Djava.awt.headless=true -cp "bin:test-bin:/path/h2-2.4.240.jar" RegressionTest
```

성공하면 `PASS: 48 regression checks`가 출력됩니다. GUI 동작, 실제 MySQL 연결, 마이그레이션의 MySQL 전용 PREPARE/metadata 분기는 별도로 검증해야 합니다. 테스트는 마이그레이션 중 링크 복사 SQL과 신규 스키마 및 DAO 동작을 검증합니다.
