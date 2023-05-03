package checker;

import checker.error.*;
import syntax.*;

import java.util.function.Function;

public class SymbolVisitor implements SyntaxTreeVisitor<Void> {
    private SymbolTable curProgram;
    private SymbolTable.STClass curClass;
    private SymbolTable.STMethod curMethod;
    public Function<DuplicateDeclarationError, Boolean> onDuplicateDeclatationError;

    public SymbolTable getLast() {
        return curProgram;
    }

    private void error(DuplicateDeclarationError e) {
        if (onDuplicateDeclatationError == null || !onDuplicateDeclatationError.apply(e))
            throw e;
    }

    public Void visit(Program n) {
        curProgram = new SymbolTable();

        if (n == null)
            return null;

        if (n.m != null)
            n.m.accept(this);

        for (var c : n.cl)
            c.accept(this);

        return null;
    }

    public Void visit(MainClass n) {
        if (curProgram.putNewClass(n.nameOfMainClass.s) != null)
            error(new DuplicateDeclarationError(n.nameOfMainClass, DuplicateDeclarationError.CLASS));

        return null;
    }

    public Void visit(SimpleClassDecl n) {
        if (curProgram.putNewClass(n.i.s) != null)
            error(new DuplicateDeclarationError(n.i, DuplicateDeclarationError.CLASS));

        curClass = curProgram.getClass(n.i.s);

        for (var f : n.fields)
            f.accept(this);
        for (var m : n.methods)
            m.accept(this);

        return null;
    }

    public Void visit(ExtendingClassDecl n) {
        if (curProgram.putNewClass(n.i.s, n.j.s) != null)
            error(new DuplicateDeclarationError(n.i, DuplicateDeclarationError.CLASS));

        curClass = curProgram.getClass(n.i.s);

        for (var f : n.fields)
            f.accept(this);
        for (var m : n.methods)
            m.accept(this);

        return null;
    }

    public Void visit(MethodDecl n) {
        if (curClass.putNewMethod(n.i.s, n.t.toString(), n.formals.stream().map(x -> x.t.toString()).toList()) != null)
            error(new DuplicateDeclarationError(n.i, DuplicateDeclarationError.METHOD));

        curMethod = curClass.getImmediateMethod(n.i.s);

        for (var f : n.formals)
            f.accept(this);
        for (var l : n.locals)
            l.accept(this);

        return null;
    }

    public Void visit(FieldDecl n) {
        if (curClass.putClassVariable(n.i.s, n.t.toString()) != null)
            error(new DuplicateDeclarationError(n.i, DuplicateDeclarationError.CLASSVAR));

        return null;
    }

    public Void visit(LocalDecl n) {
        if (curMethod.putLocalVariable(n.i.s, n.t.toString()) != null)
            error(new DuplicateDeclarationError(n.i, DuplicateDeclarationError.LOCALVAR));

        return null;
    }

    public Void visit(FormalDecl n) {
        if (curMethod.putFormalVariable(n.i.s, n.t.toString()) != null)
            error(new DuplicateDeclarationError(n.i, DuplicateDeclarationError.LOCALVAR));

        return null;
    }

    public Void visit(IdentifierType n) {
        return null;
    }

    public Void visit(Identifier n) {
        return null;
    }


    public Void visit(IntArrayType n) {
        return null;
    }

    public Void visit(BooleanType n) {
        return null;
    }

    public Void visit(IntegerType n) {
        return null;
    }

    public Void visit(VoidType n) {
        return null;
    }

    public Void visit(Block n) {
        return null;
    }

    public Void visit(If n) {
        return null;
    }

    public Void visit(While n) {
        return null;
    }

    public Void visit(Print n) {
        return null;
    }

    public Void visit(Assign n) {
        return null;
    }

    public Void visit(ArrayAssign n) {
        return null;
    }

    public Void visit(And n) {
        return null;
    }

    public Void visit(LessThan n) {
        return null;
    }

    public Void visit(Plus n) {
        return null;
    }

    public Void visit(Minus n) {
        return null;
    }

    public Void visit(Times n) {
        return null;
    }

    public Void visit(ArrayLookup n) {
        return null;
    }

    public Void visit(ArrayLength n) {
        return null;
    }

    public Void visit(Call n) {
        return null;
    }

    public Void visit(IntegerLiteral n) {
        return null;
    }

    public Void visit(True n) {
        return null;
    }

    public Void visit(False n) {
        return null;
    }

    public Void visit(IdentifierExp n) {
        return null;
    }

    public Void visit(This n) {
        return null;
    }

    public Void visit(NewArray n) {
        return null;
    }

    public Void visit(NewObject n) {
        return null;
    }

    public Void visit(Not n) {
        return null;
    }
}
