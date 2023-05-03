package moreassem;

import java.util.Arrays;
import tree.NameOfLabel;

public class UnconditionalBranchInstruction extends assem.OperationInstruction {
    public UnconditionalBranchInstruction(String a, tree.NameOfLabel label) {
        super(a, Arrays.asList(), Arrays.asList(), Arrays.asList(label));
    }

    public NameOfLabel branchesTo() {
        return jumps().get(0);
    }
}
