package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.util.Arrays;

import org.im.dc.client.WS;

public class ArticleEditChangeWordsController extends BaseController<ArticleEditChangeWordsDialog> {
    private final ArticleEditController parent;

    public ArticleEditChangeWordsController(ArticleEditController parent) {
        super(new ArticleEditChangeWordsDialog(MainController.instance.window, true), parent.window);
        this.parent = parent;
        window.txtWords.setText(Arrays.toString(parent.article.article.words).replace("[", "").replace("]", ""));

        window.btnChange.addActionListener(change);

        setupCloseOnEscape();

        window.btnCancel.addActionListener((e) -> window.dispose());
        displayOnParent();
    }

    ActionListener change = (e) -> {
        String words = window.txtWords.getText();
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                parent.article = WS.getArticleService().changeWords(WS.header, parent.article.article.id, words,
                        parent.article.article.lastUpdated);
            }

            @Override
            protected void ok() {
                window.dispose();
                parent.show();
            }
        };
    };
}
