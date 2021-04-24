package ru.sokolov.type_inference.ast;

import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.Map;
import java.util.Set;

/**
 * An integer literal
 */
public class TypeLiteral extends Node {

    private final LiteralType type;
    private final String value;

    public TypeLiteral(int i) {
        this.value = String.valueOf(i);
        this.type = LiteralType.INTEGER;
    }

    public TypeLiteral(boolean b) {
        this.value = String.valueOf(b);
        this.type = LiteralType.BOOLEAN;
    }

    public TypeLiteral(String s) {
        this.value = String.valueOf(s);
        this.type = LiteralType.STRING;
    }

    public TypeLiteral() {
        this.value = "null";
        this.type = LiteralType.ANY;
    }

    @Override
    public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        return Inference.typeMapper.getOrDefault(type, Inference.AnyType);
    }

    @Override
    public String toString() {
        return value;
    }
}
