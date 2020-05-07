package org.im.dc.client.ui.utils;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import org.im.dc.client.ui.BaseController;
import org.im.dc.client.ui.MainController;

public class ChooseStateController extends BaseController<ChooseStateDialog> {
    public List<String> result;

    public ChooseStateController(List<String> previouslySelected) {
        super(new ChooseStateDialog(MainController.instance.window, true), MainController.instance.window);
        setupCloseOnEscape();

        window.getRootPane().setDefaultButton(window.btnOK);

        window.btnOK.addActionListener(ok);
        window.btnCancel.addActionListener((e) -> window.dispose());

        MainController.initialData.states.forEach(s -> {
            JCheckBox cb = new JCheckBox(s, previouslySelected.contains(s));
            window.panelStates.add(cb);
        });

        window.pack();
        displayOnParent();
    }

    private ActionListener ok = e -> {
        result = new ArrayList<>();
        for (Component c : window.panelStates.getComponents()) {
            JCheckBox cb = (JCheckBox) c;
            if (cb.isSelected()) {
                result.add(cb.getText());
            }
        }
        window.dispose();
    };
}
