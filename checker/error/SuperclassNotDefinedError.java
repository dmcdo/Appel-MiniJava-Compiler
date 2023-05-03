package checker.error;

public class SuperclassNotDefinedError extends SemanticsError {
    public String subclass;
    public String superclass;

    public SuperclassNotDefinedError(int l, int c, String subclass, String superclass) {
        super(l, c);
        this.subclass = subclass;
        this.superclass = superclass;
    }
}
