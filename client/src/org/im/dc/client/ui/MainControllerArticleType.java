package org.im.dc.client.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.table.TableModel;

import org.im.dc.client.WS;
import org.im.dc.gen.config.TypePermission;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.InitialData;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

public class MainControllerArticleType implements IArticleUpdatedListener {
    private InitialData.TypeInfo typeInfo;
    private final MainFramePanelArticles panelArticles = new MainFramePanelArticles();
    protected final Dockable dock;

    public MainControllerArticleType(InitialData.TypeInfo articleTypeInfo) {
        this.typeInfo = articleTypeInfo;
        dock = new Dockable() {
            DockKey key = new DockKey("articlesList-" + typeInfo.typeId, "Артыкулы - " + typeInfo.typeName);

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
        panelArticles.tableArticles.getSelectionModel().addListSelectionListener((e) -> {
            panelArticles.labelSelected.setText("Пазначана: " + panelArticles.tableArticles.getSelectedRows().length);
        });
        panelArticles.tableArticles.setAutoCreateRowSorter(true);

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

    ActionListener reassign = (e) -> {
        TableModel m = panelArticles.tableArticles.getModel();
        if (m instanceof MainFrameArticlesModel) {
            MainFrameArticlesModel model = (MainFrameArticlesModel) m;
            List<ArticleShort> articles = new ArrayList<>();
            for (int r : panelArticles.tableArticles.getSelectedRows()) {
                articles.add(model.articles.get(r));
            }

            new ReassignController(articles);
        }
    };
    ActionListener preview = (e) -> {
        TableModel m = panelArticles.tableArticles.getModel();
        if (m instanceof MainFrameArticlesModel) {
            MainFrameArticlesModel model = (MainFrameArticlesModel) m;
            int[] selectedRows = panelArticles.tableArticles.getSelectedRows();
            int[] articleIds = new int[selectedRows.length];
            for (int i = 0; i < selectedRows.length; i++) {
                articleIds[i] = model.articles.get(selectedRows[i]).id;
            }
            if (articleIds.length > 100) {
                MainController.instance.todo("Абрана зашмат артыкулаў");
            } else {
                new PreviewAllController(articleIds, typeInfo.typeId);
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
        MainController.instance.new LongProcess() {
            MainFrameArticlesModel model;

            @Override
            protected void exec() throws Exception {
                model = new MainFrameArticlesModel(
                        WS.getArticleService().listArticles(WS.header, typeInfo.typeId, filter));
            }

            @Override
            protected void ok() {
                SettingsController.replaceModel(panelArticles.tableArticles, model);
                panelArticles.labelSelected.setText("Знойдзена: " + model.getRowCount());
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
}
