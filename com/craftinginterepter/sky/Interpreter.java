package com.craftinginterepter.sky;

import java.util.List;

import com.craftinginterepter.sky.Expr.Assign;
import com.craftinginterepter.sky.Expr.Logical;
import com.craftinginterepter.sky.Expr.Postfix;
import com.craftinginterepter.sky.Expr.Prefix;
import com.craftinginterepter.sky.Expr.Variable;
import com.craftinginterepter.sky.RuntimeError.BreakException;
import com.craftinginterepter.sky.RuntimeError.ContinueException;
import com.craftinginterepter.sky.Stmt.Block;
import com.craftinginterepter.sky.Stmt.Break;
import com.craftinginterepter.sky.Stmt.Continue;
import com.craftinginterepter.sky.Stmt.Expression;
import com.craftinginterepter.sky.Stmt.If;
import com.craftinginterepter.sky.Stmt.Print;
import com.craftinginterepter.sky.Stmt.Var;
import com.craftinginterepter.sky.Stmt.While;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Sky.runtimeError(error);
        }
    }

    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String) {
                    return (String) left + stringify(right);
                }
                throw new RuntimeError(expr.operator, "Operands must be numbers or strings.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (right.equals(0.0))
                    throw new RuntimeError(expr.operator, "Can't divide by zero.");
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case MODULUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left % (double) right;
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        while (true) {
            try {
                if (!isTruthy(evaluate(stmt.condition))) break;
                execute(stmt.body);
            } catch (BreakException e) {
                break;
            } catch (ContinueException e) {
                continue;
            }
        }
        return null;
    }
    

    @Override
    public Void visitBreakStmt(Break stmt) {
        throw new RuntimeError.BreakException();
    }

    @Override
    public Void visitContinueStmt(Continue stmt) {
        throw new RuntimeError.ContinueException();
    }

    @Override
    public Object visitPrefixExpr(Prefix expr) {
        Object value = evaluate(expr.expression);
        if (expr.operator.type == TokenType.PLUS_PLUS) {
            value = (double) value + 1;
        } else if (expr.operator.type == TokenType.MINUS_MINUS) {
            value = (double) value - 1;
        }
        environment.assign(((Expr.Variable) expr.expression).name, value);
        return value;
    }

    @Override
    public Object visitPostfixExpr(Postfix expr) {
        Object value = evaluate(expr.expression);
        Object result = value; 
        if (expr.operator.type == TokenType.PLUS_PLUS) {
            value = (double) value + 1;
        } else if (expr.operator.type == TokenType.MINUS_MINUS) {
            value = (double) value - 1;
        }
        environment.assign(((Expr.Variable) expr.expression).name, value);
        return result; 
    }

    // helper methods
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt> statements,
            Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private String stringify(Object object) {
        if (object == null) {
            return "null";
        }
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        if (object instanceof String) {
            String str = object.toString();
            str = str.replace("\\t", "\t");
            str = str.replace("\\n", "\n");
            return str;
        }
        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;

        return left.equals(right);
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "[ERROR] Operand must be a number.");
    }

}
