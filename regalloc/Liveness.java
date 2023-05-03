package regalloc;

import tree.NameOfTemp;

import java.util.List;
import java.util.Set;
import controlflow.ControlFlowGraph;
import java.util.ArrayList;
import java.util.HashSet;

public class Liveness {
    List<Set<NameOfTemp>> liveTempsAt;

    public Liveness(ControlFlowGraph cfg) {
        this.liveTempsAt = new ArrayList<>(cfg.instructionCount());
        for (int i = 0; i < cfg.instructionCount(); i++)
            liveTempsAt.add(new HashSet<>());

        repeat:
        do {
            List<Set<NameOfTemp>> _liveTempsAt = new ArrayList<>(cfg.instructionCount());
            for (int i = 0; i < cfg.instructionCount(); i++) {
                _liveTempsAt.add(i, new HashSet<>());
                _liveTempsAt.get(i).addAll(liveTempsAt.get(i));
            }

            for (int i = cfg.instructionCount() - 1; i >= 0; i--) {
                if (cfg.instructionAt(i).def() != null)
                    for (var t : cfg.instructionAt(i).def())
                        _liveTempsAt.get(i).remove(t);
                if (cfg.instructionAt(i).use() != null)
                    for (var t : cfg.instructionAt(i).use())
                        _liveTempsAt.get(i).add(t);

                for (int p : cfg.getPredecessors(i))
                    _liveTempsAt.get(p).addAll(_liveTempsAt.get(i));
            }

            for (int i = 0; i < cfg.instructionCount(); i++) {
                if (!liveTempsAt.get(i).equals(_liveTempsAt.get(i))) {
                    liveTempsAt = _liveTempsAt;
                    continue repeat;
                }
            }

            break;
        } while (true);
    }

    public int nodeCount() {
        return liveTempsAt.size();
    }

    public Set<NameOfTemp> liveTempsAt(int i) {
        return liveTempsAt.get(i);
    }
}
