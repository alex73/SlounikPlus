package org.im.dc.client.ui;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

public class PreviewController extends BaseController<PreviewDialog> {

    public static String HTML_PREFIX = "<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body>\n";
    public static String HTML_SUFFIX = "\n</body></html>\n";
    private static Font BASE_FONT;

    private Runnable executor;
    private String html;

    public PreviewController(Window parent, boolean modal) {
        super(new PreviewDialog(MainController.instance.window, modal), parent);

        setupCloseOnEscape();

        if (BASE_FONT == null) {
            BASE_FONT = window.getFont().deriveFont(Font.PLAIN);
        }
        window.text.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        window.text.setFont(BASE_FONT);
        // window.text.getActionMap().put("copy", clipboardAction);
        // window.text.getActionMap().put("cut", clipboardAction);
        window.btnRefresh.addActionListener(e -> execute());
        window.btnBrowser.addActionListener(e -> browser());

        ((RootPaneContainer) window).getRootPane().registerKeyboardAction(decZoom,
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        ((RootPaneContainer) window).getRootPane().registerKeyboardAction(decZoom,
                KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        ((RootPaneContainer) window).getRootPane().registerKeyboardAction(incZoom,
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        ((RootPaneContainer) window).getRootPane().registerKeyboardAction(incZoom,
                KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        ((RootPaneContainer) window).getRootPane().registerKeyboardAction(resetZoom,
                KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        ((RootPaneContainer) window).getRootPane().registerKeyboardAction(resetZoom,
                KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        SwingUtilities.invokeLater(() -> displayOnParent());
    }

    public void setupExecutor(Runnable executor) {
        this.executor = executor;
    }

    public void execute() {
        executor.run();
    }

    public void setHtml(String html) {
        this.html = html;
        window.text.setText(html);
        window.text.setCaretPosition(0);
        window.text.getDocument().putProperty("ZOOM_FACTOR", Double.valueOf(2.5));
    }

    public void browser() {
        try {
            Path f = Files.createTempFile("slounikplus", ".html");
            Files.writeString(f, html);
            Desktop.getDesktop().browse(f.toUri());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(window, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    ActionListener resetZoom = (e) -> {
        BASE_FONT = window.getFont().deriveFont(Font.PLAIN);
        window.text.setFont(BASE_FONT);
    };
    ActionListener decZoom = (e) -> {
        BASE_FONT = BASE_FONT.deriveFont(BASE_FONT.getSize2D() / 1.2f);
        window.text.setFont(BASE_FONT);
    };
    ActionListener incZoom = (e) -> {
        BASE_FONT = BASE_FONT.deriveFont(BASE_FONT.getSize2D() * 1.2f);
        window.text.setFont(BASE_FONT);
    };

    @SuppressWarnings("serial")
    AbstractAction clipboardAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            StringSelection selection = new StringSelection(window.text.getSelectedText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        }
    };
}
