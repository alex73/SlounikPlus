package org.im.dc.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingWorker;
import javax.xml.ws.soap.SOAPFaultException;

/**
 * Base functionality for controllers.
 */
public abstract class BaseController<T extends Window> {
    /** UI window for this controller(JFrame or JDialog) */
    protected final T window;

    /**
     * Remember UI window, then create glass pane with animated gif.
     */
    public BaseController(T window) {
        this.window = window;

        JPanel glass = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 128));
                Rectangle r = g.getClipBounds();
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        };
        glass.setOpaque(false);

        JLabel la = new JLabel(new ImageIcon(BaseController.class.getResource("/progress.gif")));
        la.setOpaque(false);
        glass.add(la);
        ((RootPaneContainer) window).setGlassPane(glass);
    }

    /**
     * Show UI on the center of parent.
     */
    protected void displayOn(JFrame parent) {
        window.setLocationRelativeTo(parent);
        window.setVisible(true);
    }

    /** Show glass pane. */
    protected void showProgress() {
        ((RootPaneContainer) window).getGlassPane().setVisible(true);
    }

    /** Hide glass pane. */
    protected void hideProgress() {
        ((RootPaneContainer) window).getGlassPane().setVisible(false);
    }

    /**
     * SwingWorker extension for controllers.
     */
    protected abstract class LongProcess extends SwingWorker<Void, Void> {
        public LongProcess() {
            showProgress();
            execute();
        }

        @Override
        protected Void doInBackground() throws Exception {
            exec();
            return null;
        }

        @Override
        protected void done() {
            try {
                get();
                ok();
            } catch (SOAPFaultException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(window, "Remote error: " + ex.getFault().getFaultString(), "Памылка",
                        JOptionPane.ERROR_MESSAGE);
                error();
            } catch (Throwable ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(window, "Error connect: " + ex.getMessage(), "Памылка",
                        JOptionPane.ERROR_MESSAGE);
                error();
            }
            hideProgress();
        }

        abstract protected void exec() throws Exception;

        protected void ok() {
        }

        protected void error() {
        }
    }
}
