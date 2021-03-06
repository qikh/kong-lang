package evaluator

import ast.*
import environment.Environment
import types.*
import types.Function

fun Eval(node: Node, env: Environment): types.Object {
    when (node) {
        is Program -> return evalProgram(node, env)
        is ExpressionStatement -> return Eval(node.expression, env)
        is IntegerLiteral -> return types.Integer(node.value)
        is BooleanLiteral -> return nativeBoolToBoolean(node.value)
        is StringLiteral -> return types.Str(node.value)
        is PrefixExpression -> {
            val rightVal = Eval(node.right, env)
            if (isError(rightVal)) {
                return rightVal
            }
            return evalPrefixExpression(node.operator, rightVal)
        }
        is InfixExpression -> {
            val leftVal = Eval(node.left, env)
            if (isError(leftVal)) {
                return leftVal
            }
            val rightVal = Eval(node.right, env)
            if (isError(rightVal)) {
                return rightVal
            }
            return evalInfixExpression(node.operator, leftVal, rightVal)
        }
        is BlockStatement -> return evalBlockStatement(node, env)
        is IfExpression -> return evalIfExpression(node, env)
        is ReturnStatement -> {
            val value = Eval(node.returnValue, env)
            if (isError(value)) {
                return value
            }
            return ReturnValue(value)
        }
        is LetStatement -> {
            val value = Eval(node.value, env)

            if (isError(value)) {
                return value
            }

            return env.set(node.name.value, value)
        }
        is Identifier -> {
            return evalIdentifier(node, env)
        }
        is FunctionLiteral -> {
            val params = node.parameters
            val body = node.body

            return Function(params, body, env)
        }
        is CallExpression -> {
            val function = Eval(node.function, env)

            if (isError(function)) {
                return function
            }

            val args = evalExpressions(node.arguments, env)
            if (args.size == 1 && isError(args[0])) {
                return args[0]
            }

            return applyFunction(function, args)
        }
        else -> return NULL
    }
}

fun applyFunction(function: Object, args: List<Object>): Object {

    when (function) {
        is Function -> {
            val extendedEnv = extendedFunctionEnv(function, args)
            val evaluated = Eval(function.body, extendedEnv)
            return unwrapReturnValue(evaluated)
        }
        is Builtin -> {
            return function.builtinFn.invoke(args)
        }
        else -> return Error("not a function: ${function.type()}")
    }
}

fun extendedFunctionEnv(function: Function, args: List<Object>): Environment {

    val env = Environment(function.env)

    for (i in 0..function.parameters.size - 1) {
        env.set(function.parameters[i].value, args[i])
    }

    return env
}

fun unwrapReturnValue(obj: Object): Object {
    if (obj is ReturnValue) {
        return obj.value
    }
    return obj
}

fun evalExpressions(arguments: List<Expression>, env: Environment): List<types.Object> {
    val result = mutableListOf<types.Object>()
    arguments.forEach {
        val evaluated = Eval(it, env)
        if (isError(evaluated)) {
            return listOf(evaluated)
        }
        result.add(evaluated)
    }
    return result.toList()
}

fun evalIdentifier(node: Identifier, env: Environment): Object {
    val ident = env.get(node.value)
    if (ident != null) {
        return ident
    }

    val builtin = builtinFunctions.get(node.value)
    if (builtin != null) {
        return builtin
    }

    return Error("identifier not found: " + node.value)
}

fun isError(obj: types.Object): Boolean {
    if (obj != NULL) {
        return obj.type() == ERROR_OBJ
    }
    return false
}

fun evalBlockStatement(node: BlockStatement, env: Environment): Object {
    var result: types.Object = NULL

    for (statement in node.statements) {
        result = Eval(statement, env)

        if (result != NULL) {
            if (result.type() == RETURN_VALUE_OBJ || result.type() == ERROR_OBJ) {
                return result
            }
        }
    }

    return result
}

