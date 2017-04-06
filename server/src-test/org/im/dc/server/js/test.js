print(article.tag[0].attr);
print(article.tag[0].textContent);
print(article.tags[0].t2.length);
print(article.tags[0].t2[0].textContent);
print(article.tags[0].t2[1].textContent);
print(article.tags[0].t2);
for each(var t2 in article.tags[0].t2) {
  out.text("<br/>");
  out.text(t2);
}
print("===");
