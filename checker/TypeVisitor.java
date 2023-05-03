package checker;

import syntax.*;
import checker.error.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

public final class TypeVisitor implements SyntaxTreeVisitor<String> {
    public Function<TypeError, Boolean> onTypeError = null;
    public Function<SymbolNotFoundError, Boolean> onSymbolNotFoundError = null;
    public Function<InvalidArgumentsError, Boolean> onInvalidArgumentsError = null;

    private SymbolTable curProgram = null;
    private SymbolTable.STClass curClass = null;
    private SymbolTable.STMethod curMethod = null;

    public String visit(Program n, SymbolTable st) {
        curProgram = st;

        if (n == null)
            return null;

        if (n.m != null)
            n.m.accept(this);

        for (var c : n.cl)
            c.accept(this);

        return null;
    }

    public String visit(MainClass n) {
        curClass = curProgram.getClass(n.nameOfMainClass.s);
        curMethod = new SymbolTable.STMethod("main", "void", Arrays.asList(), new HashMap<>(), new HashMap<>());

        if (n.body != null)
            n.body.accept(this);

        return null;
    }

    public String visit(SimpleClassDecl n) {
        curClass = curProgram.getClass(n.i.s);

        for (var m : n.methods)
            m.accept(this);

        return null;
    }

    public String visit(ExtendingClassDecl n) {
        curClass = curProgram.getClass(n.i.s);

        for (var m : n.methods)
            m.accept(this);

        return null;
    }

    public String visit(MethodDecl n) {
        curMethod = curClass.getImmediateMethod(n.i.s);

        for (var s : n.sl)
            s.accept(this);

        String exp = n.t.toString();
        String got = n.e.accept(this);
        if (!curProgram.instanceOf(got, exp))
            error(new TypeError(n.e.lineNumber, n.e.columnNumber, exp, got));

        return null;
    }

    public String visit(And n) {
        String got;
        if ((got = n.e1.accept(this)) != "boolean")
            error(new TypeError(n.e1.lineNumber, n.e1.columnNumber, "boolean", got));
        if ((got = n.e2.accept(this)) != "boolean")
            error(new TypeError(n.e2.lineNumber, n.e2.columnNumber, "boolean", got));

        return "boolean";
    }

    public String visit(LessThan n) {
        String got;
        if ((got = n.e1.accept(this)) != "int")
            error(new TypeError(n.e1.lineNumber, n.e1.columnNumber, "int", got));
        if ((got = n.e2.accept(this)) != "int")
            error(new TypeError(n.e2.lineNumber, n.e2.columnNumber, "int", got));

        return "boolean";
    }

    public String visit(Plus n) {
        String got;
        if ((got = n.e1.accept(this)) != "int")
            error(new TypeError(n.e1.lineNumber, n.e1.columnNumber, "int", got));
        if ((got = n.e2.accept(this)) != "int")
            error(new TypeError(n.e2.lineNumber, n.e2.columnNumber, "int", got));

        return "int";
    }

    public String visit(Minus n) {
        String got;
        if ((got = n.e1.accept(this)) != "int")
            error(new TypeError(n.e1.lineNumber, n.e1.columnNumber, "int", got));
        if ((got = n.e2.accept(this)) != "int")
            error(new TypeError(n.e2.lineNumber, n.e2.columnNumber, "int", got));

        return "int";
    }

    public String visit(Times n) {
        String got;
        if ((got = n.e1.accept(this)) != "int")
            error(new TypeError(n.e1.lineNumber, n.e1.columnNumber, "int", got));
        if ((got = n.e2.accept(this)) != "int")
            error(new TypeError(n.e2.lineNumber, n.e2.columnNumber, "int", got));

        return "int";
    }

    public String visit(ArrayLookup n) {
        String got = n.expressionForArray.accept(this);
        if (got == null)
            return null;
        if (got != "int[]")
            error(new TypeError(n.expressionForArray.lineNumber, n.expressionForArray.columnNumber, "int[]", got));

        got = n.indexInArray.accept(this);
        if (got == null)
            return null;
        if (got != "int")
            error(new TypeError(n.indexInArray.lineNumber, n.indexInArray.columnNumber, "int", got));

        return "int";
    }

    public String visit(ArrayLength n) {
        String exp = "int[]";
        String got = n.expressionForArray.accept(this);

        if (!exp.equals(got))
            error(new TypeError(n.expressionForArray.lineNumber, n.expressionForArray.columnNumber, exp, got));

        return "int";
    }

