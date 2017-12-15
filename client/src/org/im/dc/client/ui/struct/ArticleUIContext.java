package org.im.dc.client.ui.struct;

import java.awt.Font;

import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.struct.AnnotationInfo.RORW;

public class ArticleUIContext {
    public String userRole;
    public String articleState;
    public ArticleEditController editController;

    public boolean getWritable(boolean parentWritable, AnnotationInfo ann) {
        Boolean wr = null;
        for (RORW e : ann.enables) {
            if (e.role.equals("*") || e.role.equals(userRole)) {
                if (e.state.equals("*") || e.state.equals(articleState)) {
                    wr = e.writable;
                }
            }
        }
        if (wr == null) {
            wr = parentWritable;
        }
        return wr;
    }

    public Font getFont() {
        return editController.panelEdit.getFont();
    }

    public String getArticleTypeId() {
        return editController.getArticleTypeId();
    }

    public void resetChanged() {
        editController.resetChanged();
    }

    public void fireChanged() {
        editController.setChanged();
    }
}
