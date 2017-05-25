package antlr

class NodeValue : Comparable<NodeValue> {

    private var value: Any? = null

    private constructor() {
        // private constructor: only used for NULL and VOID
        value = Any()
    }

    constructor(v: Any?) {
        if (v == null) {
            throw RuntimeException("v == null")
        }
        value = v
        // only accept boolean, list, number or string types
        if (!(isBoolean || isList || isNumber || isString)) {
            throw RuntimeException("invalid data type: " + v + " (" + v.javaClass + ")")
        }
    }

    fun asBoolean(): Boolean {
        return value as Boolean
    }

    fun asDouble(): Double {
        return (value as Number).toDouble()
    }

    fun asInt(): Int {
        return (value as Number).toInt()
    }

    fun asList(): MutableList<NodeValue> {
        return value as MutableList<NodeValue>
    }

    fun asString(): String {
        return value as String
    }

    //@Override
    override fun compareTo(that: NodeValue): Int {
        if (this.isNumber && that.isNumber) {
            if (this == that) {
                return 0
            } else {
                return this.asDouble()!!.compareTo(that.asDouble()!!)
            }
        } else if (this.isString && that.isString) {
            return this.asString().compareTo(that.asString())
        } else {
            throw RuntimeException("illegal expression: can't compare `" + this + "` to `" + that + "`")
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === VOID || o === VOID) {
            throw RuntimeException("can't use VOID: " + this + " ==/!= " + o)
        }
        if (this === o) {
            return true
        }
        if (o == null || this.javaClass != o.javaClass) {
            return false
        }
        val that = o as NodeValue?
        if (this.isNumber && that!!.isNumber) {
            val diff = Math.abs(this.asDouble()!! - that.asDouble()!!)
            return diff < 0.00000000001
        } else {
            return this.value == that!!.value
        }
    }

    override fun hashCode(): Int {
        return value!!.hashCode()
    }

    val isBoolean: Boolean
        get() = value is Boolean

    val isNumber: Boolean
        get() = value is Number

    val isList: Boolean
        get() = value is List<*>

    val isNull: Boolean
        get() = this === NULL

    val isVoid: Boolean
        get() = this === VOID

    val isString: Boolean
        get() = value is String

    override fun toString(): String {
        return if (isNull) "NULL" else if (isVoid) "VOID" else value.toString()
    }

    companion object {

        val NULL = NodeValue()
        val VOID = NodeValue()
    }
}
