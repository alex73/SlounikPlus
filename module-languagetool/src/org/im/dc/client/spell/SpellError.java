package org.im.dc.client.spell;

public class SpellError {
    public final int offset, length;
    public final String error;

    public SpellError(int offset, int length, String error) {
        this.offset = offset;
        this.length = length;
        this.error = error;
    }
}
