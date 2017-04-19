package types

import kotlin.Boolean

typealias ObjectType = String

val INTEGER_OBJ = "INTEGER"
val BOOL_OBJ = "BOOL"
val NULL_OBJ = "NULL"

interface Object {
    fun type(): ObjectType
    fun inspect(): String
}

class Integer(val value: Int): Object{

    override fun type(): ObjectType {
        return INTEGER_OBJ
    }

    override fun inspect(): String {
        return "$value"
    }

}

class Boolean(val value: Boolean): Object{

    override fun type(): ObjectType {
        return BOOL_OBJ
    }

    override fun inspect(): String {
        return "$value"
    }

}

class Null: Object{

    override fun type(): ObjectType {
        return NULL_OBJ
    }

    override fun inspect(): String {
        return "null"
    }

}
