package translate;

import checker.SymbolTable;
import common.Pair;
import syntax.*;
import tree.*;
import moretree.*;
import java.util.*;

public class IRVisitor implements syntax.SyntaxTreeVisitor<Void> {
    public static final tree.TEMP FRAME_PTR = new tree.TEMP("%fp");
    public static final tree.TEMP THIS_PTR  = new tree.TEMP("%i0");
    public static final tree.TEMP RETURN    = new tree.TEMP("%i0");

    public List<tree.Stm> methodFragments;
    public tree.Stm mainFragment;

    private SymbolTable curProgram = null;
    private SymbolTable.STClass curClass = null;
    private SymbolTable.STMethod curMethod = null;

    private tree.Exp lastExp;
    private tree.Stm lastStm;

    private int lcount = 1;

    private NameOfLabel getNextLabel(final String... a) {
        String[] b = new String[a.length + 1];
        System.arraycopy(a, 0, b, 0, a.length);
        b[a.length] = String.format("%03d", lcount++);

        return new NameOfLabel(b);
    }

    public Void visit(syntax.Program n, SymbolTable st) {
        curProgram = st;
        methodFragments = new ArrayList<>();

        n.m.accept(this);
        for (var c : n.cl) {
            c.accept(this);
        }

        return null;
    }

    public Void visit(syntax.ClassDecl n) {
        curClass = curProgram.getClass(n.i.s);

        for (var m : n.methods) {
            m.accept(this);
            methodFragments.add(lastStm);
        }

        return null;
    }

    @Override
    public Void visit(syntax.MainClass n) {
        n.body.accept(this);

        mainFragment = new tree.SEQ(
            new tree.LABEL(n.nameOfMainClass.s, "main"),
            new tree.SEQ(
                new SET_STACK_POINTER(0, 9, 0),
                new tree.SEQ(
                    lastStm,
                    new moretree.RETURN()
                )
            )
        );
        return null;
    }

    @Override
    public Void visit(syntax.SimpleClassDecl n) {
        return visit((ClassDecl)n);
    }

    @Override
    public Void visit(syntax.ExtendingClassDecl n) {
        return visit((ClassDecl)n);
    }

    @Override
    public Void visit(syntax.MethodDecl n) {
        var x = curProgram.getClassMethod(curClass.name(), n.i.s); curMethod = x.first();

        tree.Stm body;
        if (n.sl.size() == 0) {
            body = tree.Stm.NOOP;
        }
        else if (n.sl.size() == 1) {
            n.sl.get(0).accept(this);
            body = lastStm;
        }
        else {
            tree.Stm l, r;
            n.sl.get(0).accept(this); l = lastStm;
            n.sl.get(1).accept(this); r = lastStm;
            body = new tree.SEQ(l, r);

            for (int i = 2; i < n.sl.size(); i++) {
                n.sl.get(i).accept(this);
                body = new tree.SEQ(body, lastStm);
            }
        }

        n.e.accept(this);
        lastStm = new tree.SEQ(
            new tree.LABEL(new NameOfLabel(x.second(), curMethod.name())),
            new tree.SEQ(
                new tree.SEQ(
                    new SET_STACK_POINTER(curMethod.localVars().size(), 9, 0),
                    new tree.SEQ(
                        body,
                        new tree.MOVE(RETURN, lastExp)
                    )
                ),
                new moretree.RETURN()
            )
        );
        return null;
    }

