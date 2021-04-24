package ru.sokolov.executor;

import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.ast.*;
import ru.sokolov.type_inference.type.Type;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        Node[] roots1 = new Node[]{
                new Let(
                        "x",
                        new Literal(5),
                        new Identifier("x")
                ),

                // let f = (fn y => y) in ((pair (f 4)) (f true))
                new Let(
                        "f",
                        new Lambda("y", new Identifier("y")),
                        new Apply(
                                new Apply(
                                        new Identifier("pair"),
                                        new Apply(new Identifier("f"), new Literal(4))
                                ),
                                new Apply(new Identifier("f"), new Identifier("x"))
                        )
                )
        };

        Node[] roots2 = new Node[]{
                new Let(
                        "x",
                        new Identifier("y"),
                        new Identifier("x")
                ),
                new Let(
                        "y",
                        new Identifier("z"),
                        new Identifier("y")
                ),
                new Let(
                        "z",
                        new Literal(5),
                        new Identifier("z")
                )
        };

        Node[] roots3 = new Node[]{
                new Function(Arrays.asList("x", "y"), new Apply(new Apply(new Identifier("*"), new Identifier("x")),
                        new Identifier("y"))),

                new Let(
                        "z",
                        new Literal(5),
                        new Identifier("z")
                )
        };
        List<Node> inputNodes = Arrays.asList(roots3);
        List<Type> types = Inference.analyze(inputNodes);
        for (var i = 0; i < inputNodes.size(); i++) {
            System.out.println(inputNodes.get(i) + " : " + types.get(i));
        }
    }
}
