package ru.sokolov.ssa;

import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Visitor implements AbstractTreeVisitor {
    IdentifierMemory identifierMemory = new IdentifierMemory();


    private Identifier createNewIdentifier(Identifier cur) {
        return new Identifier(cur.getFilePosition(),
                identifierMemory.getNewVersion(cur.getName()),
                cur.children());
    }

    private Identifier createLatestIdentifier(Identifier cur) {
        return new Identifier(cur.getFilePosition(),
                identifierMemory.getLatestVersion(cur.getName()),
                cur.children());
    }

    @Override
    public Block visitProgram(Block ctx) {
        ArrayList<Statement> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof FunctionDeclaration) {
                res.add(visitFunctionDeclaration((FunctionDeclaration) child));
            } else if (child instanceof Conditional) {
                res.addAll(visitConditional((Conditional) child));
            } else if (child instanceof ForLoop) {
                res.add(visitForLoop((ForLoop) child));
            } else if (child instanceof WhileLoop) {
                res.add(visitWhileLoop((WhileLoop) child));
            } else if (child instanceof Declaration) {
                res.add(visitDeclaration((Declaration) child));
            } else if (child instanceof MultiDeclaration) {
                res.add(visitMultiDeclaration((MultiDeclaration) child));
            } else if (child instanceof ExpressionStmt) {
                res.add(visitExpressionStmt((ExpressionStmt) child));
            } else if (child instanceof ReturnStmt) {
                res.add(visitReturnStmt((ReturnStmt) child));
            } else {
                System.out.println("Undefined " + child + " in context " + ctx);
            }
        }
        return new Block(ctx.getFilePosition(), res);
    }

    @Override
    public ReturnStmt visitReturnStmt(ReturnStmt ctx) {
        if (ctx.children().isEmpty()) {
            System.out.println("Wrong return statement " + ctx);
            return ctx;
        }
        return new ReturnStmt(ctx.getFilePosition(), visitSimpleOperation((SimpleOperation) ctx.children().get(0)));
    }

    @Override
    public SimpleOperation visitSimpleOperation(SimpleOperation ctx) {
        ArrayList<Expression> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else if (child instanceof Reference) {
                res.add(visitReference((Reference) child));
            } else if (child instanceof Literal) {
                res.add(child);
            } else {
                System.out.println("Undefined " + child + " in context " + ctx);
                res.add(child);
            }
        }
        return new SimpleOperation(ctx.getFilePosition(), ctx.getOperator(), res);
    }

    @Override
    public Reference visitReference(Reference ctx) {
        if (ctx.children().isEmpty()) {
            System.out.println("Wrong reference " + ctx);
            return ctx;
        }
        return new Reference(createLatestIdentifier((Identifier) ctx.children().get(0)));
    }

    @Override
    public Declaration visitDeclaration(Declaration ctx) {
        ArrayList<ParseTreeNode> res = new ArrayList<>();
        for (var i = 0; i < ctx.children().size(); i++) {
            var child = ctx.children().get(i);
            var nextChild =
                    (i + 1 == ctx.children().size())
                            ? null
                            : ctx.children().get(i + 1);
            if (child instanceof Identifier) {
                res.add(createNewIdentifier((Identifier) child));
                if (nextChild == null) {
                    System.out.println("Undefined declaration found " + child);
                } else if (nextChild instanceof Literal) {
                    res.add(nextChild);
                } else if (nextChild instanceof SimpleOperation) {
                    res.add(visitSimpleOperation((SimpleOperation) nextChild));
                } else if (nextChild instanceof ArrayConstructor) {
                    res.add(visitArrayConstructor((ArrayConstructor) nextChild));
                } else {
                    System.out.println("Undefined " + nextChild);
                }
            } else if (child instanceof ExpressionStmt) {
                res.add(visitExpressionStmt((ExpressionStmt) child));
            }
        }
        return new Declaration(ctx.getFilePosition(), (Void) ctx.getValue(), res);
    }

    @Override
    public MultiDeclaration visitMultiDeclaration(MultiDeclaration ctx) {
        ArrayList<Declaration> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof Declaration) {
                res.add(visitDeclaration(child));
            } else {
                System.err.println("Undefined " + child + " in context " + ctx);
            }
        }
        return new MultiDeclaration(ctx.getFilePosition(), res);
    }

    private Declaration visitDeclarationWithCondition(Declaration ctx, ConditionalHelper conditionalHelper) {
        ArrayList<ParseTreeNode> res = new ArrayList<>();
        for (var i = 0; i < ctx.children().size(); i++) {
            var child = ctx.children().get(i);
            var nextChild =
                    (i + 1 == ctx.children().size())
                            ? null
                            : ctx.children().get(i + 1);
            if (child instanceof Identifier) {
                conditionalHelper.addToCurrent(identifierMemory, ((Identifier) child).getName());
                res.add(createLatestIdentifier((Identifier) child));
                if (nextChild == null) {
                    System.out.println("Undefined declaration found " + child);
                } else if (nextChild instanceof Literal) {
                    res.add(nextChild);
                } else if (nextChild instanceof SimpleOperation) {
                    res.add(visitSimpleOperation((SimpleOperation) nextChild));
                } else if (nextChild instanceof ArrayConstructor) {
                    res.add(visitArrayConstructor((ArrayConstructor) nextChild));
                } else {
                    System.out.println("Undefined " + nextChild);
                }
            } else if (child instanceof ExpressionStmt) {
                res.add(visitExpressionStmt((ExpressionStmt) child));
            }
        }
        return new Declaration(ctx.getFilePosition(), (Void) ctx.getValue(), res);
    }

    private MultiDeclaration visitMultiDeclarationWithCondition(MultiDeclaration ctx, ConditionalHelper conditionalHelper) {
        ArrayList<Declaration> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof Declaration) {
                res.add(visitDeclarationWithCondition(child, conditionalHelper));
            } else {
                System.err.println("Undefined " + child + " in context " + ctx);
            }
        }
        return new MultiDeclaration(ctx.getFilePosition(), res);
    }

    @Override
    public FunctionDeclaration visitFunctionDeclaration(FunctionDeclaration ctx) {

        if (ctx.children().size() != 2) {
            System.out.println("Wrong FunctionDeclaration " + ctx);
            return ctx;
        }
        var construct = (FunctionConstructor) ctx.children().get(1);
        return new FunctionDeclaration(visitFunctionConstructor(construct));
    }

    @Override
    public FunctionConstructor visitFunctionConstructor(FunctionConstructor ctx) {
        ArrayList<ParseTreeNode> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof Identifier) {
                res.add(createNewIdentifier((Identifier) child));
            } else if (child instanceof FormalParam) {
                res.add(visitFormalParam((FormalParam) child));
            } else if (child instanceof Block) {
                res.add(visitProgram((Block) child));
            } else {
                System.out.println("Undefined in Declaration " + child);
            }
        }
        return new FunctionConstructor(ctx.getFilePosition(), (Void) ctx.getValue(), res);
    }

    @Override
    public FormalParam visitFormalParam(FormalParam ctx) {
        if (ctx.children().isEmpty()) {
            System.err.println("Wrong Param in " + ctx);
            return ctx;
        }
        var identifier = (Identifier) ctx.children().get(0);
        return new FormalParam(createNewIdentifier(identifier));
    }


    @Override
    public ExpressionStmt visitExpressionStmt(ExpressionStmt ctx) {
        var operation = ctx.getExpression();
        if (operation instanceof AssignOperation) {
            return new ExpressionStmt(visitAssignOperation((AssignOperation) operation));

        } else {
            System.err.println("Unexpected operation " + operation + " in context " + ctx);
            return ctx;
        }
    }

    private ExpressionStmt visitExpressionStmtWithCondition(ExpressionStmt ctx, ConditionalHelper conditionalHelper) {
        var operation = ctx.getExpression();
        if (operation instanceof AssignOperation) {
            return new ExpressionStmt(visitAssignOperationWithCondition((AssignOperation) operation, conditionalHelper));

        } else {
            System.err.println("Unexpected operation " + operation + " in context " + ctx);
            return ctx;
        }
    }

    @Override
    public AssignOperation visitAssignOperation(AssignOperation ctx) {
        if (ctx.children().isEmpty()) {
            System.err.println("Unexpected empty assign " + ctx);
            return ctx;
        }
        var reference = (Reference) ctx.children().get(0);
        ArrayList<Expression> res = new ArrayList<>();
        for (var i = 1; i < ctx.children().size(); i++) {
            var child = ctx.children().get(i);
            if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else {
                res.add(child);
            }
        }
        var leftValue = new Reference(createNewIdentifier(reference.getIdentifier()));
        res.add(0, leftValue);
        return new AssignOperation(ctx.getFilePosition(), ctx.getOperator(), res);
    }

    private AssignOperation visitAssignOperationWithCondition(AssignOperation ctx, ConditionalHelper conditionalHelper) {
        if (ctx.children().isEmpty()) {
            System.err.println("Unexpected empty assign " + ctx);
            return ctx;
        }
        var reference = (Reference) ctx.children().get(0);
        ArrayList<Expression> res = new ArrayList<>();
        for (var i = 1; i < ctx.children().size(); i++) {
            var child = ctx.children().get(i);
            if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else {
                res.add(child);
            }
        }
        var refName = reference.getIdentifier().getName();
        conditionalHelper.addToCurrent(identifierMemory, refName);
        var leftValue = new Reference(createLatestIdentifier(reference.getIdentifier()));
        res.add(0, leftValue);
        return new AssignOperation(ctx.getFilePosition(), ctx.getOperator(), res);
    }

    private Block visitConditionalBlock(Block ctx, ConditionalHelper conditionalHelper) {

        ArrayList<Statement> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof Conditional) {
                res.addAll(visitConditional((Conditional) child));
            } else if (child instanceof ForLoop) {
                res.add(visitForLoop((ForLoop) child));
            } else if (child instanceof WhileLoop) {
                res.add(visitWhileLoop((WhileLoop) child));
            } else if (child instanceof Declaration) {
                res.add(visitDeclarationWithCondition((Declaration) child, conditionalHelper));
            } else if (child instanceof MultiDeclaration) {
                res.add(visitMultiDeclarationWithCondition((MultiDeclaration) child, conditionalHelper));
            } else if (child instanceof ExpressionStmt) {
                res.add(visitExpressionStmtWithCondition((ExpressionStmt) child, conditionalHelper));
            } else if (child instanceof ReturnStmt) {
                res.add(visitReturnStmt((ReturnStmt) child));
            } else {
                System.out.println("Undefined " + child + " in context " + ctx);
            }
        }
        conditionalHelper.addCurrentToResult();
        return new Block(ctx.getFilePosition(), res);
    }

    @Override
    public ArrayList<Statement> visitConditional(Conditional ctx) {
        ConditionalHelper conditionalHelper = new ConditionalHelper();

        if (ctx.children().size() < 2) {
            System.err.println("Wrong Conditional " + ctx);
            return new ArrayList<>(Collections.singletonList(ctx));
        }
        ArrayList<ParseTreeNode> res = new ArrayList<>();
        var isElseCond = false;
        for (var child : ctx.children()) {
            if (child instanceof Expression) {
                isElseCond = false;
                if (child instanceof SimpleOperation) {
                    res.add(visitSimpleOperation((SimpleOperation) child));
                } else {
                    System.out.println("Unexpected expression " + child + " in conditional " + ctx);
                }
            } else if (child instanceof Block) {
                if (isElseCond) {
                    conditionalHelper.setCurrentOnElse();
                }
                isElseCond = true;
                res.add(visitConditionalBlock((Block) child, conditionalHelper));
            } else {
                System.err.println("Unexpected child " + child + " in conditional " + ctx);
            }

        }
        var newCond = new Conditional(ctx.getFilePosition(), (Void) ctx.getValue(), res);
        var listFunctions = conditionalHelper.createPhiFunc(identifierMemory);
        var result = new ArrayList<Statement>();
        result.add(newCond);
        result.addAll(listFunctions);
        return result;
    }

    @Override
    public ForLoop visitForLoop(ForLoop ctx) {
        return ctx;
    }

    @Override
    public WhileLoop visitWhileLoop(WhileLoop ctx) {
        return ctx;
    }


    @Override
    public ArrayConstructor visitArrayConstructor(ArrayConstructor ctx) {
        ArrayList<Expression> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else if (child instanceof Reference) {
                res.add(visitReference((Reference) child));
            } else {
                res.add(child);
            }
        }
        return new ArrayConstructor(ctx.getFilePosition(), (Void) ctx.getValue(), res);
    }


}