    @Override
    public Void visit(syntax.LocalDecl n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(syntax.FieldDecl n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(syntax.FormalDecl n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(syntax.IdentifierType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(syntax.IntArrayType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(syntax.BooleanType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(syntax.IntegerType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(syntax.VoidType n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(syntax.Block n) {
        if (n.sl.size() == 0) {
            lastStm = tree.Stm.NOOP;
            return null;
        }

        if (n.sl.size() == 1) {
            n.sl.get(0).accept(this);
            return null;
        }

        tree.Stm l, r;
        n.sl.get(0).accept(this); l = lastStm;
        n.sl.get(1).accept(this); r = lastStm;

        tree.SEQ seq = new tree.SEQ(l, r);

        for (int i = 2; i < n.sl.size(); i++) {
            n.sl.get(i).accept(this);
            seq = new tree.SEQ(seq, lastStm);
        }

        lastStm = seq;
        return null;
    }

    @Override
    public Void visit(syntax.If n) {
        tree.Exp l, r;
        tree.Stm ifthen, ifelse, cjump;
        NameOfLabel nameof_then = getNextLabel("if", "then");
        NameOfLabel nameof_else = getNextLabel("if", "else");
        NameOfLabel nameof_end  = getNextLabel("if", "end");

        if (n.e instanceof syntax.LessThan) {
            ((syntax.LessThan)n.e).e1.accept(this); l = lastExp;
            ((syntax.LessThan)n.e).e2.accept(this); r = lastExp;

            cjump = new tree.CJUMP(
                tree.CJUMP.LT,
                l,
                r,
                nameof_then,
                nameof_else
            );
        }
        else {
            n.e.accept(this);
            l = lastExp;
            r = tree.CONST.TRUE;

            cjump = new tree.CJUMP(
                tree.CJUMP.EQ,
                l,
                r,
                nameof_then,
                nameof_else
            );
        }

        n.s1.accept(this); ifthen = lastStm;
        n.s2.accept(this); ifelse = lastStm;

        ifthen = new tree.SEQ(ifthen, new tree.JUMP(nameof_end));

        tree.SEQ seq;
        seq = new SEQ(ifthen, new tree.JUMP(nameof_end));
        seq = new SEQ(seq, ifelse);

        lastStm = new tree.SEQ(
            cjump,
            new tree.SEQ(
                new tree.LABEL(nameof_then),
                new tree.SEQ(
                    ifthen,
                    new tree.SEQ(
                        new tree.LABEL(nameof_else),
                        new tree.SEQ(
                            ifelse,
                            new tree.LABEL(nameof_end)
                        )
                    )
                )
            )
        );

        return null;
    }

    @Override
    public Void visit(syntax.While n) {
        tree.Exp cond;
        tree.Stm stm;

        n.e.accept(this); cond = lastExp;
        n.s.accept(this); stm  = lastStm;

        NameOfLabel nameof_cnd = getNextLabel("while", "test");
        NameOfLabel nameof_stm = getNextLabel("while", "statement");
        NameOfLabel nameof_end = getNextLabel("while", "end");

        lastStm = new tree.SEQ(
            new tree.LABEL(nameof_cnd),
            new tree.SEQ(
                new tree.CJUMP(tree.CJUMP.EQ, cond, tree.CONST.TRUE, nameof_stm, nameof_end),
                new tree.SEQ(
                    new tree.LABEL(nameof_stm),
                        new tree.SEQ(
                        stm,
                        new tree.SEQ(
                            new tree.JUMP(nameof_cnd),
                            new tree.LABEL(nameof_end)
                        )
                    )
                )
            )
        );

        return null;
    }

    @Override
    public Void visit(syntax.Print n) {
        n.e.accept(this);
        lastStm = new runtime.PRINT_INT(lastExp);
        return null;
    }

    @Override
    public Void visit(syntax.Assign n) {
        tree.Exp id, ex;

        n.i.accept(this); id = lastExp;
        n.e.accept(this); ex = lastExp;

        if (id instanceof runtime.ACCESS_OBJECT) {
            lastStm = new tree.EVAL(new runtime.ASSIGN_OBJECT(
                ((runtime.ACCESS_OBJECT)id).ptr,
                ((runtime.ACCESS_OBJECT)id).word,
                ex
            ));
        }
        else if (id instanceof STACK) {
            lastStm = new STORE(FRAME_PTR, ((STACK)id).offset, ex);
        }
        else {
            lastStm = new tree.MOVE(id, ex);
        }

        return null;
    }

    @Override
    public Void visit(syntax.Identifier n) {
        var formal = curMethod.getFormalVariableOrd(n.s);
        if (formal != null) {
            lastExp = new TEMP("%i" + (formal + 1));
            return null;
        }

        var local = curMethod.getLocalVariableOrd(n.s);
        if (local != null) {
            lastExp = new STACK(4 * (local + 1));
            // lastExp = new tree.BINOP(tree.BINOP.MINUS, FRAME_PTR, new tree.CONST(4 * (local + 1)));
            return null;
        }

        var ord = curClass.getImmediateClassVariableOrd(n.s);
        if (ord != null) {
            lastExp = new runtime.ACCESS_OBJECT(THIS_PTR, new tree.CONST(ord));
            return null;
        }

        throw new UnsupportedOperationException("Compiler broke because of " + n.s);
    }

    @Override
    public Void visit(syntax.ArrayAssign n) {
        tree.Exp a, i, v;

        n.nameOfArray.accept(this);  a = lastExp;
        n.indexInArray.accept(this); i = lastExp;
        n.e.accept(this);            v = lastExp;

        lastStm = new tree.EVAL(new runtime.ASSIGN_ARRAY(a, i, v));
        return null;
    }

    @Override
    public Void visit(syntax.And n) {
        tree.Exp l, r;

        n.e1.accept(this); l = lastExp;
        n.e2.accept(this); r = lastExp;

        lastExp = new tree.BINOP(tree.BINOP.AND, l, r);

        return null;
    }

    @Override
    public Void visit(syntax.LessThan n) {
        tree.Exp l, r;

        n.e1.accept(this); l = lastExp;
        n.e2.accept(this); r = lastExp;

        lastExp = new runtime.LESS_THAN(l, r);

        return null;
    }

    @Override
    public Void visit(syntax.Plus n) {
        tree.Exp l, r;

        n.e1.accept(this); l = lastExp;
        n.e2.accept(this); r = lastExp;

        lastExp = new tree.BINOP(tree.BINOP.PLUS, l, r);

        return null;
    }

    @Override
    public Void visit(syntax.Minus n) {
        tree.Exp l, r;

        n.e1.accept(this); l = lastExp;
        n.e2.accept(this); r = lastExp;

        lastExp = new tree.BINOP(tree.BINOP.MINUS, l, r);

        return null;
    }

    @Override
    public Void visit(syntax.Times n) {
        tree.Exp l, r;

        n.e1.accept(this); l = lastExp;
        n.e2.accept(this); r = lastExp;

        lastExp = new runtime.MULTIPLY(l, r);

        return null;
    }

    @Override
    public Void visit(syntax.ArrayLookup n) {
        Exp a, i;
        n.expressionForArray.accept(this); a = lastExp;
        n.indexInArray.accept(this); i = lastExp;

        lastExp = new runtime.ACCESS_ARRAY(a, i);

        return null;
    }

    @Override
    public Void visit(syntax.ArrayLength n) {
        n.expressionForArray.accept(this);

        lastExp = new runtime.LENGTH(lastExp);

        return null;
    }

    @Override
    public Void visit(syntax.Call n) {
        String topLevelClassName = _visit_call_getMethod(n).second();

        tree.NameOfLabel methodLabel = new tree.NameOfLabel(topLevelClassName, n.i.s);

        List<tree.Exp> args = new ArrayList<>(n.el.size() + 1);
        n.e.accept(this);
        args.add(lastExp);

        for (syntax.Expression e : n.el) {
            e.accept(this);
            args.add(lastExp);
        }

        lastExp = new tree.CALL(new NAME(methodLabel), args);
        return null;
    }

    private Pair<SymbolTable.STMethod, String> _visit_call_getMethod(syntax.Call n) {
        if (n.e instanceof syntax.This) {
            return curProgram.getClassMethod(curClass.name(), n.i.s);
        }

        if (n.e instanceof syntax.IdentifierExp) {
            String var;

            if ((var = curMethod.getFormalVariable(((syntax.IdentifierExp)n.e).s)) != null)
                return curProgram.getClassMethod(var, n.i.s);
            if ((var = curMethod.getLocalVariable(((syntax.IdentifierExp)n.e).s)) != null)
                return curProgram.getClassMethod(var, n.i.s);
            if ((var = curClass.getImmediateClassVariable(((syntax.IdentifierExp)n.e).s)) != null)
                return curProgram.getClassMethod(var, n.i.s);

            throw new RuntimeException(String.format("Your compiler broke because of the identifier '%s' on line %d", n.i.s, n.lineNumber));
        }

        if (n.e instanceof syntax.NewObject) {
            return curProgram.getClassMethod(((syntax.NewObject)n.e).i.s, n.i.s);
        }

        if (n.e instanceof syntax.Call) {
            return curProgram.getClassMethod(_visit_call_getMethod(((syntax.Call)n.e)).first().type(), n.i.s);
        }

        throw new RuntimeException("Your compiler broke because of the Call expression on line " + n.lineNumber);
    }

    @Override
    public Void visit(syntax.IntegerLiteral n) {
        lastExp = new tree.CONST(n.i);
        return null;
    }

    @Override
    public Void visit(syntax.True n) {
        lastExp = tree.CONST.TRUE;
        return null;
    }

    @Override
    public Void visit(syntax.False n) {
        lastExp = tree.CONST.FALSE;
        return null;
    }

    @Override
    public Void visit(syntax.IdentifierExp n) {
        var formal = curMethod.getFormalVariableOrd(n.s);
        if (formal != null) {
            lastExp = new TEMP("%i" + (formal + 1));
            return null;
        }

        var local = curMethod.getLocalVariableOrd(n.s);
        if (local != null) {
            lastExp = new STACK(4 * (local + 1));
            // lastExp = new tree.MEM(new tree.BINOP(tree.BINOP.MINUS, FRAME_PTR, new tree.CONST(4 * (local + 1))));
            return null;
        }

        var ord = curClass.getImmediateClassVariableOrd(n.s);
        if (ord != null) {
            lastExp = new runtime.ACCESS_OBJECT(THIS_PTR, new tree.CONST(ord));
            return null;
        }

        throw new UnsupportedOperationException("Compiler broke because of " + n.s);
    }

    @Override
    public Void visit(syntax.This n) {
        lastExp = THIS_PTR;
        return null;
    }

    @Override
    public Void visit(syntax.NewArray n) {
        n.e.accept(this);
        lastExp = new runtime.ALLOC_ARRAY(lastExp);
        return null;
    }

    @Override
    public Void visit(syntax.NewObject n) {
        lastExp = new runtime.ALLOC_OBJECT(new tree.CONST(curProgram.getClass(n.i.s).childVars().size()));
        return null;
    }

    @Override
    public Void visit(syntax.Not n) {
        n.e.accept(this);

        lastExp = new tree.BINOP(tree.BINOP.XOR, lastExp, tree.CONST.TRUE);

        return null;
    }

    @Override
    public Void visit(Program n) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
    
}
