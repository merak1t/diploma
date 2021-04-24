package ru.sokolov.type_inference.ast;


import ru.sokolov.type_inference.type.Arrow;
import ru.sokolov.type_inference.type.Tuple;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.*;

/**
 * A function abstraction
 */
public class Function extends Node {

    private final List<String> listVariables;
    private final Node body;

    public Function(List<String> vars, Node body) {
        this.listVariables = vars;
        this.body = body;
    }

    @Override
    public String toString() {
        return "fn[" + listVariables + "] => " + body;
    }

    @Override
    public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        var listParams = new ArrayList<TypeVariable>();
        for (var curVar : listVariables){
            TypeVariable argType = new TypeVariable();
            listParams.add(argType);
            env.put(curVar, argType);
            nonGenerics.add(argType);
        }
        Type resultType = body.getType(env, nonGenerics);
        TypeVariable[] tupleArray = new TypeVariable[listParams.size()];
        listParams.toArray(tupleArray);
        return new Arrow(new Tuple(tupleArray), resultType);
    }
}
