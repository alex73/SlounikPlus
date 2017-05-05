var adz = "";
var adzCount = 0;

var m = oneRod('fm','fme');
if (m) {
  adz += m + ' <i>м.</i>';
  adzCount++;
}

var f = oneRod('ff','ffe');
if (f && adz) adz += ', ';
if (f) {
  adz += f + ' <i>ж.</i>';
  adzCount++;
}

var n = oneRod('fn','fne');
if (n && adz) adz += ', ';
if (n) {
  adz += n + ' <i>н.</i>';
  adzCount++;
}

var mn = oneRod('fp','fpe');

if (para.mainIsSingle) {
  if (adzCount == 1) {
    print(adz.substring(0, adz.length()-10)+'; <i>мн.</i> '+mn+'; '+adz.substring(adz.length()-10));
  } else {
    print(adz+'; <i>мн.</i> '+mn);
  }
} else {
  print(mn+'; <i>адз.</i> '+adz);
}

function oneRod(formField, formEndField) {
  var ends = getEnds(formField, formEndField);
  var r = new Array();
  for (var s in ends) { //print("(s="+s+"para.showSklon="+para.showSklon+")");
    if (ends[s]==null) continue;
    var includeSo = para.showSklon.contains(s);
    var so = s;
    for (var s2 in ends) {
      if (s != s2 && ends[s] == ends[s2]) {
        so += s2;
        ends[s2] = null;
        if (para.showSklon.contains(s2)) {
          includeSo = true;
        }
      }
    }
    if (includeSo) {
      r.push('<i>'+so+'</i> '+ends[s]);
    } else {
      r.push(ends[s]);
    }
  }
  return r.join(', ');
}

function getEnds(formField, formEndField) {
  var ends = new Array();
  for each (var row in para.rows) {
    var e = end(row[formField], row[formEndField]);
    if (e) {
      if (ends[row.code]) {
        ends[row.code] += ", " + e;
      } else {
        ends[row.code] = e;
      }
    }
  }
  for (var s in ends) {
    var p = ends[s].lastIndexOf(', ');
    if (p > 0) {
      ends[s] = ends[s].substring(0, p) + ' і ' + ends[s].substring(p+2);
    }
  }
  return ends;
}

function end(form, formEnd) {
  if (formEnd == 0) {
    return null;
  }
  var max = form.replace('+','').replace('-','').length();
  if (formEnd == max) {
    return form.replace('+','\u0301');
  }
  for (var i = form.length() - 1; i >= 0; i--) {
    formEnd--;
    while (form.charAt(i) == '+' || form.charAt(i) == '-') {
      i--;
    }
    if (formEnd == 0) {
        return "-" + form.substring(i).replace('+','\u0301');
    }
  }
  return null;
}
