package ru.sokolov.type_inference.ast;

import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.Map;
import java.util.Set;

/**
 * An integer literal
 */
public class Literal extends Node {

    private final LiteralType type;

    public Literal(int i) {
        this.type = LiteralType.INTEGER;
    }

    public Literal(boolean b) {
        this.type = LiteralType.BOOLEAN;
    }

    public Literal(String s) {
        this.type = LiteralType.STRING;
    }

    @Override
    public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        return Inference.typeMapper.getOrDefault(type, Inference.AnyType);
    }

    @Override
    public String toString() {
        return Inference.typeMapper.getOrDefault(type, Inference.AnyType).toString();
    }
}
