package runtime;

import tree.*;

public class ACCESS_OBJECT extends RuntimeCall {
    public final Exp ptr;
    public final Exp word;

    public ACCESS_OBJECT(Exp ptr, Exp word) {
        super(new NAME(new NameOfLabel("__access_object__")), ptr, word);
        this.ptr = ptr;
        this.word = word;
    }
}
