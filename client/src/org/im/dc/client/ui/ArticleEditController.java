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

import javax.swing.ImageIcon;
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
import org.im.dc.gen.config.Permission;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.Related;
import org.im.dc.service.dto.Related.RelatedType;

/**
 * Controls article editor.
 */
public class ArticleEditController extends BaseController<ArticleEditDialog> {
    private XmlGroup editorUI;

    protected volatile ArticleFullInfo article;
    protected volatile boolean wasChanged;

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
        window.btnAddIssue.addActionListener((e) -> addIssue());
        /*TODO remove window.lblAddComment.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addComment();
            }
        });*/
        window.lblHasProposedChanges.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!article.youCanEdit) {
                    return;
                }
                Related r = getOpenIssue();
                if (r != null) {
                    if (askSave()) {
                        return;
                    }
                    new ArticleDetailsController(ArticleEditController.this, r);
                }
            }
        });
        window.lblWatched.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeWatch();
            }
        });
        window.lblPreview
                .setVisible(MainController.initialData.currentUserPermissions.contains(Permission.VIEW_OUTPUT.name()));
        window.lblPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new PreviewController(window, ArticleEditController.this);
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
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                window.btnSave.setEnabled(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                window.btnSave.setEnabled(true);
            }
        });
        window.tableHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArticleEditRelatedModel model = (ArticleEditRelatedModel) window.tableHistory.getModel();
                Related rel = model.related.get(window.tableHistory.getSelectedRow());
                if (rel.type == RelatedType.ISSUE && rel.requiresActivity && askSave()) {
                    return;
                }
                new ArticleDetailsController(ArticleEditController.this, rel);
            }
        });
    }

    void show() {
        window.btnSave.setVisible(article.youCanEdit);
        window.btnAddIssue.setVisible(!article.youCanEdit);
        window.txtNotes.setEditable(article.youCanEdit);
        window.btnChangeState.setVisible(!article.youCanChangeStateTo.isEmpty());
        displayWatch();
        displayIssue();

        window.setTitle(window.getTitle().replaceAll("\\[.*\\]", Arrays.toString(article.article.words)));
        window.txtWords.setText(Arrays.toString(article.article.words));
        window.txtState.setText(article.article.state);
        window.txtUsers.setText(Arrays.toString(article.article.assignedUsers));
        if (article.article.notes != null) {
            window.txtNotes.setText(article.article.notes);
        }
        window.lblValidationError
                .setText(article.article.validationError != null ? article.article.validationError : " ");

        wasChanged = false;
        updateIssueButton();
        try {
            editorUI = SchemaLoader.createUI();
            if (article.article.xml != null) {
                XMLStreamReader rd = XMLInputFactory.newInstance()
                        .createXMLStreamReader(new ByteArrayInputStream(article.article.xml));
                rd.nextTag();
                editorUI.insertData(rd);
            }
            editorUI.addChangeListener((e) -> {
                wasChanged = true;
                window.btnSave.setEnabled(true);
                updateIssueButton();
            });
        } catch (Throwable ex) {
            editorUI = null;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(window, "Памылка чытання XML артыкула: " + ex.getMessage(), "Памылка",
                    JOptionPane.ERROR_MESSAGE);
        }
        window.panelEditor.setViewportView(editorUI);
        window.btnSave.setEnabled(false);

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
        window.lblWatched.setIcon(new ImageIcon(
                getClass().getResource(article.youWatched ? "images/watch-on.png" : "images/watch-off.png")));
    }

    private void displayIssue() {
        boolean hasIssue = getOpenIssue() != null;

        window.lblHasProposedChanges.setIcon(
                new ImageIcon(getClass().getResource(hasIssue ? "images/proposed-on.png" : "images/proposed-off.png")));
    }

    private Related getOpenIssue() {
        for (Related rel : article.related) {
            if (rel.type == RelatedType.ISSUE && rel.requiresActivity) {
                return rel;
            }
        }
        return null;
    }

    protected byte[] extractXml() throws Exception {
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
            boolean saved = false;

            @Override
            protected void exec() throws Exception {
                article.article.xml = extractXml();

                String err = WS.getToolsWebservice().validate(WS.header, article.article.id, article.article.words,
                        article.article.xml);
                if (err != null) {
                    if (JOptionPane.showConfirmDialog(window,
                            "Памылка валідацыі: " + err + "\nЗахоўваць нягледзячы на гэта ?", "Памылка",
                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }

                article = WS.getArticleService().saveArticle(WS.header, article.article);
                saved = true;
            }

            @Override
            protected void ok() {
                if (saved) {
                    show();
                }
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
            boolean saved = false;

            @Override
            protected void exec() throws Exception {
                byte[] proposedXml = wasChanged ? extractXml() : null;

                String err = WS.getToolsWebservice().validate(WS.header, article.article.id, article.article.words,
                        proposedXml);
                if (err != null) {
                    if (JOptionPane.showConfirmDialog(window,
                            "Памылка валідацыі: " + err + "\nЗахоўваць нягледзячы на гэта ?", "Памылка",
                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }

                article = WS.getArticleService().addIssue(WS.header, article.article.id, comment, proposedXml,
                        article.article.lastUpdated);
                saved = true;
            }

            @Override
            protected void ok() {
                ok.run();
                if (saved) {
                    show();
                    JOptionPane.showMessageDialog(window, "Прапановы паспяхова захаваныя", "Захавана",
                            JOptionPane.INFORMATION_MESSAGE);
                }
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

    // TODO remove
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

    private void updateIssueButton() {
        window.btnAddIssue.setText(wasChanged ? "Прапанаваць змены" : "Дадаць заўвагу");
    }

    private void addIssue() {
        ArticleEditProposeChangesDialog askProposeComment = new ArticleEditProposeChangesDialog(
                MainController.instance.window, true);
        askProposeComment.setTitle(wasChanged ? "Прапанаваць змены" : "Дадаць заўвагу");

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
        if (!wasChanged) {
            return false;
        }
        if (JOptionPane.showConfirmDialog(window, "Змены, што Вы зрабілі ў артыкуле, згубяцца. Працягнуць ?",
                "Працягнуць", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            return false;
        } else {
            return true;
        }
    }
}
