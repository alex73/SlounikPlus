out.tag("<html><body>\n");
out.tag("<b>");
out.text(words[0].toUpperCase());
out.tag("</b> <i>");
out.text(article.zah[0].gram[0]); // zah.gram 1..N TODO Паметы загалоўных слоў
out.tag("</i>");
out.tag("<br/>\n");
for(var i=0; i<article.tlum.length; i++) { // root.tlum 1..N Тлумачэнне
  tlumacennie(article.tlum[i]);
  out.tag("<br/>\n");
}
for each (var term in article.term) { // root.term 0..N Тэрміналагічнае словазлучэнне
  out.text(term.term).text(" "); // term 1..1 Словазлучэнне
  out.text(term.explanation).text(" "); // explanation 1..1 Тлумачэнне
}
for each (var fraz in article.fraz) { // root.fraz 0..N Фразеалагізм
  out.text(fraz.fraz).text(" "); // fraz 1..1 Фразеалагізм
  out.text(fraz.explanation).text(" "); // explanation 1..1 Тлумачэнне
}
for each (var ustetym in article.ustetym) { // root.ustetym 0..N Этымалогія
  out.text(ustetym).text(" "); // fraz 1..1 Фразеалагізм
}

out.tag("\n</body></html>\n");

function tlumacennie(tlum) {
  if (article.tlum.length > 1) {
    out.tag("<b>").text(i+1+".").tag("</b> ");
  }
  b1(tlum);
  for each (var adc in tlum.adc) { // root.tlum.adc 0..N //
    out.tag("<br/>\n");
    out.text("// ");
    b1(adc);
  }
  for each (var usd in tlum.usd) { // root.tlum.usd 0..N /
    out.tag("<br/>\n");
    out.text("/ ");
    b1(usd);
  }
  for each (var subdesc in tlum.subdesc) { // root.tlum.subdesc 0..N Падтлумачэнне
    out.out(subdesc.desc); // desc 1..1 Тэкст тлумачэння
    for each (var ex in subdesc.ex) { // ex 0..N Прыклад
      out.out(ex.text); // text 1..1 Ілюстрацыя
      out.tag(" <i>");
      out.out(ex.author); // author 1..1 Аўтар
      out.tag("</i> ");
    }
    for each (var usd in subdesc.usd) { // root.tlum.subdesc.usd 0..N /
      out.text("/ ");
      b1(usd);
    }
    for each (var adc in subdesc.adc) { // root.tlum.subdesc.adc 0..N //
      out.text("// ");
      b1(adc);
    }
  }
}

function b1(b) {
    out.out(b.sem).text(" "); // sem 0..1 Семантычныя паметы
    out.out(b.grpam).text(" "); // grpam 0..1 Граматычныя паметы
    out.out(b.styl).text(" "); // styl 0..1 Стылістычныя паметы
    out.out(b.zaha).text(" "); // zaha 0..1 Загалоўнае слова ў артыкуле
    out.out(b.desc).text(" "); // desc 1..1 Тэкст тлумачэння
    for each (var ex in b.ex) { // ex 0..N Прыклад
      out.out(ex.text); // text 1..1 Ілюстрацыя
      out.tag(" <i>");
      out.out(ex.author); // author 1..1 Аўтар
      out.tag("</i> ");
    }
}
