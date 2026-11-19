"""팀원별 브리핑 HTML/Markdown/PDF 생성. 사용법은 docs/member_briefings/README.md 참고."""
from pathlib import Path
import argparse, hashlib, html, json, re, textwrap

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'docs/member_briefings'
PDF_DIR = ROOT / 'output/pdf/member_briefings'
DATA = json.loads((OUT / 'content.json').read_text(encoding='utf-8'))
OWNERS = json.loads((ROOT / 'docs/ownership.json').read_text(encoding='utf-8'))
SOURCE_COMMIT = DATA['source_commit']
REPO = 'https://github.com/minhyun0503/TeamProjectManager'

CLASS_DEFS = [
 ('JFrame', '독립된 프로그램 창을 만듭니다.'),
 ('JPanel', '창 안에서 관련 화면 요소를 묶는 영역입니다.'),
 ('JLabel', '안내 문구나 제목을 보여 주는 글자 표시입니다.'),
 ('JButton', '눌렀을 때 연결된 동작을 실행하는 버튼입니다.'),
 ('JPasswordField', '입력 글자가 화면에서 가려지는 비밀번호 칸입니다.'),
 ('JSplitPane', '두 화면 영역 사이의 크기를 조절하게 합니다.'),
 ('JCheckBox', '선택 여부를 켜거나 끄는 확인칸입니다.'),
 ('JList', '항목을 세로로 표시하고 선택하는 목록입니다.'),
 ('DefaultListModel', 'JList가 보여 줄 항목 자료를 보관합니다.'),
 ('JTable', '행과 열로 자료를 보여 주는 표입니다.'),
 ('DefaultTableModel', 'JTable이 표시할 열 이름과 행 자료를 보관합니다.'),
 ('JTextField', '한 줄 입력칸입니다.'),
 ('JTextArea', '여러 줄의 입력·표시 영역입니다.'),
 ('JComboBox', '목록을 펼쳐 하나를 고르는 선택칸입니다.'),
 ('JProgressBar', '진행 수치를 막대와 글자로 표시합니다.'),
 ('JScrollPane', '긴 표·목록·본문에 스크롤을 붙입니다.'),
 ('JOptionPane', '입력·확인·안내창을 간단히 만듭니다.'),
 ('JTabbedPane', '여러 화면 중 하나를 탭으로 선택합니다.'),
 ('Connection', '열린 DB 연결을 나타냅니다.'),
 ('PreparedStatement', 'SQL과 입력값을 따로 전달해 조회·변경합니다.'),
 ('ResultSet', 'DB 조회 결과입니다. next()로 다음 결과 행에 이동합니다.'),
 ('LocalDate', '연·월·일로 된 하루를 나타냅니다.'),
 ('YearMonth', '연·월과 그 달의 첫날·말일을 계산합니다.'),
 ('Timer', '정해진 간격마다 실행할 화면 동작을 요청합니다.'),
 ('SwingWorker', 'DB 배경 작업과 완료 후 화면 처리를 나눕니다.'),
]
BASICS = [
 '소스는 위에서 아래로 읽되 실행은 호출과 이벤트를 따릅니다. 생성자는 new로 객체를 만들 때, 버튼에 연결한 코드는 클릭할 때 실행됩니다.',
 'private은 클래스 내부에서 사용, public은 다른 클래스에서도 호출, static은 객체 생성 없이 클래스 이름으로 호출, final 필드는 대입 후 다시 다른 값을 넣지 않는다는 뜻입니다.',
 'this는 현재 객체, null은 가리키는 객체가 없음입니다. <자료형>은 목록 등에 넣을 값의 종류, 조건 ? A : B는 참일 때 A·거짓일 때 B를 고르는 문법입니다.',
 'e -> 동작은 이벤트가 생겼을 때 실행할 짧은 함수를 연결하는 문법입니다. return은 현재 메서드를 끝내고, throw는 오류를 호출한 곳으로 전달합니다.',
]
ESC = lambda s: html.escape(str(s), quote=True)

def fid(f): return re.sub(r'[^A-Za-z0-9_-]', '-', f['name'])
def methods(f, b=None):
    found = next((x['methods'] for x in OWNERS if x['file']==f['path']), [])
    if b: found=[m for m in found if b['start']<=m['line']<=b['end']]
    return found

