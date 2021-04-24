package ru.sokolov.type_inference.ast;

import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.Map;
import java.util.Set;

/**
 * A lambda abstraction
 */
public class Let extends Node {

	private final String var;
	private final Node defn;
	private final Node body;

	public Let(String var, Node defn, Node body) {
		this.var = var;
		this.defn = defn;
		this.body = body;
	}

	@Override
	public String toString() {
		return "(let " + var + " = " + defn + " in " + body + ")";
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        Type defnType = defn.getType(env, nonGenerics);
		env.put(var, defnType);
        return body.getType(env, nonGenerics);
	}
}
