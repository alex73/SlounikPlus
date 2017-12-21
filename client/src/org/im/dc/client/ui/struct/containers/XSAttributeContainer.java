package org.im.dc.client.ui.struct.containers;

import java.util.Collection;

import javax.swing.JComponent;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.client.ui.struct.editors.IXSEdit;
import org.im.dc.client.ui.struct.editors.XSEditArticlesList;
import org.im.dc.client.ui.struct.editors.XSEditBoolean;
import org.im.dc.client.ui.struct.editors.XSEditCombo;
import org.im.dc.client.ui.struct.editors.XSEditComboFiltered;
import org.im.dc.client.ui.struct.editors.XSEditRadio;
import org.im.dc.client.ui.struct.editors.XSEditText;

public class XSAttributeContainer extends XSBaseContainer<XSAttributeDeclaration> {
    private boolean required;
    private AnnotationInfo ann;
    private IXSEdit editor;

    public XSAttributeContainer(ArticleUIContext context, IXSContainer parentContainer, XSAttributeDeclaration obj,
            boolean required) {
        super(context, parentContainer, obj);
        ann = new AnnotationInfo(obj.getAnnotation());

        if (ann.editType == null) {
            switch (obj.getTypeDefinition().getType()) {
            case XSSimpleType.PRIMITIVE_BOOLEAN:
                editor = new XSEditBoolean(context, this, ann);
                break;
            case XSSimpleType.PRIMITIVE_DECIMAL:
            case XSSimpleType.PRIMITIVE_STRING:
                editor = new XSEditText(context, this, ann);
                break;
            default:
                throw new RuntimeException("Can't create editor for simple type: " + obj.getName());
            }
        } else {
            switch (ann.editType) {
            case CHECK:
                throw new RuntimeException("Can't create editor for type: " + ann.editType.name());
            case RADIO:
                editor = new XSEditRadio(context, this, ann);
                break;
            case COMBO:
                editor = new XSEditCombo(context, this, ann);
                break;
            case COMBO_FILTERED:
                editor = new XSEditComboFiltered(context, this, ann);
                break;
            case ARTICLES_LIST:
                editor = new XSEditArticlesList(context, this, ann);
                break;
            default:
                throw new RuntimeException("Can't create editor for type: " + ann.editType.name());
            }
        }
    }

    @Override
    public JComponent getUIComponent() {
        return editor.getUIComponent();
    }

    @Override
    public Collection<IXSContainer> children() {
        return null;
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        for (int i = 0; i < rd.getAttributeCount(); i++) {
            String attrName = rd.getAttributeLocalName(i);
            String attrValue = rd.getAttributeValue(i);
            if (attrName.equals(obj.getName())) {
                editor.setData(attrValue);
            }
        }
    }

    @Override
    public void extractData(XMLStreamWriter wr) throws Exception {
        String data = editor.getData();
        if (data != null) {
            wr.writeAttribute(obj.getName(), data);
        }
    }

    public String dump(String prefix) {
        String r = prefix + getClass().getSimpleName() + " " + ann.text + " <" + (required ? "required" : "optional")
                + "> - " + obj.getTypeDefinition().getName() + " attribute\n";
        return r;
    }
}