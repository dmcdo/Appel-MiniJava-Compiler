package runtime;

import tree.*;

public class ALLOC_ARRAY extends RuntimeCall {
    public ALLOC_ARRAY(Exp words) {
        super(new NameOfLabel("__alloc_array__"), words);
    }
}
