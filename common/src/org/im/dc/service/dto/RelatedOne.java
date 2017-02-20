package org.im.dc.service.dto;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Рэчы што датычацца артыкулаў.
 */
public class RelatedOne {
    public enum RelatedType {
        HISTORY, COMMENT, ISSUE
    };

    public RelatedType type;

    public int id;

    // калі быў зменены
    public Date when;
    // хто змяніў
    public String who;
    public String what;

    public static void sortByTimeDesc(List<RelatedOne> list) {
        Collections.sort(list, new Comparator<RelatedOne>() {
            @Override
            public int compare(RelatedOne r1, RelatedOne r2) {
                return r2.when.compareTo(r1.when);
            }
        });
    }
}
