package checker.error;

public class TypeError extends SemanticsError {
    public String expected;
    public String got;

    public TypeError(int l, int c, String expected, String got) {
        super(l, c);
        this.expected = expected;
        this.got = got;
    }
}
