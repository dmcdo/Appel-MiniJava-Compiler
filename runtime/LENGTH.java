package runtime;

import tree.*;

public class LENGTH extends RuntimeCall {
    public LENGTH(Exp ptr) {
        super(new NAME(new NameOfLabel("__array_length__")), ptr);
    }
}
