package munch;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import assem.*;
import moreassem.*;
import tree.*;
import moretree.*;

public class MunchSparc {
    public List<Instruction> program = new ArrayList<>();
    private List<NameOfTemp> empty = Arrays.asList();

    public void generateStart(NameOfLabel main) {
        program.add(new OperationInstruction(".global start"));
        program.add(new OperationInstruction("start:"));
        program.add(new OperationInstruction("CALL " + main.toString()));
        program.add(new OperationInstruction("NOP"));
        program.add(new OperationInstruction("CLR `s0", empty, Arrays.asList(new NameOfTemp("%o0"))));
        program.add(new OperationInstruction("CALL exit"));
        program.add(new OperationInstruction("NOP"));
    }

    public void reset() {
        program = new ArrayList<>();
    }

    public void munchStm(Stm s) {
        if (s instanceof STORE)
            munchStm((STORE)s);
        else if (s instanceof MOVE)
            munchStm((MOVE)s);
        else if (s instanceof EVAL)
            munchStm((EVAL)s);
        else if (s instanceof JUMP)
            munchStm((JUMP)s);
        else if (s instanceof CJUMP)
            munchStm((CJUMP)s);
        else if (s instanceof SEQ)
            munchStm((SEQ)s);
        else if (s instanceof LABEL)
            munchStm((LABEL)s);
        else if (s instanceof moretree.SET_STACK_POINTER)
            munchStm((moretree.SET_STACK_POINTER)s);
        else if (s instanceof moretree.RETURN)
            munchStm((moretree.RETURN)s);
        else
            throw new RuntimeException("Unknown Stm type " + s.getClass().getCanonicalName());
    }

    public void munchStm(moretree.SET_STACK_POINTER s) {
        program.add(new OperationInstruction(".SET LOCLS," + s.LOCLS));
        program.add(new OperationInstruction(".SET TEMPS," + s.TEMPS));
        program.add(new OperationInstruction(".SET ARGSB," + s.ARGSB));
        program.add(new OperationInstruction(
            "SAVE `s0, -4*(LOCLS+TEMPS+ARGSB+1+16)&-8, `d0",
            Arrays.asList(new NameOfTemp("%sp")),
            Arrays.asList(new NameOfTemp("%sp"))
        ));
    }

    public void munchStm(moretree.RETURN s) {
        program.add(new OperationInstruction("RET"));
        program.add(new OperationInstruction("RESTORE"));
    }

    public void munchStm(MOVE s) {
        NameOfTemp src = munchExp(s.src);
        NameOfTemp dst = munchExp(s.dst);
        program.add(new MoveInstruction("MOV `s0, `d0", dst, src));
    }

    public void munchStm(STORE s) {
        NameOfTemp src = munchExp(s.src);
        NameOfTemp dst = munchExp(s.dst);

        String a;
        if (s.offset == 0)
            a = "ST `s0, [`s1]";
        else
            a = String.format("ST  `s0, [`s1-%d]", s.offset);

        program.add(new OperationInstruction(a, empty, Arrays.asList(src, dst)));
    }

    public void munchStm(EVAL s) {
        munchExp(s.exp);
    }

    public void munchStm(JUMP s) {
        if (s.exp instanceof NAME) {
            if (!nowhere((NAME)s.exp)) {
                program.add(new SparcUnconditionalBranchInstruction(((NAME)s.exp).label));
                program.add(new OperationInstruction("NOP"));
            }
        }
        else {
            throw new UnsupportedOperationException("munchStm(JUMP s): Expression is non-NAME value.");
        }
    }

    public void munchStm(CJUMP s) {
        String instruction;
        switch (s.relop) {
            case CJUMP.EQ:
                instruction = "BE";
                break;
            case CJUMP.LT:
                instruction = "BL";
                break;
            default:
                throw new UnsupportedOperationException("Unknown relational operator in CJUMP " + s.relop);
        }

        program.add(new OperationInstruction("CMP `s0, `s1", empty, Arrays.asList(munchExp(s.left), munchExp(s.right))));
        program.add(new OperationInstruction(instruction + " `j0", empty, empty, Arrays.asList(s.iftrue)));
        program.add(new OperationInstruction("NOP"));
        program.add(new SparcUnconditionalBranchInstruction(s.iffalse));
        program.add(new OperationInstruction("NOP"));
    }

