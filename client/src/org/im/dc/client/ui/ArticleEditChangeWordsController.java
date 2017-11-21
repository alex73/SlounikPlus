package org.im.dc.client.ui;

import java.awt.event.ActionListener;

import org.im.dc.client.WS;

public class ArticleEditChangeWordsController extends BaseController<ArticleEditChangeWordsDialog> {
    private final ArticleEditController parent;

    public ArticleEditChangeWordsController(ArticleEditController parent) {
        super(new ArticleEditChangeWordsDialog(MainController.instance.window, true), parent.window);
        this.parent = parent;
        window.txtWords.setText(parent.article.article.header);

        window.btnChange.addActionListener(change);

        setupCloseOnEscape();

        window.btnCancel.addActionListener((e) -> window.dispose());
        displayOnParent();
    }

    ActionListener change = (e) -> {
        String header = window.txtWords.getText();
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                parent.article = WS.getArticleService().changeHeader(WS.header, parent.article.article.type,
                        parent.article.article.id, header, parent.article.article.lastUpdated);
            }

            @Override
            protected void ok() {
                window.dispose();
                parent.show();
            }
        };
    };
}
