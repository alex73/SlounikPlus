package org.im.dc.client.ui.struct;

import java.util.Set;

import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.struct.AnnotationInfo.RORW;

public class ArticleUIContext {
    public final String articleTypeId;
    public Set<String> userRoles;
    public String articleState;
    public ArticleEditController editController;

    public ArticleUIContext(String articleTypeId) {
        this.articleTypeId = articleTypeId;
    }

    public boolean getWritable(boolean parentWritable, AnnotationInfo ann) {
        Boolean wr = null;
        for (RORW e : ann.enables) {
            if (e.role.equals("*") || userRoles.contains(e.role)) {
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

    public boolean getVisible(boolean parentVisible, AnnotationInfo ann) {
        Boolean wr = null;
        for (RORW e : ann.visible) {
            if (e.role.equals("*") || userRoles.contains(e.role)) {
                if (e.state.equals("*") || e.state.equals(articleState)) {
                    wr = e.writable;
                }
            }
        }
        if (wr == null) {
            wr = parentVisible;
        }
        return wr;
    }

    public String getArticleTypeId() {
        return articleTypeId;
    }

    public void resetChanged() {
        editController.resetChanged();
    }

    public void fireChanged() {
        editController.setChanged();
    }
}
