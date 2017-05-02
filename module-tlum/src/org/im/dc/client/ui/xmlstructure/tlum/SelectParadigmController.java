package org.im.dc.client.ui.xmlstructure.tlum;

import java.awt.Font;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.alex73.corpus.paradigm.Paradigm;
import org.alex73.corpus.paradigm.Variant;
import org.alex73.korpus.base.BelarusianTags;
import org.alex73.korpus.base.GrammarDB2;
import org.alex73.korpus.utils.StressUtils;
import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.BaseController;
import org.im.dc.client.ui.MainController;

public class SelectParadigmController extends BaseController<SelectParadigmDialog> {
    protected static GrammarDB2 db;
    protected static Map<Integer, Paradigm> paradigmById;
    protected static List<VariantInfo> filteredParadigms;
    protected Set<String> selectedVariants = new TreeSet<>();

    public SelectParadigmController(ArticleEditController editor, XmlEditParadygmy parent, List<VariantInfo> input) {
        super(new SelectParadigmDialog(MainController.instance.window, true), editor.window);

        setupCloseOnEscape();

        for (VariantInfo p : input) {
            if (p.id != 0) {
                selectedVariants.add(p.getVariantId());
            }
        }
        if (db == null) {
            // request article from server
            new LongProcess() {
                @Override
                protected void exec() throws Exception {
                    db = GrammarDB2.initializeFromJar();
                    List<Paradigm> all = db.getAllParadigms();
                    paradigmById = new HashMap<>(all.size());
                    all.forEach(p -> paradigmById.put(p.getPdgId(), p));
                }

                @Override
                protected void ok() {
                    filterChanged();
                    show();
                }

                @Override
                protected void error() {
                    window.dispose();
                }
            };
        } else {
            filterChanged();
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
            List<VariantInfo> list = new ArrayList<>();
            selectedVariants.forEach(vid -> {
                int id = Integer.parseInt(vid.substring(0, vid.length() - 1));
                char vi = vid.charAt(vid.length() - 1);

                Paradigm p = paradigmById.get(id);
                if (p != null) {
                    list.add(new VariantInfo(p, vi));
                }
            });
            parent.setCurrent(list);
            window.dispose();
        });

        displayOnParent();
    }

    void filterChanged() {
        String filter = window.txtFilter.getText().trim();
        filteredParadigms = Collections.synchronizedList(new ArrayList<>());
        db.getAllParadigms().parallelStream().forEach(p -> {
            for (int i = 0; i < p.getVariant().size(); i++) {
                char vIndex = (char) ('a' + i);
                Variant v = p.getVariant().get(i);
                if (isVariantSelected(p, vIndex) || isLemmaFiltered(filter, v)) {
                    filteredParadigms.add(new VariantInfo(p, vIndex));
                }
            }
        });
        filteredParadigms.sort(SelectedVariant_COMPARATOR);
        ((DefaultTableModel) window.table.getModel()).fireTableDataChanged();
    }

    boolean isVariantSelected(Paradigm p, char vIndex) {
        return selectedVariants.contains(Integer.toString(p.getPdgId()) + vIndex);
    }

    boolean isLemmaFiltered(String filter, Variant v) {
        if (filter.isEmpty()) {
            return false;
        }
        return v.getLemma().replace("+", "").startsWith(filter);
    }

    void show() {
        window.table.setAutoCreateColumnsFromModel(false);
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
                VariantInfo p = filteredParadigms.get(row);
                switch (column) {
                case 0:
                    return selectedVariants.contains(p.getVariantId());
                case 1:
                    return p.lemma;
                case 2:
                    try {
                        return BelarusianTags.getInstance().describe(p.tag);
                    } catch (Exception ex) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 0) {
                    VariantInfo p = filteredParadigms.get(row);
                    if (!selectedVariants.remove(p.getVariantId())) {
                        selectedVariants.add(p.getVariantId());
                    }
                }
            }
        });
    }

    public static Locale BE = new Locale("be");
    public static Collator BEL = Collator.getInstance(BE);
    public static Comparator<VariantInfo> SelectedVariant_COMPARATOR = new Comparator<VariantInfo>() {
        @Override
        public int compare(VariantInfo p1, VariantInfo p2) {
            String w1 = StressUtils.unstress(p1.lemma.toLowerCase(BE));
            String w2 = StressUtils.unstress(p2.lemma.toLowerCase(BE));
            int r = BEL.compare(w1.toLowerCase(), w2.toLowerCase());
            if (r == 0) {
                r = BEL.compare(p1.lemma.toLowerCase(), p2.lemma.toLowerCase());
            }
            if (r == 0) {
                r = BEL.compare(p1.lemma, p2.lemma);
            }
            if (r == 0) {
                r = p1.tag.compareTo(p2.tag);
            }
            if (r == 0) {
                r = Integer.compare(p1.id, p2.id);
            }
            if (r == 0) {
                r = Character.compare(p1.variantIndex, p2.variantIndex);
            }
            return r;
        }
    };
}
