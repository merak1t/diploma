package ru.sokolov.type_inference.ast;


import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.type.Arrow;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.Map;
import java.util.Set;

public class Apply extends Node {

	private final Node fn;
	private final Node arg;

	public Apply(Node fn, Node arg) {
		this.fn = fn;
		this.arg = arg;
	}

	@Override
	public String toString() {
		return "(" + fn + " " + arg + ")";
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
		//System.out.println("APPLY " + fn + " : " + arg);
		Type fnType = fn.getType(env, nonGenerics);
		Type argType = arg.getType(env, nonGenerics);
		Type resultType = new TypeVariable();
		Inference.unify(new Arrow(argType, resultType), fnType);
		return resultType;
	}
}
