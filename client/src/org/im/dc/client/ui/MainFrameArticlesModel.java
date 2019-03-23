package org.im.dc.client.ui;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.DefaultTableModel;

import org.im.dc.service.dto.ArticleShort;

@SuppressWarnings("serial")
public class MainFrameArticlesModel extends DefaultTableModel {
    static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle");

    protected final List<ArticleShort> articles;

    public MainFrameArticlesModel(List<ArticleShort> articles) {
        this.articles = articles;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return BUNDLE.getString("MainFrameArticlesModel.Article");
        case 1:
            return BUNDLE.getString("MainFrameArticlesModel.State");
        case 2:
            return BUNDLE.getString("MainFrameArticlesModel.Users");
        case 3:
            return BUNDLE.getString("MainFrameArticlesModel.Errors");
        default:
            return null;
        }
    }

    @Override
    public int getRowCount() {
        return articles != null ? articles.size() : 0;
    }

    @Override
    public Object getValueAt(int row, int column) {
        ArticleShort a = articles.get(row);
        switch (column) {
        case 0:
            return a.header;
        case 1:
            return a.state;
        case 2:
            return String.join(",", a.assignedUsers);
        case 3:
            return a.validationError;
        default:
            return null;
        }
    }
}
