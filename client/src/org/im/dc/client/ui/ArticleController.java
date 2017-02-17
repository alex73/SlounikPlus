package org.im.dc.client.ui;

import java.util.Arrays;

import javax.swing.JFrame;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.service.dto.ArticleFullInfo;

/**
 * Controls article editor.
 */
public class ArticleController extends BaseController<ArticleEditDialog> {
    private XmlGroup editorUI;

    private ArticleFullInfo article;

    public ArticleController(JFrame parent, int articleId) {
        super(new ArticleEditDialog(parent, false));

        displayOn(parent);

        // request article from server
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                article = WS.getArticleService().getArticleFullInfo(WS.header, articleId);
            }

            @Override
            protected void ok() {
                show();
            }
        };
    }

    private void show() {
        window.txtWords.setText(Arrays.toString(article.article.words));
        window.txtState.setText(article.article.state);
        window.txtUsers.setText(Arrays.toString(article.article.assignedUsers));
        if (article.article.notes != null) {
            window.txtNotes.setText(article.article.notes);
        }

        editorUI = SchemaLoader.createUI();
        window.panelEditor.add(editorUI);
    }
}
