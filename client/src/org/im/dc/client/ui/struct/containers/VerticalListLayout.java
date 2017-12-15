package org.im.dc.client.ui.struct.containers;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class VerticalListLayout implements LayoutManager {
    private final Insets insets;

    public VerticalListLayout(Insets insets) {
        this.insets = insets;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(100, 0);
        synchronized (parent.getTreeLock()) {
            for (Component c : parent.getComponents()) {
                if (c.isVisible()) {
                    Dimension d = c.getPreferredSize();
                    dim.height += d.height + insets.top + insets.bottom;
                }
            }
        }
        return dim;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        int y = 0;
        synchronized (parent.getTreeLock()) {
            for (Component c : parent.getComponents()) {
                if (c.isVisible()) {
                    Dimension d = c.getPreferredSize();
                    d.width = parent.getWidth() - insets.left - insets.right;
                    c.setSize(d);
                    c.setLocation(insets.left, y + insets.top);
                    y += d.height + insets.top + insets.bottom;
                }
            }
        }
    }
}
