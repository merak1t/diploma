package ru.sokolov.type_inference.type;

import java.util.Map;
import java.util.Set;

/**
 * Represents a function
 */
public class Arrow extends TypeConstructor {

	private final Type from;
	private final Type to;

	public Arrow(Type from, Type to) {
		super("->", from, to);
		this.from = from;
		this.to = to;
	}

	@Override
	public Type freshType(Set<TypeVariable> nonGenerics,
			Map<TypeVariable, TypeVariable> mappings) {
		Type freshFrom = from.freshType(nonGenerics, mappings);
		Type freshTo = to.freshType(nonGenerics, mappings);
		return new Arrow(freshFrom, freshTo);
	}

	@Override
	public String toString(boolean withName) {
		return "(" + from.toString(withName) + " -> " + to.toString(withName) + ")";
	}

	@Override
	public String toString() {
		return toString(false);
	}
}
