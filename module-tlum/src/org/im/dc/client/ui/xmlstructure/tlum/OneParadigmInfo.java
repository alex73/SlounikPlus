package org.im.dc.client.ui.xmlstructure.tlum;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.alex73.corpus.paradigm.Form;
import org.alex73.corpus.paradigm.Variant;
import org.alex73.korpus.base.BelarusianTags;

@XmlRootElement(name = "OneParadigmInfo")
public class OneParadigmInfo {
    @XmlElement(name = "row")
    public List<SklonRow> rows = new ArrayList<>();
    @XmlAttribute
    public boolean mainIsSingle;
    @XmlAttribute
    public String showSklon = "";

    public OneParadigmInfo() {
        for (char c : "NGDAILV".toCharArray()) {
            rows.add(new SklonRow(c));
        }
    }

    public OneParadigmInfo(VariantInfo vi) {
        this();

        Variant v = vi.p.getVariant().get(vi.variantIndex - 'a');

        BelarusianTags tp = BelarusianTags.getInstance();
        for (Form f : v.getForm()) {
            String tag = vi.p.getTag() + f.getTag();
            char sklon = tp.getValueOfGroup(tag, "Склон");
            char lik = tp.getValueOfGroup(tag, "Лік");
            char rod = tp.getValueOfGroup(tag, "Род");
            if (f.getValue() != null && !f.getValue().isEmpty()) {
                addForm(lik, rod, sklon, f.getValue());
            }
        }
    }

    public void addForm(char lik, char rod, char sklon, String form) {
        int col;
        if (lik == 'S' && rod == 'M') {
            col = 1;
        } else if (lik == 'S' && rod == 'F') {
            col = 2;
        } else if (lik == 'S' && rod == 'N') {
            col = 3;
        } else if (lik == 'P') {
            col = 4;
        } else {
            throw new RuntimeException("Wrong lik+rod: " + lik + rod);
        }
        for (int i = 0; i < rows.size(); i++) {
            SklonRow r = rows.get(i);
            if (r.sklon() == sklon) {
                while (r.getForm(col - 1) != null) {
                    i++;
                    r = rows.get(i);
                    if (r.sklon() != sklon) {
                        r = new SklonRow(sklon);
                        rows.add(i, r);
                    }
                }
                r.setForm(col - 1, form);
                break;
            }
        }
    }

    public void changeEnd(int row, int col, int change) {
        if (col < 1) {
            return;
        }
        int max = rows.get(row).getForm(col - 1).replace("+", "").replace("-", "").length();

        int v = rows.get(row).getFormEnd(col - 1);
        v += change;
        if (v < 0) {
            v = max;
        }
        if (v > max) {
            v = max;
        }
        rows.get(row).setFormEnd(col - 1, v);
    }

    public String changeShowSklon(int row) {
        String sklon = rows.get(row).getCode();
        if (showSklon.contains(sklon)) {
            showSklon = showSklon.replace(sklon, "");
        } else {
            showSklon += sklon;
        }
        return sklon;
    }

    public boolean isChangedEnd(int row, int col) {
        if (col < 1) {
            return false;
        }
        return rows.get(row).getFormEnd(col - 1) != 0;
    }

    public boolean isCHangedSklon(int row) {
        return showSklon.contains(rows.get(row).getCode());
    }

    public static class SklonRow {
        @XmlAttribute(name = "sklon")
        public String sklonId = " ";

        @XmlAttribute(name = "fm")
        public String fm;
        @XmlAttribute(name = "ff")
        public String ff;
        @XmlAttribute(name = "fn")
        public String fn;
        @XmlAttribute(name = "fp")
        public String fp;

        @XmlAttribute(name = "fme")
        public int fme;
        @XmlAttribute(name = "ffe")
        public int ffe;
        @XmlAttribute(name = "fne")
        public int fne;
        @XmlAttribute(name = "fpe")
        public int fpe;

        public SklonRow() {
        }

        public SklonRow(char sklon) {
            this.sklonId = "" + sklon;
        }

        public char sklon() {
            return sklonId.charAt(0);
        }

        public String getName() {
            switch (sklon()) {
            case 'N':
                return "назоўны";
            case 'G':
                return "родны";
            case 'D':
                return "давальны";
            case 'A':
                return "вінавальны";
            case 'I':
                return "творны";
            case 'L':
                return "месны";
            case 'V':
                return "клічны";
            default:
                return "";
            }
        }

        public String getCode() {
            switch (sklon()) {
            case 'N':
                return "Н";
            case 'G':
                return "Р";
            case 'D':
                return "Д";
            case 'A':
                return "В";
            case 'I':
                return "Т";
            case 'L':
                return "М";
            case 'V':
                return "К";
            default:
                return "";
            }
        }

        public String getForm(int col) {
            switch (col) {
            case 0:
                return fm;
            case 1:
                return ff;
            case 2:
                return fn;
            case 3:
                return fp;
            default:
                return null;
            }
        }

        public void setForm(int col, String form) {
            switch (col) {
            case 0:
                fm = form;
                break;
            case 1:
                ff = form;
                break;
            case 2:
                fn = form;
                break;
            case 3:
                fp = form;
                break;
            }
        }

        public int getFormEnd(int col) {
            switch (col) {
            case 0:
                return fme;
            case 1:
                return ffe;
            case 2:
                return fne;
            case 3:
                return fpe;
            default:
                return 0;
            }
        }

        public void setFormEnd(int col, int end) {
            switch (col) {
            case 0:
                fme = end;
                break;
            case 1:
                ffe = end;
                break;
            case 2:
                fne = end;
                break;
            case 3:
                fpe = end;
                break;
            }
        }
    }
}
