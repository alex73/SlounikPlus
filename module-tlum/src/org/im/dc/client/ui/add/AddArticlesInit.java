package org.im.dc.client.ui.add;

import javax.swing.JMenuItem;

import org.im.dc.client.ui.AddWordsController;
import org.im.dc.client.ui.MainController;

public class AddArticlesInit {

    public void init() {
        // TODO check role name
        JMenuItem item = new JMenuItem("Дадаць артыкулы");
        MainController.instance.window.menuCommon.add(item);
        item.addActionListener((e) -> new AddWordsController(null));
    }
}
