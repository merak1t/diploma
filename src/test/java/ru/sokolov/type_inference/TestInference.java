package ru.sokolov.type_inference;

import org.junit.Assert;
import org.junit.Test;
import ru.sokolov.type_inference.ast.*;
import ru.sokolov.type_inference.type.Type;

/**
 * Tests for HM type inference. Each test creates an AST and passes it to the "inferencer"
 * We get a type back and verify it is the expected type.
 */
public class TestInference {

    @Test
    public void testFactorial() {
		/*
			let rec
				factorial =
					fn[n] =>
						if (== n 0) then 1 else (* n (factorial (- n 1))) fi
			in
				(factorial 5)
		*/
        Node root = new LetRec(
                "factorial",
                new Lambda("n",
                        new If(
                                new Apply(
                                        new Apply(new TypeIdentifier("=="), new TypeIdentifier("n")),
                                        new TypeLiteral(0)
                                ),
                                new TypeLiteral(1),
                                new Apply(
                                        new Apply(new TypeIdentifier("*"), new TypeIdentifier("n")),
                                        new Apply(new TypeIdentifier("factorial"),
                                                new Apply(
                                                        new Apply(new TypeIdentifier("-"), new TypeIdentifier("n")),
                                                        new TypeLiteral(1)
                                                )
                                        )
                                )
                        )
                ),
                new Apply(new TypeIdentifier("factorial"), new TypeLiteral(5))
        );
        Type type = Inference.analyze(root);
        Assert.assertEquals(Inference.IntegerType, type);
    }

    @Test
    public void testTypeMismatch() {
        // fn x => (pair(x(3) (x(true)))
        Node root = new Lambda(
                "x",
                new Apply(
                        new Apply(
                                new TypeIdentifier("pair"),
                                new Apply(new TypeIdentifier("x"), new TypeLiteral(3))
                        ),
                        new Apply(new TypeIdentifier("x"), new TypeLiteral(true))
                )
        );

        Exception e = null;
        try {
            Inference.analyze(root);
        } catch (IllegalArgumentException ex) {
            e = ex;
        }
        Assert.assertTrue(e instanceof IllegalArgumentException);
        Assert.assertEquals("Type mismatch: Boolean != Integer", e.getMessage());
    }

    @Test
    public void testUndefinedSymbol() {
        // pair(f(3), f(true))
        Node root = new Apply(
                new Apply(
                        new TypeIdentifier("pair"),
                        new Apply(
                                new TypeIdentifier("f"),
                                new TypeLiteral(4)
                        )
                ),
                new Apply(
                        new TypeIdentifier("f"),
                        new TypeLiteral(true)
                )
        );
        Type type = Inference.analyze(root);
        Assert.assertEquals("(Integer, Boolean)", type.toString(true));
    }

    @Test
    public void testPairs() {
        // let f = (fn x => x) in ((pair (f 4)) (f true))
        Node root = new Let(
                "f",
                new Lambda("x", new TypeIdentifier("x")),
                new Apply(
                        new Apply(
                                new TypeIdentifier("pair"),
                                new Apply(new TypeIdentifier("f"), new TypeLiteral(4))
                        ),
                        new Apply(new TypeIdentifier("f"), new TypeLiteral(true))
                )
        );

        Type type = Inference.analyze(root);
        Assert.assertEquals("(Integer, Boolean)", type.toString(true));
    }

    @Test
    public void testRecursiveUnification() {
        // fn f => f f
        Node root = new Lambda("f", new Apply(new TypeIdentifier("f"), new TypeIdentifier("f")));

        Exception e = null;
        try {
            Inference.analyze(root);
        } catch (IllegalArgumentException ex) {
            e = ex;
        }
        Assert.assertTrue(e instanceof IllegalArgumentException);
        Assert.assertEquals("Recursive unification of a and (a -> b)", e.getMessage());
    }

    @Test
    public void testApplication() {
        // let g = fn f => 5 in g g
        Node root = new Let("g",
                new Lambda("f", new TypeLiteral(5)),
                new Apply(new TypeIdentifier("g"), new TypeIdentifier("g")));

        Type type = Inference.analyze(root);
        Assert.assertEquals("Integer", type.toString(true));
    }

