package org.im.dc.client.ui;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

import org.im.dc.client.WS;

public class PreviewController extends BaseController<PreviewDialog> {
    private static Font BASE_FONT;

    public PreviewController(JDialog parent, ArticleEditController articleEditController) {
        super(new PreviewDialog(MainController.instance.window, true), parent);
        setupCloseOnEscape();

        if (BASE_FONT == null) {
            BASE_FONT = window.getFont().deriveFont(Font.PLAIN);
        }
        window.text.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        window.text.setFont(BASE_FONT);
        //window.text.getActionMap().put("copy", clipboardAction);
        //window.text.getActionMap().put("cut", clipboardAction);

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

        new LongProcess() {
            String preview;

            @Override
            protected void exec() throws Exception {
                String text = preview = WS.getToolsWebservice().preparePreview(WS.header,
                        articleEditController.article.article.words, articleEditController.extractXml());
                preview = "<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body>\n" + text
                        + "\n</body></html>\n";
            }

            @Override
            protected void ok() {
                window.text.setText(preview);
                window.text.getDocument().putProperty("ZOOM_FACTOR", new Double(2.5));
            }

            @Override
            protected void error() {
                window.dispose();
            }
        };
        displayOnParent();
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
