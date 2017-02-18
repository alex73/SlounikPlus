package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;

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
}
