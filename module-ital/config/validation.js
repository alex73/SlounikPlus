var regexpWordUppercase = /^[ЙЦУКЕНГШЎЗХФЫВАПРОЛДЖЭЯЧСМІТЬБЮЁИЩЪҐѢѲ´´ŎĂĔ\u0301]+(\-?[ЙЦУКЕНГШЎЗХФЫВАПРОЛДЖЭЯЧСМІТЬБЮЁИЩЪҐѢѲ´´ŎĂĔ\u0301]+)?$/;
//var regexpWordUppercase = /[ЙЦУКЕНГШЎЗХФЫВАПРОЛДЖЭЯЧСМІТЬБЮЁИЩЪҐѢѲ´]+/;

var zahWords = article.zah[0].textContent.split(/[\s\.,]+/);

words = new Array();
for each (var z in zahWords) {
  if (regexpWordUppercase.test(z)) {
    words.push(z);
  }
}

helper.replaceWords(words);
for each (var w in words) {
  var np = w.indexOf('´');
  if (np <= 0) {
    throw "Няма націску ў загалоўным слове: " + w;
  } else {
    if ("УЕЫАОЭЯІЮЁИѢ".indexOf(w.charAt(np-1))<0) {
      throw "Націск не на галосную ў загалоўным слове: " + w;
    }
  }
}

if (article.comment != null) {
  //throw "Камэнтар: "+article.comment[0].textContent;
}

var text = Java.type("org.im.dc.client.ui.xmlstructure.nasovic.RTFSerialization").deserializeText(article.rtf[0].textContent);
var fullText = article.zah[0].textContent;
var idx=0;
for each (var tlum in article.tlum) {
  if (article.tlum.length>1) {
    idx++;
    fullText+=idx+")";
  }
  if (tlum.gram) {
    fullText+=tlum.gram[0].textContent;
  }
  fullText += tlum.desc[0].textContent;
  for each (var ex in tlum.ex) {
    fullText += ex.textContent;
  }
}

var diff = Java.type("org.im.dc.client.ui.xmlstructure.nasovic.DiffChecker").diffText(text, fullText);
if (diff != null) {
  throw "Несупадае тэкст: "+diff;
}

function rtfReadInt(rtf, pos) {
     var ch1 = rtf.charAt(pos+0);
     var ch2 = rtf.charAt(pos+1);
     var ch3 = rtf.charAt(pos+2);
     var ch4 = rtf.charAt(pos+3);
     if ((ch1 | ch2 | ch3 | ch4) < 0)
         throw "EOFException";
     return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
}

function zah_check(zah) {
  if (zah.lik[0].textContent == 'адз.' && zah.rod[0].value == null) throw "Род непазначаны";
}
function tlum_check(tlum) {
  collectLinks(tlum.desc[0].textContent);
}

function collectLinks(str) {
  var regexp = /@(.+?)(\/([0-9]+))?(\[([0-9]+)\])?@/g;
  
  var prevPos = 0;
  while(true) {
    var m = regexp.exec(str);
    if (m == null) {
      break;
    }
    var link;
    if (m[3]) {
      link = m[1]+'/'+m[3];
    } else {
      link = m[1];
    }
    if (!helper.checkExistWord(link)) {
      throw "Спасылаецца на неіснуючы артыкул";
    }
    helper.addLink(link);
  }
}

//каб толькі правільныя сімвалы былі
//напрыканцы могуць быць /1 /2 /3
function checkWord(w) {

  if (!regexp.test(w)) {
    throw "Няправільныя сімвалы ў загалоўным слове: " + w;
  }
}
