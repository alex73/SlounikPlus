if (words.length > 1) {
	throw "Больш за 1 загалоўнае слова пакуль не падтрымліваецца";
}

if (article.zah[0].gram.length != words.length) {
	throw "Колькасць паметаў загалоўных слоў несупадае з колькасцю слоў";
}
