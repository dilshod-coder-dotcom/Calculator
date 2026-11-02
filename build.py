import base64,pathlib
R=pathlib.Path(__file__).parent
s=(R/'src/index.src.html').read_text()
rep={'/*@@XLSX@@*/':(R/'libs/xlsx.js').read_text(),'/*@@JSPDF@@*/':(R/'libs/jspdf.js').read_text(),'/*@@AUTOTABLE@@*/':(R/'libs/autotable.js').read_text(),
 '/*@@FONT_R@@*/':(R/'libs/font_r.b64').read_text().strip(),'/*@@FONT_B@@*/':(R/'libs/font_b.b64').read_text().strip(),
 '/*@@LOGO@@*/':'data:image/png;base64,'+base64.b64encode((R/'icon-192.png').read_bytes()).decode()}
for k,v in rep.items():
    assert s.count(k)==1,k; s=s.replace(k,v)
(R/'index.html').write_text(s); print('index.html',len(s))
