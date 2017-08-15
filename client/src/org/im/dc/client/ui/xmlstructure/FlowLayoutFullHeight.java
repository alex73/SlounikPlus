package org.im.dc.client.ui.xmlstructure;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * Layout for display all components in specific width.
 */
@SuppressWarnings("serial")
public class FlowLayoutFullHeight extends FlowLayout {
    @Override
    public Dimension preferredLayoutSize(Container target) {
        Dimension d = minimumLayoutSize(target);
        return d;
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            boolean useBaseline = getAlignOnBaseline();
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            int maxAscent = 0;
            int maxDescent = 0;
            int rowh = 0;
            int width = 0;
            boolean firstInRow = true;

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getMinimumSize();
                    int addWidth;
                    if (firstInRow) {
                        addWidth = 0;
                        firstInRow = false;
                    } else {
                        addWidth = getHgap();
                    }
                    addWidth += d.width;
                    if (width > 0 && addWidth + width > target.getWidth()) {
                        // next row if at least one component has
                        if (dim.height > 0) {
                            dim.height += getVgap();
                        }
                        dim.height += rowh;
                        dim.width = Math.max(dim.width, width);
                        rowh = 0;
                        width = 0;
                    }
                    width += addWidth;
                    rowh = Math.max(rowh, d.height);
                    if (useBaseline) {
                        int baseline = m.getBaseline(d.width, d.height);
                        if (baseline >= 0) {
                            maxAscent = Math.max(maxAscent, baseline);
                            maxDescent = Math.max(maxDescent, dim.height - baseline);
                        }
                    }
                }
            }
            if (width > 0) {
                // next row if at least one component has
                if (dim.height > 0) {
                    dim.height += getVgap();
                }
                dim.height += rowh;
                dim.width = Math.max(dim.width, width);
            }

            if (useBaseline) {
                dim.height = Math.max(maxAscent + maxDescent, dim.height);
            }

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right + getHgap() * 2;
            dim.height += insets.top + insets.bottom + getVgap() * 2;
            System.out
                    .println("minimum=" + dim + " for " + target.getSize() + " parent " + target.getParent().getSize());
            return dim;
        }
    }
}
