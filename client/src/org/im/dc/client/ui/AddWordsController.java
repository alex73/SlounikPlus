package org.im.dc.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.im.dc.client.WS;

public class AddWordsController extends BaseController<AddWordsDialog> {
    public AddWordsController(JFrame parent) {
        super(new AddWordsDialog(parent, true));
        setupCloseOnEscape();

        window.btnOk.addActionListener(ok);
        window.btnCancel.addActionListener((e) -> window.dispose());

        window.cbInitialState.setModel(new DefaultComboBoxModel<>(new Vector<>(MainController.initialData.states)));
        for (Map.Entry<String, String> en : MainController.initialData.allUsers.entrySet()) {
            JCheckBox cb = new JCheckBox(en.getKey() + " (" + en.getValue() + ')');
            cb.setName(en.getKey());
            window.panelUsers.add(cb);
        }

        displayOn(parent);
    }

    private ActionListener ok = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> users = new ArrayList<>();
            for (int i = 0; i < window.panelUsers.getComponentCount(); i++) {
                JCheckBox cb = (JCheckBox) window.panelUsers.getComponent(i);
                if (cb.isSelected()) {
                    users.add(cb.getName());
                }
            }
            String initialState = window.cbInitialState.getSelectedItem().toString();
            String w = window.words.getText();
            new LongProcess() {
                @Override
                protected void exec() throws Exception {
                    String[] words = w.split("\n");
                    WS.getToolsWebservice().addWords(WS.header, users.toArray(new String[users.size()]), words,
                            initialState);
                }

                @Override
                protected void ok() {
                    JOptionPane.showMessageDialog(window, "Словы паспяхова захаваныя", "Новыя словы",
                            JOptionPane.INFORMATION_MESSAGE);
                    window.dispose();
                }
            };
        }
    };
}
