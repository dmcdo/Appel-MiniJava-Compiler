package moretree;

import tree.*;
import java.util.List;
import java.util.Collections;

public class SET_STACK_POINTER extends tree.Stm {
    public final int LOCLS;
    public final int TEMPS;
    public final int ARGSB;

    public SET_STACK_POINTER(int LOCLS, int TEMPS, int ARGSB) {
        this.LOCLS = LOCLS;
        this.TEMPS = TEMPS;
        this.ARGSB = ARGSB;
    }

    @Override
    public Stm build(ExpList kids) { return this; }
    @Override
    public ExpList kids() { return null; }
    @Override
    public List<Exp> subcomponents() { return Collections.emptyList();}
    @Override
    public Stm create (List<Exp> kids) {return this;}
}
