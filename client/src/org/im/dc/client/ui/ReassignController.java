package org.im.dc.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.im.dc.client.WS;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.InitialData;

public class ReassignController extends BaseController<ReassignDialog> {
    private final InitialData.TypeInfo typeInfo;
    private int[] articleIds;

    public ReassignController(List<ArticleShort> articles, String typeId) {
        super(new ReassignDialog(MainController.instance.window, true), MainController.instance.window);
        setupCloseOnEscape();

        typeInfo = MainController.initialData.getTypeInfo(typeId);

        window.btnReassign.addActionListener(reassign);
        window.btnCancel.addActionListener((e) -> window.dispose());

        for (Map.Entry<String, String[]> en : MainController.initialData.allUsers.entrySet()) {
            int c = calcForUser(articles, en.getKey());
            JCheckBox cb = new JCheckBox(en.getKey()
                    + (c > 0 ? " (" + String.join(",", en.getValue()) + ") - на яго прызначана " + c + " слоў" : ""));
            cb.setName(en.getKey());
            if (c > 0) {
                cb.setSelected(true);
            }
            window.panelUsers.add(cb);
        }
        articleIds = new int[articles.size()];
        for (int i = 0; i < articles.size(); i++) {
            articleIds[i] = articles.get(i).id;
        }
        window.pack();

        displayOnParent();
    }

    private int calcForUser(List<ArticleShort> articles, String user) {
        int count = 0;
        for (ArticleShort a : articles) {
            for (String u : a.assignedUsers) {
                if (user.equals(u)) {
                    count++;
                }
            }
        }
        return count;
    }

    private ActionListener reassign = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> users = new ArrayList<>();
            for (int i = 0; i < window.panelUsers.getComponentCount(); i++) {
                JCheckBox cb = (JCheckBox) window.panelUsers.getComponent(i);
                if (cb.isSelected()) {
                    users.add(cb.getName());
                }
            }

            new LongProcess() {
                @Override
                protected void exec() throws Exception {
                    WS.getToolsWebservice().reassignUsers(WS.header, typeInfo.typeId, articleIds,
                            users.toArray(new String[users.size()]));
                }

                @Override
                protected void ok() {
                    JOptionPane.showMessageDialog(window, "Словы паспяхова пераназначаныя", "Пераназначэнне",
                            JOptionPane.INFORMATION_MESSAGE);
                    window.dispose();
                }
            };
        }
    };
}
