// \u25AD - падзел цытат ад рэдакцыйных прыкладаў
// \u25CB - тэрміналагічнае спалучэнне
// \u25CA - фразеалагізм


var POSLETTERS="абвгдежзіклмнопрсту";

out.tag("<!DOCTYPE html>\n");
out.tag("<html><head><meta charset=\"UTF-8\"></head><body>");

var zcomplex = new Array();
for each (var co in article.zah[0].partComplex) {
  zcomplex.push(complex_prepare(co));
}

out.tag(zcomplex.join('; '));

/*for (var i = 0; i < article.zah.length; i++) {
  zah_create(i);
}
var z_roznalikavyja = false;
for (var index = 0; index < z.length - 1; index++) {
  if (article.zah[index].lik[0] != article.zah[index+1].lik[0]) {
    z_roznalikavyja = true;
  }
}
while (zah_prepare());
z[0].lik = null;

zah_out();*/
/*for(var i=0; i<article.tlum.length; i++) { // root.tlum 1..N Тлумачэнне
  tlum_out(article.tlum[i]);
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
}*/

out.tag("</body></html>\n");

function complex_prepare(complex) {
  var r = "";
  var rods = new Array();
  for( var i = 0; i < complex.partSimple.length; i++) {
    rods.push(rod_prepare(complex.partSimple[i].rod[0]));
    if (i > 0 && rods[i-1] == rods[i]) {
      rods[i-1] = "";
    }
  }
  for( var i = 0; i < complex.partSimple.length; i++) {
    if (i==0) {
    } else if (i==complex.partSimple.length-1) {
      r += ' і ';
    } else {
      r += ', ';
    }
    r += simple_prepare(complex.partSimple[i], rods[i]);
  }
  return r;
}
function simple_prepare(simple, rod_prepared) {
  var s = wordform_prepare(simple.wordform[0]);
  var razd = ", ";
  if (simple.insyLik) {
    var hetaMnozny = simple.lik[0].textContent == 'адз.';
    var lik = out.prepare("<i>", hetaMnozny ? 'мн.' : 'адз.', "</i>");  
    var il = new Array();
    for each (var ilw in simple.insyLik[0].wordform) {
      il.push(wordform_prepare(ilw));
    }
    s+='; '+lik + ' ' +il.join(" і ");
    var rlp = rod_prepare(simple.insyLik[0].rod[0]);
    if (rlp) {
      s += ', '+rlp;
    }
    if (hetaMnozny) {
      razd ='; ';
    }
  }
  if (rod_prepared) {
    s += razd + rod_prepared;
  }
  return s;
}

