package org.im.dc.client.ui.xmlstructure;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class JFilterComboBox extends JComboBox<String> {
    protected List<String> array;
    private int currentCaretPosition = 0;

    public JFilterComboBox(List<String> array) {
        super(array.toArray(new String[0]));
        this.array = array;
        this.setEditable(true);
        final JTextField textfield = (JTextField) this.getEditor().getEditorComponent();
        textfield.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        currentCaretPosition = textfield.getCaretPosition();
                        if (textfield.getSelectedText() == null) {
                            textfield.setCaretPosition(0);
                            comboFilter(textfield.getText());
                            textfield.setCaretPosition(currentCaretPosition);
                        }
                    }
                });
            }
        });
    }

    public void comboFilter(String enteredText) {
        if (!this.isPopupVisible()) {
            this.showPopup();
        }

        List<String> filterArray = new ArrayList<String>();
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).toLowerCase().contains(enteredText.toLowerCase())) {
                filterArray.add(array.get(i));
            }
        }
        if (filterArray.size() > 0) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) this.getModel();
            model.removeAllElements();
            for (String s : filterArray)
                model.addElement(s);

            JTextField textfield = (JTextField) this.getEditor().getEditorComponent();
            textfield.setText(enteredText);
        }
    }
}
