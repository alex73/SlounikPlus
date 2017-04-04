package org.im.dc.client.ui;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.im.dc.client.WS;
import org.im.dc.service.dto.ArticleCommentFull;
import org.im.dc.service.dto.ArticleHistoryFull;
import org.im.dc.service.dto.Related;

public class ArticleDetailsController extends BaseController<ArticleDetailsDialog> {
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private String who;
    private Date when;
    private String xmlOld, xmlNew;
    private String commentText;

    public ArticleDetailsController(JDialog parent, Related rel) {
        super(new ArticleDetailsDialog(MainController.instance.window, true));
        setupCloseOnEscape();

        // request article from server
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                switch (rel.type) {
                case HISTORY:
                    ArticleHistoryFull history = WS.getArticleService().getHistory(WS.header, rel.id);
                    who = history.who;
                    when = history.when;
                    Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                    if (history.oldXml != null) {
                        StreamResult resultOld = new StreamResult(new StringWriter());
                        Source sourceOld = new StreamSource(new ByteArrayInputStream(history.oldXml));
                        transformer.transform(sourceOld, resultOld);
                        xmlOld = resultOld.getWriter().toString();
                    } else {
                        xmlOld = "";
                    }

                    if (history.newXml != null) {
                        StreamResult resultNew = new StreamResult(new StringWriter());
                        Source sourceNew = new StreamSource(new ByteArrayInputStream(history.newXml));
                        transformer.transform(sourceNew, resultNew);
                        xmlNew = resultNew.getWriter().toString();
                    } else {
                        xmlNew = "";
                    }
                    break;
                case COMMENT:
                    ArticleCommentFull comment = WS.getArticleService().getComment(WS.header, rel.id);
                    who = comment.who;
                    when = comment.when;
                    commentText = comment.comment;
                    break;
                }
            }

            @Override
            protected void ok() {
                show();
            }

            @Override
            protected void error() {
                window.dispose();
            }
        };

        displayOn(parent);
    }

    private void show() {
        if (commentText != null) {
            JTextArea a = new JTextArea();
            a.setLineWrap(true);
            a.setWrapStyleWord(true);
            a.setText(commentText);
            window.getContentPane().add(a);
        } else {
            XMLDiffPanel p = new XMLDiffPanel(xmlOld, xmlNew);
            window.getContentPane().add(p);
        }
    }
}
