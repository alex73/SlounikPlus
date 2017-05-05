package org.im.dc.client.ui.xmlstructure.tlum;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.xmlstructure.AnnotationInfo;
import org.im.dc.client.ui.xmlstructure.XmlEditBase;
import org.im.dc.client.ui.xmlstructure.XmlGroup;

@SuppressWarnings("serial")
public class XmlEditOneParadigm extends XmlEditBase<XmlEditOneParadigmPanel> {
    static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(OneParadigmInfo.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private List<VariantInfo> current;

    public XmlEditOneParadigm(XmlGroup rootPanel, XmlGroup parentPanel, AnnotationInfo ann,
            ArticleEditController editController) {
        super(rootPanel, parentPanel, ann, editController);
    }

    @Override
    protected XmlEditOneParadigmPanel createField() {
        XmlEditOneParadigmPanel r = new XmlEditOneParadigmPanel();
        r.cbPara.addActionListener((e) -> {
            int idx = field.cbPara.getSelectedIndex();
            update(new OneParadigmInfo(current.get(idx)));
        });
        r.rbOne.addActionListener((e) -> refreshOut());
        r.rbMany.addActionListener((e) -> refreshOut());
        for (int i = 0; i < 5; i++) {
            r.table.getColumnModel().getColumn(i).setCellRenderer(new ColorRenderer());
        }

        r.table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int c = 0;
                if (e.getKeyChar() == '+') {
                    c++;
                } else if (e.getKeyChar() == '-') {
                    c--;
                }
                if (c == 0) {
                    return;
                }
                FormsTableModel model = (FormsTableModel) r.table.getModel();
                int row = r.table.getSelectedRow();
                int col = r.table.getSelectedColumn();
                if (col == 0) {
                    String sklon = model.info.changeShowSklon(row);
                    for (int i = 0; i < model.info.rows.size(); i++) {
                        if (model.info.rows.get(i).getCode().equals(sklon)) {
                            model.fireTableCellUpdated(i, 0);
                        }
                    }
                } else {
                    model.info.changeEnd(row, col, c);
                    model.fireTableCellUpdated(row, col);
                }
                e.consume();
                refreshOut();
                rootPanel.fireChanged();
            }
        });

        return r;
    }

    void refreshList() {
        XmlEditParadygmy paraList = (XmlEditParadygmy) rootPanel.getManyPart("paradyhmy").getElements().get(0);
        current = paraList.getCurrent();
        Vector<String> list = new Vector<>();
        for (VariantInfo p : current) {
            list.add(p.toString());
        }
        field.cbPara.setModel(new DefaultComboBoxModel<String>(list));
    }

    void refreshOut() {
        FormsTableModel model = (FormsTableModel) field.table.getModel();
        model.info.mainIsSingle = field.rbOne.isSelected();
        try {
            String out = JsOutput.exec(model.info);
            field.txtResult.setText(out);
        } catch (Exception ex) {
            field.txtResult.setText(ex.getMessage());
        }
    }

    void update(OneParadigmInfo info) {
        field.table.setAutoCreateColumnsFromModel(false);

        FormsTableModel model = new FormsTableModel(info);
        field.table.setModel(model);
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn c = field.table.getColumnModel().getColumn(i);
            if (model.isColumnEmpty(i)) {
                c.setMinWidth(0);
                c.setMaxWidth(0);
            } else {
                c.setMinWidth(10);
                c.setMaxWidth(Integer.MAX_VALUE);
            }
        }
        field.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        field.table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        field.table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        refreshOut();
        rootPanel.fireChanged();
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        Unmarshaller m = CONTEXT.createUnmarshaller();

        int n = rd.nextTag();
        if (n != XMLStreamConstants.START_ELEMENT || !rd.getLocalName().equals("OneParadigmInfo")) {
            throw new Exception("Expected 'OneParadigmInfo' tag");
        }
        OneParadigmInfo info = m.unmarshal(rd, OneParadigmInfo.class).getValue();
        if (rd.getEventType() != XMLStreamConstants.END_ELEMENT || !rd.getLocalName().equals("partSimple")) {
            throw new Exception("Expected 'partSimple' tag end");
        }

        update(info);
    }

    @Override
    public void displayed() {
        refreshList();
    }

    @Override
    public void extractData(String tag, XMLStreamWriter wr) throws Exception {
        Marshaller m = CONTEXT.createMarshaller();
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        wr.writeStartElement(tag);

        if (field.table.getModel() instanceof FormsTableModel) {
            FormsTableModel model = (FormsTableModel) field.table.getModel();
            m.marshal(model.info, wr);
        }else {
            m.marshal(new OneParadigmInfo(), wr);
        }

        wr.writeEndElement();
    }

    class ColorRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
            if (field.table.getModel() instanceof FormsTableModel) {
                if (hasFocus) {
                    cellComponent.setBackground(Color.YELLOW);
                } else {
                    FormsTableModel model = (FormsTableModel) field.table.getModel();
                    boolean changed = column == 0 ? model.info.isCHangedSklon(row)
                            : model.info.isChangedEnd(row, column);
                    cellComponent.setBackground(changed ? Color.CYAN : Color.WHITE);
                }
            }
            return cellComponent;
        }
    }
}
