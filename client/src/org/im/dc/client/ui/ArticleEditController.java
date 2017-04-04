package org.im.dc.client.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.Related;

/**
 * Controls article editor.
 */
public class ArticleEditController extends BaseController<ArticleEditDialog> {
    private XmlGroup editorUI;

    private volatile ArticleFullInfo article;

    public ArticleEditController(JFrame parent, int articleId) {
        super(new ArticleEditDialog(parent, false));

        ActionListener cancelListener = (e) -> {
            if (askSave()) {
                return;
            }
            window.dispose();
        };
        ((RootPaneContainer) window).getRootPane().registerKeyboardAction(cancelListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

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
        window.panelEditor.getVerticalScrollBar().setUnitIncrement(20);
        window.btnSave.addActionListener((e) -> save());
        window.btnChangeState.addActionListener((e) -> changeStateAsk());
        window.btnProposeSave.addActionListener((e) -> proposeChanges());
        window.lblAddComment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addComment();
            }
        });
        window.lblHasProposedChanges.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                todo("будзе паказваць ці ёсць прапанаваныя змены, і адчыняць дыялог параўнання");
            }
        });
        window.lblWatched.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeWatch();
            }
        });
        window.lblPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                todo("прагляд папяровага варыянта калі карыстальнік мае дазвол");
            }
        });
        window.txtWords.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                askWords();
            }
        });
        window.txtNotes.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                window.btnSave.setEnabled(true);
                window.btnProposeSave.setEnabled(true);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                window.btnSave.setEnabled(true);
                window.btnProposeSave.setEnabled(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                window.btnSave.setEnabled(true);
                window.btnProposeSave.setEnabled(true);
            }
        });
        window.tableHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArticleEditRelatedModel model = (ArticleEditRelatedModel) window.tableHistory.getModel();
                Related rel = model.related.get(window.tableHistory.getSelectedRow());
                new ArticleDetailsController(window, rel);
            }
        });
    }

    private void show() {
        window.btnSave.setVisible(article.youCanEdit);
        window.btnProposeSave.setVisible(!article.youCanEdit);
        window.txtNotes.setEditable(article.youCanEdit);
        window.btnChangeState.setVisible(!article.youCanChangeStateTo.isEmpty());
        displayWatch();

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
            editorUI.addChangeListener((e) -> {
                window.btnSave.setEnabled(true);
                window.btnProposeSave.setEnabled(true);
            });
        } catch (Throwable ex) {
            editorUI = null;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(window, "Памылка чытання XML артыкула: " + ex.getMessage(), "Памылка",
                    JOptionPane.ERROR_MESSAGE);
        }
        window.panelEditor.setViewportView(editorUI);
        window.btnSave.setEnabled(false);
        window.btnProposeSave.setEnabled(false);

        Related.sortByTimeDesc(article.related);

        SettingsController.savePlacesForWindow(window);
        window.tableHistory.setModel(new ArticleEditRelatedModel(article.related));
        SettingsController.loadPlacesForWindow(window);

        if (article.linksFrom.isEmpty()) {
            window.panelLinkedFrom.setVisible(false);
        } else {
            window.panelLinkedFrom.setVisible(true);
            window.panelLinkedFrom.removeAll();
            window.panelLinkedFrom.add(new JLabel("На гэты артыкул спасылаюцца:"));
            for (ArticleFullInfo.LinkFrom lf : article.linksFrom) {
                JLabel lbl = new JLabel(Arrays.toString(lf.words));
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        new ArticleEditController(MainController.instance.window, lf.articleId);
                    }
                });
                asLink(lbl);
                window.panelLinkedFrom.add(lbl);
            }
        }
        if (article.linksExternal.isEmpty()) {
            window.panelLinkedExternal.setVisible(false);
        } else {
            window.panelLinkedExternal.setVisible(true);
            window.panelLinkedExternal.removeAll();
            for (ArticleFullInfo.LinkExternal le : article.linksExternal) {
                JLabel lbl = new JLabel(le.name);
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI(le.url));
                        } catch (Exception ex) {
                        }
                    }
                });
                asLink(lbl);
                window.panelLinkedExternal.add(lbl);
            }
        }
    }

    private void asLink(JLabel label) {
        Font font = label.getFont();
        Map<TextAttribute, ?> attributes = font.getAttributes();
        ((Map) attributes).put(TextAttribute.FOREGROUND, Color.BLUE);
        ((Map) attributes).put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        label.setFont(font.deriveFont(attributes));
    }

    private void displayWatch() {
        window.lblWatched.setText(article.youWatched ? "*" : "-");
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

    private void saveWords(String words, Runnable ok) {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                article = WS.getArticleService().changeWords(WS.header, article.article.id, words,
                        article.article.lastUpdated);
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

    private void changeWatch() {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                WS.getArticleService().setWatch(WS.header, article.article.id, !article.youWatched);
                article.youWatched = !article.youWatched;
            }

            @Override
            protected void ok() {
                displayWatch();
            }
        };
    }

    private void changeStateAsk() {
        if (askSave()) {
            return;
        }

        ArticleEditNewStateDialog askState = new ArticleEditNewStateDialog(MainController.instance.window, true);

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
        if (askSave()) {
            return;
        }

        ArticleEditAddCommentDialog askComment = new ArticleEditAddCommentDialog(MainController.instance.window, true);

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

    private void askWords() {
        if (askSave()) {
            return;
        }

        ArticleEditChangeWordsDialog askWords = new ArticleEditChangeWordsDialog(MainController.instance.window, true);
        askWords.txtWords.setText(Arrays.toString(article.article.words).replace("[", "").replace("]", ""));

        askWords.btnChange.addActionListener((e) -> {
            saveWords(askWords.txtWords.getText(), () -> {
                askWords.dispose();
                window.requestFocus();
            });
        });

        // setup cancel button
        ActionListener cancelListener = (e) -> {
            askWords.dispose();
        };
        askWords.btnCancel.addActionListener(cancelListener);
        askWords.getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        askWords.setLocationRelativeTo(window);
        askWords.setVisible(true);
    }

    private void proposeChanges() {
        ArticleEditProposeChangesDialog askProposeComment = new ArticleEditProposeChangesDialog(
                MainController.instance.window, true);

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

    private boolean askSave() {
        if (!window.btnSave.isEnabled() && !window.btnProposeSave.isEnabled()) {
            return false;
        }
        if (JOptionPane.showConfirmDialog(window, "Змены, што Вы зрабілі ў артыкуле, згубяцца. Працягнуць ?",
                "Працягнуць", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
            return true;
        } else {
            return false;
        }
    }
}
