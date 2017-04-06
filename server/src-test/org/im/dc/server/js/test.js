out.tag("<html><body>\n");
out.tag("<b>");
out.text(words[0]);
out.tag("</b> <i>");
out.text(article.zah[0].gram[0]);
out.tag("</i>");
out.tag("<br/>\n");
for(var i=0; i<article.tlum.length; i++) {
	var tlum = article.tlum[i];
	out.tag("<b>").text(i+1+".").tag("</b> ");
	out.text(tlum.desc[0]);
	out.tag("<br/>\n");
}


out.tag("\n</body></html>\n");
