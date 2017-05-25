package antlr

import java.util.HashMap

class Scope {
    private var parent: Scope? = null
    private var variables = mutableMapOf<String, NodeValue>()

    constructor()

    constructor(p: Scope?) {
        this.parent = p
    }

    fun assignParam(key: String, value: NodeValue) {
        variables.put(key, value)
    }

    fun assign(key: String, value: NodeValue) {
        if (resolve(key) != null) {
            // There is already such a variable, re-assign it
            this.reAssign(key, value)
        } else {
            // A newly declared variable
            variables.put(key, value)
        }
    }

    fun copy(): Scope {
        // Create a shallow copy of this scope. Used in case functions are
        // are recursively called. If we wouldn't create a copy in such cases,
        // changing the variables would result in changes ro the Maps from
        // other "recursive scopes".
        val s = Scope()
        s.variables = HashMap(this.variables)
        s.parent = this.parent
        return s
    }

    val isGlobalScope: Boolean
        get() = parent == null

    fun parent(): Scope? {
        return parent
    }

    private fun reAssign(identifier: String, value: NodeValue) {
        if (variables.containsKey(identifier)) {
            // The variable is declared in this scope
            variables.put(identifier, value)
        } else if (parent != null) {
            // The variable was not declared in this scope, so let
            // the parent scope re-assign it
            parent?.reAssign(identifier, value)
        }
    }

    fun resolve(key: String): NodeValue? {
        val value = variables[key]
        if (value != null) {
            // The variable resides in this scope
            return value
        } else if (!isGlobalScope) {
            // Let the parent scope look for the variable
            return parent?.resolve(key)
        } else {
            // Unknown variable
            return null
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for ((key, value) in variables) {
            sb.append("$key->$value,")
        }
        return sb.toString()
    }
}// only for the global scope, the parent is null
