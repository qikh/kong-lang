package evaluator

import types.Builtin

val len: (args: List<types.Object>) -> types.Object = {
    if (it.size != 1) {
        types.Error("wrong number of arguments.")
    } else {
        val arg = it[0]
        if (arg is types.Str) {
            types.Integer(arg.value.length)
        } else {
            types.Error("argument to `len` not supported, got ${arg.type()}")
        }
    }
}

val builtinFunctions = mapOf(
    "len" to Builtin(len)
)
