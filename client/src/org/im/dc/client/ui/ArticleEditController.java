package org.im.dc.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
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
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.ui.rtfeditor.TinySwingPanel;
import org.im.dc.client.ui.rtfeditor.rtf_fix.RTFReaderFix;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.containers.XSAttributeContainer;
import org.im.dc.client.ui.struct.containers.XSSimpleElementContainer;
import org.im.dc.client.ui.struct.editors.IXSEdit;
import org.im.dc.gen.config.TypePermission;
import org.im.dc.service.OutputSummaryStorage;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;
import org.im.dc.service.dto.Related.RelatedType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * Controls article editor.
 */
public class ArticleEditController extends BaseController<ArticleEditDialog> {
    static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle");

    public static final XMLInputFactory READER_FACTORY = XMLInputFactory.newInstance();
    public static final XMLOutputFactory WRITER_FACTORY = XMLOutputFactory.newInstance();
    public static final DocumentBuilderFactory DOC_FACTORY = DocumentBuilderFactory.newInstance();

    private final InitialData.TypeInfo typeInfo;
    public IXSContainer editorUI;

    public volatile ArticleFullInfo article;
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
    protected boolean closing() {
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
        article.article.assignedUsers = new String[] { WS.header.user };
        init();
        show();
    }

    /**
     * New article for UI testing.
     */
    protected ArticleEditController(InitialData.TypeInfo typeInfo, ArticleFullInfo article) {
        this(true, typeInfo);

        this.article = article;
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
            DockKey key = new DockKey("edit", BUNDLE.getString("ArticleEdit.dock.Record"));

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
            DockKey key = new DockKey("history", BUNDLE.getString("ArticleEdit.dock.History"));

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
            DockKey key = new DockKey("notes", BUNDLE.getString("ArticleEdit.dock.Notebook"));

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
                PreviewController previewer = new PreviewController(window, true);
                previewer.setupExecutor(() -> {
                    previewer.new LongProcess() {
                        String preview;

                        @Override
                        protected void exec() throws Exception {
                            OutputSummaryStorage result = WS.getToolsWebservice().preparePreview(WS.header,
                                    article.article.type, article.article.id, extractXml());
                            StringBuilder text = new StringBuilder();
                            result.summaryErrors.forEach(e -> text.append("<p>АГУЛЬНАЯ ПАМЫЛКА: " + e + "</p>\n"));
                            result.errors.forEach(e -> text.append("<p>ПАМЫЛКА: " + e.error + "</p>\n"));
                            result.outputs.forEach(o -> text.append("<p>" + o.html + "</p>\n"));
                            preview = PreviewController.HTML_PREFIX + text + PreviewController.HTML_SUFFIX;
                        }

                        @Override
                        protected void ok() {
                            previewer.setHtml(preview);
                        }

                        @Override
                        protected void error() {
                            previewer.window.dispose();
                        }
                    };
                });
                previewer.execute();
            }
        });
        TinySwingPanel buttonsNotes = new TinySwingPanel(panelNotes.txtNotes);
        panelNotes.add(buttonsNotes, BorderLayout.SOUTH);
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

        panelEdit.panelEditor.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent arg0) {
                resizeEditor();
            }
        });
        panelEdit.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeEditor();
            }
        });
    }

    private void resizeEditor() {
        Component c = panelEdit.panelEditor.getViewport().getView();
        if (c != null) {
            c.setSize(panelEdit.panelEditor.getViewport().getWidth(), c.getHeight());
            c.validate();
        }
    }

    public boolean isNew() {
        return article.article.id == 0;
    }

    void show() {
        window.btnSave.setVisible(article.youCanEdit);
        window.btnAddIssue.setVisible(!article.youCanEdit && article.youCanProposeChanges);
        panelNotes.txtNotes.setEditable(article.youCanEdit);
        window.btnChangeState.setVisible(!article.youCanChangeStateTo.isEmpty());
        displayWatch();
        displayIssue();

        if (article.article.header != null) {
            String h = article.article.header;
            if (!isNew()) {
                h = "#" + article.article.id + ": " + h;
            }
            window.setTitle(window.getTitle().replaceAll("\\[.*\\]", h));
        }
        window.txtState.setText(article.article.state);
        window.txtUsers.setVisible(article.article.assignedUsers != null);
        window.txtUsers.setText(Arrays.toString(article.article.assignedUsers));
        panelNotes.txtNotes.setText("");
        StyledDocument notesDoc = (StyledDocument) panelNotes.txtNotes.getDocument();
        if (article.article.notes != null) {
            try (RTFReaderFix rdr = new RTFReaderFix(notesDoc)) {
                rdr.readFromStream(new ByteArrayInputStream(article.article.notes));
            } catch (Exception ex) {
            }
            try {
                String plainText = notesDoc.getText(0, notesDoc.getLength());
                if (plainText.endsWith("\n")) {
                    // remove latest EOL because it added every time
                    notesDoc.remove(plainText.length() - 1, 1);
                }
            } catch (Exception ex) {
            }
        } else {
            // Set default white color. Instead, default background will be black.
            StyleConstants.setBackground(panelNotes.txtNotes.getInputAttributes(), Color.WHITE);
        }
        window.lblValidationError
                .setText(article.article.validationError != null ? article.article.validationError : " ");
        window.lblValidationError.setToolTipText(window.lblValidationError.getText());

        ArticleUIContext editContext = new ArticleUIContext(typeInfo.typeId);
        editContext.userRoles = MainController.initialData.currentUserRoles;
        editContext.articleState = article.article.state;
        editContext.userCanEdit = article.youCanEdit;
        editContext.userCanProposeChanges = article.youCanProposeChanges;

        resetChanged();
        try {
            editorUI = SchemaLoader.createUI(editContext);
            editContext.editController = this;
            if (article.article.xml != null) {
                DocumentBuilder builder = DOC_FACTORY.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(article.article.xml));
                editorUI.insertData((Element) doc.getFirstChild());
            }
            resetChanged();
        } catch (Throwable ex) {
            editorUI = null;
            ex.printStackTrace();
            JOptionPane.showMessageDialog(window, "Памылка чытання XML артыкула: " + ex.getMessage(), "Памылка",
                    JOptionPane.ERROR_MESSAGE);
            window.dispose();
            return;
        }
        editorUI.getUIComponent().setEnabled(article.youCanEdit || article.youCanProposeChanges);
        applyFont(editorUI.getUIComponent());
        panelEdit.panelEditor.setViewportView(editorUI.getUIComponent());
        SwingUtilities.invokeLater(() -> panelEdit.panelEditor.getVerticalScrollBar().setValue(0));

        Related.sortByTimeDesc(article.related);

        panelHistory.tableHistory.setModel(new ArticleEditRelatedModel(article.related));

        if (article.linksFrom.isEmpty()) {
            window.panelLinkedFrom.setVisible(false);
        } else {
            window.panelLinkedFrom.setVisible(true);
            window.panelLinkedFrom.removeAll();
            window.panelLinkedFrom.add(new JLabel("На гэты артыкул спасылаюцца:"));

            for (ArticleFullInfo.LinkFrom lf : article.linksFrom) {
                JLabel lbl = new JLabel(lf.header);
                lbl.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        InitialData.TypeInfo ti = MainController.initialData.getTypeInfo(lf.articleType);
                        new ArticleEditController(ti, lf.articleId);
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

    public void applyFont(JComponent comp) {
        comp.setFont(panelEdit.getFont());
        for (int i = 0; i < comp.getComponentCount(); i++) {
            Component c = comp.getComponent(i);
            if (c instanceof JComponent) {
                applyFont((JComponent) c);
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
    public void requestRetrieveHeaders(String articleTypeId, Runnable after) {
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
                    Collections.sort(headers, MainController.getComparatorForArticleType(articleTypeId));
                    return null;
                }

                protected void done() {
                    try {
                        get();
                        synchronized (articleHeaders) {
                            articleHeaders.put(articleTypeId, headers);
                        }
                        if (after != null) {
                            after.run();
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }.execute();
        }
    }

    public void requestForceRetrieveHeaders(String articleTypeId, Runnable after) {
        synchronized (articleHeaders) {
            articleHeaders.remove(articleTypeId);
            requestRetrieveHeaders(articleTypeId, after);
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
        window.lblWatched.setIcon(new ImageIcon(ArticleEditController.class
                .getResource(article.youWatched ? "images/watch-on.png" : "images/watch-off.png")));
    }

    private void displayIssue() {
        boolean hasIssue = getOpenIssue() != null;

        window.lblHasProposedChanges.setIcon(new ImageIcon(ArticleEditController.class
                .getResource(hasIssue ? "images/proposed-on-animated.gif" : "images/proposed-off.png")));
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
        editorUI.extractData(wr);
        wr.flush();
        return w.toString().getBytes("UTF-8");
    }

    /**
     * Save changes to server.
     */
    private void save() {
        article.article.notes = null;
        try {
            String plainText = panelNotes.txtNotes.getDocument().getText(0,
                    panelNotes.txtNotes.getDocument().getLength());
            if (!plainText.trim().isEmpty()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                article.article.notes = null;
                panelNotes.txtNotes.getEditorKit().write(out, panelNotes.txtNotes.getDocument(), 0, plainText.length());
                article.article.notes = out.toByteArray();
            }
        } catch (Exception ex) {
            if (JOptionPane.showConfirmDialog(window,
                    "Немагчыма захаваць нататку: " + ex.getMessage() + "\nЗахоўваць нягледзячы на гэта(нататка будзе згубленая) ?", "Памылка",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                return;
            }
        }
        new LongProcess() {
            boolean saved = false;

            @Override
            protected void exec() throws Exception {
                article.article.xml = extractXml();

                String error = WS.getToolsWebservice().validate(WS.header, article.article.type, article.article.id,
                        article.article.xml);
                if (error != null) {
                    if (JOptionPane.showConfirmDialog(window,
                            MessageFormat.format(BUNDLE.getString("Message.Error.Validation"), error),
                            BUNDLE.getString("Message.ErrorTitle"),
                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }

                article = WS.getArticleService().saveArticle(WS.header, article.article, false);
                saved = true;
                MainController.instance.fireArticleUpdated(article.article);
            }

            @Override
            protected void ok() {
                if (saved) {
                    show();
                    afterSave(article.article);
                }
            }
        };
    }

    /**
     * Method can be overrided by caller for check article changes.
     */
    protected void afterSave(ArticleFull article) {
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
        window.btnAddIssue.setText(
                wasChanged ? BUNDLE.getString("ArticleEdit.ProposeChanges") : BUNDLE.getString("ArticleEdit.AddIssue"));
    }

    private void addIssue() {
        new ArticleEditProposeChangesController(this);
    }

    private boolean askSave() {
        if (!wasChanged) {
            return false;
        }
        if (JOptionPane.showConfirmDialog(window, BUNDLE.getString("Message.Continue.LostChanges"),
                BUNDLE.getString("Message.ContinueTitle"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            return false;
        } else {
            return true;
        }
    }

    public <T extends IXSContainer> List<T> findSubcontainers(Class<T> containerClass, String tag) {
        List<T> result = new ArrayList<>();
        findSubcontainers(editorUI, containerClass, tag, result);
        return result;
    }

    public <T extends IXSEdit> List<T> findEditors(Class<T> editorClass, String tag) {
        List<T> result = new ArrayList<>();
        findEditors(editorUI, editorClass, tag, result);
        return result;
    }

    protected <T extends IXSContainer> void findSubcontainers(IXSContainer child, Class<T> containerClass, String tag,
            List<T> result) {
        if (containerClass.isAssignableFrom(child.getClass()) && tag.equals(child.getTag())) {
            result.add((T) child);
            return;
        }
        Collection<IXSContainer> children = child.children();
        if (children == null) {
            return;
        }
        for (IXSContainer ch : child.children()) {
            findSubcontainers(ch, containerClass, tag, result);
        }
    }

    protected <T extends IXSEdit> void findEditors(IXSContainer child, Class<T> editorClass, String tag,
            List<T> result) {
        if (XSAttributeContainer.class.isAssignableFrom(child.getClass()) && tag.equals(child.getTag())) {
            IXSEdit editor = ((XSAttributeContainer) child).getEditor();
            if (editorClass.isAssignableFrom(editor.getClass())) {
                result.add((T) editor);
            }
        } else if (XSSimpleElementContainer.class.isAssignableFrom(child.getClass()) && tag.equals(child.getTag())) {
            IXSEdit editor = ((XSSimpleElementContainer) child).getEditor();
            if (editorClass.isAssignableFrom(editor.getClass())) {
                result.add((T) editor);
            }
        }
        Collection<IXSContainer> children = child.children();
        if (children == null) {
            return;
        }
        for (IXSContainer ch : child.children()) {
            findEditors(ch, editorClass, tag, result);
        }
    }
}
