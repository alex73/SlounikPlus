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
