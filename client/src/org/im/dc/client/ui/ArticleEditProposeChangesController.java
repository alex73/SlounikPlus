package org.im.dc.client.ui;

import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.im.dc.client.WS;

public class ArticleEditProposeChangesController extends BaseController<ArticleEditProposeChangesDialog> {
    private final ArticleEditController parent;

    public ArticleEditProposeChangesController(ArticleEditController parent) {
        super(new ArticleEditProposeChangesDialog(MainController.instance.window, true), parent.window);
        this.parent = parent;

        window.setTitle(parent.wasChanged ? "Прапанаваць змены" : "Дадаць заўвагу");

        window.btnOk.addActionListener(save);
        window.btnCancel.addActionListener((e) -> window.dispose());

        setupCloseOnEscape();

        displayOnParent();
    }

    ActionListener save = (e) -> {
        String comment = window.txtComment.getText();

        new LongProcess() {
            boolean saved = false;

            @Override
            protected void exec() throws Exception {
                byte[] proposedXml = parent.wasChanged ? parent.extractXml() : null;

                String err = WS.getToolsWebservice().validate(WS.header, parent.article.article.type,
                        parent.article.article.id, proposedXml);
                if (err != null) {
                    if (JOptionPane.showConfirmDialog(window,
                            "Памылка валідацыі: " + err + "\nЗахоўваць нягледзячы на гэта ?", "Памылка",
                            JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }

                parent.article = WS.getArticleService().addIssue(WS.header, parent.article.article.type,
                        parent.article.article.id, comment, proposedXml, parent.article.article.lastUpdated);
                saved = true;
            }

            @Override
            protected void ok() {
                if (saved) {
                    parent.show();
                    JOptionPane.showMessageDialog(window, "Прапановы паспяхова захаваныя", "Захавана",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                window.dispose();
            }
        };
    };
}
