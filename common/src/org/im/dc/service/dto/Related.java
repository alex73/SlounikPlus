package org.im.dc.service.dto;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Рэчы што датычацца артыкулаў.
 */
public class Related {
    public enum RelatedType {
        HISTORY, COMMENT, ISSUE
    };

    public RelatedType type;

    public int id;

    // спасылаецца на артыкул
    public int articleId;
    // загалоўныя словы артыкула (не заўсёды вызначаныя)
    public String[] words;

    // калі быў зменены
    public Date when;
    // хто змяніў
    public String who;
    // падрабязнасьці
    public String what;

    public String getDescription() {
        switch (type) {
        case COMMENT:
            return "К:" + what;
        case HISTORY:
            return "В:" + what;
        case ISSUE:
            return "З:" + what;
        }
        return null;
    }

    public static void sortByTimeDesc(List<Related> list) {
        Collections.sort(list, new Comparator<Related>() {
            @Override
            public int compare(Related r1, Related r2) {
                return r2.when.compareTo(r1.when);
            }
        });
    }
}
