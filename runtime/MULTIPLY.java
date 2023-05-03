package runtime;

import tree.*;

public class MULTIPLY extends RuntimeCall {
    public MULTIPLY(Exp x, Exp y) {
        super(new NAME(new NameOfLabel("__multiply__")), x, y);
    }
}
