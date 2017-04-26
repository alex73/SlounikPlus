print (article.zah[0].textContent);
var regexp = /[ЁЙЦУКЕНГШЗХФЫВАПРОЛДЖЭЯЧСМІИЪТЬБЮ]+/;
var m=regexp.exec(article.zah[0].textContent);
words = new Array();
if (m) {
	print(11111);
  words.push(m[0]);
  print(m[0]);
} else {
	print(2222);
}
helper.replaceWords(words);


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
