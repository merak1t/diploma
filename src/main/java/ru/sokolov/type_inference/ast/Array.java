package ru.sokolov.type_inference.ast;


import ru.sokolov.type_inference.type.Tuple;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Array extends Node {

    private final List<Node> args;

    public Array(List<Node> args) {
        this.args = args;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("[");
        for (var i = 0; i < args.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(args.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        //System.out.println(args);
        return new Tuple(args
                .stream()
                .map(node -> node.getType(env, nonGenerics))
                .collect(Collectors.toList())
        );
    }
}