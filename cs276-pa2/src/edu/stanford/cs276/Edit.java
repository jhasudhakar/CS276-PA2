package edu.stanford.cs276;

public class Edit {
    public EditType type;
    public char x;
    public char y;

    public Edit(EditType t, char x, char y) {
        this.type = t;
        this.x = x;
        this.y = y;
    }
}
