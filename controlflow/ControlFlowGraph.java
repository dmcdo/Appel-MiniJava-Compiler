package controlflow;

import assem.*;
import moreassem.UnconditionalBranchInstruction;
import tree.NameOfLabel;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class ControlFlowGraph {
    List<Instruction> code;
    int end;
    Map<Integer, List<Integer>> pred;
    Map<Integer, List<Integer>> succ;
    public Map<NameOfLabel, Integer> labelMap = new HashMap<>();

    public ControlFlowGraph(List<Instruction> code) {
        this.code = code;

        pred = new HashMap<>(code.size());
        succ = new HashMap<>(code.size());

        for (int i = 0; i < code.size(); i++) {
            pred.put(i, new ArrayList<>(1));
            succ.put(i, new ArrayList<>(1));
        }

        // Create label map
        for (int i = 0; i < code.size(); i++) {
            if (code.get(i) instanceof LabelInstruction)
                labelMap.put(((LabelInstruction)code.get(i)).label, i);
        }

        for (int i = 0; i < code.size(); i++) {
            Instruction cur = code.get(i);

            if (cur.jumps() != null) {
                for (NameOfLabel jump : cur.jumps()) {
                    int j = labelMap.get(jump);
                    succ.get(i).add(j);
                    pred.get(j).add(i);
                }
            }

            if (!(cur instanceof UnconditionalBranchInstruction))
                if (i + 1 < code.size()) {
                    succ.get(i).add(i + 1);
                    pred.get(i + 1).add(i);
                }
        }
    }

    public Instruction instructionAt(int i) {
        return code.get(i);
    }

    public int instructionCount() {
        return code.size();
    }

    public int end() {
        return end;
    }

    public List<Integer> getSuccessors(int i) {
        return succ.get(i);
    }

    public List<Integer> getPredecessors(int i) {
        return pred.get(i);
    }

    public int getLabelIndex(NameOfLabel label) {
        return labelMap.get(label);
    }
}
