package org.im.dc.client.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextPane;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.im.dc.client.WS;
import org.im.dc.client.ui.utils.ChooseStateController;
import org.im.dc.gen.config.TypePermission;
import org.im.dc.service.OutputSummaryStorage;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.InitialData;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

public class MainControllerArticleType implements IArticleUpdatedListener {
    static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle");

    private InitialData.TypeInfo typeInfo;
    private final MainFramePanelArticles panelArticles = new MainFramePanelArticles();
    protected final Dockable dock;
    private List<String> selectedStates = new ArrayList<>();

    public MainControllerArticleType(InitialData.TypeInfo articleTypeInfo) {
        this.typeInfo = articleTypeInfo;
        dock = new Dockable() {
            DockKey key = new DockKey("articlesList-" + typeInfo.typeId, typeInfo.typeName);

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelArticles;
            }
        };
        init();

        MainController.instance.addArticleUpdatedListener(this);
    }

    public String getArticleTypeId() {
        return typeInfo.typeId;
    }

    private void init() {
        Vector<String> users = new Vector<>();
        users.add(null);
        users.addAll(MainController.initialData.allUsers.keySet());
        panelArticles.cbUser.setModel(new DefaultComboBoxModel<>(users));
        panelArticles.btnSearch.addActionListener((e) -> search());
        panelArticles.tableArticles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = panelArticles.tableArticles.rowAtPoint(e.getPoint());
                int index = panelArticles.tableArticles.convertRowIndexToModel(row);
                ArticleShort a = ((MainFrameArticlesModel) panelArticles.tableArticles.getModel()).articles.get(index);
                new ArticleEditController(typeInfo, a.id);
            }
        });
        panelArticles.cbUser.addKeyListener(pressEnter);
        panelArticles.btnStates.addActionListener(chooseStates);
        panelArticles.btnStates.setPreferredSize(new Dimension(250, panelArticles.btnStates.getPreferredSize().height));
        panelArticles.txtWord.addKeyListener(pressEnter);
        panelArticles.txtText.addKeyListener(pressEnter);
        panelArticles.txtID.addKeyListener(pressEnter);
        panelArticles.tableArticles.getSelectionModel().addListSelectionListener((e) -> {
            panelArticles.labelSelected.setText("Пазначана: " + panelArticles.tableArticles.getSelectedRows().length);
        });

        if (typeInfo.currentUserTypePermissions.contains(TypePermission.ADD_ARTICLE.name())) {
            panelArticles.btnAddArticle.addActionListener((e) -> new ArticleEditController(typeInfo));
        } else {
            panelArticles.btnAddArticle.setVisible(false);
        }

        if (typeInfo.currentUserTypePermissions.contains(TypePermission.REASSIGN.name())) {
            panelArticles.btnReassign.addActionListener(reassign);
        } else {
            panelArticles.btnReassign.setVisible(false);
        }

        if (typeInfo.currentUserTypePermissions.contains(TypePermission.VIEW_OUTPUT.name())) {
            panelArticles.btnPreview.addActionListener(preview);
        } else {
            panelArticles.btnPreview.setVisible(false);
        }
    }

    KeyAdapter pressEnter = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                search();
            }
        }
    };

    ActionListener chooseStates = (e) -> {
        ChooseStateController c = new ChooseStateController(selectedStates);
        if (c.result != null) {
            selectedStates.clear();
            selectedStates.addAll(c.result);
            if (c.result.isEmpty()) {
                panelArticles.btnStates.setText(" ");
            } else {
                panelArticles.btnStates.setText(String.join(",", c.result));
            }
            search();
        }
    };

    ActionListener reassign = (e) -> {
        TableModel m = panelArticles.tableArticles.getModel();
        if (m instanceof MainFrameArticlesModel) {
            MainFrameArticlesModel model = (MainFrameArticlesModel) m;
            List<ArticleShort> articles = new ArrayList<>();
            for (int r : panelArticles.tableArticles.getSelectedRows()) {
                r = panelArticles.tableArticles.convertRowIndexToModel(r);
                articles.add(model.articles.get(r));
            }

            new ReassignController(articles, typeInfo.typeId);
        }
    };
    ActionListener preview = (e) -> {
        TableModel m = panelArticles.tableArticles.getModel();
        if (m instanceof MainFrameArticlesModel) {
            MainFrameArticlesModel model = (MainFrameArticlesModel) m;
            int[] selectedRows = panelArticles.tableArticles.getSelectedRows();
            int[] articleIds = new int[selectedRows.length];
            for (int i = 0; i < selectedRows.length; i++) {
                int row = selectedRows[i];
                row = panelArticles.tableArticles.convertRowIndexToModel(row);
                articleIds[i] = model.articles.get(row).id;
            }
            if (articleIds.length > 100) {
                MainController.instance.todo("Абрана зашмат артыкулаў");
            } else {
                PreviewController previewer = new PreviewController(MainController.instance.window, false);
                previewer.setupExecutor(() -> preview(previewer, articleIds));
                previewer.window.text.addHyperlinkListener(he -> {
                    if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        int idx = Integer.parseInt(he.getDescription());
                        new ArticleEditController(typeInfo, idx);
                    }
                });
                previewer.execute();
            }
        }
    };

    void preview(PreviewController previewer, int[] articleIds) {
        previewer.new LongProcess() {
            StringBuilder out, outClipboard;
            boolean needHr = false;

            @Override
            protected void exec() throws Exception {
                out = new StringBuilder(PreviewController.HTML_PREFIX);
                outClipboard = new StringBuilder(PreviewController.HTML_PREFIX);

                OutputSummaryStorage articlesPreview = WS.getToolsWebservice().preparePreviews(WS.header,
                        typeInfo.typeId, articleIds);
                for (String e : articlesPreview.summaryErrors) {
                    out.append("<b>АГУЛЬНАЯ ПАМЫЛКА: " + e + "</b><br/>\n");
                    outClipboard.append("<b>АГУЛЬНАЯ ПАМЫЛКА: " + e + "</b><br/>\n");
                    needHr = true;
                }
                for (OutputSummaryStorage.ArticleError e : articlesPreview.errors) {
                    out.append("<a href='" + e.articleId + "'>рэдагаваць: " + e.key + "</a> ");
                    out.append("<b>ПАМЫЛКА у " + e.key + ": " + e.error + "</b><br/>\n");
                    outClipboard.append("<b>ПАМЫЛКА ў " + e.key + ": " + e.error + "</b><br/>\n");
                    needHr = true;
                }
                articlesPreview.outputs.forEach(ao -> {
                    if (needHr) {
                        out.append("<hr/>\n");
                    } else {
                        needHr = true;
                    }
                    out.append(ao.html);
                    outClipboard.append(ao.html);
                    out.append(" <a href='" + ao.articleId + "'>рэдагаваць</a>\n");
                });

                out.append(PreviewController.HTML_SUFFIX);
                outClipboard.append(PreviewController.HTML_SUFFIX);
            }

            @Override
            protected void ok() {
                previewer.setHtml(out.toString());
                JTextPane o = new JTextPane();
                o.setContentType("text/html");
                o.setText(outClipboard.toString());
                o.selectAll();
                o.copy();
            }

            @Override
            protected void error() {
                previewer.window.dispose();
            }
        };
    }

    /**
     * Search articles by filter.
     */
    private void search() {
        ArticlesFilter filter = new ArticlesFilter();
        filter.user = (String) panelArticles.cbUser.getSelectedItem();
        filter.states = selectedStates;
        filter.partHeader = panelArticles.txtWord.getText().trim().isEmpty() ? null
                : panelArticles.txtWord.getText().trim();
        filter.partText = panelArticles.txtText.getText().trim().isEmpty() ? null
                : panelArticles.txtText.getText().trim();
        String ids = panelArticles.txtID.getText().trim();
        MainController.instance.new LongProcess() {
            MainFrameArticlesModel model;

            @Override
            protected void exec() throws Exception {
                if (!ids.isEmpty()) {
                    filter.ids = new ArrayList<>();
                    for (String s : ids.split("[^0-9]")) {
                        if (!s.isEmpty()) {
                            filter.ids.add(Integer.parseInt(s));
                        }
                    }
                }
                model = new MainFrameArticlesModel(
                        WS.getArticleService().listArticles(WS.header, typeInfo.typeId, filter));
            }

            @Override
            protected void ok() {
                if (panelArticles.tableArticles.getRowSorter() == null) {
                    SettingsController.replaceModel(panelArticles.tableArticles, model);
                    panelArticles.tableArticles.setRowSorter(new TableArticlesSorter(model));
                } else {
                    List<? extends RowSorter.SortKey> sortKeys = panelArticles.tableArticles.getRowSorter()
                            .getSortKeys();
                    SettingsController.replaceModel(panelArticles.tableArticles, model);
                    ((TableRowSorter<MainFrameArticlesModel>) panelArticles.tableArticles.getRowSorter())
                            .setModel(model);
                    panelArticles.tableArticles.getRowSorter().setSortKeys(sortKeys);
                }
                panelArticles.labelSelected.setText(
                        MessageFormat.format(BUNDLE.getString("MainFrameArticles.Found"), model.getRowCount()));
            }
        };
    }

    @Override
    public void onArticleUpdated(ArticleFull article) {
        if (!getArticleTypeId().equals(article.type)) {
            return;
        }

        if (panelArticles.tableArticles.getModel() instanceof MainFrameArticlesModel) {
            MainFrameArticlesModel model = (MainFrameArticlesModel) panelArticles.tableArticles.getModel();
            boolean needUpdate = false;
            for (ArticleShort a : model.articles) {
                if (a.id == article.id) {
                    a.assignedUsers = article.assignedUsers;
                    a.state = article.state;
                    a.validationError = article.validationError;
                    a.header = article.header;
                    needUpdate = true;
                }
            }
            if (needUpdate) {
                model.fireTableDataChanged();
            }
        }
    }

    public class TableArticlesSorter extends TableRowSorter<MainFrameArticlesModel> {
        private Comparator<String> comparatorHeader = MainController.getComparatorForArticleType(typeInfo.typeId);
        private Comparator<Object> comparatorOther = Collator
                .getInstance(new Locale(MainController.initialData.headerLocale));

        public TableArticlesSorter(MainFrameArticlesModel model) {
            super(model);
            setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        }

        @Override
        public Comparator<?> getComparator(int column) {
            if (column == 0) {
                return comparatorHeader;
            } else {
                return comparatorOther;
            }
        }
    }
}
