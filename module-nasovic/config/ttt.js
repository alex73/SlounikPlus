CYR_UPPER_LETTERS = "ЙЦУКЕНГШЎЗХФЫВАПРОЛДЖЭЯЧСМІТЬБЮЁИЩЪҐѢѲ"
CYR_WRONG_LETTERS = "ŎÔĂÊĔăôŏĕê’ΪÏï0123456789^+*<>«»";
CYR_LOWER_LETTERS = "йцукенгшўзхфывапролджэячсмітьбюёищъґѣѳ";
CYR_LETTERS=CYR_UPPER_LETTERS+CYR_LOWER_LETTERS+CYR_WRONG_LETTERS+"´"+"παίωπλέχωτρέπαίρε";
NON_LETTERS=" ,.)!?-;:[](|)–=\n/“”\"";
LAT_LETTERS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNMüžłėę";

var nextPos = 0;
while(true) {
  pos = findChar(article, nextPos);
  nextPos = findNonChar(article, pos);
  if (pos<0 || nextPos<0) {
    break;
  }
  //print(article.substring(pos,nextPos));
}

function findChar(text, from) {
  for (var i = from; i < text.length(); i++) {
    var c = text.charAt(i);
    if (CYR_LETTERS.indexOf(c) >= 0) {
      return i;
    }
    if (LAT_LETTERS.indexOf(c) >= 0) {
      return i;
    }
    if (NON_LETTERS.indexOf(c) < 0) {
      print("Невядомы сімвал : "+text.substring(i-3, i+4));
    }
  }
  return -1;
}

function findNonChar(text, from) {
  for (var i = from; i < text.length(); i++) {
    var c = text.charAt(i);
    if (NON_LETTERS.indexOf(c) >= 0) {
      return i;
    }
    if (CYR_LETTERS.indexOf(c) < 0 && LAT_LETTERS.indexOf(c) < 0) {
      print("Невядомы сімвал : "+text.substring(i-3, i+3));
    }
  }
  return -1;
}

function isCorrect(word) {
  if (CYR_LETTERS.indexOf(word.charAt(0)) >= 0) {
    for(var i=1; i<word.length(); i++) {
      if (CYR_LETTERS.indexOf(word.charAt(0)) < 0) {
        print("Няправільнае слова: "+word);
      }
    }
  } else if (LAT_LETTERS.indexOf(word.charAt(0)) >= 0) {
    for(var i=1; i<word.length(); i++) {
      if (LAT_LETTERS.indexOf(word.charAt(0)) < 0) {
        print("Няправільнае слова: "+word);
      }
    }
  } else {
    print("Няправільнае слова: "+word);
  }
}
