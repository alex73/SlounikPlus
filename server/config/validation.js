if (words.length > 1) {
	throw "Больш за 1 загалоўнае слова пакуль не падтрымліваецца";
}

if (article.zah[0].gram.length != words.length) {
	throw "Колькасць паметаў загалоўных слоў несупадае з колькасцю слоў";
}

for(var i=0; i<article.tlum.length; i++) { // root.tlum 1..N Тлумачэнне
  tlumacennie(article.tlum[i]);
}

//TODO каб толькі правільныя сімвалы былі
//напрыканцы могуць быць /1 /2 /3

function tlumacennie(tlum) {
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
    if (m[3]) {
      helper.addLink(m[1]+'/'+m[3]);
    } else {
      helper.addLink(m[1]);
    }
  }
}
