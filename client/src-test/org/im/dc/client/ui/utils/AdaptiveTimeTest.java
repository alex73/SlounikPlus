package org.im.dc.client.ui.utils;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.junit.Test;

public class AdaptiveTimeTest {
    static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    static final Locale BE = new Locale("be");

    @Test
    public void dates() throws Exception {
        assertEquals("2021-03-25 11:45", AdaptiveTime.format(DF.parse("2021-03-25 11:45"), DF.parse("2021-03-25 11:44"), BE));
        assertEquals("11:45", AdaptiveTime.format(DF.parse("2021-03-25 11:45"), DF.parse("2021-03-25 11:46"), BE));
        assertEquals("11:45", AdaptiveTime.format(DF.parse("2021-03-25 11:45"), DF.parse("2021-03-25 11:46"), BE));
        assertEquals("11:45", AdaptiveTime.format(DF.parse("2021-03-25 11:45"), DF.parse("2021-03-25 19:44"), BE));
        assertEquals("сёння а 11:45",
                AdaptiveTime.format(DF.parse("2021-03-25 11:45"), DF.parse("2021-03-25 19:45"), BE));
        assertEquals("сёння а 00:00",
                AdaptiveTime.format(DF.parse("2021-03-25 00:00"), DF.parse("2021-03-25 11:45"), BE));
        assertEquals("учора а 23:59",
                AdaptiveTime.format(DF.parse("2021-03-24 23:59"), DF.parse("2021-03-25 11:45"), BE));
        assertEquals("23:00", AdaptiveTime.format(DF.parse("2021-03-24 23:00"), DF.parse("2021-03-25 03:45"), BE));
        assertEquals("пн 23:00", AdaptiveTime.format(DF.parse("2021-03-22 23:00"), DF.parse("2021-03-25 03:45"), BE));
        assertEquals("пт 23:00", AdaptiveTime.format(DF.parse("2021-03-19 23:00"), DF.parse("2021-03-25 03:45"), BE));
        assertEquals("01 сакавіка", AdaptiveTime.format(DF.parse("2021-03-01 23:00"), DF.parse("2021-03-25 03:45"), BE));
        assertEquals("сакавіка 2020",
                AdaptiveTime.format(DF.parse("2020-03-01 23:00"), DF.parse("2021-03-25 03:45"), BE));
    }
}
