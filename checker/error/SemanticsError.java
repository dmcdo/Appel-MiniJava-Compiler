package checker.error;

public class SemanticsError extends Error {
    public int l;
    public int c;

    public SemanticsError(int l, int c) {
        super(String.format("Uncaught Semantics Error at (%d, %d)", l, c));
        this.l = l;
        this.c = c;
    }
}
