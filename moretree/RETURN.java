package moretree;

import tree.*;
import java.util.List;
import java.util.Collections;

public class RETURN extends Stm {
    @Override
    public Stm build(ExpList kids) { return this; }
    @Override
    public ExpList kids() { return null; }
    @Override
    public List<Exp> subcomponents() { return Collections.emptyList();}
    @Override
    public Stm create (List<Exp> kids) {return this;}
}
