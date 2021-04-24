package ru.sokolov.type_inference.ast;

import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A lambda abstraction
 */
public class LetRec extends Node {

    private final String var;
    private final Node defn;
    private final Node body;

    public LetRec(String var, Node defn, Node body) {
        this.var = var;
        this.defn = defn;
        this.body = body;
    }

    @Override
    public String toString() {
        return "(let rec " + var + " = " + defn + " in " + body + ")";
    }

    @Override
    public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        TypeVariable newType = new TypeVariable();
        env.put(var, newType);
        HashSet<TypeVariable> newNonGenerics = new HashSet<>(nonGenerics);
        newNonGenerics.add(newType);
        Type defnType = defn.getType(env, newNonGenerics);
        Inference.unify(newType, defnType);
        Type resultType = body.getType(env, nonGenerics);
        return resultType;
    }
}
