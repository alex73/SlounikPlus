package org.im.dc.client.ui.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdaptiveTime {
    public static String format(Date eventDate) {
        return format(eventDate, new Date(), Locale.getDefault());
    }

    public static String format(Date eventDate, Date now, Locale loc) {
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTime(eventDate);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);

        long diff = now.getTime() - eventDate.getTime();
        // in future
        if (diff < 0) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", loc).format(eventDate);
        }

        // some hours ago
        if (diff < 8L * 60 * 60 * 1000) {
            return new SimpleDateFormat("HH:mm", loc).format(eventDate);
        }

        // today
        if (eventCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
                && eventCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)) {
            String tm = new SimpleDateFormat("HH:mm", loc).format(eventDate);
            return "be".equals(loc.getLanguage()) ? "сёння а " + tm : "today at " + tm;
        }

        // yesterday
        Calendar days = Calendar.getInstance();
        days.setTime(now);
        days.add(Calendar.DAY_OF_MONTH, -1);
        if (eventCal.get(Calendar.YEAR) == days.get(Calendar.YEAR)
                && eventCal.get(Calendar.DAY_OF_YEAR) == days.get(Calendar.DAY_OF_YEAR)) {
            String tm = new SimpleDateFormat("HH:mm", loc).format(eventDate);
            return "be".equals(loc.getLanguage()) ? "учора а " + tm : "yesterday at " + tm;
        }

        // some days ago
        if (diff < 6L * 24 * 60 * 60 * 1000) {
            return new SimpleDateFormat("E HH:mm", loc).format(eventDate);
        }

        // less than year ago
        if (diff < 8L * 30 * 24 * 60 * 60 * 1000) {
            return new SimpleDateFormat("dd MMMM", loc).format(eventDate);
        }

        // long time ago
        return new SimpleDateFormat("MMMM yyyy", loc).format(eventDate);
    }
}
