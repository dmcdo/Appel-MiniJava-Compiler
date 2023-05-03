package checker.error;

public class CyclicInheritenceError extends SemanticsError {
    public String className;

    public CyclicInheritenceError(int l, int c, String className) {
        super(l, c);
        this.className = className;
    }
}
