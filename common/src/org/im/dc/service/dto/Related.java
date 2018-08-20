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

    public String articleTypeId;

    public int id;

    // спасылаецца на артыкул
    public int articleId;
    // загаловак артыкула (не заўсёды вызначаны)
    public String header;

    // калі быў зменены
    public Date when;
    // хто змяніў
    public String who;
    // скарот тыпу
    public String sk;
    // падрабязнасьці
    public String what;

    public boolean requiresActivity;

    public String getDescription() {
        return sk + ": " + what;
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
