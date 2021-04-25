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
    private final List<Node> body;
    private final Node returnStmt;
    private final Node fn;

    public Function(Node fn, List<String> vars, List<Node> body, Node returnStmt) {
        this.fn = fn;
        this.listVariables = vars;
        this.body = body;
        this.returnStmt = returnStmt;
    }

    @Override
    public String toString() {
        return "fn[" + listVariables + "] => " + body;
    }

    @Override
    public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        var listParams = new ArrayList<Type>();
        var newEnv = new HashMap<>(env);
        var newNonGenerics = new HashSet<>(nonGenerics);
        for (var curVar : listVariables) {
            TypeVariable argType = new TypeVariable();
            listParams.add(argType);
            newEnv.put(curVar, argType);
            newNonGenerics.add(argType);
        }
        for (var cur : body) {
            cur.getType(newEnv, newNonGenerics);
        }
        Type resultType = returnStmt.getType(newEnv, newNonGenerics);
        env.put(fn.toString(),  new Arrow(new Tuple(listParams), resultType));
        newEnv.put(fn.toString(),  new Arrow(new Tuple(listParams), resultType));
        return new Arrow(new Tuple(listParams), resultType);
    }
}
