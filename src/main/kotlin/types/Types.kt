package types

import ast.BlockStatement
import ast.Identifier
import environment.Environment

typealias ObjectType = String

val TRUE = types.Boolean(true)
val FALSE = types.Boolean(false)
val NULL = types.Null()

val INTEGER_OBJ = "INTEGER"
val BOOL_OBJ = "BOOL"
val NULL_OBJ = "NULL"
val RETURN_VALUE_OBJ = "RETURN_VALUE"
val ERROR_OBJ = "ERROR"
val FUNCTION_OBJ = "FUNCTION"
val STRING_OBJ = "STRING"
val BUILTIN_OBJ = "BUILTIN"

interface Object {
    fun type(): ObjectType
    fun inspect(): String
}

class Integer(val value: Int) : Object {

    override fun type(): ObjectType {
        return INTEGER_OBJ
    }

    override fun inspect(): String {
        return "$value"
    }

}

class Boolean(val value: kotlin.Boolean) : Object {

    override fun type(): ObjectType {
        return BOOL_OBJ
    }

    override fun inspect(): String {
        return "$value"
    }

}

class Str(val value: String) : Object {

    override fun type(): ObjectType {
        return STRING_OBJ
    }

    override fun inspect(): String {
        return value
    }

}

class Null : Object {

    override fun type(): ObjectType {
        return NULL_OBJ
    }

    override fun inspect(): String {
        return "null"
    }

}

class ReturnValue(val value: Object) : Object {

    override fun type(): ObjectType {
        return RETURN_VALUE_OBJ
    }

    override fun inspect(): String {
        return value.inspect()
    }

}

class Error(val messge: String) : Object {

    override fun type(): ObjectType {
        return NULL_OBJ
    }

    override fun inspect(): String {
        return "ERROR:$messge"
    }

}

class Function(val parameters: List<Identifier>, val body: BlockStatement, val env: Environment) : Object {

    override fun type(): ObjectType {
        return FUNCTION_OBJ
    }

    override fun inspect(): String {
        val buffer = StringBuilder()

        buffer.append("fn")
        buffer.append("(")
        buffer.append(parameters.joinToString { it.toString() })
        buffer.append(")\n")
        buffer.append(body.toString())
        buffer.append("\n")

        return buffer.toString()
    }

}

typealias BuiltinFunction = (args: List<Object>) -> Object

class Builtin(val builtinFn: BuiltinFunction) : Object {

    override fun type(): ObjectType {
        return BUILTIN_OBJ
    }

    override fun inspect(): String {
        return "builtin function"
    }

}
