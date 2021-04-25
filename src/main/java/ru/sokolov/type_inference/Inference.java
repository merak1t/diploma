package ru.sokolov.type_inference;


import ru.sokolov.type_inference.ast.LiteralType;
import ru.sokolov.type_inference.ast.Node;
import ru.sokolov.type_inference.type.*;

import java.util.*;

public class Inference {

    public static final Type BooleanType = new TypeConstructor("Boolean");
    public static final Type IntegerType = new TypeConstructor("Integer");
    public static final Type StringType = new TypeConstructor("String");
    public static final Type AnyType = new TypeConstructor("Any");

    public static final Map<LiteralType, Type> typeMapper = new HashMap<>() {{
        put(LiteralType.BOOLEAN, BooleanType);
        put(LiteralType.INTEGER, IntegerType);
        put(LiteralType.STRING, StringType);
        put(LiteralType.ANY, AnyType);
    }};

    private static final Map<String, Type> StandardEnv = new HashMap<>();

    static {
        Type var1 = new TypeVariable();
        Type var2 = new TypeVariable();

        StandardEnv.put("==", new Arrow(var1, new Arrow(var1, BooleanType)));

        //StandardEnv.put("*", new Arrow(IntegerType, new Arrow(IntegerType, IntegerType)));
        //StandardEnv.put("-", new Arrow(IntegerType, new Arrow(IntegerType, IntegerType)));
        //StandardEnv.put("+", new Arrow(IntegerType, new Arrow(IntegerType, IntegerType)));
        //StandardEnv.put("+", new Arrow(StringType, new Arrow(IntegerType, StringType)));
        StandardEnv.put("pair", new Arrow(var1, new Arrow(var2, new Tuple(var1, var2))));

        // List
        Type listType = new TypeConstructor("List", var1);
        StandardEnv.put("[]", listType);
        StandardEnv.put("first", new Arrow(listType, var1));
        StandardEnv.put("rest", new Arrow(listType, listType));
        StandardEnv.put("empty?", new Arrow(listType, BooleanType));
        StandardEnv.put("cons", new Arrow(var1, new Arrow(listType, listType)));
    }

    public static Type analyze(Node node) {
        Set<TypeVariable> nonGenerics = new HashSet<>();
        nextVariableName = StartingVariableName;
        return node.getType(StandardEnv, nonGenerics);
    }

    public static List<Type> analyze(List<Node> nodes) {
        Set<TypeVariable> nonGenerics = new HashSet<>();
        nextVariableName = StartingVariableName;
        var resTypes = new ArrayList<Type>();
        for (var i = 0; i < 3; i++) {
            for (var node : nodes) {
                node.getType(StandardEnv, nonGenerics);
            }
        }
        /*for (var cur : StandardEnv.entrySet()) {
            System.out.println(cur.getKey() + " " + cur.getValue());
        }*/
        for (var node : nodes) {
            resTypes.add(node.getType(StandardEnv, nonGenerics));
        }
        return resTypes;
    }

    /**
     * Robinson's unification algorithm
     */
    public static void unify(Type a, Type b) {
        Type t1 = a.prune();
        Type t2 = b.prune();
        t1.unify(t2);
    }

    private static final char StartingVariableName = 'a';
    private static char nextVariableName = StartingVariableName;

    public static char getNextVariableName() {
        return nextVariableName++;
    }
}
