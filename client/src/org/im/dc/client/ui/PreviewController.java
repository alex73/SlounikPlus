package org.im.dc.client.ui;

import javax.swing.JDialog;

import org.im.dc.client.WS;

public class PreviewController extends BaseController<PreviewDialog> {
    public PreviewController(JDialog parent, int articleId) {
        super(new PreviewDialog(MainController.instance.window, true));
        setupCloseOnEscape();

        new LongProcess() {
            String preview;

            @Override
            protected void exec() throws Exception {
                preview = WS.getToolsWebservice().printPreview(WS.header, articleId);
            }

            @Override
            protected void ok() {
                window.text.setText(preview);
            }

            @Override
            protected void error() {
                window.dispose();
            }
        };
        displayOn(parent);
    }
}
