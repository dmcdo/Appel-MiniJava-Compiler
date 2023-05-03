package runtime;

import tree.NameOfLabel;

import tree.*;

public class LESS_THAN extends RuntimeCall {
    public LESS_THAN(Exp l, Exp r) {
        super(new NAME(new NameOfLabel("__less_than__")), l, r);
    }
}