    @Test
    public void testGenerics() {
        // example that demonstrates generic and non-generic variables:
        // fn g => let f = fn x => g in pair (f 3, f true)
        Node root = new Lambda("g",
                new Let("f",
                        new Lambda("x", new TypeIdentifier("g")),
                        new Apply(
                                new Apply(
                                        new TypeIdentifier("pair"),
                                        new Apply(new TypeIdentifier("f"), new TypeLiteral(3))
                                ),
                                new Apply(new TypeIdentifier("f"), new TypeLiteral(true))
                        )
                )
        );

        Type type = Inference.analyze(root);
        Assert.assertEquals("(a -> (a, a))", type.toString(true));
    }

    @Test
    public void testFunctionComposition() {
        // fn f (fn g (fn arg (f g arg)))
        Node root = new Lambda("f",
                new Lambda("g",
                        new Lambda("arg",
                                new Apply(
                                        new TypeIdentifier("g"),
                                        new Apply(new TypeIdentifier("f"), new TypeIdentifier("arg"))
                                )
                        )
                )
        );

        Type type = Inference.analyze(root);
        Assert.assertEquals("((a -> b) -> ((b -> c) -> (a -> c)))", type.toString(true));
    }

    // x = 5
    // while cond
    //      z = y
    //      y = x
    // let rec while cond body =
    // if cond then
    // while cond body
    @Test
    public void testMap() {
        // map function
        //
        // let rec map =
        // 		fn [f] =>
        // 			fn [lst] =>
        // 				if (empty? lst)
        // 					then []
        // 					else (cons (f (first lst)) (map f (rest lst)))
        // in map
        Node root =
                new LetRec("map",
                        new Lambda("f",
                                new Lambda("lst",
                                        new If(new Apply(new TypeIdentifier("empty?"), new TypeIdentifier("lst")),
                                                new TypeIdentifier("[]"),
                                                new Apply(
                                                        new Apply(new TypeIdentifier("cons"), new Apply(new TypeIdentifier("f"), new Apply(new TypeIdentifier("first"), new TypeIdentifier("lst")))),
                                                        new Apply(
                                                                new Apply(new TypeIdentifier("map"), new TypeIdentifier("f")),
                                                                new Apply(new TypeIdentifier("rest"), new TypeIdentifier("lst"))))
                                        ))),
                        new TypeIdentifier("map"));

        Type type = Inference.analyze(root);
        Assert.assertEquals("((a -> b) -> (List[a] -> List[b]))", type.toString(true));
    }

    @Test
    public void testLet() {
        /*
			let x = 5 in x
			arr = [1, 2, 3]
			create
		*/
        Node root = new Let(
                "x",
                new TypeLiteral(5),
                new TypeIdentifier("x")
        );

        Type type = Inference.analyze(root);
        System.out.println(root + " : " + type);
        Assert.assertEquals("Integer", type.toString(true));
    }

    @Test
    public void testEither() {
        // fn[n] => fn[x] => fn[y] => if (((== n) 0)) then x else y
        Node root = new Lambda("n", new Lambda("x", new Lambda("y",
                new If(new Apply(
                        new Apply(new TypeIdentifier("=="), new TypeIdentifier("n")),
                        new TypeLiteral(0)
                ), new TypeIdentifier("x"),
                        new TypeIdentifier("y"))
        )));

        Type type = Inference.analyze(root);
        System.out.println(root + " : " + type);
        Assert.assertEquals("(Integer -> (a -> (a -> a)))", type.toString(true));
    }

    @Test
    public void testList() {
        /*
			let rec
				toList =
					fn[n] =>
						if (== n 0)
						then []
						else (cons n (toList (- n 1))) fi
			in
				(toList 5)
		*/
        Node root = new LetRec(
                "toList",
                new Lambda("n",
                        new If(
                                new Apply(
                                        new Apply(new TypeIdentifier("=="), new TypeIdentifier("n")),
                                        new TypeLiteral(0)
                                ), new TypeIdentifier("[]"),
                                new Apply(
                                        new Apply(new TypeIdentifier("cons"), new TypeIdentifier("n")),
                                        new Apply(new TypeIdentifier("toList"),
                                                new Apply(
                                                        new Apply(new TypeIdentifier("-"), new TypeIdentifier("n")),
                                                        new TypeLiteral(1)
                                                )
                                        )
                                )
                        )
                ),
                new Apply(new TypeIdentifier("toList"), new TypeLiteral(5))
        );
        Type type = Inference.analyze(root);
        System.err.println(root.toString());

        Assert.assertEquals("List[Integer]", type.toString(true));
    }

