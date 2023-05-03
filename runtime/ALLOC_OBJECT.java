package runtime;

import tree.*;

public class ALLOC_OBJECT extends RuntimeCall {
    public ALLOC_OBJECT(Exp words) {
        super(new NameOfLabel("__alloc_object__"), words);
    }
}
