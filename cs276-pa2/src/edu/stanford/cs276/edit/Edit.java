package edu.stanford.cs276.edit;

public class Edit {
    private static String[] formats = {"del[%c,%c]", "ins[%c,%c]", "sub[%c,%c]", "trans[%c,%c]"};

    public EditType type;
    public char x;
    public char y;

    public Edit(EditType t, char x, char y) {
        this.type = t;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format(formats[type.ordinal()], x, y);
    }
}
