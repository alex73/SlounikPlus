package org.im.dc.client.ui.rtfeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Icons source: https://material.io/tools/icons/ and https://materialdesignicons.com
 */
@SuppressWarnings("serial")
public class TinySwingPanel extends JPanel implements ActionListener {

    protected final JTextPane text;

    protected JButton sizeInc, sizeDec, bold, italic, underline, strikethrough, subscript, superscript, colorFg,
            colorBg, indentDec, indentInc, clear;

    public TinySwingPanel(JTextPane text) {
        this.text = text;
        setLayout(new WrapLayout(WrapLayout.CENTER, 0, 0));
        sizeInc = createButton("format-font-size-increase.png", "sizeInc");
        sizeDec = createButton("format-font-size-decrease.png", "sizeDec");
        bold = createButton("baseline-format_bold-24px.png", "bold");
        italic = createButton("baseline-format_italic-24px.png", "italic");
        underline = createButton("baseline-format_underlined-24px.png", "underline");
        strikethrough = createButton("baseline-strikethrough_s-24px.png", "strikethrough");
        subscript = createButton("format-subscript.png", "subscript");
        superscript = createButton("format-superscript.png", "superscript");
        colorFg = createButton("border-color.png", "colorFg");
        colorBg = createButton("format-color-fill.png", "colorBg");
        indentDec = createButton("baseline-format_indent_decrease-24px.png", "indentDec");
        indentInc = createButton("baseline-format_indent_increase-24px.png", "indentInc");
        clear = createButton("baseline-format_clear-24px.png", "clear");
    }

    protected JButton createButton(String icon, String command) {
        URL imageURL = TinySwingPanel.class.getResource("/org/im/dc/client/ui/rtfeditor/icons/" + icon);

        JButton button = new JButton();
        button.addActionListener(this);
        button.setActionCommand(command);
        button.setIcon(new ImageIcon(imageURL));
        button.setBorder(null);
        button.setContentAreaFilled(false);

        final Border raisedBevelBorder = BorderFactory.createRaisedBevelBorder();
        final Insets insets = raisedBevelBorder.getBorderInsets(button);
        final EmptyBorder emptyBorder = new EmptyBorder(insets);
        button.setBorder(emptyBorder);
        button.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ButtonModel model = (ButtonModel) e.getSource();
                if (model.isRollover()) {
                    button.setBorder(raisedBevelBorder);
                } else {
                    button.setBorder(emptyBorder);
                }
            }
        });
        add(button);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        int s = text.getSelectionStart();
        int e = text.getSelectionEnd();
        StyledDocument doc = (StyledDocument) text.getDocument();
        AttributeSet exist = doc.getCharacterElement(s).getAttributes();
        AttributeSet existp = doc.getParagraphElement(s).getAttributes();
        MutableAttributeSet attrs = new SimpleAttributeSet();
        switch (ae.getActionCommand()) {
        case "sizeInc":
            int si = StyleConstants.getFontSize(exist) + 2;
            StyleConstants.setFontSize(attrs, si);
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "sizeDec":
            int sd = StyleConstants.getFontSize(exist) - 2;
            if (sd < 10) {
                sd = 10;
            }
            StyleConstants.setFontSize(attrs, sd);
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "bold":
            StyleConstants.setBold(attrs, !StyleConstants.isBold(exist));
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "italic":
            StyleConstants.setItalic(attrs, !StyleConstants.isItalic(exist));
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "underline":
            StyleConstants.setUnderline(attrs, !StyleConstants.isUnderline(exist));
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "strikethrough":
            StyleConstants.setStrikeThrough(attrs, !StyleConstants.isStrikeThrough(exist));
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "subscript":
            StyleConstants.setSubscript(attrs, !StyleConstants.isSubscript(exist));
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "superscript":
            StyleConstants.setSuperscript(attrs, !StyleConstants.isSuperscript(exist));
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "indentDec":
            float vd = StyleConstants.getLeftIndent(existp) - 40;
            if (vd < 0) {
                vd = 0;
            }
            StyleConstants.setLeftIndent(attrs, vd);
            doc.setParagraphAttributes(s, e - s, attrs, false);
            break;
        case "colorFg":
            Color newColorFg = JColorChooser.showDialog(this, "Choose a color", new Color(0, 0, 0, 0));
            if (newColorFg == null) {
                return;
            }
            if (newColorFg.getAlpha() < 255) {
                newColorFg = text.getForeground();
            }
            StyleConstants.setForeground(attrs, newColorFg);
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "colorBg":
            Color newColorBg = JColorChooser.showDialog(this, "Choose a color", new Color(0, 0, 0, 0));
            if (newColorBg == null) {
                return;
            }
            if (newColorBg.getAlpha() < 255) {
                newColorBg = text.getBackground();
            }
            StyleConstants.setBackground(attrs, newColorBg);
            doc.setCharacterAttributes(s, e - s, attrs, false);
            break;
        case "indentInc":
            float vi = StyleConstants.getLeftIndent(existp) + 40;
            StyleConstants.setLeftIndent(attrs, vi);
            doc.setParagraphAttributes(s, e - s, attrs, false);
            break;
        case "clear":
            doc.setCharacterAttributes(s, e - s, attrs, true);
            break;
        default:
            return;
        }
    }
}
