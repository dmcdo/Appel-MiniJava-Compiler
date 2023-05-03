package checker;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import checker.error.CyclicInheritenceError;
import checker.error.DuplicateClassVariable;
import checker.error.SemanticsError;
import checker.error.SuperclassNotDefinedError;
import common.Pair;

public final class SymbolTable {
    public static final record STClass(String name, HashMap<String, Pair<String, Integer>> childVars, HashMap<String, STMethod> childMethods) {
        public STMethod getImmediateMethod(String name) {
            return childMethods.get(name);
        }

        public String getImmediateClassVariable(String name) {
            var x = childVars.get(name);
            return x == null ? null : x.first();
        }

        public Integer getImmediateClassVariableOrd(String name) {
            var x = childVars.get(name);
            return x == null ? null : x.second();
        }

        public STMethod putNewMethod(String name, String type, List<String> params) {
            return childMethods.put(name, new STMethod(name, type, params, new HashMap<>(), new HashMap<>()));
        }

        public Pair<String, Integer> putClassVariable(String name, String type) {
            return childVars.put(name, new Pair<>(type, childVars.size()));
        }
    }

    public static final record STMethod(
        String name,
        String type,
        List<String> params,
        HashMap<String, Pair<String, Integer>> localVars,
        HashMap<String, Pair<String, Integer>> formalVars)
    {
        public Pair<String, Integer> putLocalVariable(String name, String type) {
            return localVars.put(name, new Pair<>(type, localVars.size()));
        }

        public Pair<String, Integer> putFormalVariable(String name, String type) {
            return formalVars.put(name, new Pair<>(type, formalVars.size()));
        }

        // Returns -> variable type, variable order
        public String getLocalVariable(String name) {
            var x = localVars.get(name);
            return x == null ? null : x.first();
        }

        public Integer getLocalVariableOrd(String name) {
            var x = localVars.get(name);
            return x == null ? null : x.second();
        }

        public String getFormalVariable(String name) {
            var x = formalVars.get(name);
            return x == null ? null : x.first();
        }

        public Integer getFormalVariableOrd(String name) {
            var x = formalVars.get(name);
            return x == null ? null : x.second();
        }

        public int getLocalVarCount() {
            return localVars.size();
        }
    }

    final HashMap<String, STClass> childClasses;
    final HashMap<String, String> inheritanceTable;

    public SymbolTable() {
        childClasses = new HashMap<>();
        inheritanceTable = new HashMap<>();
    }

    public STClass getClass(String s) {
        return childClasses.get(s);
    }

    public STClass putNewClass(String s) {
        return putNewClass(s, null);
    }

    public STClass putNewClass(String name, String inherits) {
        inheritanceTable.put(name, inherits);
        return childClasses.put(name, new STClass(name, new HashMap<>(), new HashMap<>()));
    }

    // public String getClassVariable(String className, String varName) {
    //     String varType;

    //     for (; className != null; className = inheritanceTable.get(className)) {
    //         if ((varType = getClass(className).getImmediateClassVariable(varName)) != null) {
    //             return varType;
    //         }
    //     }

    //     return null;
    // }

    // Returns -> method object, top level class name
    public Pair<STMethod, String> getClassMethod(String className, String methodName) {
        for (; className != null; className = inheritanceTable.get(className)) {
            STMethod method;

            if ((method = getClass(className).getImmediateMethod(methodName)) != null) {
                return new Pair<>(method, className);
            }
        }

        return null;
    }

    public boolean instanceOf(String subclass, String superclass) {
        for (; subclass != null; subclass = inheritanceTable.get(subclass)) {
            if (subclass.equals(superclass)) {
                return true;
            }
        }

        return false;
    }

    public List<SemanticsError> finish() {
        List<SemanticsError> errorList = new ArrayList<>();

        // Make sure the inheritance tree is complete
        for (var i : childClasses.entrySet()) {
            String parentClassName = inheritanceTable.get(i.getKey());

            if (parentClassName == null) {
                continue;
            }

            if (getClass(parentClassName) == null) {
                errorList.add(new SuperclassNotDefinedError(0, 0, i.getKey(), parentClassName));
            }
        }

        // Make sure there is no cyclic inheritance
        for (var i : childClasses.entrySet()) {
            String parent = inheritanceTable.get(i.getKey());
            if (parent != null && instanceOf(parent, i.getKey())) {
                errorList.add(new CyclicInheritenceError(0, 0, i.getKey()));
            }
        }

        // Consolidate variables in child classes
        HashSet<String> visited = new HashSet<>();
        for (var i : childClasses.entrySet()) {
            _finish_consolitate(i.getKey(), errorList, visited);
        }

        return errorList;
    }

    private HashMap<String, Pair<String, Integer>> _finish_consolitate(String cur, List<SemanticsError> errorList, HashSet<String> visited) {
        STClass curClass = getClass(cur);

        if (curClass == null) {
            return new HashMap<>();
        }

        if (visited.contains(cur)) {
            return curClass.childVars();
        }

        var parent     = inheritanceTable.get(cur);
        var parentVars = _finish_consolitate(parent, errorList, visited);

        for (var v : curClass.childVars().entrySet()) {
            curClass.childVars().put(v.getKey(), new Pair<>(v.getValue().first(), v.getValue().second() + parentVars.size()));
        }

        for (var v : parentVars.entrySet()) {
            if (curClass.childVars().put(v.getKey(), v.getValue()) != null) {
                errorList.add(new DuplicateClassVariable(0, 0, v.getKey(), cur, parent));
            }

            visited.add(cur);
        }

        return curClass.childVars();
    }
}
