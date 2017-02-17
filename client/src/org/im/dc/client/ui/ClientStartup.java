package org.im.dc.client.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.im.dc.client.WS;
import org.im.dc.service.dto.Article;

public class ClientStartup {

    public static void main(String[] args) {
        try {
            Thread.sleep(1000);
            System.out.println("start");
            WS.init("http://localhost:9080/myapp/articles?wsdl", "u", "p");

            long be = System.currentTimeMillis();
            ordered();
            long af = System.currentTimeMillis();
            System.out.println(af - be);
        } catch (Throwable ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error start client: " + ex.getMessage());
        }
    }
}
