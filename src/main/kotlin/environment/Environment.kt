package environment

class Environment() {

    val store: MutableMap<String, types.Object> = mutableMapOf()

    var outer: Environment? = null

    constructor(outerEnv: Environment? = null): this() {
        outer = outerEnv
    }

    fun get(name: String): types.Object? {
        val obj = store.get(name)
        if (obj == null) {
            return outer?.get(name)
        }
        return obj
    }

    fun set(name: String, value: types.Object): types.Object {
        store.put(name, value)
        return value
    }
}
