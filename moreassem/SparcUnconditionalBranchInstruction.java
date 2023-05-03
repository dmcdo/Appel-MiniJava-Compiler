package moreassem;

import tree.NameOfLabel;

public class SparcUnconditionalBranchInstruction extends UnconditionalBranchInstruction {
    public SparcUnconditionalBranchInstruction(NameOfLabel jump) {
        super("BA `j0", jump);
    }
}
