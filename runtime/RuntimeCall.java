package runtime;

import tree.*;
import java.util.Arrays;

public class RuntimeCall extends CALL {
    public RuntimeCall(Exp f, Exp... x) {
        super(f, Arrays.asList(x));
    }

    public RuntimeCall(NameOfLabel f, Exp a) {
        this(new NAME(f), a);
    }
}
