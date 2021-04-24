package ru.sokolov.ssa;

import com.google.caja.parser.js.*;

import java.util.*;

public class ConditionalHelper {
    private final HashMap<String, String> initialIdentifierMapper = new HashMap<>();
    private final HashMap<String, Integer> currentIdentifierVersionMapper = new HashMap<>();
    private final HashMap<String, ArrayList<String>> resultsIdentifier = new HashMap<>();
    private boolean isElseExists = false;

    public void setCurrentOnElse() {
        isElseExists = true;
    }

    public void addToCurrent(IdentifierMemory identifierMemory, String varName) {
        var currentId = identifierMemory.getLatestVersion(varName);
        var newId = identifierMemory.getNewVersion(varName);
        var parsedId = IdentifierMemory.getNameFromIdentifier(newId);
        var idVersion = parsedId.b;
        if (currentIdentifierVersionMapper.containsKey(varName)) {
            currentIdentifierVersionMapper.replace(varName, idVersion);
        } else {
            if (currentId != null && !initialIdentifierMapper.containsKey(varName)) {
                initialIdentifierMapper.put(varName, currentId);
            }
            currentIdentifierVersionMapper.put(varName, idVersion);
        }
    }


    public void addCurrentToResult() {
        for (var cur : currentIdentifierVersionMapper.entrySet()) {
            var key = cur.getKey();
            var value = key + "$" + cur.getValue();
            if (resultsIdentifier.containsKey(key)) {
                resultsIdentifier.compute(key, (a, b) -> {
                    b.add(value);
                    return b;
                });
            } else {
                resultsIdentifier.put(key, new ArrayList<>(Collections.singletonList(value)));
            }
        }
        currentIdentifierVersionMapper.clear();
    }


    private FormalParam createParam(String val) {
        return new FormalParam(new Identifier(null, val));
    }

    public List<Declaration> createPhiFunc(IdentifierMemory identifierMemory) {
        // ToDo create tuple for phi function
        var result = new ArrayList<Declaration>();
        for (var curRes : resultsIdentifier.entrySet()) {
            var body = new Block();
            var list = new ArrayList<FormalParam>();
            var newIdentifier = identifierMemory.getNewVersion(curRes.getKey());
            for (var curIdentifier : curRes.getValue()) {
                list.add(createParam(curIdentifier));
            }
            if (!isElseExists) {
                if (initialIdentifierMapper.containsKey(curRes.getKey())) {
                    list.add(createParam(initialIdentifierMapper.get(curRes.getKey())));
                }
            }
            var constructor = new FunctionConstructor(null, new Identifier(null, "PHI"), list, body);
            result.add(new Declaration(null, new Identifier(null, newIdentifier), constructor));
        }
        return result;
    }
}
