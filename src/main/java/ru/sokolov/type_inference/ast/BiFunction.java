package ru.sokolov.type_inference.ast;


import com.google.caja.parser.js.Operator;
import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.type.Arrow;
import ru.sokolov.type_inference.type.Tuple;
import ru.sokolov.type_inference.type.Type;
import ru.sokolov.type_inference.type.TypeVariable;

import java.util.*;

import static ru.sokolov.type_inference.Inference.*;

public class BiFunction extends Node {

    private final Operator operator;
    private final Node left;
    private final Node right;

    private static final Map<Type, Integer> typePriority = new HashMap<>() {{
        put(StringType, 3);
        put(IntegerType, 2);
        put(BooleanType, 1);
        put(AnyType, 0);
    }};

    private static final List<Type> typeList = new ArrayList<>(
            Arrays.asList(AnyType, BooleanType, IntegerType, StringType)
    );


    public BiFunction(Operator operator, Node left, Node right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator.getSymbol() + " " + right + ")";
    }

    @Override
    public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        Type leftType = left.getType(env, nonGenerics);
        Type rightType = right.getType(env, nonGenerics);
        int leftPriority = typePriority.getOrDefault(leftType, 0);
        int rightPriority = typePriority.getOrDefault(rightType, 0);
        //System.out.println("BI " + leftType + " " + rightType);

        Type res = typeList.get(Math.max(leftPriority, rightPriority));
        Type resultType = new TypeVariable();
        Inference.unify(new Arrow(new Tuple(leftType, rightType), resultType), new Arrow(new Tuple(leftType, rightType), res));
        return new Arrow(new Tuple(leftType, rightType), resultType);
    }
}
