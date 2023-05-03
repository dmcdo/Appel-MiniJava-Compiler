package runtime;

import tree.*;

public class ASSIGN_OBJECT extends CALL {
    public final Exp word;
    public final Exp val;

    public ASSIGN_OBJECT(Exp ptr, Exp word, Exp val) {
        super(new NAME(new NameOfLabel("__assign_object__")), ptr, word, val);
        this.word = word;
        this.val = val;
    }
}
