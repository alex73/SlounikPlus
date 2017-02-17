package org.im.dc.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.im.dc.client.WS;

public class AddWordsController extends BaseController<AddWordsDialog> {
    public AddWordsController(JFrame parent) {
        super(new AddWordsDialog(parent, true));

        window.btnOk.addActionListener(ok);

        window.cbInitialState.setModel(new DefaultComboBoxModel<>(new Vector<>(MainController.initialData.states)));

        displayOn(parent);
    }

    private ActionListener ok = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String[] users = new String[0];
            String initialState = window.cbInitialState.getSelectedItem().toString();
            String w = window.words.getText();
            new LongProcess() {
                @Override
                protected void exec() throws Exception {
                    String[] words = w.split("\n");
                    WS.getToolsWebservice().addWords(WS.header, users, words, initialState);
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
