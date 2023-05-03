package moretree;

import tree.Exp;
import tree.ExpList;
import java.util.List;

public class STACK extends Exp {
    public final int offset;
    public STACK(int offset) {
        this.offset = offset;
    }

    public ExpList kids() {return null;}
    public Exp build(ExpList kids) {return this;}
    public Exp  create (final List<Exp> kids) {return this;}
    public List<Exp> subcomponents () {return null;}
}
