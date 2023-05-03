package moreassem;

import assem.OperationInstruction;
import tree.NameOfTemp;
import java.util.List;
import java.util.Map;

public class EmptyInstruction extends OperationInstruction {
    public EmptyInstruction(List<NameOfTemp> d, List<NameOfTemp> s) {
        super("", d, s);
    }

    public <T> String format(final Map<NameOfTemp, T> map) {
        return "";
    }
}
