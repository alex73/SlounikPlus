package org.im.dc.client.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.im.dc.client.WS;
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
        Vector<String> states = new Vector<>();
        states.add(null);
        states.addAll(MainController.initialData.states);
        panelArticles.cbState.setModel(new DefaultComboBoxModel<>(states));
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
        panelArticles.cbState.addKeyListener(pressEnter);
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
                JButton btnRefresh = new JButton();
                ActionListener show = a->{
                    previewer.new LongProcess() {
                        StringBuilder out, outClipboard;
                        boolean needHr = false;

                        @Override
                        protected void exec() throws Exception {
                            out = new StringBuilder(
                                    "<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body>\n");
                            outClipboard = new StringBuilder(
                                    "<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body>\n");

                            OutputSummaryStorage articlesPreview = WS.getToolsWebservice().preparePreviews(WS.header,
                                    typeInfo.typeId, articleIds);
                            for (OutputSummaryStorage.ArticleError e : articlesPreview.errors) {
                                out.append("<a href='" + e.articleId + "'>рэдагаваць: " + e.key + "</a> ");
                                out.append("<b>ПАМЫЛКА: " + e + "</b><br/>\n");
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
                                outClipboard.append("<br/>\n");
                            });

                            out.append("\n</body></html>\n");
                            outClipboard.append("\n</body></html>\n");
                        }

                        @Override
                        protected void ok() {
                            previewer.window.text.setText(out.toString());
                            previewer.window.text.setCaretPosition(0);
                            previewer.window.text.getDocument().putProperty("ZOOM_FACTOR", new Double(2.5));

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
                };
                btnRefresh.addActionListener(show);
                previewer.window.text.addHyperlinkListener(he -> {
                    if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        int idx = Integer.parseInt(he.getDescription());
                        new ArticleEditController(typeInfo, idx);
                    }
                });
                show.actionPerformed(null);
            }
        }
    };

    /**
     * Search articles by filter.
     */
    private void search() {
        ArticlesFilter filter = new ArticlesFilter();
        filter.user = (String) panelArticles.cbUser.getSelectedItem();
        filter.state = (String) panelArticles.cbState.getSelectedItem();
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
                    for (String s : ids.split("\\s+")) {
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

    public static class TableArticlesSorter extends TableRowSorter<MainFrameArticlesModel> {
        public TableArticlesSorter(MainFrameArticlesModel model) {
            super(model);
            setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        }

        @Override
        public Comparator<?> getComparator(int column) {
            return new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    return WordsComparator.INSTANCE.compare(o1.toString(), o2.toString());
                }
            };
        }
    }
}
