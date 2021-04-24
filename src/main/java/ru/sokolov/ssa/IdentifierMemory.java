package ru.sokolov.ssa;

import com.google.caja.util.Pair;

import java.util.HashMap;

public class IdentifierMemory {
    HashMap<String, Integer> versionByVar;

    public IdentifierMemory() {
        versionByVar = new HashMap<>();
    }


    public static Pair<String, Integer> getNameFromIdentifier(String identifier) {
        String[] idArr = identifier.split("\\$");
        return new Pair<>(idArr[0], Integer.parseInt(idArr[1]));
    }

    public String getNewVersion(String curVar) {
        if (versionByVar.containsKey(curVar)) {
            versionByVar.compute(curVar, (key, value) -> value + 1);

        } else {
            versionByVar.put(curVar, 1);
        }
        return curVar + "$" + versionByVar.get(curVar).toString();
    }

    public String getLatestVersion(String curVar) {
        if (versionByVar.containsKey(curVar)) {
            return curVar + "$" + versionByVar.get(curVar).toString();

        } else {
            System.err.println("Undefined variable " + curVar);
            return null;
        }
    }
}
