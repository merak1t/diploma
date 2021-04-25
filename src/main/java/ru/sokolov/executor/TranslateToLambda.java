package ru.sokolov.executor;

import com.google.caja.parser.ParseTreeNode;
import com.google.caja.parser.js.*;
import ru.sokolov.type_inference.ast.*;
import ru.sokolov.type_inference.type.Tuple;
import ru.sokolov.type_inference.type.Type;

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
                // Do nothing
            } else {
                System.out.println("Undefined " + child + " in context " + ctx);
            }
        }
        return res;
    }

    @Override
    public Node visitDeclaration(Declaration ctx) {
        ArrayList<Node> res = new ArrayList<>();
        for (var child : ctx.children()) {
            System.out.println(child);
            if (child instanceof Identifier) {
                res.add(new TypeIdentifier(((Identifier) child).getValue()));
            } else if (child instanceof Literal) {
                res.add(createLiteral((Literal) child));
            } else if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else if (child instanceof ArrayConstructor) {
                res.add(visitArrayConstructor((ArrayConstructor) child));
            } else if (child instanceof ExpressionStmt) {
                res.addAll(visitExpressionStmt((ExpressionStmt) child));
            } else if (child instanceof SpecialOperation) {
                System.out.println(child);
                res.add(visitSpecialOperation((SpecialOperation) child));
            } else {
                System.err.println("Undefined " + child + " in context " + ctx);
            }
        }
        if (res.size() == 1) {
            return res.get(0);
        }
        assert res.size() == 2;
        var varName = res.get(0).toString();
        System.out.println("res " + res);
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

        if (ctx.children().size() != 2) {
            System.err.println("Wrong FunctionDeclaration " + ctx);
            return new ArrayList<>();
        }
        var construct = (FunctionConstructor) ctx.children().get(1);
        return visitFunctionConstructor(construct);
    }

    @Override
    public List<Node> visitExpressionStmt(ExpressionStmt ctx) {
        var res = new ArrayList<Node>();
        var operation = ctx.getExpression();
        if (operation instanceof AssignOperation) {
            res.add(visitAssignOperation((AssignOperation) operation));
        } else {
            System.err.println("Unexpected operation " + operation + " in context " + ctx);
        }
        return res;

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
                System.err.println("Type.Undefined " + child + " in context " + ctx);
            }
        }
        assert res.size() == 2;
        return new Apply(new TypeIdentifier("+"), new Array(res));
    }

    @Override
    public Node visitArrayConstructor(ArrayConstructor ctx) {
        var res = new ArrayList<Node>();
        for (var child : ctx.children()) {
            if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else if (child instanceof Reference) {
                res.add(visitReference((Reference) child));
            } else if (child instanceof Literal) {
                res.add(createLiteral((Literal) child));
            } else {
                System.err.println("Type.Undefined " + child + " in context " + ctx);
            }
        }
        return new Array(res);
    }

    private Node getReturnStmt(Block ctx) {
        Node res = null;
        for (var child : ctx.children()) {
            if (child instanceof ReturnStmt) {
                res = visitReturnStmt((ReturnStmt) child);
            }
        }
        ;
        assert res != null;
        return res;
    }

    @Override
    public List<Node> visitFunctionConstructor(FunctionConstructor ctx) {
        var body = new ArrayList<Node>();
        var listParams = new ArrayList<String>();
        Node fn = null;
        Node returnStmt = null;
        for (var child : ctx.children()) {
            if (child instanceof Identifier) {
                fn = new TypeIdentifier(((Identifier) child).getValue());
            } else if (child instanceof FormalParam) {
                listParams.addAll(visitFormalParam((FormalParam) child));
            } else if (child instanceof Block) {
                body.addAll(visitProgram((Block) child));
                returnStmt = getReturnStmt((Block) child);
            } else {
                System.err.println("Type.Undefined in Declaration " + child);
            }
        }
        assert fn != null && returnStmt != null;
        Node resFunc = new Function(fn, listParams, body, returnStmt);
        var res = new ArrayList<Node>();
        res.add(resFunc);
        res.addAll(body);
        return res;

    }

    @Override
    public List<String> visitFormalParam(FormalParam ctx) {
        var res = new ArrayList<String>();
        if (ctx.children().isEmpty()) {
            System.err.println("Type.Wrong Param in " + ctx);
            return res;
        }
        var identifier = (Identifier) ctx.children().get(0);
        res.add(identifier.getValue());
        return res;
    }

    @Override
    public Node visitReturnStmt(ReturnStmt ctx) {
        return visitSimpleOperation((SimpleOperation) ctx.children().get(0));

    }

    @Override
    public Node visitReference(Reference ctx) {
        return new TypeIdentifier(ctx.getIdentifierName());
    }

    @Override
    public Node visitAssignOperation(AssignOperation ctx) {
        var res = new ArrayList<Node>();
        for (var child : ctx.children()) {
            if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else if (child instanceof Reference) {
                res.add(visitReference((Reference) child));
            } else if (child instanceof Literal) {
                res.add(createLiteral((Literal) child));
            } else {
                System.err.println("Type.Undefined " + child + " in context " + ctx);
            }
        }
        assert res.size() == 2 && res.get(0) instanceof TypeIdentifier;
        System.out.println("ASSIGN " + res);
        var leftVarName = ((Reference)ctx.children().get(0)).getIdentifierName();
        return new Let(leftVarName, res.get(1), res.get(0));
    }

    @Override
    public Node visitSpecialOperation(SpecialOperation ctx) {
        Node fn = null;
        var res = new ArrayList<Node>();
        if (!ctx.children().isEmpty() && ctx.children().get(0) instanceof Reference) {
            fn = new TypeIdentifier(((Reference) ctx.children().get(0)).getIdentifierName());
        }
        for (int i = 1; i < ctx.children().size(); i++) {
            var child = ctx.children().get(i);
            if (child instanceof Reference) {
                res.add(visitReference((Reference) child));
            } else if (child instanceof Literal) {
                res.add(createLiteral((Literal) child));
            } else if (child instanceof SimpleOperation) {
                res.add(visitSimpleOperation((SimpleOperation) child));
            } else {
                System.err.println("Type.Undefined " + child + " in context " + ctx);
            }
        }
        assert fn != null;
        //System.out.println("FN " + fn + " : " + new Array(res));
        return new Apply(fn, new Array(res));
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
