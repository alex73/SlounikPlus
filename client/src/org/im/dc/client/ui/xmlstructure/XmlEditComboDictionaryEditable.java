package org.im.dc.client.ui.xmlstructure;

import org.im.dc.client.ui.ArticleEditController;

@SuppressWarnings("serial")
public class XmlEditComboDictionaryEditable extends XmlEditBase<JFilterComboBox> implements IXmlSimpleElement {
    public XmlEditComboDictionaryEditable(ArticleUIContext context, XmlGroup parentPanel, AnnotationInfo ann, boolean parentWritable) {
        super(context, parentPanel, ann, parentWritable);
    }

    @Override
    protected JFilterComboBox createField() {
        /*
         * Dictionaries.Dictionary dict = editController.dictionaries.dicts.get(ann.editDetails); if (dict == null) {
         * dict = new Dictionaries.Dictionary(); } JFilterComboBox fc = new JFilterComboBox(dict.values);
         * fc.setFont(rootPanel.getFont()); fc.setSelectedItem(""); fc.addItemListener(new ItemListener() {
         * 
         * @Override public void itemStateChanged(ItemEvent e) { rootPanel.fireChanged(); } }); return fc;
         */
        return null;
    }

    @Override
    public void setData(String data) throws Exception {
        field.setSelectedItem(data);
    }
    @Override
    public String getData() throws Exception {
        return (String) field.getSelectedItem();
    }
}
