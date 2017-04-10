package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.gen.config.Permission;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;

/**
 * Controls main window.
 */
public class MainController extends BaseController<MainFrame> {
    static InitialData initialData;
    static MainController instance;
    int fontSize;

    private final String addr;
    private MainFrameIssuesModel issuesModel;
    private MainFrameNewsModel newsModel;

    public MainController(String addr) {
        super(new MainFrame());
        this.addr = addr;
        instance = this;
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
        window.cbUser.setModel(new DefaultComboBoxModel<>(users));
        window.btnSearch.addActionListener((e) -> search());
        window.tableArticles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArticleShort a = ((MainFrameArticlesModel) window.tableArticles.getModel()).articles
                        .get(window.tableArticles.rowAtPoint(e.getPoint()));
                new ArticleEditController(window, a.id);
            }
        });
        window.tableArticles.getSelectionModel().addListSelectionListener((e) -> {
            window.labelSelected.setText("Пазначана: " + window.tableArticles.getSelectedRows().length);
        });
        window.tableIssues.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Related a = ((MainFrameIssuesModel) window.tableIssues.getModel()).issues
                        .get(window.tableIssues.getSelectedRow());
                new ArticleEditController(window, a.articleId);
            }
        });
        window.tableNews.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Related a = ((MainFrameNewsModel) window.tableNews.getModel()).news
                        .get(window.tableNews.getSelectedRow());
                new ArticleEditController(window, a.articleId);
            }
        });

        window.btnAddWords.addActionListener((e) -> new AddWordsController(window));
        window.btnAddWords.setVisible(initialData.currentUserPermissions.contains(Permission.ADD_WORDS.name()));

        window.btnStat.addActionListener((e) -> todo("Тут будзе статыстыка для тых хто мае дазвол"));
        window.btnStat.setVisible(initialData.currentUserPermissions.contains(Permission.STATISTICS.name()));

        window.btnValidateFull
                .setVisible(initialData.currentUserPermissions.contains(Permission.FULL_VALIDATION.name()));

        window.btnSettings.addActionListener((e) -> new SettingsController(window));

        window.btnUsers.addActionListener(reassign);

        window.btnValidateFull.addActionListener((e) -> todo("Тут будзе праверка ўсяго слоўніка"));

        SettingsController.savePlacesForWindow(window);
        window.tableIssues.setModel(issuesModel);
        window.tableNews.setModel(newsModel);
        SettingsController.loadPlacesForWindow(window);
    }

    ActionListener reassign = (e) -> {
        MainFrameArticlesModel model = ((MainFrameArticlesModel) window.tableArticles.getModel());
        List<ArticleShort> articles = new ArrayList<>();
        for (int r : window.tableArticles.getSelectedRows()) {
            articles.add(model.articles.get(r));
        }

        new ReassignController(window, articles);
    };

    /**
     * Search articles by filter.
     */
    private void search() {
        ArticlesFilter filter = new ArticlesFilter();
        filter.user = (String) window.cbUser.getSelectedItem();
        filter.word = window.txtWord.getText().trim().isEmpty() ? null : window.txtWord.getText().trim();
        filter.text = window.txtText.getText().trim().isEmpty() ? null : window.txtText.getText().trim();
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
                SettingsController.savePlacesForWindow(window);
                window.tableArticles.setModel(model);
                window.tableIssues.setModel(issuesModel);
                window.tableNews.setModel(newsModel);
                window.labelSelected.setText("Знойдзена: " + model.getRowCount());
                SettingsController.loadPlacesForWindow(window);
            }
        };
    }
}
