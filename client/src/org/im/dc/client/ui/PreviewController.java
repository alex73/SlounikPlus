package org.im.dc.client.ui;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;

import org.im.dc.client.WS;

public class PreviewController extends BaseController<PreviewDialog> {
    private static Font BASE_FONT;

    public PreviewController(JDialog parent, ArticleEditController articleEditController) {
        super(new PreviewDialog(MainController.instance.window, true));
        setupCloseOnEscape();

        if (BASE_FONT == null) {
            BASE_FONT = window.getFont().deriveFont(Font.PLAIN);
        }
        window.text.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        window.text.setFont(BASE_FONT);

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
                preview = WS.getToolsWebservice().preparePreview(WS.header, articleEditController.article.article.words,
                        articleEditController.extractXml());
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
        displayOn(parent);
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
}
