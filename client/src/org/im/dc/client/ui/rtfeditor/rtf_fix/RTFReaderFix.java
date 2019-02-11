package org.im.dc.client.ui.rtfeditor.rtf_fix;

import javax.swing.text.StyledDocument;

/**
 * It overrides standard RTFEditor. All files in this package are the copy of
 * JDK files without any changes except 'public' modifier. This code required
 * for read background color from RTF file.
 */
public class RTFReaderFix extends RTFReader {
    public RTFReaderFix(StyledDocument destination) {
        super(destination);
    }

    @Override
    public boolean handleKeyword(String keyword, int parameter) {
        if (keyword.equals("cb")) {
            parserState.put(keyword, Integer.valueOf(parameter));
            return true;
        }
        return super.handleKeyword(keyword, parameter);
    }
}
