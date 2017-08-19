package org.im.dc.client.ui;

import java.awt.event.ActionListener;

import javax.swing.JRadioButton;

import org.im.dc.client.WS;

public class ArticleEditNewStateController extends BaseController<ArticleEditNewStateDialog> {
    private final ArticleEditController parent;

    public ArticleEditNewStateController(ArticleEditController parent) {
        super(new ArticleEditNewStateDialog(MainController.instance.window, true), parent.window);
        this.parent = parent;

        for (String state : parent.article.youCanChangeStateTo) {
            JRadioButton rb = new JRadioButton(state);
            window.statesGroup.add(rb);
            window.panelStates.add(rb);
        }

        window.btnChange.addActionListener(change);
        window.btnCancel.addActionListener((e) -> window.dispose());

        setupCloseOnEscape();
        window.pack();

        displayOnParent();
    }

    ActionListener change = (e) -> {
        String newState = null;
        for (int i = 0; i < window.panelStates.getComponentCount(); i++) {
            JRadioButton rb = (JRadioButton) window.panelStates.getComponent(i);
            if (rb.isSelected()) {
                newState = rb.getText();
                break;
            }
        }
        String ns = newState;

        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                parent.article = WS.getArticleService().changeState(WS.header, parent.article.article.id, ns,
                        parent.article.article.lastUpdated);
                MainController.instance.fireArticleUpdated(parent.article.article);
            }

            @Override
            protected void ok() {
                window.dispose();
                parent.show();
            }
        };
    };
}
