package ru.sokolov.executor;

import com.google.caja.lexer.ParseException;
import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.Block;
import ru.sokolov.ssa.Visitor;

import java.io.IOException;
import java.io.PrintWriter;

import static ru.sokolov.executor.Utils.fromResource;
import static ru.sokolov.executor.Utils.js;

public class SSA {

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

    public static void main(String[] args) throws IOException, ParseException {
        String testFile = "ssatest.js";
        Block parseTree = js(fromResource(testFile));
        //dfs(parseTree);
        Visitor visitor = new Visitor();
        var newTree = visitor.visitProgram(parseTree);
        print(parseTree, "tree.txt");
        print(newTree, "newTree.txt");

    }
}
