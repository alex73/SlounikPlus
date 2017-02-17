package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.gen.config.Permission;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.InitialData;

/**
 * Controls main window.
 */
public class MainController extends BaseController<MainFrame> {
    static InitialData initialData;

    public MainController() {
        super(new MainFrame());
    }

    public void start() throws Exception {
        window.setVisible(true);

        showProgress();
        askPassword();
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
                WS.init("http://localhost:9080/myapp", username, pass);
                initialData = WS.getToolsWebservice().getInitialData(WS.header);
                if (initialData.currentUserPermissions == null) {
                    initialData.currentUserPermissions = Collections.emptySet();
                }
                window.setTitle(window.getTitle() + " : " + username);

                SchemaLoader.init(initialData.articleSchema);
            }

            @Override
            protected void ok() {
                init();
            }
        };
    }

    private void init() {
        window.btnSearch.addActionListener((e) -> search());
        window.tableArticles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArticleShort a = ((MainFrameArticlesModel) window.tableArticles.getModel()).articles
                        .get(window.tableArticles.getSelectedRow());
                new ArticleController(window, a.id);
            }
        });

        window.btnAddWords.setVisible(initialData.currentUserPermissions.contains(Permission.ADD_WORDS.name()));
        window.btnStat.setVisible(initialData.currentUserPermissions.contains(Permission.STATISTICS.name()));
        window.btnValidateFull
                .setVisible(initialData.currentUserPermissions.contains(Permission.FULL_VALIDATION.name()));
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
                window.tableArticles.setModel(model);
            }
        };
    }
}
