package org.im.dc.client.ui;

import java.util.Arrays;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.service.dto.ArticleFullInfo;

public class ArticleController {
    private XmlGroup editorUI;

    public ArticleController(int articleId) {
        if (UI.checkError(() -> {
            ArticleFullInfo article = WS.getArticleService().getArticleFullInfo(WS.header, articleId);
            show(article);
        })) {
            return;
        }
    }

    private void show(ArticleFullInfo article) {
        ArticleEditDialog dialog = new ArticleEditDialog(MainController.ui, false);

        dialog.txtWords.setText(Arrays.toString(article.article.words));
        dialog.txtState.setText(article.article.state);
        dialog.txtUsers.setText(Arrays.toString(article.article.assignedUsers));
        if (article.article.notes != null) {
            dialog.txtNotes.setText(article.article.notes);
        }

        editorUI = SchemaLoader.createUI();
        dialog.panelEditor.add(editorUI);

        UI.displayDialog(dialog);
    }
}
