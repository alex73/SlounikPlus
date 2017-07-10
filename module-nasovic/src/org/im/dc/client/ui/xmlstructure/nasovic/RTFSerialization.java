package org.im.dc.client.ui.xmlstructure.nasovic;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Base64;

import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class RTFSerialization {
    public static void serialize(JEditorPane pane, DataOutputStream out) throws Exception {
        StyledDocument doc = (StyledDocument) pane.getDocument();
        int max = doc.getLength();
        while (max > 0 && Character.isSpaceChar(doc.getText(max - 1, 1).charAt(0))) {
            max--;
        }
        String text = doc.getText(0, max);
        out.writeInt(text.length());
        for (int i = 0; i < text.length(); i++) {
            Element el = doc.getCharacterElement(i);
            AttributeSet a = el.getAttributes();
            out.writeInt(text.charAt(i));
            out.writeBoolean(StyleConstants.isItalic(a));
            out.writeBoolean(StyleConstants.isBold(a));
            Color bg = (Color) a.getAttribute(StyleConstants.Background);
            Color fg = (Color) a.getAttribute(StyleConstants.Foreground);
            out.writeInt(bg != null ? bg.getRGB() : -1);
            out.writeInt(fg != null ? fg.getRGB() : -1);
        }
        out.flush();
    }

    public static void deserialize(JEditorPane pane, DataInputStream in) throws Exception {
        StyledDocument doc = (StyledDocument) pane.getDocument();
        int count = in.readInt();
        StringBuilder text = new StringBuilder(count);
        boolean[] italic = new boolean[count];
        boolean[] bold = new boolean[count];
        int[] bg = new int[count];
        int[] fg = new int[count];
        for (int i = 0; i < count; i++) {
            text.append((char) in.readInt());
            italic[i] = in.readBoolean();
            bold[i] = in.readBoolean();
            bg[i] = in.readInt();
            fg[i] = in.readInt();
        }
        doc.remove(0, doc.getLength());
        doc.insertString(0, text.toString(), null);
        // pane.setText(text.toString());
        for (int i = 0; i < count; i++) {
            SimpleAttributeSet a = new SimpleAttributeSet();
            StyleConstants.setItalic(a, italic[i]);
            StyleConstants.setBold(a, bold[i]);
            if (bg[i] != -1) {
                StyleConstants.setBackground(a, new Color(bg[i]));
            }
            if (fg[i] != -1) {
                StyleConstants.setForeground(a, new Color(fg[i]));
            }
            doc.setCharacterAttributes(i, 1, a, false);
        }
    }

    public static String deserializeText(String base64in) throws Exception {
        byte[] data = Base64.getDecoder().decode(base64in);
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            int count = in.readInt();
            StringBuilder text = new StringBuilder(count);
            for (int i = 0; i < count; i++) {
                text.append((char) in.readInt());
                in.readBoolean();
                in.readBoolean();
                in.readInt();
                in.readInt();
            }
            return text.toString();
        }
    }
}
