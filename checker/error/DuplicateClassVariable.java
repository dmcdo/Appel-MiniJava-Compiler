package checker.error;

public class DuplicateClassVariable extends DuplicateDeclarationError {
    public String className, superClassName;

    public DuplicateClassVariable(int l, int c, String varName, String className, String superClassName) {
        super(l, c, varName, DuplicateClassVariable.CLASSVAR);
        this.className = className;
        this.superClassName = superClassName;
    }
}
