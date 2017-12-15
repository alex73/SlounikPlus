package org.im.dc.client.ui.struct.containers;

import org.apache.xerces.xs.XSObject;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;

public abstract class XSBaseContainer<T extends XSObject> implements IXSContainer {
    protected ArticleUIContext context;
    protected final IXSContainer parentContainer;
    protected final T obj;

    public XSBaseContainer(ArticleUIContext context, IXSContainer parentContainer, T obj) {
        this.context = context;
        this.parentContainer = parentContainer;
        this.obj = obj;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public IXSContainer getParentContainer() {
        return parentContainer;
    }
}
