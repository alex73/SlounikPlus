package org.im.dc.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
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

        Vector<String> users=new Vector<>(MainController.initialData.allUsers.keySet());
        Collections.sort(users, Collator.getInstance());
        window.list.setModel(new DefaultComboBoxModel<>(users));

        articleIds = new int[articles.size()];
        for (int i = 0; i < articles.size(); i++) {
            articleIds[i] = articles.get(i).id;
        }
        window.pack();

        displayOnParent();
    }

    private ActionListener reassign = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = (String)window.list.getSelectedItem();

            new LongProcess() {
                @Override
                protected void exec() throws Exception {
                    if (window.rbAdd.isSelected()) {
                        WS.getToolsWebservice().assignUser(WS.header, typeInfo.typeId, articleIds, username);
                    } else {
                        WS.getToolsWebservice().unassignUser(WS.header, typeInfo.typeId, articleIds, username);
                    }
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
