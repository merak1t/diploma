package ru.sokolov.type_inference.ast;


import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TypeIdentifier extends Node {

	private final String name;

	public TypeIdentifier(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
		if (env.containsKey(name)) {
			Map<TypeVariable, TypeVariable> mappings = new HashMap<>();
			return env.get(name).freshType(nonGenerics, mappings);
		}
		else {
			// ToDo fix function
			TypeVariable newType = new TypeVariable();
			env.put(name, newType);
			Map<TypeVariable, TypeVariable> mappings = new HashMap<>();
			return env.get(name).freshType(nonGenerics, mappings);
		}
		//throw new IllegalArgumentException("Undefined identifier " + name);
	}
}
