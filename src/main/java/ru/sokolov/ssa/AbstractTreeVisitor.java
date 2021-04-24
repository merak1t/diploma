package ru.sokolov.ssa;

import com.google.caja.parser.js.*;

import java.util.ArrayList;

public interface AbstractTreeVisitor {
    Block visitProgram(Block ctx);

    Declaration visitDeclaration(Declaration ctx);

    MultiDeclaration visitMultiDeclaration(MultiDeclaration ctx);

    FunctionDeclaration visitFunctionDeclaration(FunctionDeclaration ctx);

    ExpressionStmt visitExpressionStmt(ExpressionStmt ctx);

    SimpleOperation visitSimpleOperation(SimpleOperation ctx);

    ArrayConstructor visitArrayConstructor(ArrayConstructor ctx);

    FunctionConstructor visitFunctionConstructor(FunctionConstructor ctx);

    FormalParam visitFormalParam(FormalParam ctx);

    ReturnStmt visitReturnStmt(ReturnStmt ctx);

    Reference visitReference(Reference ctx);

    AssignOperation visitAssignOperation(AssignOperation ctx);

    ArrayList<Statement> visitConditional(Conditional ctx);

    ForLoop visitForLoop(ForLoop ctx);

    WhileLoop visitWhileLoop(WhileLoop ctx);
}
