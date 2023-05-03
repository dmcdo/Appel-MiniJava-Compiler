package main;
import parser.*;
import regalloc.Liveness;
import syntax.*;
import translate.*;
import checker.*;
import canon.*;
import tree.*;
import munch.*;
import checker.error.*;
import common.Constants;
import controlflow.*;
import regalloc.*;
import assem.*;
import moreassem.*;

import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


public class Main {
    private static final PrintStream stdout = System.out;
    private static final PrintStream stderr = System.err;
    private static PrintStream irDebug = null;
    private static PrintStream assemOut;
    private static String fname;
    private static int errors = 0;

    private static Program program;
    private static SymbolTable st;

    private static List<tree.Stm> mainFragment;
    private static List<List<tree.Stm>> methodFragments;

    private static List<List<Instruction>> munchedFragments = new ArrayList<>();

    /* Usage: ./compile <MiniJava file> */
    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
        parseArgs(args);

        /* Parse & Bulid AST */
        do {
            parse();
            buildST();
            if (errors != 0)
                break;

            typeCheck();
            if (errors != 0)
                break;

            translateIR();
            munch();
            allocateRegisters();

        } while (false);

        /* Print errors */
        stdout.printf("filename=%s, errors=%d\n", fname, errors);
    }

    private static void allocateRegisters() {
        for (var f : munchedFragments) {
            var cfg  = new ControlFlowGraph(f);
            var live = new Liveness(cfg);
            var ig   = new InterferenceGraph(live);
            var map  = ig.color(Constants.reservedSPARCRegisters, Constants.usableSPARCRegisters);

            for (var instr : f) {
                if (!(instr instanceof EmptyInstruction)) {
                    assemOut.println(instr.format(map));
                }
            }
            assemOut.println();
        }
    }

    private static void munch() {
        MunchSparc munch = new MunchSparc();

        // Create `start` entry point.
        munch.generateStart(((LABEL)mainFragment.get(0)).label);
        munchedFragments.add(munch.program);
        munch.reset();

        // Munch main method
        for (var s : mainFragment)
            munch.munchStm(s);
        munchedFragments.add(munch.program);
        munch.reset();

        // Munch all other methods
        for (var fragment : methodFragments) {
            for (var s : fragment)
                munch.munchStm(s);
        
            munchedFragments.add(munch.program);
            munch.reset();
        }
    }

    private static void translateIR() {
        var ir = new IRVisitor();

        ir.visit(program, st);

        mainFragment = TraceSchedule.trace(BasicBlocks.makeBlocks(Canon.linearize(ir.mainFragment)));
        methodFragments = ir.methodFragments.stream().map(s -> TraceSchedule.trace(BasicBlocks.makeBlocks(Canon.linearize(s)))).toList();

        if (irDebug != null) {
            irDebug.println("!  Main procedure fragment");
            for (Stm s : mainFragment) irDebug.print(s.toString());
            irDebug.println("!  End main");
            irDebug.println();

            int count = 0;
            for (var m : methodFragments) {
                irDebug.println("!  Procedure fragment " + ++count);

                for (var s : m) {
                    irDebug.print(s.toString());
                }

                irDebug.println("!  End " + count);
                irDebug.println();
            }
        }
    }

    private static void typeCheck() {
        /* Type Check */
        var tv = new TypeVisitor();
        tv.onTypeError = (TypeError ex) -> {
            errors++;

            stderr.printf(
                "%s:%03d.%03d: Error: Was expecting a(n) %s expression here, but got a(n) %s expression\n",
                fname,
                ex.l,
                ex.c,
                ex.expected,
                ex.got
            );

            return true;
        };
        tv.onSymbolNotFoundError = (SymbolNotFoundError ex) -> {
            errors++;

            stderr.printf(
                "%s:%03d.%03d: Error: Cannot resolve symbol '%s'\n",
                fname,
                ex.l,
                ex.c,
                ex.symbol
            );

            return true;
        };
        tv.onInvalidArgumentsError = (InvalidArgumentsError ex) -> {
            errors++;

            stderr.printf(
                "%s:%03d.%03d: Error: %s.%s takes exactly %d argument(s), but got %d\n",
                fname,
                ex.l,
                ex.c,
                ex.className,
                ex.method,
                ex.ecount,
                ex.count
            );

            return true;
        };

        tv.visit(program, st);
    }

    private static void buildST() {
        /* Bulld Symbol Table */
        var sv = new SymbolVisitor();

        sv.onDuplicateDeclatationError = (DuplicateDeclarationError ex) -> {
            errors++;

            String explanation;
            switch (ex.scope) {
                case DuplicateDeclarationError.CLASS:
                    explanation = "class declaration";
                    break;
                case DuplicateDeclarationError.METHOD:
                    explanation = "method declaraion";
                    break;
                case DuplicateDeclarationError.CLASSVAR:
                    explanation = "class variable";
                    break;
                case DuplicateDeclarationError.LOCALVAR:
                    explanation = "local variable";
                    break;
                default:
                    explanation = "(YOUR COMPILER IS BROKEN)";
                    break;
            }

            stderr.printf(
                "%s:%03d.%03d: Error: Duplicate %s %s\n",
                fname,
                ex.l,
                ex.c,
                explanation,
                ex.name
            );

            return true;
        };

        sv.visit(program);
        st = sv.getLast();

        var stErrors = st.finish();
        for (var stError : stErrors) {
            errors++;

            if (stError instanceof CyclicInheritenceError) {
                stderr.printf(
                    "%s:%03d.%03d: Error: Cyclic inheritance involving %s\n",
                    fname,
                    stError.l,
                    stError.c,
                    ((CyclicInheritenceError)stError).className
                );
            }
            else if (stError instanceof SuperclassNotDefinedError) {
                stderr.printf(
                    "%s:%03d.%03d: Error: %s is a child class of %s, but %s is never defined\n",
                    fname,
                    stError.l,
                    stError.c,
                    ((SuperclassNotDefinedError)stError).subclass,
                    ((SuperclassNotDefinedError)stError).superclass,
                    ((SuperclassNotDefinedError)stError).superclass
                );
            }
            else if (stError instanceof DuplicateClassVariable) {
                stderr.printf(
                    "%s:%03d.%03d: Error: The class %s defines variable %s, but %s is already defined in superclass %s\n",
                    fname,
                    stError.l,
                    stError.c,
                    ((DuplicateClassVariable)stError).className,
                    ((DuplicateClassVariable)stError).name,
                    ((DuplicateClassVariable)stError).name,
                    ((DuplicateClassVariable)stError).superClassName
                );
            }
        }
    }

    private static void parse() throws ParseException {
        MiniParser.onException = (ParseException ex) -> {
            errors++;

            stderr.printf(
                "%s:%03d.%03d: Syntax Error: Was expecting a %s here, but got %s.\n",
                fname,
                ex.currentToken.next.beginLine,
                ex.currentToken.next.beginColumn,
                tokenSequencesToString(ex.expectedTokenSequences),
                TokenExtras.getPrintableImage(ex.currentToken.next)
            );

            return true;
        };

        /* Get Program */
        program = MiniParser.Program();
    }
    
    private static void parseArgs(String[] args) throws FileNotFoundException, IOException {
        if (args.length < 1)
            throw new FileNotFoundException("No input files");
        // args = new String[] {"./Obj.java"};

        new MiniParser(new FileInputStream(fname = args[0]));
        MiniParser.disable_tracing();

        String basename = args[0].substring(0, args[0].lastIndexOf("."));
        assemOut = new PrintStream(basename + ".s");
    }

    private static String tokenSequencesToString(int[][] sequences) {
        String[] repr = new String[sequences.length];
        for (int i = 0; i < sequences.length; i++)
            repr[i] = TokenExtras.getPrintableImage(sequences[i][0]);
        return String.join(" or ", repr);
    }
}