function wordform_prepare(wordform) {
  var s = out.prepare("<b>", wordform.word[0], "</b>");
  var fs = form_prepare(wordform.form);
  if (fs) {
    if (s) s+=', ';
    s += fs;
  }
  return s;
}
function rod_prepare(rod) {
  var r = "";
  if (rod.value) {
    for (var ri = 0; ri < rod.value.length; ri++) {
      if (ri == 0) {
        r += "<i>" + out.prepare(rod.value[ri]) + "</i>";
      } else if (ri == rod.value.length - 1) {
        r += out.prepare(" і ");
        r += "<i>" + out.prepare(rod.value[ri]) + "</i>";
      } else {
        r += out.prepare(", ");
        r += "<i>" + out.prepare(rod.value[ri]) + "</i>";
      }
    }
  }
  return r;
}
function zah_create(index) {
  z.push(new Object());
  if (index == 0) {
    z[index].word = out.prepare(words[index].toUpperCase());
  } else {
    z[index].word = out.prepare(words[index]);
  }

  var styl = new Array();
  if (article.zah[index].styl) {
    for each (var st in article.zah[index].styl) {
      styl.push(out.prepare(st));
    }
    z[index].styl = styl.join(", ");
  }

  z[index].lik = out.prepare("<i>", article.zah[index].lik[0], "</i> ");
  z[index].rod = "";
  if (article.zah[index].rod[0].value) {
    for (var ri = 0; ri < article.zah[index].rod[0].value.length; ri++) {
      if (ri == 0) {
        z[index].rod += "<i>" + out.prepare(article.zah[index].rod[0].value[ri]) + "</i>";
      } else if (ri == article.zah[index].rod[0].value.length - 1) {
        z[index].rod += out.prepare(" і ");
        z[index].rod += "<i>" + out.prepare(article.zah[index].rod[0].value[ri]) + "</i>";
      } else {
        z[index].rod += out.prepare(", ");
        z[index].rod += "<i>" + out.prepare(article.zah[index].rod[0].value[ri]) + "</i>";
      }
    }
  }
  z[index].forms = form_prepare(article.zah[index].form);
  if (article.zah[index].insyLik) {
    z[index].forms2 = form_prepare(article.zah[index].insyLik[0].form);
    z[index].forms2lik = article.zah[index].lik[0].textContent == 'адз.' ? 'мн.' : 'адз.';
  } else {
    z[index].forms2 = null;
  }
}
function form_prepare(form) {
  if (form == null) {
    return null;
  }
  var f = new Array();
  for (var i = 0; i < form.length; i++) {
    var s = "";
    if (form[i].sklon[0].value) {
      for each (var sk in form[i].sklon[0].value) {
        s += out.prepare(sk);
      }
    }
    s = out.prepare("<i>", s, "</i> ");
    f.push(s + out.prepare(form[i].end[0]));
  }
  return f.join(', ');
}
function zah_prepare() {
  for(var i = 0; i < z.length - 1; i++) {
    if (z[i].rod != null && z[i].rod == z[i+1].rod) {
      z[i].rod = null;
      return true;
    }
  }
  for(var i = z.length - 1; i > 0; i--) {
    if (z[i].lik != null && z[i].lik == z[i-1].lik) {
      z[i].lik = null;
      return true;
    }
  }
  return false;
}
function zah_out() {
  for (var index = 0; index < z.length; index++) {
    if (index == 0) {
    } else if (index == z.length - 1) {
      out.tag(' і ');
    } else {
      out.tag(z_roznalikavyja ? '; ' : ', ');
    }
    if (z[index].styl) {
     out.tag("(");
     out.tag(z[index].styl);
     out.tag(") ");
    }
    if (z[index].lik) {
      out.tag(z[index].lik+" ");
    }
    out.tag("<b>" + z[index].word + "</b>");
    if (z[index].forms) {
      out.tag(", ");
      out.tag(z[index].forms);
    }
    if (z[index].forms2) {
      out.tag("; <i>" + z[index].forms2lik + "</i> ");
      out.tag(z[index].forms2)
      out.tag("; ");
    }
    if (z[index].rod) {
      if (z[index].forms2 == null) {
        out.tag(", ");
      }
      out.tag(z[index].rod);
    }
  }
}

function tlum_out(tlum) {
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
    var biazAutara = false;
    for each (var ex in subdesc.ex) { // ex 0..N Прыклад
      if (biazAutara && ex.author[0].textContent) {
        out.text("\u2610");
      }
      out.out(ex.text).text(" "); // text 1..1 Ілюстрацыя
      if (ex.author[0].textContent) {
        out.tag("<i>");
        out.out(ex.author); // author 1..1 Аўтар
        out.tag(".</i> ");
      } else {
        biazAutara = true;
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
  out.tag("<br/>\n");
}

function b1(b) {
    out.tag("<i>").out(b.sem).tag("</i>").text(" "); // sem 0..1 Семантычныя паметы
    out.tag("<i>").out(b.grpam).tag("</i>").text(" "); // grpam 0..1 Граматычныя паметы
    out.tag("<i>").out(b.styl).tag("</i>").text(" "); // styl 0..1 Стылістычныя паметы
    out.out(b.zaha).text(" "); // zaha 0..1 Загалоўнае слова ў артыкуле
    outLinks(b.desc[0].textContent); // desc 1..1 Тэкст тлумачэння
    out.text(" ");
    for each (var ex in b.ex) { // ex 0..N Прыклад
      out.tag("<i>").out(ex.text).text("</i> "); // text 1..1 Ілюстрацыя
      if (ex.author[0].textContent) {
        out.out(ex.author); // author 1..1 Аўтар
        out.tag(". ");
      }
    }
}

function outLinks(str) {
  var regexp = /@(.+?)@/g;
  var regexp1 = /@(.+?)(\/([0-9]+))?(\[([0-9]+)\])?@/;
  
  var prevPos = 0;
  while(true) {
    var pos = str.search(regexp);
    if (pos<0) {
      out.text(str.substring(prevPos));
      break;
    }
    out.text(str.substring(prevPos, pos)); // usual text
    
    var newPos = str.indexOf('@', pos+1) + 1;
    var m = str.substring(pos,newPos).match(regexp1);
    out.text(m[1]);
    if (m[3]) {
      out.tag("<sup>").text(m[3]).tag("</sup>");
    }
    if (m[5]) {
      out.text("(у "+m[5]+"-м знач.)");
    }
    
    prevPos = newPos;
  }
}
