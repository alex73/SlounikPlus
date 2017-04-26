package org.im.dc.client.ui.xmlstructure.tlum;

import java.awt.Font;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import org.alex73.corpus.paradigm.Paradigm;
import org.alex73.korpus.base.BelarusianTags;
import org.alex73.korpus.base.GrammarDB2;
import org.alex73.korpus.base.GrammarDBSaver;
import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.BaseController;
import org.im.dc.client.ui.MainController;

public class SelectParadigmController extends BaseController<SelectParadigmDialog> {
    protected static GrammarDB2 db;
    protected static List<Paradigm> filteredParadigms;
    protected Set<Integer> selectedParadigms = new TreeSet<>();

    public SelectParadigmController(ArticleEditController editor, XmlEditParadygmy parent) {
        super(new SelectParadigmDialog(MainController.instance.window, true), editor.window);

        setupCloseOnEscape();

        if (db == null) {
            // request article from server
            new LongProcess() {
                @Override
                protected void exec() throws Exception {
                    db = GrammarDB2.initializeFromJar();
                }

                @Override
                protected void ok() {
                    filteredParadigms = db.getAllParadigms();
                    show();
                }

                @Override
                protected void error() {
                    window.dispose();
                }
            };
        } else {
            filteredParadigms = db.getAllParadigms();
            show();
        }
        window.table.setFont(window.table.getFont().deriveFont(Font.PLAIN));

        window.txtFilter.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterChanged();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterChanged();
            }
        });

        window.btnChoose.addActionListener(e -> {
            parent.field.setText(selectedParadigms.toString());
            window.dispose();
        });

        displayOnParent();
    }

    void filterChanged() {
        String filter = window.txtFilter.getText().trim();
        if (filter.isEmpty()) {
            filteredParadigms = db.getAllParadigms();
        } else {
            filteredParadigms = db.getAllParadigms().parallelStream().filter(
                    p -> selectedParadigms.contains(p.getPdgId()) || p.getLemma().replace("+", "").startsWith(filter))
                    .collect(Collectors.toList());
            filteredParadigms.sort(GrammarDBSaver.COMPARATOR);
        }
        ((DefaultTableModel) window.table.getModel()).fireTableDataChanged();
    }

    void show() {
        window.table.setModel(new DefaultTableModel() {
            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public int getRowCount() {
                return filteredParadigms.size();
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
                case 2:
                    return String.class;
                }
                return null;
            }

            @Override
            public Object getValueAt(int row, int column) {
                Paradigm p = filteredParadigms.get(row);
                switch (column) {
                case 0:
                    return selectedParadigms.contains(p.getPdgId());
                case 1:
                    return p.getLemma();
                case 2:
                    try {
                        return BelarusianTags.getInstance().describe(p.getTag());
                    } catch (Exception ex) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    Paradigm p = filteredParadigms.get(row);
                    if (!selectedParadigms.remove(p.getPdgId())) {
                        selectedParadigms.add(p.getPdgId());
                    }
                }
            }
        });
    }
}
