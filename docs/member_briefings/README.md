# 팀원별 소스 브리핑

각 파일을 실제로 열어 세부 동작을 설명하는 자료입니다. 짧은 6장 요약본과 별도로 사용합니다.

**추천 사용법:** 본인 HTML을 열면 실제 코드 전체와 설명이 함께 나옵니다. 파일 순서대로 이동하고 설명의 코드 강조 버튼으로 위치를 찾으세요. PDF와 Markdown은 같은 설명의 인쇄·저장소 열람용입니다.

| 담당 | 범위 | 코드와 설명 | 인쇄용 | 저장소에서 읽기 |
|---|---|---|---|---|
| 장*민 | 공통 실행·DB | [7개 파일](01_common.html) | [PDF](../../output/pdf/member_briefings/01_common.pdf) | [설명](01_common.md) |
| 이*찬 | 로그인·팀원·공유 메모 | [6개 파일](02_member.html) | [PDF](../../output/pdf/member_briefings/02_member.pdf) | [설명](02_member.md) |
| 윤*훈 | 업무 관리·상태별 보드 | [5개 파일](03_task.html) | [PDF](../../output/pdf/member_briefings/03_task.pdf) | [설명](03_task.md) |
| 이*주 | 진행률·일정 달력 | [5개 파일](04_schedule.html) | [PDF](../../output/pdf/member_briefings/04_schedule.pdf) | [설명](04_schedule.md) |
| 신*준 | 통합 화면·자료·아이디어 | [8개 파일](05_resource.html) | [PDF](../../output/pdf/member_briefings/05_resource.pdf) | [설명](05_resource.md) |

각 자료에는 원래 담당과 추가 담당, 파일별 변수·모듈·클래스·생성자·메서드, 조건·반복과 다음 호출, 시연 예와 질문이 들어 있습니다. 트랜잭션 등 어려운 용어는 사용하는 설명 옆에 뜻을 붙였습니다.

전체 Java 담당 파일 29개·선언 178개를 확인했습니다. 공통 담당에는 module-info.java와 schema.sql도 포함해 총 31개 파일, 190개 연속 코드 구간을 설명합니다. 단순 값 반환 메서드는 같은 자료형 안에서 묶어 설명합니다.

코드 기준: `1beec761ec6fac0d1a6b4eb4e19694740fb5e583`. 설명 범위·원본 파일 해시는 [coverage.json](coverage.json)에 기록합니다. 소스가 바뀌면 줄 번호와 설명도 함께 갱신해야 합니다.

## 재생성

표준 Python으로 `python tools/build_member_briefings.py`를 실행하면 HTML·Markdown을 만듭니다. `--check`는 파일·선언·설명 구간을 대조합니다. PDF는 reportlab과 NanumGothic.ttf·NanumGothic-Bold.ttf가 있는 폴더가 필요하며 `--font-dir 경로`로 지정합니다. 설명 원문은 content.json입니다. 프로그램 실행에는 이 생성 도구가 필요하지 않습니다.
