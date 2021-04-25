package ru.sokolov.executor;

import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.ast.*;
import ru.sokolov.type_inference.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LambdaInference {

    public static void main(String[] args) throws Exception {
        Node[] roots1 = new Node[]{
                new Let(
                        "x",
                        new TypeLiteral(5),
                        new TypeIdentifier("x")
                ),

                // let f = (fn y => y) in ((pair (f 4)) (f true))
                new Let(
                        "f",
                        new Lambda("y", new TypeIdentifier("y")),
                        new Apply(
                                new Apply(
                                        new TypeIdentifier("pair"),
                                        new Apply(new TypeIdentifier("f"), new TypeLiteral(4))
                                ),
                                new Apply(new TypeIdentifier("f"), new TypeIdentifier("x"))
                        )
                )
        };

        Node[] roots2 = new Node[]{
                new Let(
                        "x",
                        new TypeIdentifier("y"),
                        new TypeIdentifier("x")
                ),
                new Let(
                        "y",
                        new TypeIdentifier("z"),
                        new TypeIdentifier("y")
                ),
                new Let(
                        "z",
                        new TypeLiteral(5),
                        new TypeIdentifier("z")
                )
        };

        Node[] roots3 = new Node[]{
                new Function(new TypeIdentifier("fn"), Arrays.asList("x", "y"), new ArrayList<>(),new Apply(new Apply(new TypeIdentifier("*"), new TypeIdentifier("x")),
                        new TypeIdentifier("y"))),

                new Let(
                        "z",
                        new TypeLiteral(5),
                        new TypeIdentifier("z")
                )
        };
        List<Node> inputNodes = Arrays.asList(roots3);
        List<Type> types = Inference.analyze(inputNodes);
        for (var i = 0; i < inputNodes.size(); i++) {
            System.out.println(inputNodes.get(i) + " : " + types.get(i));
        }
    }
}
