/*
if (article.zah.length != words.length) {
  throw "Колькасць паметаў загалоўных слоў несупадае з колькасцю слоў";
}

for each (var w in words) {
	checkWord(w);
}
for each (var zah in article.zah) {
  zah_check(zah);
}
for each (var tlum in article.tlum) {
  tlum_check(tlum);
}*/

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
  var regexp = /^[ЁЙЦУКЕНГШЗХФЫВАПРОЛДЖЭЯЧСМІТЬБЮ]?[ёйцукенгшўзх'фывапролджэячсмітьбю\-\+]+(\/[1-9])?$/;
  if (!regexp.test(w)) {
    throw "Няправільныя сімвалы ў загалоўным слове: " + w;
  }
}
