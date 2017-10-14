package org.im.dc.client.os;

public class Commands {
    public static final boolean WINDOWS;

    static {
        String os = System.getProperty("os.name");
        WINDOWS = os.startsWith("Windows");
    }

    public static void showPdf(String path) throws Exception {
        if (WINDOWS) {
            Runtime.getRuntime().exec(new String[] { "cmd", "/c", path });
        } else {
            Runtime.getRuntime().exec(new String[] { "xdg-open", path });
        }
    }

    public static void main(String[] args) throws Exception {
        showPdf("../out.pdf");
    }
}
