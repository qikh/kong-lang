package repl

import environment.Environment
import evaluator.Eval
import jline.console.ConsoleReader
import lexer.Lexer
import parser.Parser
import java.io.PrintWriter
import java.io.Writer

class Repl {
    val PROMPT = ">> "

    fun Start() {
        val env = Environment()

        val reader = ConsoleReader()
        reader.bellEnabled = false
        reader.prompt = PROMPT

        val writer = PrintWriter(reader.output)

        while (true) {
            val line = reader.readLine()
            if (line == "quit") {
                writer.println()
                break
            }
            val lexer = Lexer(line)

            val p = Parser(lexer)

            val program = p.parseProgram()
            if (p.errors.size > 0) {
                printParseErrors(writer, p.errors)
            }

            val evaluated = Eval(program, env)
            writer.println(evaluated.inspect())
        }

    }

    private fun printParseErrors(out: Writer, errors: List<String>) {
        errors.forEach { out.write("\t" + it + "\n") }
    }
}
