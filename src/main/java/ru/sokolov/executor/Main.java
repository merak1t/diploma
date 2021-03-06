package ru.sokolov.executor;

import com.google.caja.lexer.ParseException;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Block;
import ru.sokolov.ssa.Visitor;
import ru.sokolov.type_inference.Inference;
import ru.sokolov.type_inference.ast.Node;
import ru.sokolov.type_inference.type.Type;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static ru.sokolov.executor.Utils.fromResource;
import static ru.sokolov.executor.Utils.js;

public class Main {

    private static void dfs(ParseTreeNode cur) {
        for (var item : cur.children()) {
            System.out.println(item);
            dfs(item);
        }
    }

    private static void print(ParseTreeNode cur, String fileName) throws IOException {
        StringBuilder output = new StringBuilder();
        cur.format(Utils.mc, output);
        output.append('\n');
        System.out.println(output);
        PrintWriter writer = new PrintWriter("src/main/resources/" + fileName, "UTF-8");
        writer.println(output);
        writer.close();
    }

    private static void print(List<Node> inputNodes, String fileName) throws IOException  {
        StringBuilder output = new StringBuilder();
        for (var it : inputNodes) {
            System.out.println(it);
        }
        List<Type> types = Inference.analyze(inputNodes);
        for (var i = 0; i < inputNodes.size(); i++) {
            output.append(inputNodes.get(i)).append(" : ").append(types.get(i).toString(true));
            output.append('\n');
        }
        PrintWriter writer = new PrintWriter("src/main/resources/" + fileName, "UTF-8");
        writer.println(output);
        writer.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        String testFile = "test.js";
        Block parseTree = js(fromResource(testFile));
        //dfs(parseTree);
        Visitor visitor = new Visitor();
        var newTree = visitor.visitProgram(parseTree);
        print(parseTree, "tree.txt");
        print(newTree, "newTree.txt");

        TranslateToLambda translator = new TranslateToLambda();
        var inputNodes = translator.visitProgram(newTree);
        print(inputNodes, "typed_test.txt");

    }
}
