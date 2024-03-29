package org.im.dc.client.ui;

public class ClientStartup {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("ClientStartup <addr>, where addr~=http://localhost:9081/myapp");
            System.exit(1);
        }

        SettingsController.initialize();

        MainController co = new MainController(args[0]);
        if (args[0].startsWith("http://") || args[0].startsWith("https://")) {
            if (args.length == 3) {
                co.startWithUser(args[1], args[2]);
            } else {
                co.start();
            }
        } else {
            if (args.length == 2) {
                co.startWithUser(args[1], null);
            } else {
                System.err.println("ClientStartup <git addr> <app user>");
                System.exit(1);
            }
        }
    }
}
