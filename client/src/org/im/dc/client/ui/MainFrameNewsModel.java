package org.im.dc.client.ui;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.DefaultTableModel;

import org.im.dc.service.dto.Related;

@SuppressWarnings("serial")
public class MainFrameNewsModel extends DefaultTableModel {
    static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle");

    private SimpleDateFormat TIME_FMT = new SimpleDateFormat("dd MMM HH:mm");

    protected final List<Related> news;

    public MainFrameNewsModel(List<Related> news) {
        this.news = news;
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
            return BUNDLE.getString("MainFrameIssuesModel.Time");
        case 1:
            return BUNDLE.getString("MainFrameIssuesModel.Who");
        case 2:
            return BUNDLE.getString("MainFrameIssuesModel.Details");
        case 3:
            return BUNDLE.getString("MainFrameIssuesModel.What");
        default:
            return null;
        }
    }

    @Override
    public int getRowCount() {
        return news != null ? news.size() : 0;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Related a = news.get(row);
        switch (column) {
        case 0:
            return TIME_FMT.format(a.when);
        case 1:
            return a.who;
        case 2:
            return a.getDescription();
        case 3:
            return a.header;
        default:
            return null;
        }
    }
}
