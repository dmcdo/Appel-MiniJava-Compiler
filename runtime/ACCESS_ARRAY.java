package runtime;

import tree.*;

public class ACCESS_ARRAY extends RuntimeCall {
    public ACCESS_ARRAY(Exp ptr, Exp word) {
        super(new NAME(new NameOfLabel("__access_array__")), ptr, word);
    }
}
