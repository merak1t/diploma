package ru.sokolov.type_inference.ast;


import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.Map;
import java.util.Set;

public class If extends Node {

    private final Node cond;
    private final Node thenPart;
    private final Node elsePart;

    public If(Node cond, Node thenPart, Node elsePart) {
        this.cond = cond;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    @Override
    public String toString() {
        return "if (" + cond + ") then " + thenPart + " else " + elsePart + " fi";
    }

    @Override
    public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        Type condType = cond.getType(env, nonGenerics);
        Inference.unify(condType, Inference.BooleanType);
        Type t1 = thenPart.getType(env, nonGenerics);
        Type t2 = elsePart.getType(env, nonGenerics);
        Inference.unify(t1, t2);
        return t1;
    }
}
