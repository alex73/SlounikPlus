for each (var tlum in article.tlum) {
  if (tlum.desc[0].textContent.trim().length == 0 ) {
    throw "Пусты тэкст тлумачэння";
  }
  for each (var ex in tlum.ex) {
    var it = ex.it[0].textContent.trim();
    var bel = ex.bel[0].textContent.trim();
    if (it.length == 0 || bel.length == 0 ) {
        throw "Пусты прыклад";
      }
  }
}