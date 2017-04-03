package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.gen.config.Permission;
import org.im.dc.service.dto.ArticleShort;
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
        window.cbUser.setModel(new DefaultComboBoxModel<>(new Vector<>(initialData.allUsers)));
        window.btnSearch.addActionListener((e) -> search());
        window.tableArticles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArticleShort a = ((MainFrameArticlesModel) window.tableArticles.getModel()).articles
                        .get(window.tableArticles.getSelectedRow());
                new ArticleEditController(window, a.id);
            }
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

        window.btnUsers.addActionListener(
                (e) -> todo("Тут будзе пераназначэнне карыстальнікаў, з фільтрам па карыстальнікам і станах"));

        window.btnValidateFull.addActionListener((e) -> todo("Тут будзе праверка ўсяго слоўніка"));

        SettingsController.savePlacesForWindow(window);
        window.tableIssues.setModel(issuesModel);
        window.tableNews.setModel(newsModel);
        SettingsController.loadPlacesForWindow(window);
    }

    /**
     * Search articles by filter.
     */
    private void search() {
        new LongProcess() {
            MainFrameArticlesModel model;

            @Override
            protected void exec() throws Exception {
                model = new MainFrameArticlesModel(WS.getArticleService().listArticles(WS.header, null));
            }

            @Override
            protected void ok() {
                SettingsController.savePlacesForWindow(window);
                window.tableArticles.setModel(model);
                SettingsController.loadPlacesForWindow(window);
            }
        };
    }
}
