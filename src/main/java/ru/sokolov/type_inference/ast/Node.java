package ru.sokolov.type_inference.ast;


import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.Map;
import java.util.Set;

public abstract class Node {

	public abstract Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics);
}
