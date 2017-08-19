package org.im.dc.client.ui;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.gen.config.Permission;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.Related;
import org.im.dc.service.dto.Related.RelatedType;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingConstants;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * Controls article editor.
 */
public class ArticleEditController extends BaseController<ArticleEditDialog> {
    public static XMLInputFactory READER_FACTORY = XMLInputFactory.newInstance();
    public static XMLOutputFactory WRITER_FACTORY = XMLOutputFactory.newInstance();

    private XmlGroup editorUI;

    protected volatile ArticleFullInfo article;
    protected volatile boolean wasChanged;

    private ArticlePanelEdit panelEdit = new ArticlePanelEdit();
    private ArticlePanelHistory panelHistory = new ArticlePanelHistory();
    private ArticlePanelNotes panelNotes = new ArticlePanelNotes();


    private ArticleEditController(boolean isnew) {
        super(new ArticleEditDialog(MainController.instance.window, false), MainController.instance.window);
        initDocking();
        setupCloseOnEscape();
        displayOnParent();
    }

    @Override
    boolean closing() {
        return !askSave();
    }

    /**
     * New article.
     */
    public ArticleEditController() {
        this(true);

        article = new ArticleFullInfo();
        article.youCanEdit = true;
        article.article = new ArticleFull();
        article.article.state = MainController.initialData.newArticleState;
        article.article.assignedUsers = MainController.initialData.newArticleUsers;
        init();
        show();
    }

    /**
     * Edit exist article.
     */
    public ArticleEditController(int articleId) {
        this(false);

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

    void initDocking() {
        Dockable edit = new Dockable() {
            DockKey key = new DockKey("edit", "Артыкул");

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelEdit;
            }
        };
        Dockable history = new Dockable() {
            DockKey key = new DockKey("history", "Гісторыя");

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelHistory;
            }
        };
        Dockable notes = new Dockable() {
            DockKey key = new DockKey("notes", "Заўвагі");

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelNotes;
            }
        };
        desk = new DockingDesktop();
        window.getContentPane().add(desk);
        desk.registerDockable(edit);
        desk.registerDockable(notes);
        desk.registerDockable(history);
        SettingsController.loadDocking(window, desk);
    }

    private void init() {
        panelEdit.panelEditor.getVerticalScrollBar().setUnitIncrement(20);
        window.btnSave.addActionListener((e) -> save());
        window.btnChangeState.addActionListener((e) -> changeStateAsk());
        window.btnAddIssue.addActionListener((e) -> addIssue());
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
        panelNotes.txtNotes.getDocument().addDocumentListener(new DocumentListener() {
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
        panelHistory.tableHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArticleEditRelatedModel model = (ArticleEditRelatedModel) panelHistory.tableHistory.getModel();
                Related rel = model.related.get(panelHistory.tableHistory.getSelectedRow());
                if (rel.type == RelatedType.ISSUE && rel.requiresActivity && askSave()) {
                    return;
                }
                new ArticleDetailsController(ArticleEditController.this, rel);
            }
        });
    }

    public boolean isNew() {
        return article.article.id == 0;
    }

    void show() {
        window.btnSave.setVisible(article.youCanEdit);
        window.btnAddIssue.setVisible(!article.youCanEdit);
        panelNotes.txtNotes.setEditable(article.youCanEdit);
        window.btnChangeState.setVisible(!article.youCanChangeStateTo.isEmpty());
        displayWatch();
        displayIssue();

        if (article.article.words != null) {
            window.setTitle(window.getTitle().replaceAll("\\[.*\\]", Arrays.toString(article.article.words)));
            window.txtWords.setText(Arrays.toString(article.article.words));
        }
        window.txtState.setText(article.article.state);
        window.txtUsers.setText(Arrays.toString(article.article.assignedUsers));
        if (article.article.notes != null) {
            panelNotes.txtNotes.setText(article.article.notes);
        }
        window.lblValidationError
                .setText(article.article.validationError != null ? article.article.validationError : " ");

        wasChanged = false;
        updateIssueButton();
        try {
            editorUI = SchemaLoader.createUI(this);
            if (article.article.xml != null) {
                XMLStreamReader rd = READER_FACTORY
                        .createXMLStreamReader(new ByteArrayInputStream(article.article.xml));
                rd.nextTag();
                editorUI.insertData(rd);
                editorUI.displayed();
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
        panelEdit.panelEditor.setViewportView(editorUI);
        window.btnSave.setEnabled(false);

        Related.sortByTimeDesc(article.related);

        SettingsController.savePlacesForWindow(window, desk);
        panelHistory.tableHistory.setModel(new ArticleEditRelatedModel(article.related));
        SettingsController.loadPlacesForWindow(window, desk);

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
                        new ArticleEditController(lf.articleId);
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

    public static XMLStreamReader getReader(byte[] xml) throws Exception {
        XMLStreamReader rd = READER_FACTORY.createXMLStreamReader(new ByteArrayInputStream(xml));
        while (rd.hasNext()) {
            int t = rd.nextTag();
            if (t == XMLStreamConstants.START_ELEMENT) {
                break;
            }
        }
        return rd;
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
        XMLStreamWriter wr = WRITER_FACTORY.createXMLStreamWriter(w);
        editorUI.extractData("root", wr);
        wr.flush();
        return w.toString().getBytes("UTF-8");
    }

    /**
     * Захоўвае змены на серверы.
     */
    private void save() {
        article.article.notes = panelNotes.txtNotes.getText();
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

        new ArticleEditNewStateController(this);
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
        new ArticleEditChangeWordsController(this);
    }

    private void updateIssueButton() {
        window.btnAddIssue.setText(wasChanged ? "Прапанаваць змены" : "Дадаць заўвагу");
    }

    private void addIssue() {
        new ArticleEditProposeChangesController(this);
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
