package org.im.dc.client.ui;

public class ClientStartup {
    public static void main(String[] args) throws Exception {
        SettingsController.initialize();

        MainController co = new MainController();
        if (args.length == 2) {
            co.startWithUser(args[0], args[1]);
        } else {
            co.start();
        }
    }
}
