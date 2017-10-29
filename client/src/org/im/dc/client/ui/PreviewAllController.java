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
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.im.dc.client.WS;

public class PreviewAllController extends BaseController<PreviewAllDialog> {
    private static Font BASE_FONT;

    public PreviewAllController(int[] articleIds) {
        super(new PreviewAllDialog(MainController.instance.window, false), MainController.instance.window);
        setupCloseOnEscape();

        if (BASE_FONT == null) {
            BASE_FONT = window.getFont().deriveFont(Font.PLAIN);
        }
        window.output.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        window.output.setFont(BASE_FONT);
        //window.output.getActionMap().put("copy", clipboardAction);
        //window.output.getActionMap().put("cut", clipboardAction);

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

        window.output.addHyperlinkListener(linkListener);

        window.btnRefresh.addActionListener(l -> {
            show(articleIds);
        });

        show(articleIds);

        displayOnParent();
    }

    void show(int[] articleIds) {
        new LongProcess() {
            StringBuilder out;

            @Override
            protected void exec() throws Exception {
                out = new StringBuilder("<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body>\n");

                String[] articlesPreview = WS.getToolsWebservice().preparePreviews(WS.header, articleIds);
                for (int i = 0; i < articleIds.length; i++) {
                    if (articlesPreview[i] == null) {
                        continue;
                    }
                    out.append(articlesPreview[i]);
                    out.append(" <a href='" + articleIds[i] + "'>рэдагаваць</a>\n");
                    out.append("<hr/>\n");
                }
                out.append("\n</body></html>\n");
            }

            @Override
            protected void ok() {
                window.output.setText(out.toString());
                window.output.getDocument().putProperty("ZOOM_FACTOR", new Double(2.5));
            }

            @Override
            protected void error() {
                window.dispose();
            }
        };
    }

    HyperlinkListener linkListener = new HyperlinkListener() {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                int idx = Integer.parseInt(e.getDescription());
                new ArticleEditController(idx);
            }
        }
    };

    ActionListener resetZoom = (e) -> {
        BASE_FONT = window.getFont().deriveFont(Font.PLAIN);
        window.output.setFont(BASE_FONT);
    };
    ActionListener decZoom = (e) -> {
        BASE_FONT = BASE_FONT.deriveFont(BASE_FONT.getSize2D() / 1.2f);
        window.output.setFont(BASE_FONT);
    };
    ActionListener incZoom = (e) -> {
        BASE_FONT = BASE_FONT.deriveFont(BASE_FONT.getSize2D() * 1.2f);
        window.output.setFont(BASE_FONT);
    };

    @SuppressWarnings("serial")
    AbstractAction clipboardAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            StringSelection selection = new StringSelection(window.output.getSelectedText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        }
    };
}
