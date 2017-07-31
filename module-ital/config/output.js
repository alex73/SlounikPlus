out.tag("<!DOCTYPE html>\n");
out.tag("<html><head><meta charset=\"UTF-8\"></head><body>");

out.tag("<b>");
for(var i=0; i < words.length; i++) {
  if (i > 0) {out.tag(", ");}
  out.tag(words[i]);
}
out.tag("</b> ");

for each (var g in article.gramzah[0].value) {
  out.tag(out.prepare(" <i>", g.textContent, "</i> "));
}
for each (var s in article.stylzah) {
  out.tag(out.prepare(" <i>", s.textContent, "</i> "));
}
if (article.inszah) {
  var t = out.prepare(article.inszah[0].textContent).replace(/{(.+?)}/, '<i>$1</i>');
  out.tag(t);
}

var tlumIndex = 1;
for each (var tlum in article.tlum) {
  if (article.tlum.length > 1) {
    out.tag(out.prepare(" <b>", tlumIndex+".", "</b> "));
    tlumIndex++;
  }

  for each (var g in tlum.gram[0].value) {
    out.tag(out.prepare(" <i>", g.textContent, "</i> "));
  }
  for each (var s in tlum.styl) {
    out.tag(out.prepare(" <i>", s.textContent, "</i> "));
  }

  var d = out.prepare(tlum.desc[0]);
  d = d.replace(' м.', ' <i>м.</i>');
  d = d.replace(' н.', ' <i>н.</i>');
  d = d.replace(' ж.', ' <i>ж.</i>');
  d = d.replace(/{(.+?)}/, '<i>$1</i>');
  out.tag(d);
  
  var lastTlum='';
  for each (var ex in tlum.ex) {
    out.tag("; <i>");
    out.tag(out.prepare("<b>", ex.it[0], "</b> "));
    out.tag(out.prepare(ex.bel[0]));
    out.tag("</i>");
    lastTlum = ex.bel[0].textContent;
  }
  for each (var ust in tlum.ust) {
    out.tag("; ♦ ");
    out.tag(out.prepare("<b>", ex.it[0], "</b> "));
    out.tag(out.prepare(ex.bel[0]));
    lastTlum = ust.bel[0].textContent;
  }

  if (tlumIndex < article.tlum.length-1) {
    out.tag(';');
  } else if (!lastTlum.trim().match(/[\?\!\.]$/)) {
    out.tag('.');
  }
}

if (article.si[0].textContent === 'true') {
   out.tag(" || -si.");
}

out.tag("</body></html>\n");
