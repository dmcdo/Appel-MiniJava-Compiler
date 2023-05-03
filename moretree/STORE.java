package moretree;

import tree.*;
import java.util.List;
import java.util.Collections;

public class STORE extends Stm {
    public final Exp dst, src;
    public final int offset;
    public STORE (final Exp d, int offset, final Exp s) {
        this.dst = d;
        this.src = s;
        this.offset = offset;
    }

    public STORE (final Exp d, final Exp s) {
        this(d, 0, s);
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
