package environment

class Environment(val store: MutableMap<String, types.Object> = mutableMapOf()) {

    fun get(name: String): types.Object? {
        return store.get(name)
    }

    fun set(name: String, value: types.Object): types.Object {
        store.put(name, value)
        return value
    }
}
