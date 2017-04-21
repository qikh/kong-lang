package repl

import evaluator.Eval
import lexer.Lexer
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import parser.Parser
import java.io.Writer

class Repl {
    val PROMPT = ">> "

    fun Start() {
        // Suppress warning message.
        java.util.logging.LogManager
            .getLogManager().
            readConfiguration(javaClass.getResourceAsStream("/logging.properties"))

        val terminal = TerminalBuilder.builder()
            .system(true)
            .build()
        val lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .appName("mini-vm")
            .build()
        val writer = terminal.writer()

        while (true) {
            val line = lineReader.readLine(PROMPT)
            if (line == "quit") {
                writer.write("Bye!")
                writer.write("\n")
                break
            }
            val lexer = Lexer(line)

            val p = Parser(lexer)

            val program = p.parseProgram()
            if (p.errors.size > 0) {
                printParseErrors(writer, p.errors)
            }

            val evaluated = Eval(program)
            writer.write(evaluated.inspect())
            writer.write("\n")
        }

    }

    private fun printParseErrors(out: Writer, errors: List<String>) {
        errors.forEach { out.write("\t" + it + "\n") }
    }
}
