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

    Node visitArrayConstructor(ArrayConstructor ctx);

    List<Node> visitFunctionConstructor(FunctionConstructor ctx);

    List<String> visitFormalParam(FormalParam ctx);

    Node visitReturnStmt(ReturnStmt ctx);

    Node visitReference(Reference ctx);

    Node visitAssignOperation(AssignOperation ctx);

    Node visitSpecialOperation(SpecialOperation ctx);

    List<Node> visitConditional(Conditional ctx);

    List<Node> visitForLoop(ForLoop ctx);

    List<Node> visitWhileLoop(WhileLoop ctx);
}
