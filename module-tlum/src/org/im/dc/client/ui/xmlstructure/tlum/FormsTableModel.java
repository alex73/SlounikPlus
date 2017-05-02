package org.im.dc.client.ui.xmlstructure.tlum;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class FormsTableModel extends AbstractTableModel {
    protected final OneParadigmInfo info;

    public FormsTableModel(OneParadigmInfo info) {
        this.info = info;
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public int getRowCount() {
        return info.rows.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        OneParadigmInfo.SklonRow r = info.rows.get(rowIndex);
        if (columnIndex == 0) {
            return r.getName();
        } else {
            String v = r.getForm(columnIndex - 1);
            int m = r.getFormEnd(columnIndex - 1);
            if (m < 0) {
                return "--" + v;
            } else if (m == 0) {
                return v;
            } else {
                for (int i = v.length() - 1; i >= 0; i--) {
                    m--;
                    while (v.charAt(i) == '+' || v.charAt(i) == '-') {
                        i--;
                    }
                    if (m == 0) {
                        return v.substring(0, i) + "  ~" + v.substring(i);
                    }
                }
                return null;
            }
        }
    }

    public boolean isColumnEmpty(int columnIndex) {
        if (columnIndex == 0) {
            return false;
        } else {
            for (OneParadigmInfo.SklonRow r : info.rows) {
                if (r.getForm(columnIndex - 1) != null) {
                    return false;
                }
            }
            return true;
        }
    }
}
