package org.im.dc.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.xml.ws.soap.SOAPFaultException;

import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * Base functionality for controllers.
 */
public abstract class BaseController<T extends Window> {
    /** UI window for this controller(JFrame or JDialog) */
    public final T window;
    protected final Window parentWindow;
    protected DockingDesktop desk;

    /**
     * Remember UI window, then create glass pane with animated gif.
     */
    public BaseController(T newWindow, Window parentWindow) {
        this.window = newWindow;
        this.parentWindow = parentWindow;
        SettingsController.setupFontForWindow(window);
        SettingsController.loadPlacesForWindow(window, desk);

        if (window instanceof JDialog) {
            JDialog d = (JDialog) window;
            d.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (closing()) {
                    window.dispose();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                SettingsController.savePlacesForWindow(window, desk);
                SwingUtilities.invokeLater(() -> parentWindow.requestFocus());
            }
        });

        @SuppressWarnings("serial")
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

        JLabel la = new JLabel(new ImageIcon(BaseController.class.getResource("images/progress.gif")));
        la.setOpaque(false);
        glass.add(la);
        ((RootPaneContainer) window).setGlassPane(glass);
    }

    protected boolean closing() {
        return true;
    }

    /**
     * Show UI on the center of parent.
     */
    protected void displayOnParent() {
        window.setLocationRelativeTo(parentWindow);
        window.setVisible(true);
    }

    /**
     * Зачыняць вакно па націсканьні ESC.
     */
    protected void setupCloseOnEscape() {
        ActionListener cancelListener = (e) -> {
            if (closing()) {
                window.dispose();
            }
        };
        ((RootPaneContainer) window).getRootPane().registerKeyboardAction(cancelListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

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
    public abstract class LongProcess extends SwingWorker<Void, Void> {
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
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(window, "Interrupted: " + ex.getMessage(), "Памылка",
                        JOptionPane.ERROR_MESSAGE);
                error();
            } catch (Exception e) {
                Throwable ex = e.getCause();
                if (ex == null) {
                    ex = e;
                }
                if (ex instanceof Error) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(window, "Error " + ex.getClass().getName() + ": " + ex.getMessage(),
                            "Памылка", JOptionPane.ERROR_MESSAGE);
                    error();
                } else if (ex instanceof SOAPFaultException) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(window,
                            "Remote error: " + ((SOAPFaultException) ex).getFault().getFaultString(), "Памылка",
                            JOptionPane.ERROR_MESSAGE);
                    error();
                } else {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(window, "Error " + ex.getClass().getName() + ": " + ex.getMessage(),
                            "Памылка", JOptionPane.ERROR_MESSAGE);
                    error();
                }
            }
            hideProgress();
        }

        abstract protected void exec() throws Exception;

        protected void ok() {
        }

        protected void error() {
        }
    }

    protected void todo(String todo) {
        JOptionPane.showMessageDialog(window, "TODO: " + todo, "Яшчэ не зроблена", JOptionPane.WARNING_MESSAGE);
    }
}
