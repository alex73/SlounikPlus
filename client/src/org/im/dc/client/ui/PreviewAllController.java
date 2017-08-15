package org.im.dc.client.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.im.dc.client.WS;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleShort;

public class PreviewAllController extends BaseController<PreviewAllDialog> {
    private static Font BASE_FONT;

    public PreviewAllController(List<ArticleShort> articles) {
        super(new PreviewAllDialog(MainController.instance.window, false), MainController.instance.window);
        setupCloseOnEscape();

        if (BASE_FONT == null) {
            BASE_FONT = window.getFont().deriveFont(Font.PLAIN);
        }
        window.output.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        window.output.setFont(BASE_FONT);
        window.output.getActionMap().put("copy", emptyAction);
        window.output.getActionMap().put("cut", emptyAction);

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
            show(articles);
        });

        show(articles);

        displayOnParent();
    }

    void show(List<ArticleShort> articles) {
        new LongProcess() {
            StringBuilder out;

            @Override
            protected void exec() throws Exception {
                out = new StringBuilder("<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body>\n");

                for (ArticleShort a : articles) {
                    ArticleFullInfo info = WS.getArticleService().getArticleFullInfo(WS.header, a.id);
                    if (info.article.xml == null) {
                        continue;
                    }
                    out.append(WS.getToolsWebservice().preparePreview(WS.header, info.article.words, info.article.xml));
                    out.append(" <a href='" + a.id + "'>рэдагаваць</a>\n");
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
    AbstractAction emptyAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
        }
    };
}
