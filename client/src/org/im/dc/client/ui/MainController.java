package org.im.dc.client.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.InitialData;

public class MainController {
    static MainFrame ui;
    static InitialData initialData;

    public static void start() {
        ui = new MainFrame();
        ui.setVisible(true);

        // show ask password dialog
        PasswordDialog askPassword = new PasswordDialog(ui, true);
        UI.displayDialog(askPassword, askPassword.btnOk, askPassword.btnCancel, () -> {
            if (UI.checkError(() -> login(askPassword.txtUser.getText().trim(),
                    new String(askPassword.txtPass.getPassword()).trim()))) {
                ui.dispose();
            } else {
                init();
            }
        }, () -> ui.dispose());
    }

    private static void init() {
        ui.getRootPane().setDefaultButton(ui.btnSearch);
        ui.btnSearch.addActionListener((e) -> {
            UI.checkError(() -> {
                MainFrameArticlesModel model = new MainFrameArticlesModel(
                        WS.getArticleService().listArticles(WS.header, null));
                ui.tableArticles.setModel(model);
            });
        });
        ui.tableArticles.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ArticleShort a = ((MainFrameArticlesModel) ui.tableArticles.getModel()).articles
                        .get(ui.tableArticles.getSelectedRow());
                new ArticleController(a.id);
            }
        });
    }

    private static void login(String user, String pass) throws Exception {
        Thread.sleep(20000);
        WS.init("http://localhost:9080/myapp", user, pass);
        initialData = WS.getToolsWebservice().getInitialData(WS.header);
        ui.setTitle(ui.getTitle() + " : " + user);

        SchemaLoader.init(initialData.articleSchema);
    }
}
