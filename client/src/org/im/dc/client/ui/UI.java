package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.xml.ws.soap.SOAPFaultException;

public class UI {

    static void displayDialog(JDialog dialog) {
        dialog.setLocationRelativeTo(MainController.ui);
        dialog.setVisible(true);
    }

    static void displayDialog(JDialog dialog, JButton btnOk, JButton btnCancel, Runnable ok, Runnable cancel) {
        dialog.getRootPane().setDefaultButton(btnOk);
        btnOk.addActionListener((e) -> {
            dialog.dispose();
            ok.run();
        });
        ActionListener cancelListener = (e) -> {
            dialog.dispose();
            if (cancel != null) {
                cancel.run();
            }
        };
        btnCancel.addActionListener(cancelListener);
        dialog.getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setLocationRelativeTo(MainController.ui);
        dialog.setVisible(true);
    }

    static boolean checkError(RunnableWithException r) {
        try {
            r.run();
        } catch (SOAPFaultException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(MainController.ui, "Remote error: " + ex.getFault().getFaultString(),
                    "Памылка", JOptionPane.ERROR_MESSAGE);
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(MainController.ui, "Error connect: " + ex.getMessage(), "Памылка",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    public interface RunnableWithException {
        void run() throws Exception;
    }
}
