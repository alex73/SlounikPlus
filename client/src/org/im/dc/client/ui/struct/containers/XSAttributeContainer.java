package org.im.dc.client.ui.struct.containers;

import java.lang.reflect.Constructor;
import java.util.Collection;

import javax.swing.JComponent;
import javax.xml.stream.XMLStreamWriter;

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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class XSAttributeContainer extends XSBaseContainer<XSAttributeDeclaration> {
    private boolean required;
    private AnnotationInfo ann;
    private IXSEdit editor;

    public XSAttributeContainer(ArticleUIContext context, IXSContainer parentContainer, XSAttributeDeclaration obj,
            boolean required) {
        super(context, parentContainer, obj);
        ann = new AnnotationInfo(obj.getAnnotation());

        if (ann.customImpl != null) {
            try {
                Constructor<?> c = ann.customImpl.getConstructor(ArticleUIContext.class, IXSContainer.class,
                        AnnotationInfo.class);
                editor = (IXSEdit) c.newInstance(context, this, ann);
            } catch (Exception ex) {
                throw new RuntimeException("Error create custom control from " + ann.customImpl.getName(), ex);
            }
        } else if (ann.editType == null) {
            String typeName;
            if (obj.getTypeDefinition().getAnonymous()) {
                typeName = obj.getTypeDefinition().getBaseType().getName();
            } else {
                typeName = obj.getTypeDefinition().getName();
            }
            if (typeName == null) {
                throw new RuntimeException("Can't create editor for unknown simple type: " + obj.getName());
            }
            switch (typeName) {
            case "boolean":
                editor = new XSEditBoolean(context, this, ann);
                break;
            case "integer":
            case "string":
                editor = new XSEditText(context, this, ann);
                break;
            default:
                throw new RuntimeException("Can't create editor for simple type: " + obj.getName());
            }
        } else {
            switch (ann.editType) {
            case TEXT:
                editor = new XSEditText(context, this, ann);
                break;
            case CHECK:
                editor = new XSEditBoolean(context, this, ann);
                break;
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

    public IXSEdit getEditor() {
        return editor;
    }

    @Override
    public Collection<IXSContainer> children() {
        return null;
    }

    @Override
    public boolean isWritable() {
        return context.getWritable(parentContainer, ann);
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public void insertData(Element node) throws Exception {
        NamedNodeMap attrs=  node.getAttributes();
      for( int i=0;i<attrs.getLength();i++  ) {
          String attrName =   attrs.item(i).getNodeName();
          String attrValue =   attrs.item(i).getTextContent();
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