    public void munchStm(SEQ s) {
        munchStm(s.left);
        munchStm(s.right);
    }

    public void munchStm(LABEL s) {
        program.add(new LabelInstruction(s.label));
    }


    public NameOfTemp munchExp(Exp e) {
        if (e instanceof CONST)
            return munchExp((CONST)e);
        if (e instanceof TEMP)
            return munchExp((TEMP)e);
        if (e instanceof BINOP)
            return munchExp((BINOP)e);
        if (e instanceof MEM)
            return munchExp((MEM)e);
        if (e instanceof CALL)
            return munchExp((CALL)e);
        if (e instanceof ESEQ)
            return munchExp((ESEQ)e);
        if (e instanceof STACK)
            return munchExp((STACK)e);
        throw new RuntimeException("Unknown Exp type " + e.getClass().getCanonicalName());
    }

    public NameOfTemp munchExp(CONST e) {
        var dest = NameOfTemp.generateTemp();
        var oper = new OperationInstruction("SET " + e.value +  ", `d0", dest);
        program.add(oper);
        return dest;
    }

    public NameOfTemp munchExp(TEMP e) {
        return e.temp;
    }

    public NameOfTemp munchExp(BINOP e) {
        NameOfTemp left = munchExp(e.left);
        NameOfTemp right = munchExp(e.right);
        NameOfTemp dest = NameOfTemp.generateTemp();

        String instr;
        switch (e.binop) {
            case BINOP.AND:
                instr = "AND";
                break;
            case BINOP.MINUS:
                instr = "SUB";
                break;
            case BINOP.PLUS:
                instr = "ADD";
                break;
            case BINOP.XOR:
                instr = "XOR";
                break;
            case BINOP.MUL:
                instr = "MUL";
                break;
            default:
                throw new RuntimeException("Unknown binary operator " + e.binop);
        }

        program.add(new OperationInstruction(instr + " `s0, `s1, `d0", dest, left, right));
        return dest;
    }

    public NameOfTemp munchExp(MEM e) {
        NameOfTemp src = munchExp(e.exp);
        NameOfTemp dst = NameOfTemp.generateTemp();

        program.add(new OperationInstruction("LD  [`s0], `d0", dst, src));
        return dst;
    }

    public NameOfTemp munchExp(STACK e) {
        NameOfTemp src = new NameOfTemp("%fp");
        NameOfTemp dst = NameOfTemp.generateTemp();
        String a = String.format("LD  [`s0-%d], `d0", e.offset);
        
        program.add(new OperationInstruction(a, dst, src));

        return dst;
    }

    public NameOfTemp munchExp(CALL e) {
        String fname = ((NAME)e.func).label.toString();
        NameOfTemp retval = NameOfTemp.generateTemp();

        List<NameOfTemp> args = new ArrayList<>();

        for (ExpList l = e.args; l != null; l = l.tail) {
            args.add(munchExp(l.head));
        }

        int i = 0;
        for (NameOfTemp arg : args) {
            program.add(new MoveInstruction("MOV `s0, `d0", new NameOfTemp("%o" + i++), arg));
        }

        program.add(new OperationInstruction("CALL " + fname, Arrays.asList(new NameOfTemp("%o0")), args));
        program.add(new OperationInstruction("NOP", empty, Arrays.asList(new NameOfTemp("%o0"))));
        program.add(new MoveInstruction("MOV `s0, `d0", retval, new NameOfTemp("%o0")));
        program.add(new EmptyInstruction(empty, Arrays.asList(retval)));
        return retval;
    }

    public NameOfTemp munchExp(ESEQ e) {
        munchStm(e.stm);
        return munchExp(e.exp);
    }

    private boolean nowhere(NAME name) {
        return name.label.toString().startsWith("BB$nowhere");
    }
}
