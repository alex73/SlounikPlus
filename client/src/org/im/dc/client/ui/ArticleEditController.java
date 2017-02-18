package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.soap.SOAPFaultException;

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
        window.btnChangeState.addActionListener((e) -> changeStateAsk());
        window.btnProposeSave.addActionListener((e) -> proposeChanges());
        window.lblAddComment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addComment();
            }
        });
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

        try {
            editorUI = SchemaLoader.createUI();
            if (article.article.xml != null) {
                XMLStreamReader rd = XMLInputFactory.newInstance()
                        .createXMLStreamReader(new ByteArrayInputStream(article.article.xml));
                rd.nextTag();
                editorUI.insertData(rd);
            }
        } catch (Throwable ex) {
            editorUI = null;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(window,
                    "Памылка чытання XML: " + ((SOAPFaultException) ex).getFault().getFaultString(), "Памылка",
                    JOptionPane.ERROR_MESSAGE);
        }
        window.panelEditor.setViewportView(editorUI);

        Collections.sort(article.related, new Comparator<ArticleFullInfo.Related>() {
            @Override
            public int compare(ArticleFullInfo.Related r1, ArticleFullInfo.Related r2) {
                return r2.when.compareTo(r1.when);
            }
        });
        window.tableHistory.setModel(new ArticleEditRelatedModel(article.related));
    }

    private byte[] extractXml() throws Exception {
        StringWriter w = new StringWriter();
        XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
        editorUI.extractData("root", wr);
        wr.flush();
        return w.toString().getBytes("UTF-8");
    }

    /**
     * Захоўвае змены на серверы.
     */
    private void save() {
        article.article.notes = window.txtNotes.getText();
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                article.article.xml = extractXml();

                article = WS.getArticleService().saveArticle(WS.header, article.article);
            }

            @Override
            protected void ok() {
                show();
            }
        };
    }

    private void saveState(String newState, Runnable ok) {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                article = WS.getArticleService().changeState(WS.header, article.article.id, newState,
                        article.article.lastUpdated);
            }

            @Override
            protected void ok() {
                ok.run();
                show();
            }
        };
    }

    private void saveComment(String comment, Runnable ok) {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                article = WS.getArticleService().addComment(WS.header, article.article.id, comment);
            }

            @Override
            protected void ok() {
                ok.run();
                show();
            }
        };
    }

    private void saveProposal(String comment, Runnable ok) {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                byte[] proposedXml = extractXml();

                article = WS.getArticleService().addIssue(WS.header, article.article.id, comment, proposedXml,
                        article.article.lastUpdated);
            }

            @Override
            protected void ok() {
                ok.run();
                show();
            }
        };
    }

    private void changeStateAsk() {
        ArticleEditNewStateDialog askState = new ArticleEditNewStateDialog(MainController.mainWindow, true);

        for (String state : article.youCanChangeStateTo) {
            JRadioButton rb = new JRadioButton(state);
            askState.statesGroup.add(rb);
            askState.panelStates.add(rb);
        }

        askState.btnChange.addActionListener((e) -> {
            String newState = null;
            for (int i = 0; i < askState.panelStates.getComponentCount(); i++) {
                JRadioButton rb = (JRadioButton) askState.panelStates.getComponent(i);
                if (rb.isSelected()) {
                    newState = rb.getText();
                    break;
                }
            }
            saveState(newState, () -> {
                askState.dispose();
                window.requestFocus();
            });
        });

        // setup cancel button
        ActionListener cancelListener = (e) -> {
            askState.dispose();
        };
        askState.btnCancel.addActionListener(cancelListener);
        askState.getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        askState.pack();
        askState.setLocationRelativeTo(window);
        askState.setVisible(true);
    }

    private void addComment() {
        ArticleEditAddCommentDialog askComment = new ArticleEditAddCommentDialog(MainController.mainWindow, true);

        askComment.btnAdd.addActionListener((e) -> {
            saveComment(askComment.txtComment.getText(), () -> {
                askComment.dispose();
                window.requestFocus();
            });
        });

        // setup cancel button
        ActionListener cancelListener = (e) -> {
            askComment.dispose();
        };
        askComment.btnCancel.addActionListener(cancelListener);
        askComment.getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        askComment.setLocationRelativeTo(window);
        askComment.setVisible(true);
    }

    private void proposeChanges() {
        ArticleEditProposeChangesDialog askProposeComment = new ArticleEditProposeChangesDialog(
                MainController.mainWindow, true);

        askProposeComment.btnOk.addActionListener((e) -> {
            saveProposal(askProposeComment.txtComment.getText(), () -> {
                askProposeComment.dispose();
                window.requestFocus();
            });
        });

        // setup cancel button
        ActionListener cancelListener = (e) -> {
            askProposeComment.dispose();
        };
        askProposeComment.btnCancel.addActionListener(cancelListener);
        askProposeComment.getRootPane().registerKeyboardAction(cancelListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        askProposeComment.setLocationRelativeTo(window);
        askProposeComment.setVisible(true);
    }
}