def url(f, line=1): return f'{REPO}/blob/{SOURCE_COMMIT}/{f["path"]}#L{line}'
def term_notes(text, seen):
    found=[]
    for term in sorted(DATA['glossary'], key=len, reverse=True):
        if term not in seen and term in text:
            # LEFT JOIN을 설명할 때 짧은 JOIN 정의까지 겹치지 않게 합니다.
            if term=='JOIN' and any(t=='LEFT JOIN' for t,_ in found): continue
            found.append((term,DATA['glossary'][term])); seen.add(term)
    return found

def validate():
    expected={x['file'] for x in OWNERS}
    actual={f['path'] for f in DATA['files']}
    assert expected <= actual, f'담당 파일 설명 누락: {expected-actual}'
    assert len(actual)==len(DATA['files']), '중복 파일'
    report=[]
    for f in DATA['files']:
        path=ROOT/f['path']; lines=path.read_text(encoding='utf-8').splitlines()
        assert f['blocks'][0]['start']==1
        end=0
        for b in f['blocks']:
            assert b['start']==end+1 and b['start']<=b['end']<=len(lines), (f['name'],b)
            assert b['steps'] and all(len(t)>15 for t in b['steps'])
            end=b['end']
        assert end==len(lines),f'설명 범위 누락 {path}'
        if f['path'] in expected:
            pattern=re.compile(r'(?m)^\s*(?:public|private|protected)\s+(?:(?:static|final)\s+)*(?:(?:[\w.$]+(?:<[^;\n{}]+>)?(?:\[\])?)\s+)?(\w+)\s*\(')
            source='\n'.join(lines)
            declared=[]
            for match in pattern.finditer(source):
                line=source.count('\n',0,match.start())+1
                while line<=len(lines) and not lines[line-1].strip():line+=1
                declared.append((match.group(1),line))
            assert declared==[(m['method'],m['line']) for m in methods(f)], f'선언 색인이 소스와 다름: {f["name"]}'
        for m in methods(f):
            assert sum(b['start']<=m['line']<=b['end'] for b in f['blocks'])==1
            line=lines[m['line']-1].strip()
            assert re.search(r'\b'+re.escape(m['method'])+r'\s*\(',line), (f['name'],m,line)
        report.append(dict(file=f['path'],owner=next(r['owner'] for r in DATA['roles'] if r['key']==f['package']),
            sha256=hashlib.sha256(path.read_bytes()).hexdigest(),lines=len(lines),
            sections=len(f['blocks']),declarations=len(methods(f))))
    (OUT/'coverage.json').write_text(json.dumps(dict(source_commit=SOURCE_COMMIT,files=report,
        total_java_files=len(OWNERS),total_declarations=sum(len(x['methods']) for x in OWNERS),
        total_sections=sum(len(f['blocks']) for f in DATA['files'])),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    return report

CSS = '''*{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:#f4f6fa;color:#17263d;font:16px/1.8 system-ui,-apple-system,"Malgun Gothic",sans-serif}a{color:#1a5eb2}aside.menu{position:fixed;inset:0 auto 0 0;width:230px;overflow:auto;padding:25px 18px;background:#132740;color:#fff}aside.menu h2{font-size:22px;margin:0}aside.menu a{display:block;padding:7px 0;color:#c5dbf6;font-size:14px;text-decoration:none}aside.menu .small{color:#b2c3d9;font-size:12px}main{margin-left:230px;padding:28px;max-width:1660px}header,.guide,.file,.qa{background:#fff;padding:25px;margin-bottom:28px;border:1px solid #d9e2ed;border-radius:12px}header{border-top:5px solid #2268b4}h1{font-size:29px;line-height:1.4;margin:4px 0 14px}h2{font-size:23px}h3{font-size:19px}h4{font-size:17px;margin:0 0 8px}p{margin:9px 0}table{border-collapse:collapse;width:100%;margin:14px 0;font-size:14px}td,th{border:1px solid #dce4ed;text-align:left;padding:8px;vertical-align:top}th{background:#edf3fa}button,.pill{border:1px solid #b8cde7;background:#edf4ff;color:#174f89;padding:7px 11px;border-radius:6px;font:inherit;font-size:13px;cursor:pointer}.small{font-size:13px;color:#536b87}.callout{padding:14px;border-left:4px solid #3479c2;background:#eef5ff}.layout{display:grid;grid-template-columns:minmax(0,1fr) minmax(0,1.08fr);gap:22px;align-items:start}.source-pane{position:sticky;top:15px;min-width:0;background:#12223a;color:#ecf2fc;border-radius:9px;overflow:hidden}.source-pane summary{padding:10px 14px;cursor:pointer;background:#1b3554;font-size:14px}.source-scroll{height:72vh;overflow:auto;margin:0;padding:14px 0;font:12px/1.7 Consolas,"D2Coding",monospace;tab-size:2;scroll-behavior:auto}.code-line{display:block;min-height:1.7em;padding:0 15px 0 0;white-space:pre;width:max-content;min-width:100%}.number{display:inline-block;width:4em;padding-right:1em;text-align:right;color:#8eaccb;user-select:none}.code-line.hit{background:#244e75;box-shadow:inset 3px 0 #f8d56b}.walk{min-width:0}.block{border:1px solid #dae4ef;border-radius:8px;margin-bottom:17px;padding:16px;background:#fbfcff;scroll-margin-top:15px}.block .when{font-size:13px;color:#316493;margin:6px 0 10px}.block ol{margin:5px 0;padding-left:22px}.block li{padding:3px 0}.terms{background:#eaf3f7;border-left:3px solid #77aebf;font-size:13px;padding:8px 11px;margin-top:10px}.decls{font-size:12px;color:#5a6c81;overflow-wrap:anywhere}.flow svg{width:100%;max-height:245px}.file{scroll-margin-top:10px}.file-head{border-bottom:1px solid #dce5f0;margin-bottom:20px;padding-bottom:16px}.file-head code{overflow-wrap:anywhere;font-size:13px}.file-flow{background:#eef3f8;padding:9px;font-size:14px;overflow-wrap:anywhere}.footer{font-size:12px;color:#61718b}code{font-size:.9em}.navtools{display:flex;gap:8px;flex-wrap:wrap}.glossary{display:grid;grid-template-columns:1fr 1fr;gap:0 20px}.glossary p{font-size:14px}@media(max-width:1050px){aside.menu{position:static;width:auto}aside.menu a{display:inline-block;margin-right:15px}main{margin:0;padding:14px}.layout{display:block}.source-pane{position:static;margin-bottom:18px}.source-scroll{height:42vh}.glossary{display:block}}@media print{aside.menu,.navtools,button,.source-pane{display:none}body{background:#fff;font-size:10pt}main{margin:0;padding:0}.layout{display:block}header,.guide,.file,.qa{border:0;padding:0;border-radius:0}.file{break-before:page}.block{break-inside:avoid;padding:8px;border-color:#ccc}a{color:inherit;text-decoration:none}.flow svg{max-height:160px}.file h2{font-size:16pt}h4{font-size:11pt}}'''
JS = '''document.querySelectorAll('button[data-source]').forEach(button=>{button.addEventListener('click',()=>{const box=document.getElementById(button.dataset.source);const start=Number(button.dataset.start),end=Number(button.dataset.end);box.closest('details').open=true;box.querySelectorAll('.hit').forEach(line=>line.classList.remove('hit'));for(let n=start;n<=end;n++){const line=box.querySelector('[data-line="'+n+'"]');if(line)line.classList.add('hit');}const first=box.querySelector('[data-line="'+start+'"]');if(first){box.scrollTop=first.offsetTop-box.offsetTop-18;}button.setAttribute('aria-pressed','true');});});'''

def class_rows(fs):
    text='\n'.join((ROOT/f['path']).read_text(encoding='utf-8') for f in fs)
    return [(n,d) for n,d in CLASS_DEFS if n in text]

def make_html(role, fs, stem):
    nav=''.join(f'<a href="#{fid(f)}">{i}. {ESC(f["name"])}</a>' for i,f in enumerate(fs,1))
    overview=''.join(f'<tr><td>{i}</td><td><a href="#{fid(f)}">{ESC(Path(f["path"]).name)}</a></td><td>{ESC(f["role"])}</td></tr>' for i,f in enumerate(fs,1))
    classes=''.join(f'<tr><td>{ESC(n)}</td><td>{ESC(d)}</td></tr>' for n,d in class_rows(fs))
    body=[]
    for i,f in enumerate(fs,1):
        id=fid(f); source=''.join(f'<span class="code-line" data-line="{n}"><span class="number">{n}</span>{ESC(t)}</span>' for n,t in enumerate((ROOT/f['path']).read_text(encoding='utf-8').splitlines(),1))
        blocks=[];seen=set()
        for b in f['blocks']:
            notes=term_notes(' '.join(b['steps']),seen)
            terms=''.join(f'<div class="terms"><strong>{ESC(n)}</strong>: {ESC(d)}</div>' for n,d in notes)
            ds=', '.join(f'{m["method"]} ({m["line"]}줄)' for m in methods(f,b))
            blocks.append(f'''<article class="block" id="{id}-note-{b['start']}"><h4>{ESC(b['title'])}</h4><button data-source="{id}-code" data-start="{b['start']}" data-end="{b['end']}" aria-label="{ESC(f['name'])} {b['start']}~{b['end']}줄 코드 강조">{b['start']}~{b['end']}줄 코드 강조</button><p class="when">실행·읽기 시점: {ESC(b['when'])}</p><ol>{''.join(f'<li>{ESC(t)}</li>' for t in b['steps'])}</ol>{f'<p class="decls">관련 선언: {ESC(ds)}</p>' if ds else ''}{terms}</article>''')
        body.append(f'''<section class="file" id="{id}"><div class="file-head"><p class="small">파일 {i} / {len(fs)}</p><h2>{ESC(Path(f['path']).name)}</h2><p>{ESC(f['role'])}</p><p><code>{ESC(f['path'])}</code> · <a href="{url(f)}" target="_blank" rel="noopener">기준 소스 열기</a> · <a href="../../{ESC(f['path'])}">프로젝트 안의 파일</a></p><p><strong>먼저 볼 변수·인수</strong>: {ESC(f['values'])}</p><p class="file-flow">실행 흐름: {ESC(f['flow'])}</p><p class="callout"><strong>실행 예:</strong> {ESC(f['example'])}</p></div><div class="layout"><details open class="source-pane"><summary>실제 코드 전체 · 설명의 버튼을 누르면 해당 줄 강조</summary><pre class="source-scroll" id="{id}-code">{source}</pre></details><div class="walk">{''.join(blocks)}</div></div></section>''')
    svg=(ROOT/f'docs/easy/flow-{role["key"]}.svg').read_text(encoding='utf-8')
    qa=''.join(f'<h3>{ESC(q)}</h3><p>{ESC(a)}</p>' for q,a in role['questions'])
    text=f'''<!doctype html><html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>{ESC(role['owner'])} 소스별 브리핑</title><style>{CSS}</style></head><body><aside class="menu"><h2>{ESC(role['owner'])}</h2><p>{ESC(role['title'])}</p><a href="#start">읽는 방법·담당 범위</a>{nav}<a href="#qa">시연·질문</a><p class="small">Java 17 / Eclipse 2023-12<br>코드 기준 {SOURCE_COMMIT[:7]}</p></aside><main><header id="start"><p class="small">실제 파일을 열어 설명하는 개인별 브리핑</p><h1>{ESC(role['owner'])} · {ESC(role['title'])}</h1><p><strong>PPT 원래 담당:</strong> {ESC(role['original'])}<br><strong>추가 담당:</strong> {ESC(role['added'])}</p><p class="callout">아래 순서로 소스파일을 열고 설명하세요. 파일마다 상단 선언 → 변수 → 생성자 → 메서드를 위에서 아래로 읽습니다. 실행 흐름은 호출·버튼·타이머에 따라 옮겨갑니다. 각 설명의 ‘코드 강조’ 버튼으로 실제 위치를 바로 확인할 수 있습니다.</p><div class="navtools"><button onclick="window.print()">설명 인쇄</button><a class="pill" href="../../output/pdf/member_briefings/{stem}.pdf">개인 PDF</a></div><table><thead><tr><th>순서</th><th>열 파일</th><th>담당 동작</th></tr></thead><tbody>{overview}</tbody></table></header><section class="guide"><h2>코드를 읽는 기준</h2>{''.join(f'<p>{ESC(t)}</p>' for t in BASICS)}<p><strong>모듈:</strong> java.desktop은 Swing·화면 배치, java.sql은 DB 통신을 제공합니다. JList·Connection 등은 모듈 안의 클래스입니다. java.util·java.time 등은 기본 모듈 java.base에 포함된 패키지입니다.</p><details><summary>이 담당 코드의 주요 클래스 뜻 보기</summary><table><tr><th>이름</th><th>역할</th></tr>{classes}</table></details><figure class="flow">{svg}<figcaption class="small">담당 기능의 대표 처리 관계. 파일별 정확한 호출은 아래 실행 흐름에서 확인합니다.</figcaption></figure></section>{''.join(body)}<section class="qa" id="qa"><h2>마지막 시연·질문</h2><p>{ESC(role['demo'])}</p>{qa}<p class="callout">설명 순서: “언제 실행되는지 → 받은 값 → 조건·반복 → DB 또는 화면 결과 → 다음 호출”을 본인 말로 말하세요. 다른 담당 파일을 부르는 곳에서는 넘기는 값과 받는 결과까지 설명하면 됩니다.</p></section><footer class="footer">소스 기준 커밋 {SOURCE_COMMIT}. HTML에는 해당 기준의 전체 소스가 들어 있습니다. 뒤에 코드를 수정하면 줄 번호도 갱신해야 합니다. 외부 인터넷 없이 단독으로 열 수 있습니다.</footer></main><script>{JS}</script></body></html>'''
    (OUT/f'{stem}.html').write_text(text,encoding='utf-8')

def make_md(role,fs,stem):
    out=[f'# {role["owner"]} · 소스별 브리핑',f'담당: {role["title"]}\n\nPPT 원래 담당: {role["original"]} / 추가 담당: {role["added"]}',
        f'[코드와 설명 함께 보기]({stem}.html) · [PDF](../../output/pdf/member_briefings/{stem}.pdf)',
        '각 파일을 열고 아래 순서로 위에서 아래로 설명합니다. 실행은 선언 순서가 아니라 호출·이벤트 순서를 따릅니다.',
        '## 모듈·문법 먼저 확인',*BASICS,
        'java.desktop은 Swing·화면 배치, java.sql은 DB 통신 모듈입니다. JList·Connection은 그 안의 클래스입니다. java.util·java.time은 java.base 안의 패키지입니다.',
        '\n'.join(['| 주요 클래스 | 역할 |','|---|---|',*[f'| {n} | {d} |' for n,d in class_rows(fs)]])]
    for i,f in enumerate(fs,1):
        out += [f'## {i}. {Path(f["path"]).name}',f'[{f["path"]}]({url(f)})',f['role'],f'먼저 볼 변수·인수: {f["values"]}',f'실행 흐름: {f["flow"]}',f'실행 예: {f["example"]}']
        seen=set()
        for b in f['blocks']:
            out += [f'### {b["title"]} · {b["start"]}~{b["end"]}줄',f'[코드 위치]({url(f,b["start"])}) · 실행·읽기 시점: {b["when"]}',
                '\n'.join(f'{i}. {s}' for i,s in enumerate(b['steps'],1))]
            ds=', '.join(f'{m["method"]} ({m["line"]}줄)' for m in methods(f,b))
            if ds:out.append('관련 선언: '+ds)
            for n,d in term_notes(' '.join(b['steps']),seen):out.append(f'> **{n}**: {d}')
    out += ['## 마지막 시연·질문',role['demo']]
    for q,a in role['questions']:out += [f'**{q}**',a]
    out += [f'소스 기준 커밋: {SOURCE_COMMIT}. 줄 번호는 이 기준의 실제 파일과 일치합니다.']
    (OUT/f'{stem}.md').write_text('\n\n'.join(out)+'\n',encoding='utf-8')

def make_pdf(role,fs,stem,font_dir):
    from reportlab.pdfbase import pdfmetrics
    from reportlab.pdfbase.ttfonts import TTFont
    from reportlab.lib import colors
    from reportlab.lib.pagesizes import A4
    from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
    from reportlab.platypus import SimpleDocTemplate,Paragraph,Spacer,Table,TableStyle,PageBreak,KeepTogether
    pdfmetrics.registerFont(TTFont('BriefKR',str(font_dir/'NanumGothic.ttf')))
    pdfmetrics.registerFont(TTFont('BriefKR-Bold',str(font_dir/'NanumGothic-Bold.ttf')))
    pdfmetrics.registerFontFamily('BriefKR',normal='BriefKR',bold='BriefKR-Bold',italic='BriefKR',boldItalic='BriefKR-Bold')
    ss=getSampleStyleSheet(); blue=colors.HexColor('#235a93')
    ss.add(ParagraphStyle(name='KR',fontName='BriefKR',fontSize=9.4,leading=14.8,wordWrap='CJK',spaceAfter=6))
    ss.add(ParagraphStyle(name='KRSmall',parent=ss['KR'],fontSize=8.0,leading=12,spaceAfter=3,textColor=colors.HexColor('#526b87')))
    ss.add(ParagraphStyle(name='KRTitle',parent=ss['KR'],fontName='BriefKR-Bold',fontSize=21,leading=29,spaceAfter=14,textColor=blue))
    ss.add(ParagraphStyle(name='KRFile',parent=ss['KR'],fontName='BriefKR-Bold',fontSize=16,leading=23,spaceAfter=10,textColor=blue,keepWithNext=True))
    ss.add(ParagraphStyle(name='KRBlock',parent=ss['KR'],fontName='BriefKR-Bold',fontSize=10.5,leading=16,spaceBefore=10,spaceAfter=5,keepWithNext=True))
    ss.add(ParagraphStyle(name='KRWhen',parent=ss['KRSmall'],keepWithNext=True))
    ss.add(ParagraphStyle(name='KRTerm',parent=ss['KR'],fontSize=8.2,leading=12.5,backColor=colors.HexColor('#edf4f8'),borderPadding=5,leftIndent=6,rightIndent=6,spaceBefore=3,spaceAfter=7))
    ss.add(ParagraphStyle(name='KRCode',parent=ss['KRSmall'],fontSize=7.8,leading=11,backColor=colors.HexColor('#f0f3f8'),borderPadding=5,spaceAfter=7,keepWithNext=True))
    P=lambda t,st='KR':Paragraph(ESC(t).replace('\n','<br/>'),ss[st])
    story=[]
    def para(t,st='KR'):story.append(P(t,st))
    para(f'{role["owner"]} · 소스별 브리핑','KRTitle')
    para(role['title'],'KRFile');para('PPT 원래 담당: '+role['original']);para('추가 담당: '+role['added'])
    para('Eclipse에서 담당 파일을 열고 줄 번호로 대조하며 읽는 자료입니다. HTML에는 전체 소스와 줄 강조 기능이 들어 있고, 이 PDF는 같은 설명을 인쇄하기 위한 자료입니다.')
    para('파일은 아래 순서로 열고, 각 파일에서는 상단 선언 → 변수 → 생성자 → 메서드 순서로 설명하세요. 버튼 동작은 클릭할 때 실행됩니다.')
    rows=[[P('순서','KRSmall'),P('열 파일','KRSmall'),P('담당 동작','KRSmall')]]
    for i,f in enumerate(fs,1):rows.append([P(i,'KRSmall'),P(Path(f['path']).name,'KRSmall'),P(f['role'],'KRSmall')])
    table=Table(rows,colWidths=[32,125,326],repeatRows=1,hAlign='LEFT')
    table.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#e8f0fa')),('GRID',(0,0),(-1,-1),.4,colors.HexColor('#d3deec')),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),7),('RIGHTPADDING',(0,0),(-1,-1),7),('TOPPADDING',(0,0),(-1,-1),6),('BOTTOMPADDING',(0,0),(-1,-1),6)]));story.extend([table,Spacer(1,12)])
    para('읽기 전에 필요한 모듈·문법','KRBlock')
    para('java.desktop: Swing 화면·배치 / java.sql: JDBC DB 통신. JList·Connection은 모듈 안의 클래스입니다. java.util·java.time은 기본 모듈 java.base 안의 패키지입니다.')
    for s in BASICS:para(s,'KRSmall')
    para('이 담당의 주요 클래스','KRBlock')
    # 정의를 두 개씩 나누지 않고 줄별로 배치해 용어를 바로 찾게 합니다.
    for n,d in class_rows(fs)[:8]:para(f'{n}: {d}','KRSmall')
    para('전체 주요 클래스 뜻은 개인 HTML의 접힌 표에서 확인할 수 있습니다. 아래 각 파일 설명에도 필요한 용어를 붙였습니다.','KRSmall')
    for i,f in enumerate(fs,1):
        story.append(PageBreak());para(f'{i}. {Path(f["path"]).name}','KRFile');para(f['path'],'KRSmall')
        para(f['role']);para('먼저 볼 변수·인수: '+f['values']);para('실행 흐름: '+f['flow'],'KRSmall');para('실행 예: '+f['example'],'KRTerm')
        seen=set();lines=(ROOT/f['path']).read_text(encoding='utf-8').splitlines()
        for b in f['blocks']:
            para(f'{b["start"]}~{b["end"]}줄 · {b["title"]}','KRBlock');para('실행·읽기 시점: '+b['when'],'KRWhen')
            ds=methods(f,b)
            # 관련 선언을 모두 적되 전체 코드는 HTML과 실제 파일에서 보도록 합니다.
            if ds:para('관련 선언: '+', '.join(f'{m["method"]}({m["line"]}줄)' for m in ds),'KRCode')
            for j,s in enumerate(b['steps'],1):para(f'{j}. {s}')
            for n,d in term_notes(' '.join(b['steps']),seen):para(f'용어 - {n}: {d}','KRTerm')
    story.append(Spacer(1,16));para('마지막 시연·질문','KRFile');para(role['demo'])
    for q,a in role['questions']:para(q,'KRBlock');para(a)
    para('설명 연습: “언제 실행되는지 → 받은 값 → 조건·반복 → DB 또는 화면 결과 → 다음 호출”을 본인 말로 이어 설명하세요. 다른 담당 파일을 부르는 곳은 넘기는 값과 받는 결과까지 설명하면 됩니다.','KRTerm')
    para('소스 기준 커밋: '+SOURCE_COMMIT,'KRSmall')
    def footer(canvas,doc):
        canvas.setStrokeColor(colors.HexColor('#d9e3ef'));canvas.line(40,40,A4[0]-40,40)
        canvas.setFont('BriefKR',7.5);canvas.setFillColor(colors.HexColor('#6b7e96'))
        canvas.drawString(40,26,f'{role["owner"]} | 소스별 브리핑 | Java 17 · Eclipse 2023-12')
        canvas.drawRightString(A4[0]-40,26,str(doc.page))
    PDF_DIR.mkdir(parents=True,exist_ok=True)
    doc=SimpleDocTemplate(str(PDF_DIR/f'{stem}.pdf'),pagesize=A4,leftMargin=40,rightMargin=40,topMargin=38,bottomMargin=54,
        title=role['owner']+' 소스별 브리핑',author='팀프로젝트 3조')
    doc.build(story,onFirstPage=footer,onLaterPages=footer)

def main():
    args=argparse.ArgumentParser();args.add_argument('--font-dir',type=Path);args.add_argument('--check',action='store_true');args=args.parse_args()
    coverage=validate()
    if args.check:
        print(f'PASS: {len(coverage)} files, 178 declarations, 190 sections; source ranges and hashes checked');return
    index=['# 팀원별 소스 브리핑\n','각 파일을 실제로 열어 세부 동작을 설명하는 자료입니다. 짧은 6장 요약본과 별도로 사용합니다.\n',
        '**추천 사용법:** 본인 HTML을 열면 실제 코드 전체와 설명이 함께 나옵니다. 파일 순서대로 이동하고 설명의 코드 강조 버튼으로 위치를 찾으세요. PDF와 Markdown은 같은 설명의 인쇄·저장소 열람용입니다.\n',
        '| 담당 | 범위 | 코드와 설명 | 인쇄용 | 저장소에서 읽기 |','|---|---|---|---|---|']
    for i,role in enumerate(DATA['roles'],1):
        fs=[f for f in DATA['files'] if f['package']==role['key']];stem=f'{i:02d}_{role["key"]}'
        make_html(role,fs,stem);make_md(role,fs,stem)
        if args.font_dir:make_pdf(role,fs,stem,args.font_dir)
        index.append(f'| {role["owner"]} | {role["title"]} | [{len(fs)}개 파일]({stem}.html) | [PDF](../../output/pdf/member_briefings/{stem}.pdf) | [설명]({stem}.md) |')
    index += ['\n각 자료에는 원래 담당과 추가 담당, 파일별 변수·모듈·클래스·생성자·메서드, 조건·반복과 다음 호출, 시연 예와 질문이 들어 있습니다. 트랜잭션 등 어려운 용어는 사용하는 설명 옆에 뜻을 붙였습니다.',
        '\n전체 Java 담당 파일 29개·선언 178개를 확인했습니다. 공통 담당에는 module-info.java와 schema.sql도 포함해 총 31개 파일, 190개 연속 코드 구간을 설명합니다. 단순 값 반환 메서드는 같은 자료형 안에서 묶어 설명합니다.',
        f'\n코드 기준: `{SOURCE_COMMIT}`. 설명 범위·원본 파일 해시는 [coverage.json](coverage.json)에 기록합니다. 소스가 바뀌면 줄 번호와 설명도 함께 갱신해야 합니다.',
        '\n## 재생성\n\n표준 Python으로 `python tools/build_member_briefings.py`를 실행하면 HTML·Markdown을 만듭니다. `--check`는 파일·선언·설명 구간을 대조합니다. PDF는 reportlab과 NanumGothic.ttf·NanumGothic-Bold.ttf가 있는 폴더가 필요하며 `--font-dir 경로`로 지정합니다. 설명 원문은 content.json입니다. 프로그램 실행에는 이 생성 도구가 필요하지 않습니다.']
    (OUT/'README.md').write_text('\n'.join(index)+'\n',encoding='utf-8')
    cards=[]
    for i,role in enumerate(DATA['roles'],1):
        stem=f'{i:02d}_{role["key"]}'
        cards.append(f'<article><h2>{ESC(role["owner"])}</h2><p>{ESC(role["title"])}</p><p><a href="{stem}.html">실제 코드와 설명 함께 보기</a></p><p><a href="../../output/pdf/member_briefings/{stem}.pdf">개인 PDF</a></p></article>')
    landing=f'''<!doctype html><html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>팀원별 소스 브리핑</title><style>body{{max-width:1100px;margin:35px auto;padding:20px;background:#f4f7fb;color:#17324f;font:17px/1.8 system-ui,sans-serif}}main{{display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:18px}}article{{padding:22px;background:white;border:1px solid #cbd9ea;border-radius:12px}}h1{{font-size:30px}}h2{{margin:0}}a{{color:#185fa6}}.note{{background:#e7f0fa;padding:18px}}</style></head><body><h1>팀원별 소스 브리핑</h1><p class="note">본인 자료를 열고 파일 순서대로 실제 코드를 설명하세요. HTML에는 전체 코드와 줄 강조 버튼, PDF에는 같은 설명을 담았습니다. 어려운 용어는 사용하는 설명 옆에 뜻을 붙였습니다.</p><main>{''.join(cards)}</main><p>프로그램 기준: Java 17 · Eclipse 2023-12. 원래 역할과 추가 역할을 구분했습니다. 전체 Java 29개 파일·선언 178개, module-info.java와 schema.sql까지 31개 파일의 190개 코드 구간을 설명합니다.</p><p>코드 기준 {SOURCE_COMMIT[:7]} · PDF를 읽을 때는 실제 파일의 줄 번호와 대조하세요. 짧은 요약 자료는 <a href="../easy_guide.html">6장 요약본</a>에 있습니다.</p></body></html>'''
    (OUT/'index.html').write_text(landing,encoding='utf-8')
    print(f'Generated 5 member briefings; {len(coverage)} source files; {sum(f["sections"] for f in coverage)} sections')
if __name__=='__main__':main()
