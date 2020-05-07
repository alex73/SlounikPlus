package org.im.dc.client.ui.utils;

import java.awt.Color;
import java.util.List;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

public class DiffInit {

    static public DiffPanel init(String text1, String text2) {
        DiffPanel window = new DiffPanel();
        SimpleAttributeSet mark = new SimpleAttributeSet();
        StyleConstants.setBackground(mark, Color.RED);
        SimpleAttributeSet markLight = new SimpleAttributeSet();
        StyleConstants.setBackground(markLight, Color.PINK);

        window.text1.setEditorKit(new NoWrapEditorKit());
        window.text2.setEditorKit(new NoWrapEditorKit());
        StyledDocument doc1 = (StyledDocument) window.text1.getDocument();
        StyledDocument doc2 = (StyledDocument) window.text2.getDocument();
        List<Diff> diffs = new diff_match_patch().diff_main(text1, text2, true);
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff = diffs.get(i);
            if (diff.text.isEmpty()) {
                diffs.remove(i);
                i--;
            }
        }
        for (int i = 1; i < diffs.size(); i++) {
            Diff prev = diffs.get(i - 1);
            Diff diff = diffs.get(i);
            if (diff.operation == prev.operation) {
                prev.text += diff.text;
                diffs.remove(i);
                i--;
            }
        }
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff = diffs.get(i);
            Diff prev, next;
            try {
                prev = diffs.get(i - 1);
            } catch (IndexOutOfBoundsException ex) {
                prev = null;
            }
            try {
                next = diffs.get(i + 1);
            } catch (IndexOutOfBoundsException ex) {
                next = null;
            }
            switch (diff.operation) {
            case DELETE:
                if (next == null || next.operation != Operation.INSERT) {
                    next = new Diff(Operation.INSERT, "");
                    diffs.add(i + 1, next);
                }
                balanceDiff(diff, next);
                break;
            case INSERT:
                if (prev == null || prev.operation != Operation.DELETE) {
                    prev = new Diff(Operation.DELETE, "");
                    diffs.add(i, prev);
                }
                balanceDiff(prev, diff);
                break;
            }
        }
        for (int i = 2; i < diffs.size(); i++) {
            Diff diffDelete = diffs.get(i - 2);
            Diff diffInsert = diffs.get(i - 1);
            Diff diffEquals = diffs.get(i);
            if (diffDelete.operation == Operation.DELETE && diffInsert.operation == Operation.INSERT
                    && diffEquals.operation == Operation.EQUAL) {
                while (diffDelete.text.endsWith("\n") && diffInsert.text.endsWith("\n")) {
                    diffEquals.text = '\n' + diffEquals.text;
                    diffDelete.text = diffDelete.text.substring(0, diffDelete.text.length() - 1);
                    diffInsert.text = diffInsert.text.substring(0, diffInsert.text.length() - 1);
                }
            }
        }
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff = diffs.get(i);
            if (diff.operation == Operation.EQUAL && i > 0) {
                // fix after change
                int p = diff.text.indexOf('\n');
                if (p > 0) {
                    String s = diff.text.substring(0, p);
                    diff.text = diff.text.substring(p);
                    diffs.add(i, new Diff(null, s));
                    i++;
                } else if (p < 0) {
                    diff.operation = null;
                }
            }
            if (diff.operation == Operation.EQUAL && i < diffs.size() - 1) {
                // fix before change
                int p = diff.text.lastIndexOf('\n');
                if (p >= 0 && p < diff.text.length() - 1) {
                    String s = diff.text.substring(p + 1);
                    diff.text = diff.text.substring(0, p + 1);
                    diffs.add(i + 1, new Diff(null, s));
                    i++;
                } else if (p < 0) {
                    diff.operation = null;
                }
            }
        }

        for (Diff diff : diffs) {
            try {
                if (diff.operation == null) {
                    doc1.insertString(doc1.getLength(), diff.text, markLight);
                    doc2.insertString(doc2.getLength(), diff.text, markLight);
                } else {
                    switch (diff.operation) {
                    case EQUAL:
                        doc1.insertString(doc1.getLength(), diff.text, null);
                        doc2.insertString(doc2.getLength(), diff.text, null);
                        break;
                    case DELETE:
                        doc1.insertString(doc1.getLength(), diff.text, mark);
                        break;
                    case INSERT:
                        doc2.insertString(doc2.getLength(), diff.text, mark);
                        break;
                    }
                }
            } catch (BadLocationException ex) {
            }
        }
        
        window.text1.setCaretPosition(0);
        window.text2.setCaretPosition(0);
        return window;
    }

    static private void balanceDiff(Diff delete, Diff insert) {
        int deleteEols = countEOL(delete.text);
        int insertEols = countEOL(insert.text);
        for (; deleteEols < insertEols; deleteEols++) {
            delete.text += "\n";
        }
        for (; insertEols < deleteEols; insertEols++) {
            insert.text += "\n";
        }
    }

    static private int countEOL(String s) {
        int r = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                r++;
            }
        }
        return r;
    }

    static class NoWrapEditorKit extends StyledEditorKit {
        ViewFactory defaultFactory = new WrapColumnFactory();

        public ViewFactory getViewFactory() {
            return defaultFactory;
        }
    }

    static class WrapColumnFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new NoWrapParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            return new LabelView(elem);
        }
    }

    static class NoWrapParagraphView extends ParagraphView {
        public NoWrapParagraphView(final Element elem) {
            super(elem);
        }

        @Override
        public void layout(final int width, final int height) {
            super.layout(Short.MAX_VALUE, height);
        }

        @Override
        public float getMinimumSpan(final int axis) {
            return super.getPreferredSpan(axis);
        }
    }
}
