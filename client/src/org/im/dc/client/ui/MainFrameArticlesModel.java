package org.im.dc.client.ui;

import java.util.Arrays;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.im.dc.service.dto.ArticleShort;

@SuppressWarnings("serial")
public class MainFrameArticlesModel extends DefaultTableModel {
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
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return "Артыкул";
        case 1:
            return "Стан";
        case 2:
            return "Памылкі";
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
            return Arrays.toString(a.words);
        case 1:
            return a.state;
        case 2:
            return a.validationError;
        default:
            return null;
        }
    }
}
