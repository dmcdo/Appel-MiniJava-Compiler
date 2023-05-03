package checker.error;

public class InvalidArgumentsError extends SemanticsError {
    public int ecount;
    public int count;
    public String className;
    public String method;

    public InvalidArgumentsError(int l, int c, int ecount, int count, String className, String method) {
        super(l, c);
        this.ecount    = ecount;
        this.count     = count;
        this.className = className;
        this.method    = method;
    }
}
