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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.ui.xmlstructure.ArticleUIContext;
import org.im.dc.client.ui.xmlstructure.XmlGroup;
import org.im.dc.gen.config.TypePermission;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;
import org.im.dc.service.dto.Related.RelatedType;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * Controls article editor.
 */
public class ArticleEditController extends BaseController<ArticleEditDialog> {
    public static XMLInputFactory READER_FACTORY = XMLInputFactory.newInstance();
    public static XMLOutputFactory WRITER_FACTORY = XMLOutputFactory.newInstance();

    private final InitialData.TypeInfo typeInfo;
    private XmlGroup editorUI;

    protected volatile ArticleFullInfo article;
    protected volatile boolean wasChanged;

    public ArticlePanelEdit panelEdit = new ArticlePanelEdit();
    public ArticlePanelHistory panelHistory = new ArticlePanelHistory();
    public ArticlePanelNotes panelNotes = new ArticlePanelNotes();

    protected Map<String, List<String>> articleHeaders = new TreeMap<>();

    private ArticleEditController(boolean isnew, InitialData.TypeInfo typeInfo) {
        super(new ArticleEditDialog(MainController.instance.window, false), MainController.instance.window);
        this.typeInfo = typeInfo;
        initDocking();
        setupCloseOnEscape();
        displayOnParent();
    }

    public String getArticleTypeId() {
        return typeInfo.typeId;
    }

    @Override
    boolean closing() {
        return !askSave();
    }

    /**
     * New article.
     */
    public ArticleEditController(InitialData.TypeInfo typeInfo) {
        this(true, typeInfo);

        article = new ArticleFullInfo();
        article.youCanEdit = true;
        article.article = new ArticleFull();
        article.article.type = typeInfo.typeId;
        article.article.state = typeInfo.newArticleState;
        article.article.assignedUsers = MainController.initialData.newArticleUsers;
        init();
        show();
    }

    /**
     * Edit exist article.
     */
    public ArticleEditController(InitialData.TypeInfo typeInfo, int articleId) {
        this(false, typeInfo);

        // request article from server
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                article = WS.getArticleService().getArticleFullInfo(WS.header, typeInfo.typeId, articleId);
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
        window.lblPreview.setVisible(typeInfo.currentUserTypePermissions.contains(TypePermission.VIEW_OUTPUT.name()));
        window.lblPreview.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new PreviewController(window, ArticleEditController.this);
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

        if (article.article.header != null) {
            window.setTitle(window.getTitle().replaceAll("\\[.*\\]", article.article.header));
        }
        window.txtState.setText(article.article.state);
        window.txtUsers.setVisible(article.article.assignedUsers != null);
        window.txtUsers.setText(Arrays.toString(article.article.assignedUsers));
        if (article.article.notes != null) {
            panelNotes.txtNotes.setText(article.article.notes);
        }
        window.lblValidationError
                .setText(article.article.validationError != null ? article.article.validationError : " ");

        ArticleUIContext editContext = new ArticleUIContext();
        resetChanged();
        try {
            editContext.editController = this;
            editContext.userRole = MainController.initialData.currentUserRole;
            editContext.articleState = article.article.state;
            editorUI = SchemaLoader.createUI(editContext);
            if (article.article.xml != null) {
                XMLStreamReader rd = READER_FACTORY
                        .createXMLStreamReader(new ByteArrayInputStream(article.article.xml));
                rd.nextTag();
                editorUI.insertData(rd);
                editorUI.displayed();
            }
            resetChanged();
        } catch (Throwable ex) {
            editorUI = null;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(window, "Памылка чытання XML артыкула: " + ex.getMessage(), "Памылка",
                    JOptionPane.ERROR_MESSAGE);
        }
        panelEdit.panelEditor.setViewportView(editorUI);

        Related.sortByTimeDesc(article.related);

        panelHistory.tableHistory.setModel(new ArticleEditRelatedModel(article.related));

        if (article.linksFrom.isEmpty()) {
            window.panelLinkedFrom.setVisible(false);
        } else {
            window.panelLinkedFrom.setVisible(true);
            window.panelLinkedFrom.removeAll();
            window.panelLinkedFrom.add(new JLabel("На гэты артыкул спасылаюцца:"));
            /*
             * for (ArticleFullInfo.LinkFrom lf : article.linksFrom) { JLabel lbl = new
             * JLabel(Arrays.toString(lf.words)); lbl.addMouseListener(new MouseAdapter() {
             * 
             * @Override public void mouseClicked(MouseEvent e) { new ArticleEditController(lf.articleId); } });
             * asLink(lbl); window.panelLinkedFrom.add(lbl); }
             */
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

    public List<String> getHeaders(String articleTypeId) {
        synchronized (articleHeaders) {
            return articleHeaders.get(articleTypeId);
        }
    }

    /**
     * Retrieve article headers.
     */
    public void requestRetrieveHeaders(String articleTypeId) {
        synchronized (articleHeaders) {
            if (articleHeaders.containsKey(articleTypeId)) {
                // already requested
                return;
            }
            articleHeaders.put(articleTypeId, null);
            new SwingWorker<Void, Void>() {
                List<String> headers = new ArrayList<>();

                @Override
                protected Void doInBackground() throws Exception {
                    List<ArticleShort> articles = WS.getArticleService().listArticles(WS.header, articleTypeId,
                            new ArticlesFilter());
                    articles.forEach(a -> headers.add(a.header));
                    Collator collator = Collator.getInstance(new Locale(MainController.initialData.headerLocale));
                    Collections.sort(headers, collator);
                    return null;
                }

                protected void done() {
                    try {
                        get();
                        synchronized (articleHeaders) {
                            articleHeaders.put(articleTypeId, headers);
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
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

    public void resetChanged() {
        wasChanged = false;
        SwingUtilities.invokeLater(() -> updateIssueButton());
    }

    public void setChanged() {
        wasChanged = true;
        SwingUtilities.invokeLater(() -> updateIssueButton());
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
        editorUI.extractData(typeInfo.typeId, wr);
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

                String err = WS.getToolsWebservice().validate(WS.header, article.article.type, article.article.id,
                        article.article.header, article.article.xml);
                if (err != null) {
                    if (JOptionPane.showConfirmDialog(window,
                            "Памылка валідацыі: " + err + "\nЗахоўваць нягледзячы на гэта ?", "Памылка",
                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }

                article = WS.getArticleService().saveArticle(WS.header, article.article);
                saved = true;
                MainController.instance.fireArticleUpdated(article.article);
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
                article = WS.getArticleService().addComment(WS.header, article.article.type, article.article.id,
                        comment);
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
                WS.getArticleService().setWatch(WS.header, article.article.type, article.article.id,
                        !article.youWatched);
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

    private void updateIssueButton() {
        window.btnSave.setEnabled(wasChanged);
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
