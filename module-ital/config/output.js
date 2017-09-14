var ow = out.start("<b>", "</b> ", ", ");
for each (var w in words) {
  ow.add(w.replace(/\/([0-9]+)$/, "<sup>$1</sup>"));
}
ow.end();

out.out("(<i>pl</i> ", article.pl, ") ");

var ow = out.start(" <i>", "</i> ", " ");
for each (var g in article.gramzah[0].value) {
  ow.add(g);
}
ow.end();

out.out(" <i>", article.movazah, "</i> ");

styl(article.stylzah);
if (article.inszah) {
  var t = out.prepare(article.inszah[0].textContent).replace(/{(.+?)}/g, '<i>$1</i>');
  out.tag(t);
}

var tlumIndex = 0;
for each (var tlum in article.tlum) {
  tlumIndex++;
  if (article.tlum.length > 1) {
    out.out(" <b>", tlumIndex+".", "</b> ");
  }

  out.out("(<i>pl</i> ",tlum.pl,") ");
  for each (var g in tlum.gram[0].value) {
    out.out(" <i>", g, "</i> ");
  }
  out.out(" <i>",tlum.mova,"</i> ");
  styl(tlum.styl);

  var d = out.prepare(tlum.desc);
  d = d.replace(/ м\./g, ' <i>м.</i>');
  d = d.replace(/ н\./g, ' <i>н.</i>');
  d = d.replace(/ ж\./g, ' <i>ж.</i>');
  d = d.replace(/ мн\./g, ' <i>мн.</i>');
  d = d.replace(/ нескл\./g, ' <i>нескл.</i>');
  d = d.replace(/{(.+?)}/g, '<i>$1</i>');
  d = d.replace(/\((.+?)\)/g, '(<i>$1</i>)');
  d = d.replace(/\[(.+?)\]/g, '($1)');
  out.tag(d);

  for each (var ex in tlum.ex) {
    if (":".indexOf(out.latestNonSpace())<0) {
      out.tag(";");
    }
    out.tag(" <i>");
    out.tag(out.prepare("<b>", ex.it[0], "</b> "));
    if (ex.pam) {
      out.tag(out.prepare("(", ex.pam[0], ") "));
    }
    out.tag(out.prepare(ex.bel[0]));
    out.tag("</i>");
  }
  if (tlum.ust) {
    out.tag("; ♦ ");
  }
  var was = false;
  for each (var ust in tlum.ust) {
    if (was) {
      out.tag("; ");
    }
    was = true;
    out.tag("<i>");
    out.tag(out.prepare("<b>", ust.it[0], "</b> "));
    if (ust.pam) {
      out.tag(out.prepare("(", ust.pam[0], ") "));
    }
    out.tag(out.prepare(ust.bel[0]));
    out.tag("</i>");
  }

  if (tlumIndex < article.tlum.length) {
    out.tag(';');
  } else if ("?!.".indexOf(out.latestNonSpace())<0) {
    out.tag('.');
  }
}

if (article.si[0].textContent === 'true') {
   out.tag(" || -si.");
}

function styl(styl) {
  var ow = out.start(" <i>", "</i> ", ", ");
  for each (var s in styl) {
    ow.add(s);
  }
  ow.end();
}
