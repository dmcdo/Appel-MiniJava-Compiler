package regalloc;

import tree.NameOfTemp;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

public class InterferenceGraph {
    public Map<NameOfTemp, Set<NameOfTemp>> graph;

    public InterferenceGraph(Liveness liveness) {
        graph = new HashMap<>(liveness.nodeCount());

        for (int i = 0; i < liveness.nodeCount(); i++) {
            for (NameOfTemp t : liveness.liveTempsAt(i)) {
                if (!graph.containsKey(t))
                    graph.put(t, new HashSet<>());

                graph.get(t).addAll(liveness.liveTempsAt(i));
            }
        }
    }

    public Map<NameOfTemp, NameOfTemp> color(NameOfTemp[] reserved, NameOfTemp[] usable) {
        Map<NameOfTemp, NameOfTemp> colorMap = new HashMap<>(nodeCount());
        Set<NameOfTemp> uncolored = new HashSet<>(nodeCount());

        // Precolor predefined machine registers
        for (var reg : reserved) {
            colorMap.put(reg, reg);
        }
        for (var use : usable) {
            colorMap.put(use, use);
        }

        for (var temp : graph.keySet()) {
            if (!temp.toString().startsWith("%")) {
                uncolored.add(temp);
            }
        }

        if (!color_backtracking(colorMap, usable, uncolored))
            throw new RuntimeException("Register spilling required but not implemented.");

        return colorMap;
    }

    private boolean color_backtracking(Map<NameOfTemp, NameOfTemp> colorMap, NameOfTemp[] colors, Set<NameOfTemp> uncolored) {
        if (uncolored.size() == 0) 
            return true;

        for (NameOfTemp temp : Set.copyOf(uncolored)) {
            Set<NameOfTemp> possible = new HashSet<>(colors.length);
            for (var color : colors)
                possible.add(color);
            if (graph.containsKey(temp))
                for (var adj : graph.get(temp))
                    if (tempsInterfere(temp, adj))
                        possible.remove(colorMap.get(adj));

            for (var p : possible) {
                colorMap.put(temp, p);
                uncolored.remove(temp);

                if (color_backtracking(colorMap, colors, uncolored))
                    return true;
                else {
                    uncolored.add(temp);
                    colorMap.remove(temp);
                }
            }
        }

        return false;
    }


    public boolean tempsInterfere(NameOfTemp a, NameOfTemp b) {
        return graph.get(a).contains(b);
    }

    public int nodeCount() {
        return graph.size();
    }
}
