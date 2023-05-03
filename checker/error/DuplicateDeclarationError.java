package checker.error;

import syntax.Identifier;

public class DuplicateDeclarationError extends SemanticsError {
    public static final int CLASS = 0;
    public static final int METHOD = 1;
    public static final int CLASSVAR = 2;
    public static final int LOCALVAR = 3;

    public String name;
    public int scope;

    public DuplicateDeclarationError(Identifier i, int scope) {
        super(i.lineNumber, i.columnNumber);
        this.name = i.s;
        this.scope = scope;
    }

    public DuplicateDeclarationError(int l, int c, String name, int scope) {
        super(l, c);
        this.name = name;
        this.scope = scope;
    }
}
