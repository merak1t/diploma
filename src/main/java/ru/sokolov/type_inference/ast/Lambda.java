package ru.sokolov.type_inference.ast;

import ru.sokolov.type_inference.type.Arrow;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.HashMap;
import java.util.HashSet;
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
        var newEnv = new HashMap<>(env);
		newEnv.put(var, argType);
        var newNonGenerics = new HashSet<>(nonGenerics);
		newNonGenerics.add(argType);
        Type resultType = body.getType(newEnv, newNonGenerics);
        return new Arrow(argType, resultType);
	}
}
