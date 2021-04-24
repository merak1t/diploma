package ru.sokolov.type_inference.ast;

import ru.sokolov.type_inference.type.Arrow;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.Map;
import java.util.Set;

/**
 * A lambda abstraction
 */
public class Lambda extends Node {

	private final String var;
	private final Node body;

	public Lambda(String var, Node body) {
		this.var = var;
		this.body = body;
	}

	@Override
	public String toString() {
		return "fn[" + var + "] => " + body;
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        TypeVariable argType = new TypeVariable();
		env.put(var, argType);
		nonGenerics.add(argType);
        Type resultType = body.getType(env, nonGenerics);
        return new Arrow(argType, resultType);
	}
}
