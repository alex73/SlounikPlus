package org.im.dc.client.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.os.Commands;
import org.im.dc.gen.config.Permission;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * Controls main window.
 */
public class MainController extends BaseController<MainFrame> {
    static InitialData initialData;
    public static MainController instance;
    int fontSize;

    private final String addr;
    private MainFrameIssuesModel issuesModel;
    private MainFrameNewsModel newsModel;

    private MainFramePanelArticles panelArticles = new MainFramePanelArticles();
    private MainFramePanelIssues panelIssues = new MainFramePanelIssues();
    private MainFramePanelNews panelNews = new MainFramePanelNews();

    public MainController(String addr) {
        super(new MainFrame(), null);
        initDocking();
        this.addr = addr;
        instance = this;
    }

    void initDocking() {
        Dockable articles = new Dockable() {
            DockKey key = new DockKey("articlesList", "Артыкулы");

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelArticles;
            }
        };
        Dockable issues = new Dockable() {
            DockKey key = new DockKey("issuesList", "Заўвагі");

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelIssues;
            }
        };
        Dockable news = new Dockable() {
            DockKey key = new DockKey("newsList", "Навіны");

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelNews;
            }
        };
        desk = new DockingDesktop();
        window.getContentPane().add(desk);
        desk.registerDockable(articles);
        desk.registerDockable(news);
        desk.registerDockable(issues);
        SettingsController.loadDocking(window, desk);
    }

    public void start() throws Exception {
        window.setVisible(true);

        showProgress();
        askPassword();
    }

    public void startWithUser(String username, String pass) throws Exception {
        window.setVisible(true);

        login(username, pass);
    }

    /**
     * Show ask password dialog
     */
    private void askPassword() {
        // show ask password dialog
        PasswordDialog askPassword = new PasswordDialog(window, true);

        // setup default button
        askPassword.getRootPane().setDefaultButton(askPassword.btnOk);
        askPassword.btnOk.addActionListener((e) -> {
            askPassword.btnCancel = null;
            askPassword.dispose();
            login(askPassword.txtUser.getText().trim(), new String(askPassword.txtPass.getPassword()).trim());
        });

        // setup cancel button
        ActionListener cancelListener = (e) -> {
            askPassword.dispose();
        };
        askPassword.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (askPassword.btnCancel != null) {
                    window.dispose();
                }
            }
        });
        askPassword.btnCancel.addActionListener(cancelListener);
        askPassword.getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // display
        askPassword.setLocationRelativeTo(window);
        askPassword.setVisible(true);
    }

    /**
     * Login to server and retrieve initial info.
     */
    void login(String username, String pass) {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                WS.init(addr, username, pass);
                initialData = WS.getToolsWebservice().getInitialData(WS.header);
                WS.header.configVersion = initialData.configVersion;

                SchemaLoader.init(initialData.articleSchema);

                issuesModel = new MainFrameIssuesModel(WS.getToolsWebservice().listIssues(WS.header));
                newsModel = new MainFrameNewsModel(WS.getToolsWebservice().listNews(WS.header));
            }

            @Override
            protected void ok() {
                window.setTitle(window.getTitle() + " : " + username);
                init();
            }
        };
    }

    private void init() {
        Vector<String> users = new Vector<>();
        users.add(null);
        users.addAll(initialData.allUsers.keySet());
        panelArticles.cbUser.setModel(new DefaultComboBoxModel<>(users));
        Vector<String> states = new Vector<>();
        states.add(null);
        states.addAll(initialData.states);
        panelArticles.cbState.setModel(new DefaultComboBoxModel<>(states));
        panelArticles.btnSearch.addActionListener((e) -> search());
        panelArticles.tableArticles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = panelArticles.tableArticles.rowAtPoint(e.getPoint());
                int index = panelArticles.tableArticles.convertRowIndexToModel(row);
                ArticleShort a = ((MainFrameArticlesModel) panelArticles.tableArticles.getModel()).articles.get(index);
                new ArticleEditController(a.id);
            }
        });
        panelArticles.tableArticles.getSelectionModel().addListSelectionListener((e) -> {
            panelArticles.labelSelected.setText("Пазначана: " + panelArticles.tableArticles.getSelectedRows().length);
        });
        panelArticles.tableArticles.setAutoCreateRowSorter(true);
        panelIssues.tableIssues.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Related a = ((MainFrameIssuesModel) panelIssues.tableIssues.getModel()).issues
                        .get(panelIssues.tableIssues.getSelectedRow());
                new ArticleEditController(a.articleId);
            }
        });
        panelNews.tableNews.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Related a = ((MainFrameNewsModel) panelNews.tableNews.getModel()).news
                        .get(panelNews.tableNews.getSelectedRow());
                new ArticleEditController(a.articleId);
            }
        });

        if (initialData.currentUserPermissions.contains(Permission.ADD_WORDS.name())) {
            window.miAddWords.addActionListener((e) -> new AddWordsController());
        } else {
            window.miAddWords.setVisible(false);
        }

        if (initialData.currentUserPermissions.contains(Permission.ADD_ARTICLE.name())) {
            panelArticles.btnAddArticle.addActionListener((e) -> new ArticleEditController());
        } else {
            panelArticles.btnAddArticle.setVisible(false);
        }

        if (initialData.currentUserPermissions.contains(Permission.FULL_STATISTICS.name())) {
            window.miStat.addActionListener((e) -> todo("Тут будзе статыстыка ў залежнасці ад дазволу"));
        } else {
            window.miStat.setVisible(false);
        }

        window.miSettings.addActionListener((e) -> new SettingsController());

        if (initialData.currentUserPermissions.contains(Permission.REASSIGN.name())) {
            panelArticles.btnReassign.addActionListener(reassign);
        } else {
            panelArticles.btnReassign.setVisible(false);
        }

        if (initialData.currentUserPermissions.contains(Permission.FULL_VALIDATION.name())) {
            window.miValidateFull.addActionListener((e) -> validateFull());
        } else {
            window.miValidateFull.setVisible(false);
        }

        if (initialData.currentUserPermissions.contains(Permission.VIEW_OUTPUT.name())) {
            panelArticles.btnPreview.addActionListener(preview);
            window.miExport.addActionListener((e) -> exportAll());
        } else {
            panelArticles.btnPreview.setVisible(false);
            window.miExport.setVisible(false);
        }

        window.miResetDesk.addActionListener(e -> {
            SettingsController.resetPlaces(MainFrame.class);
            SettingsController.resetPlaces(ArticleEditDialog.class);
            SettingsController.loadPlacesForWindow(window, desk);
        });

        SettingsController.savePlacesForWindow(window, desk);
        panelIssues.tableIssues.setModel(issuesModel);
        panelNews.tableNews.setModel(newsModel);
        SettingsController.loadPlacesForWindow(window, desk);
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
            List<ArticleShort> articles = new ArrayList<>();
            for (int r : panelArticles.tableArticles.getSelectedRows()) {
                articles.add(model.articles.get(r));
            }
            if (articles.size() > 100) {
                todo("Абрана зашмат артыкулаў");
            } else {
                new PreviewAllController(articles);
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
        filter.word = panelArticles.txtWord.getText().trim().isEmpty() ? null : panelArticles.txtWord.getText().trim();
        filter.text = panelArticles.txtText.getText().trim().isEmpty() ? null : panelArticles.txtText.getText().trim();
        new LongProcess() {
            MainFrameArticlesModel model;

            @Override
            protected void exec() throws Exception {
                model = new MainFrameArticlesModel(WS.getArticleService().listArticles(WS.header, filter));
                issuesModel = new MainFrameIssuesModel(WS.getToolsWebservice().listIssues(WS.header));
                newsModel = new MainFrameNewsModel(WS.getToolsWebservice().listNews(WS.header));
            }

            @Override
            protected void ok() {
                SettingsController.savePlacesForWindow(window, desk);
                panelArticles.tableArticles.setModel(model);
                panelIssues.tableIssues.setModel(issuesModel);
                panelNews.tableNews.setModel(newsModel);
                panelArticles.labelSelected.setText("Знойдзена: " + model.getRowCount());
                SettingsController.loadPlacesForWindow(window, desk);
            }
        };
    }

    private void validateFull() {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                WS.getToolsWebservice().validateAll(WS.header);
            }

            @Override
            protected void ok() {
                JOptionPane.showMessageDialog(window,
                        "Валідацыя прайшла паспяхова. Абнавіце спіс артыкулаў каб бачыць вынікі", "Валідацыя",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };
    }

    private void exportAll() {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                ArticlesFilter filter = new ArticlesFilter();
                byte[] pdf = WS.getToolsWebservice().previewAll(WS.header, filter);
                Files.write(Paths.get("preview.pdf"), pdf);
                Commands.showPdf("preview.pdf");
            }
        };
    }

    public void fireArticleUpdated(ArticleFull article) {
        if (panelArticles.tableArticles.getModel() instanceof MainFrameArticlesModel) {
            MainFrameArticlesModel model = (MainFrameArticlesModel) panelArticles.tableArticles.getModel();
            for (ArticleShort a : model.articles) {
                if (a.id == article.id) {
                    a.assignedUsers = article.assignedUsers;
                    a.state = article.state;
                    a.validationError = article.validationError;
                    a.words = article.words;
                }
            }
            model.fireTableDataChanged();
        }
    }
}