    @Test
    public void testReduce() {
        // reduce function
        //
        // let rec reduce =
        // 		fn [f] =>
        //			fn [initVal] =>
        //				fn [lst] =>
        //					if (empty? lst) then initval
        //						else (reduce f (f initVal (first lst)) (rest lst))
        // in reduce
        Node root = new LetRec("reduce",
                new Lambda("f",
                        new Lambda("initVal",
                                new Lambda("lst",
                                        new If(new Apply(new TypeIdentifier("empty?"), new TypeIdentifier("lst")),
                                                new TypeIdentifier("initVal"),
                                                new Apply(
                                                        new Apply(
                                                                new Apply(new TypeIdentifier("reduce"), new TypeIdentifier("f")),
                                                                new Apply(
                                                                        new Apply(new TypeIdentifier("f"), new TypeIdentifier("initVal")),
                                                                        new Apply(new TypeIdentifier("first"), new TypeIdentifier("lst"))
                                                                )
                                                        ),
                                                        new Apply(new TypeIdentifier("rest"), new TypeIdentifier("lst"))
                                                )
                                        )
                                )
                        )
                ),
                new TypeIdentifier("reduce"));

        Type type = Inference.analyze(root);
        Assert.assertEquals("((a -> (b -> a)) -> (a -> (List[b] -> a)))", type.toString(true));
    }

    @Test
    public void testIterate() {
        // iterate function from clojure - (iterate f x) returns an infinite sequence of x, (f x), (f (f x)) etc
        // (let rec iterate =
        // 		fn [f] =>
        // 			fn [x] =>
        // 				(cons x (iterate f (f x)))
        // 	in iterate)
        Node root = new LetRec("iterate",
                new Lambda("f",
                        new Lambda("x",
                                new Apply(
                                        new Apply(new TypeIdentifier("cons"), new TypeIdentifier("x")
                                        ),
                                        new Apply(
                                                new Apply(new TypeIdentifier("iterate"), new TypeIdentifier("f")),
                                                new Apply(new TypeIdentifier("f"), new TypeIdentifier("x"))
                                        )
                                )
                        )
                ),
                new TypeIdentifier("iterate"));

        Type type = Inference.analyze(root);
        Assert.assertEquals("((a -> a) -> (a -> List[a]))", type.toString(true));
    }

    @Test
    public void testZipMap() {
        // zipmap - returns a list of pairs (assumes lists are of same length)
        // (let rec zipmap =
        // 		fn [a] =>
        // 			fn [b] =>
        //				(if (empty? a)
        // 					then []
        // 					else (cons (pair (first a) (first b)) (zipmap (rest a) (rest b))))
        // 	in zipmap)
        Node root = new LetRec("zipmap",
                new Lambda("a",
                        new Lambda("b",
                                new If(
                                        new Apply(new TypeIdentifier("empty?"), new TypeIdentifier("a")),
                                        new TypeIdentifier("[]"),
                                        new Apply(
                                                new Apply(
                                                        new TypeIdentifier("cons"),
                                                        new Apply(
                                                                new Apply(new TypeIdentifier("pair"), new Apply(new TypeIdentifier("first"), new TypeIdentifier("a"))),
                                                                new Apply(new TypeIdentifier("first"), new TypeIdentifier("b"))
                                                        )
                                                ),
                                                new Apply(
                                                        new Apply(
                                                                new TypeIdentifier("zipmap"),
                                                                new Apply(new TypeIdentifier("rest"), new TypeIdentifier("a"))
                                                        ),
                                                        new Apply(new TypeIdentifier("rest"), new TypeIdentifier("b"))
                                                )
                                        )
                                )
                        )
                ),
                new TypeIdentifier("zipmap"));

        Type type = Inference.analyze(root);
        Assert.assertEquals("(List[a] -> (List[b] -> List[(a, b)]))", type.toString(true));
    }
}
