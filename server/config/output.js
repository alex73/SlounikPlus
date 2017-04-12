var POSLETTERS="абвгдежзіклмнопрсту";
out.tag("<!DOCTYPE html>\n");
out.tag("<html><head><meta charset=\"UTF-8\"></head><body>\n");
out.tag("<b>");
out.text(words[0].toUpperCase());
out.tag("</b> <i>");
zahoutput(article.zah[0]); // zah.gram 1..N TODO Паметы загалоўных слоў
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

function zahoutput(zah) {
    for each (var para in zah.para) { // root.zah.para Парадыгма
      out.tag("<i>").out(para.sklon).tag("</i>").text(" "); // root.zah.para.sklon
      out.out(para.forma).text(" "); // root.zah.para.forma
    }
    out.tag("<i>").out(zah.gram).tag("</i>").text(" "); // root.zah.gram 0..1 Граматычная памета
}

function tlumacennie(tlum) {
  if (article.tlum.length > 1) {
    out.tag("<b>").text(i+1+".").tag("</b> ");
  }
  b1(tlum);
  for each (var adc in tlum.adc) { // root.tlum.adc 0..N //
    out.text("// ");
    b1(adc);
  }
  for each (var usd in tlum.usd) { // root.tlum.usd 0..N /
    out.text("/ ");
    b1(usd);
  }
  if (tlum.subdesc) {
  for(var j=0; j<tlum.subdesc.length; j++) { // root.tlum.subdesc 0..N Падтлумачэнне
    var subdesc = tlum.subdesc[j];
    if (j>POSLETTERS.length) {
      out.err("Занадта вялікі індэкс");
    }
    out.tag("<br/>\n").text(POSLETTERS[j]+") ");
    out.out(subdesc.desc).text(" "); // desc 1..1 Тэкст тлумачэння
    for each (var ex in subdesc.ex) { // ex 0..N Прыклад
      out.out(ex.text).text(" "); // text 1..1 Ілюстрацыя
      if (ex.author[0].textContent) {
        out.tag("<i>");
        out.out(ex.author); // author 1..1 Аўтар
        out.tag(".</i> ");
      }
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
}

function b1(b) {
    out.tag("<i>").out(b.sem).tag("</i>").text(" "); // sem 0..1 Семантычныя паметы
    out.tag("<i>").out(b.grpam).tag("</i>").text(" "); // grpam 0..1 Граматычныя паметы
    out.tag("<i>").out(b.styl).tag("</i>").text(" "); // styl 0..1 Стылістычныя паметы
    out.out(b.zaha).text(" "); // zaha 0..1 Загалоўнае слова ў артыкуле
   // var desc = linksTo(b.desc[0].textContent);
    var desc = b.desc[0].textContent;
    out.text(desc).text(" "); // desc 1..1 Тэкст тлумачэння
    for each (var ex in b.ex) { // ex 0..N Прыклад
      out.out(ex.text).text(" "); // text 1..1 Ілюстрацыя
      if (ex.author[0].textContent) {
        out.tag("<i>");
        out.out(ex.author); // author 1..1 Аўтар
        out.tag(".</i> ");
      }
    }
}

function linksTo(str) {
  var regexp = /@(.+?)(\[([0-9]+)\])?@/g;
  var res = str.match(/@\S+@/g);
  if (res) {
    for each (var r in res) {
      var m = regexp.exec(r);
      out.text(" -"+r+"======"+m+"=====");
    }
  }
  return str;
}