    public String visit(Call n) {
        var nameOfMethodClass = n.e.accept(this);
        if (nameOfMethodClass == null)
            return null;

        var classOfMethod = curProgram.getClass(nameOfMethodClass);
        if (classOfMethod == null) {
            error(new TypeError(n.e.lineNumber, n.e.columnNumber, "Object", nameOfMethodClass));
            return null;
        }

        var method = curProgram.getClassMethod(classOfMethod.name(), n.i.s).first();
        if (method == null) {
            error(new SymbolNotFoundError(n.i.lineNumber, n.i.columnNumber, n.i.s));
            return null;
        }


        if (n.el.size() != method.params().size())
            error(new InvalidArgumentsError(
                n.lineNumber,
                n.columnNumber,
                method.params().size(),
                n.el.size(),
                nameOfMethodClass,
                n.i.s
            ));

            else
                for (int i = 0; i < method.params().size(); i++) {
                    String exp = method.params().get(i);
                    String got = n.el.get(i).accept(this);

                    if (got == null)
                        continue;
                    if (!curProgram.instanceOf(got, exp))
                        error(new TypeError(n.el.get(i).lineNumber,
                                            n.el.get(i).columnNumber,
                                            exp,
                                            got));
                }

        return method.type();
    }

    public String visit(IntegerLiteral n) {
        return "int";
    }

    public String visit(True n) {
        return "boolean";
    }

    public String visit(False n) {
        return "boolean";
    }

    public String visit(IdentifierExp n) {
        var formal = curMethod.getFormalVariable(n.s);
        if (formal != null) {
            return formal;
        }

        var local = curMethod.getLocalVariable(n.s);
        if (local != null) {
            return local;
        }

        var type = curClass.getImmediateClassVariable(n.s);
        if (type != null) {
            return type;
        }

        error(new SymbolNotFoundError(n.lineNumber, n.columnNumber, n.s));
        return null;
    }

    public String visit(This n) {
        return curClass.name();
    }

    public String visit(NewArray n) {
        String exp = "int";
        String got = n.e.accept(this);

        if (!exp.equals(got))
            error(new TypeError(n.e.lineNumber, n.e.columnNumber, exp, got));

        return "int[]";
    }

    public String visit(NewObject n) {
        if (curProgram.getClass(n.i.s) != null) {
            return n.i.s;
        }
        else {
            error(new SymbolNotFoundError(n.i.lineNumber, n.i.columnNumber, n.i.s));
            return null;
        }
    }

    public String visit(Not n) {
        String exp = "boolean";
        String got = n.e.accept(this);

        if (!exp.equals(got))
            error(new TypeError(n.e.lineNumber, n.e.columnNumber, exp, got));

        return "boolean";
    }

    public String visit(IdentifierType n) {
        return n.nameOfType;
    }

    public String visit(Block n) {
        for (var s : n.sl)
            s.accept(this);

        return null;
    }

    public String visit(If n) {
        String exp = "boolean";
        String got = n.e.accept(this);
        if (!exp.equals(got))
            error(new TypeError(n.e.lineNumber, n.e.columnNumber, exp, got));

        if (n.s1 != null)
            n.s1.accept(this);

        if (n.s2 != null)
            n.s2.accept(this);

        return null;
    }

    public String visit(While n) {
        String exp = "boolean";
        String got = n.e.accept(this);
        if (!exp.equals(got))
            error(new TypeError(n.e.lineNumber, n.e.columnNumber, exp, got));

        if (n.s != null)
            n.s.accept(this);

        return null;
    }

    public String visit(Assign n) {
        String exp = n.i.accept(this);
        String got = n.e.accept(this);

        if (exp == null || got == null)
            return null;

        if (!curProgram.instanceOf(got, exp))
            error(new TypeError(n.e.lineNumber, n.e.columnNumber, exp, got));

        return null;
    }

    public String visit(ArrayAssign n) {
        String exp = "int[]";
        String got = n.nameOfArray.accept(this);

        if (got == null)
            return null;
        if (!exp.equals(got))
            error(new TypeError(n.nameOfArray.lineNumber, n.nameOfArray.columnNumber, exp, got));

        exp = "int";
        got = n.indexInArray.accept(this);

        if (got == null)
            return null;
        if (!exp.equals(got)) {
            error(new TypeError(n.indexInArray.lineNumber, n.indexInArray.columnNumber, exp, got));

        exp = "int";
        got = n.e.accept(this);

        if (got == null)
            return null;
        if (!exp.equals(got))
            error(new TypeError(n.e.lineNumber, n.e.columnNumber, exp, got));
        }

        return null;
    }

    public String visit(Identifier n) {
        return visit(new IdentifierExp(n.lineNumber, n.columnNumber, n.s));
    }

    public String visit(Print n) {
        n.e.accept(this);
        return null;
    }

    // Error catching functions
    private void error(TypeError e) {
        if (onTypeError == null || !onTypeError.apply(e))
            throw e;
    }

    private void error(SymbolNotFoundError e) {
        if (onSymbolNotFoundError == null || !onSymbolNotFoundError.apply(e))
            throw e;
    }

    private void error(InvalidArgumentsError e) {
        if (onInvalidArgumentsError == null || !onInvalidArgumentsError.apply(e))
            throw e;
    }

    @Override
    public String visit(Program n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(LocalDecl n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(FieldDecl n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(FormalDecl n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(IntArrayType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(BooleanType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(IntegerType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public String visit(VoidType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}
