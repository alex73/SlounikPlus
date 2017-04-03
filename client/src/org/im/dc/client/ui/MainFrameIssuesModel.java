package org.im.dc.client.ui;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.im.dc.service.dto.Related;

@SuppressWarnings("serial")
public class MainFrameIssuesModel extends DefaultTableModel {
    private SimpleDateFormat TIME_FMT = new SimpleDateFormat("dd MMM HH:mm");

    protected final List<Related> issues;

    public MainFrameIssuesModel(List<Related> issues) {
        this.issues = issues;
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
            return "Час";
        case 1:
            return "Хто";
        case 2:
            return "Сутнасць";
        case 3:
            return "Артыкул";
        default:
            return null;
        }
    }

    @Override
    public int getRowCount() {
        return issues != null ? issues.size() : 0;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Related a = issues.get(row);
        switch (column) {
        case 0:
            return TIME_FMT.format(a.when);
        case 1:
            return a.who;
        case 2:
            return a.getDescription();
        case 3:
            return Arrays.toString(a.words);
        default:
            return null;
        }
    }
}
