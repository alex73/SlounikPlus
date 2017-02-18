package org.im.dc.client.ui;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.service.dto.ArticleFullInfo;

/**
 * Controls article editor.
 */
public class ArticleEditController extends BaseController<ArticleEditDialog> {
    private XmlGroup editorUI;

    private volatile ArticleFullInfo article;

    public ArticleEditController(JFrame parent, int articleId) {
        super(new ArticleEditDialog(parent, false));
        setupCloseOnEscape();

        displayOn(parent);

        // request article from server
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                article = WS.getArticleService().getArticleFullInfo(WS.header, articleId);

                editorUI = SchemaLoader.createUI();
                if (article.article.xml != null) {
                    XMLStreamReader rd = XMLInputFactory.newInstance()
                            .createXMLStreamReader(new ByteArrayInputStream(article.article.xml));
                    rd.nextTag();
                    editorUI.insertData(rd);
                }
            }

            @Override
            protected void ok() {
                init();
                show();
            }

            @Override
            protected void error() {
                window.dispose();
            }
        };
    }

    private void init() {
        window.btnSave.addActionListener((e) -> save());
    }

    private void show() {
        window.btnSave.setVisible(article.youCanEdit);
        window.btnProposeSave.setVisible(!article.youCanEdit);
        window.btnChangeState.setVisible(!article.youCanChangeStateTo.isEmpty());

        window.setTitle(window.getTitle().replaceAll("\\[.*\\]", Arrays.toString(article.article.words)));
        window.txtWords.setText(Arrays.toString(article.article.words));
        window.txtState.setText(article.article.state);
        window.txtUsers.setText(Arrays.toString(article.article.assignedUsers));
        if (article.article.notes != null) {
            window.txtNotes.setText(article.article.notes);
        }
        window.panelEditor.setViewportView(editorUI);
    }

    /**
     * Захоўвае змены на серверы.
     */
    private void save() {
        article.article.notes = window.txtNotes.getText();
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                StringWriter w = new StringWriter();
                XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
                editorUI.extractData("root", wr);
                wr.flush();
                article.article.xml = w.toString().getBytes("UTF-8");

                article = WS.getArticleService().saveArticle(WS.header, article.article);

                editorUI = SchemaLoader.createUI();
                XMLStreamReader rd = XMLInputFactory.newInstance()
                        .createXMLStreamReader(new ByteArrayInputStream(article.article.xml));
                rd.nextTag();
                editorUI.insertData(rd);
            }

            @Override
            protected void ok() {
                show();
            }
        };
    }
}
