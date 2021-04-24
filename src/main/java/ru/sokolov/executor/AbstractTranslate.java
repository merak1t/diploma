package ru.sokolov.executor;

import com.google.caja.parser.js.*;
import ru.sokolov.type_inference.ast.Node;

import java.util.ArrayList;
import java.util.List;

public interface AbstractTranslate {
    List<Node> visitProgram(Block ctx);

    Node visitDeclaration(Declaration ctx);

    List<Node> visitMultiDeclaration(MultiDeclaration ctx);

    List<Node> visitFunctionDeclaration(FunctionDeclaration ctx);

    List<Node> visitExpressionStmt(ExpressionStmt ctx);

    Node visitSimpleOperation(SimpleOperation ctx);

    List<Node> visitArrayConstructor(ArrayConstructor ctx);

    List<Node> visitFunctionConstructor(FunctionConstructor ctx);

    List<Node> visitFormalParam(FormalParam ctx);

    List<Node> visitReturnStmt(ReturnStmt ctx);

    Node visitReference(Reference ctx);

    List<Node> visitAssignOperation(AssignOperation ctx);

    List<Node> visitConditional(Conditional ctx);

    List<Node> visitForLoop(ForLoop ctx);

    List<Node> visitWhileLoop(WhileLoop ctx);
}
