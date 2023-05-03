package runtime;

import tree.*;

public class PRINT_INT extends EVAL {
    public PRINT_INT(Exp e) {
        super(new RuntimeCall(new NameOfLabel("__print_int__"), e));
    }
}
