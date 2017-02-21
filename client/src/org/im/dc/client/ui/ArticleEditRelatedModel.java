package org.im.dc.client.ui;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.im.dc.service.dto.Related;

@SuppressWarnings("serial")
public class ArticleEditRelatedModel extends DefaultTableModel {
    private SimpleDateFormat TIME_FMT = new SimpleDateFormat("dd MMM HH:mm");

    protected final List<Related> related;

    public ArticleEditRelatedModel(List<Related> related) {
        this.related = related;
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
            return "Час";
        case 1:
            return "Хто";
        case 2:
            return "Сутнасць";
        default:
            return null;
        }
    }

    @Override
    public int getRowCount() {
        return related != null ? related.size() : 0;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Related a = related.get(row);
        switch (column) {
        case 0:
            return TIME_FMT.format(a.when);
        case 1:
            return a.who;
        case 2:
            return a.what;
        default:
            return null;
        }
    }
}
