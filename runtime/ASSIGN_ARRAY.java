package runtime;

import tree.*;

public class ASSIGN_ARRAY extends RuntimeCall {
    public ASSIGN_ARRAY(Exp ptr, Exp word, Exp val) {
        super(new NAME(new NameOfLabel("__assign_array__")), ptr, word, val);
    }
}
