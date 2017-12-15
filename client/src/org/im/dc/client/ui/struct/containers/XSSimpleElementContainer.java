package org.im.dc.client.ui.struct.containers;

import java.lang.reflect.Constructor;
import java.util.Collection;

import javax.swing.JComponent;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
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

public class XSSimpleElementContainer extends XSBaseContainer<XSSimpleTypeDefinition> {
    protected XSElementDeclaration elem;
    private final AnnotationInfo ann;
    private final IXSEdit editor;

    public XSSimpleElementContainer(ArticleUIContext context, IXSContainer parentContainer, XSElementDeclaration elem,
            XSSimpleType obj) {
        super(context, parentContainer, obj);
        this.elem = elem;
        ann = new AnnotationInfo(elem.getAnnotation());

        if (ann.customImpl != null) {
            // custom implementation
            try {
                Constructor<?> c = ann.customImpl.getConstructor(ArticleUIContext.class, IXSContainer.class,
                        AnnotationInfo.class);
                editor = (IXSEdit) c.newInstance(context, this, elem, ann);
            } catch (Exception ex) {
                throw new RuntimeException("Error create custom control from " + ann.customImpl.getName(), ex);
            }
            return;
        }

        if (ann.editType == null) {
            switch (obj.getPrimitiveKind()) {
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
    public Collection<IXSContainer> children() {
        return null;
    }

    @Override
    public String getTag() {
        return elem.getName();
    }

    @Override
    public JComponent getUIComponent() {
        return editor.getUIComponent();
    }

    public IXSEdit getEditor() {
        return editor;
    }

    @Override
    public void insertData(XMLStreamReader rd) throws Exception {
        if (!rd.getLocalName().equals(elem.getName())) {
            throw new Exception("Wrong parsing");
        }
        editor.setData(rd.getElementText());
        if (rd.getEventType() != XMLStreamConstants.END_ELEMENT) {
            throw new Exception("Wrong parsing");
        }
    }

    @Override
    public void extractData(XMLStreamWriter wr) throws Exception {
        String data = editor.getData();
        if (data != null) {
            wr.writeStartElement(elem.getName());
            wr.writeCharacters(data);
            wr.writeEndElement();
        }
    }

    public String dump(String prefix) {
        String r = prefix + getClass().getSimpleName() + " " + ann.text + " - simple\n";
        return r;
    }
}
