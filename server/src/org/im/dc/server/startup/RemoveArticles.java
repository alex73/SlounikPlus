package org.im.dc.server.startup;

import java.io.File;

import org.im.dc.server.Config;
import org.im.dc.server.Db;

public class RemoveArticles {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("RemoveArticles <dir>");
            System.exit(1);
        }

        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init();

        Db.exec((api) -> {
            try {
                api.getSession().getConnection().createStatement().execute("DELETE FROM ArticleNotes");
                api.getSession().getConnection().createStatement().execute("DELETE FROM Issues");
                api.getSession().getConnection().createStatement().execute("DELETE FROM Comments");
                api.getSession().getConnection().createStatement().execute("DELETE FROM ArticlesHistory");
                api.getSession().getConnection().createStatement().execute("DELETE FROM Articles");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