fun evalProgram(node: Program, env: Environment): Object {
    var result: types.Object = NULL

    for (statement in node.statements) {
        result = Eval(statement, env)

        when (result) {
            is ReturnValue -> return result.value
            is Error -> return result
        }

    }

    return result
}

fun evalIfExpression(node: IfExpression, env: Environment): Object {
    val condition = Eval(node.condition, env)

    if (isError(condition)) {
        return condition
    }

    if (isTruth(condition)) {
        return Eval(node.consequence, env)
    } else if (node.alternative != null) {
        return Eval(node.alternative, env)
    } else {
        return NULL
    }
}

fun isTruth(condition: Object): Boolean {
    when (condition) {
        NULL -> return false
        TRUE -> return true
        FALSE -> return false
        else -> return true
    }
}

fun evalInfixExpression(operator: String, left: Object, right: Object): Object {
    if (left.type() == types.INTEGER_OBJ && right.type() == types.INTEGER_OBJ) {
        return evalIntegerInfixExpression(operator, left, right)
    } else if (left.type() != right.type()) {
        return types.Error("type mismatch: ${left.type()} $operator ${right.type()}")
    } else if (left.type() == types.STRING_OBJ && right.type() == types.STRING_OBJ) {
        return evalStringInfixExpression(operator, left, right)
    } else if (operator == "==") {
        return nativeBoolToBoolean(left == right)
    } else if (operator == "!=") {
        return nativeBoolToBoolean(left != right)
    } else {
        return types.Error("unknown operator: ${left.type()} $operator ${right.type()}")
    }
}

fun evalIntegerInfixExpression(operator: String, left: Object, right: Object): Object {
    val leftVal = (left as Integer).value
    val rightVal = (right as Integer).value

    when (operator) {
        "+" -> return Integer(leftVal + rightVal)
        "-" -> return Integer(leftVal - rightVal)
        "*" -> return Integer(leftVal * rightVal)
        "/" -> return Integer(leftVal / rightVal)
        "<" -> return nativeBoolToBoolean(leftVal < rightVal)
        ">" -> return nativeBoolToBoolean(leftVal > rightVal)
        "==" -> return nativeBoolToBoolean(leftVal == rightVal)
        "!=" -> return nativeBoolToBoolean(leftVal != rightVal)
        else -> return types.Error("unknown operator: ${left.type()} $operator ${right.type()}")
    }
}

fun evalStringInfixExpression(operator: String, left: Object, right: Object): Object {
    val leftVal = (left as Str).value
    val rightVal = (right as Str).value

    when (operator) {
        "+" -> return Str(leftVal + rightVal)
        "<" -> return nativeBoolToBoolean(leftVal < rightVal)
        ">" -> return nativeBoolToBoolean(leftVal > rightVal)
        "==" -> return nativeBoolToBoolean(leftVal == rightVal)
        "!=" -> return nativeBoolToBoolean(leftVal != rightVal)
        else -> return types.Error("unknown operator: ${left.type()} $operator ${right.type()}")
    }
}

fun evalPrefixExpression(operator: String, right: Object): Object {
    when (operator) {
        "!" -> return evalBangOperatorExpression(right)
        "-" -> return evalMinusPrefixOperatorExpression(right)
        else -> return types.Error("unknown operator: $operator ${right.type()}")
    }
}

fun evalMinusPrefixOperatorExpression(right: Object): Object {
    if (right.type() != INTEGER_OBJ) {
        return types.Error("unknown operator: -${right.type()}")
    }

    val value = (right as types.Integer).value
    return types.Integer(-value)
}

fun evalBangOperatorExpression(right: Object): Object {
    when (right) {
        TRUE -> return FALSE
        FALSE -> return TRUE
        NULL -> return TRUE
        else -> return FALSE
    }

}

fun nativeBoolToBoolean(value: Boolean): types.Boolean {
    if (value) {
        return TRUE
    } else {
        return FALSE
    }

}
