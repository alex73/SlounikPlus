package org.im.dc.client.ui.xmlstructure.tlum;

import org.alex73.corpus.paradigm.Paradigm;

public class VariantInfo {
    Paradigm p;
    int id;
    char variantIndex;
    String tag;
    String lemma;

    public VariantInfo(String s) {
        String[] p = s.split("/");
        if (p.length == 3) {
            id = Integer.parseInt(p[0].substring(0, p[0].length() - 1));
            variantIndex = p[0].charAt(p[0].length() - 1);
            tag = p[1];
            lemma = p[2];
        }
    }

    public VariantInfo(Paradigm p, char variantIndex) {
        this.p = p;
        this.id = p.getPdgId();
        this.variantIndex = variantIndex;
        this.tag = p.getTag();
        this.lemma = p.getVariant().get(variantIndex - 'a').getLemma();
    }

    @Override
    public String toString() {
        return getVariantId() + "/" + tag + "/" + lemma;
    }

    public String getVariantId() {
        return Integer.toString(id) + variantIndex;
    }
}
