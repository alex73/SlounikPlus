package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.im.dc.client.WS;
import org.im.dc.service.dto.ArticleCommentFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleHistoryFull;
import org.im.dc.service.dto.ArticleIssueFull;
import org.im.dc.service.dto.Related;

public class ArticleDetailsController extends BaseController<ArticleDetailsDialog> {
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private SimpleDateFormat TIME_FMT = new SimpleDateFormat("dd MMM HH:mm");

    private ArticleEditController parent;
    private Related rel;
    private String xmlOld, xmlNew;
    private String commentText;
    private byte[] newXml;

    public ArticleDetailsController(ArticleEditController parent, Related rel) {
        super(new ArticleDetailsDialog(MainController.instance.window, true), parent.window);
        this.parent = parent;
        this.rel = rel;
        setupCloseOnEscape();

        // request article from server
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                JButton closeButton = new JButton("Зачыніць");
                closeButton.addActionListener((e) -> window.dispose());
                switch (rel.type) {
                case HISTORY:
                    ArticleHistoryFull history = WS.getArticleService().getHistory(WS.header, rel.id);
                    xmlOld = xml2text(history.oldXml);
                    xmlNew = xml2text(history.newXml);
                    window.panelHeader.add(new JLabel("Змены " + history.who + " ад " + TIME_FMT.format(history.when),
                            null, JLabel.CENTER));
                    window.panelButtons.add(closeButton);
                    break;
                case COMMENT:
                    ArticleCommentFull comment = WS.getArticleService().getComment(WS.header, rel.id);
                    commentText = comment.comment;
                    window.panelHeader.add(new JLabel(
                            "Каментар " + comment.who + " ад " + TIME_FMT.format(comment.when), null, JLabel.CENTER));
                    window.panelButtons.add(closeButton);
                    break;
                case ISSUE:
                    ArticleIssueFull issue = WS.getArticleService().getIssue(WS.header, rel.id);
                    if (issue.newXml != null) {
                        xmlOld = xml2text(issue.oldXml);
                        xmlNew = xml2text(issue.newXml);
                    } else {
                        commentText = issue.comment;
                    }
                    String z = "Заўвага " + issue.who + " ад " + TIME_FMT.format(issue.when);
                    if (issue.fixed != null) {
                        z += " - улічаная " + issue.fixer + " " + TIME_FMT.format(issue.fixed);
                    } else {
                        JButton process, reject;
                        if (issue.newXml == null) {
                            z += " - актуальная";
                            process = new JButton("Пацвердзіць выкананне");
                            process.addActionListener(acceptIssue);
                        } else if (Arrays.equals(parent.article.article.xml, issue.oldXml)) {
                            newXml = issue.newXml;
                            z += " - актуальная";
                            process = new JButton("Прыняць змены");
                            process.addActionListener(acceptIssue);
                        } else {
                            z += " - немагчыма прыняць, бо зьмяніўся артыкул";
                            process = new JButton("Пацвердзіць выкананне");
                            process.addActionListener(acceptIssue);
                        }
                        window.panelButtons.add(process);
                        reject = new JButton("Адкінуць заўвагу");
                        reject.addActionListener(rejectIssue);
                        window.panelButtons.add(reject);
                    }
                    window.panelHeader.add(new JLabel(z, null, JLabel.CENTER));
                    if (commentText == null && issue.comment != null && !issue.comment.trim().isEmpty()) {
                        window.panelHeader.add(new JLabel(issue.comment, null, JLabel.CENTER));
                    }
                    window.panelButtons.add(closeButton);
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

        displayOnParent();
    }

    /**
     * Прыняць змены ці пацвердзіць выкананне.
     */
    private ActionListener acceptIssue = (e) -> {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                if (newXml != null) {
                    parent.article.article.xml = newXml;
                    ArticleFullInfo ai = WS.getArticleService().saveArticle(WS.header, parent.article.article.type,
                            parent.article.article);
                    MainController.instance.fireArticleUpdated(ai.article);
                }
                parent.article = WS.getArticleService().fixIssue(WS.header, parent.article.article.type,
                        parent.article.article.id, rel.id, true);
            }

            @Override
            protected void ok() {
                parent.show();
                window.dispose();
            }
        };
    };

    private ActionListener rejectIssue = (e) -> {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                parent.article = WS.getArticleService().fixIssue(WS.header, parent.article.article.type,
                        parent.article.article.id, rel.id, false);
            }

            @Override
            protected void ok() {
                parent.show();
                window.dispose();
            }
        };
    };

    private String xml2text(byte[] xml) throws Exception {
        if (xml == null) {
            return "";
        }
        Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StreamResult res = new StreamResult(new StringWriter());
        Source sou = new StreamSource(new ByteArrayInputStream(xml));
        transformer.transform(sou, res);
        return res.getWriter().toString();
    }

    private void show() {
        if (commentText != null) {
            JTextArea a = new JTextArea();
            a.setLineWrap(true);
            a.setWrapStyleWord(true);
            a.setText(commentText);
            window.scroll.getViewport().add(a);
        } else {
            XMLDiffPanel p = new XMLDiffPanel(xmlOld, xmlNew);
            window.scroll.getViewport().add(p);
        }
    }
}
