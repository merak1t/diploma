package ru.sokolov.executor;

import com.google.caja.parser.js.*;
import ru.sokolov.type_inference.ast.*;

import java.util.ArrayList;
import java.util.List;

public class TranslateToLambda implements AbstractTranslate {

    private Node createLiteral(Literal cur) {
        if (cur instanceof StringLiteral) {
            return new TypeLiteral(((StringLiteral) cur).getValue());
        } else if (cur instanceof IntegerLiteral) {
            return new TypeLiteral(((IntegerLiteral) cur).getValue().intValue());
        } else if (cur instanceof BooleanLiteral) {
            return new TypeLiteral(((BooleanLiteral) cur).getValue());
        } else {
            return new TypeLiteral();
        }
    }

    @Override
    public List<Node> visitProgram(Block ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof FunctionDeclaration) {
                res.addAll(visitFunctionDeclaration((FunctionDeclaration) child));
            } else if (child instanceof Conditional) {
                res.addAll(visitConditional((Conditional) child));
            } else if (child instanceof ForLoop) {
                res.addAll(visitForLoop((ForLoop) child));
            } else if (child instanceof WhileLoop) {
                res.addAll(visitWhileLoop((WhileLoop) child));
            } else if (child instanceof Declaration) {
                res.add(visitDeclaration((Declaration) child));
            } else if (child instanceof MultiDeclaration) {
                res.addAll(visitMultiDeclaration((MultiDeclaration) child));
            } else if (child instanceof ExpressionStmt) {
                res.addAll(visitExpressionStmt((ExpressionStmt) child));
            } else if (child instanceof ReturnStmt) {
                res.addAll(visitReturnStmt((ReturnStmt) child));
            } else {
                System.out.println("Undefined " + child + " in context " + ctx);
            }
        }
        return res;
    }

    @Override
    public Node visitDeclaration(Declaration ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (var i = 0; i < ctx.children().size(); i++) {
            var child = ctx.children().get(i);
            if (child instanceof Identifier) {
                res.add(new TypeIdentifier(((Identifier) child).getValue()));
            } else if (child instanceof Literal) {
                res.add(createLiteral((Literal) child));
            } else if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else if (child instanceof ArrayConstructor) {
                res.addAll(visitArrayConstructor((ArrayConstructor) child));
            } else if (child instanceof ExpressionStmt) {
                res.addAll(visitExpressionStmt((ExpressionStmt) child));
            }
        }
        assert res.size() == 2;
        var varName = res.get(0).toString();
        return new Let(varName, res.get(1), res.get(0));
    }

    @Override
    public List<Node> visitMultiDeclaration(MultiDeclaration ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof Declaration) {
                res.add(visitDeclaration(child));
            } else {
                System.err.println("Undefined " + child + " in context " + ctx);
            }
        }
        return res;
    }

    @Override
    public List<Node> visitFunctionDeclaration(FunctionDeclaration ctx) {
        return null;
    }

    @Override
    public List<Node> visitExpressionStmt(ExpressionStmt ctx) {
        return null;
    }

    @Override
    public Node visitSimpleOperation(SimpleOperation ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (var child : ctx.children()) {
            if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else if (child instanceof Reference) {
                res.add(visitReference((Reference) child));
            } else if (child instanceof Literal) {
                res.add(createLiteral((Literal) child));
            } else {
                System.out.println("Type.Undefined " + child + " in context " + ctx);
            }
        }
        assert res.size() == 2;
        return new Apply(
                new Apply(
                        new TypeIdentifier(ctx.getOperator().getSymbol()),
                        res.get(0)),
                res.get(1));
    }

    @Override
    public List<Node> visitArrayConstructor(ArrayConstructor ctx) {
        return null;
    }

    @Override
    public List<Node> visitFunctionConstructor(FunctionConstructor ctx) {
        return null;
    }

    @Override
    public List<Node> visitFormalParam(FormalParam ctx) {
        return null;
    }

    @Override
    public List<Node> visitReturnStmt(ReturnStmt ctx) {
        return null;
    }

    @Override
    public Node visitReference(Reference ctx) {
        return new TypeIdentifier(ctx.getIdentifierName());
    }

    @Override
    public List<Node> visitAssignOperation(AssignOperation ctx) {
        return null;
    }

    @Override
    public List<Node> visitConditional(Conditional ctx) {
        return null;
    }

    @Override
    public List<Node> visitForLoop(ForLoop ctx) {
        return null;
    }

    @Override
    public List<Node> visitWhileLoop(WhileLoop ctx) {
        return null;
    }


}
