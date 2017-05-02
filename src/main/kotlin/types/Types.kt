package types

typealias ObjectType = String

val TRUE = types.Boolean(true)
val FALSE = types.Boolean(false)
val NULL = types.Null()

val INTEGER_OBJ = "INTEGER"
val BOOL_OBJ = "BOOL"
val NULL_OBJ = "NULL"
val RETURN_VALUE_OBJ = "RETURN_VALUE"
val ERROR_OBJ = "ERROR"

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
