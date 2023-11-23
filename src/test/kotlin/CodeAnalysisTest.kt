import components.lexer.Lexer
import components.lexer.Token.TokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class CodeAnalysisTest {
    private val lexer = Lexer()

    @Test
    fun testEmptyInput() {
        val tokens = lexer.tokenize("")
        assertEquals(1, tokens.size)
        assertEquals(TokenType.END, tokens.first().type)
    }

    @Test
    fun testSemicolons() {
        val tokens = lexer.tokenize(";;")

        // SEMICOLON, SEMICOLON, END
        assertEquals(3, tokens.size)

        assertEquals(TokenType.SEMICOLON, tokens[0].type)
        assertEquals(TokenType.SEMICOLON, tokens[1].type)
        assertEquals(TokenType.END, tokens[2].type)
    }


    @Test
    fun testEmptyInputWithWhitespaces() {
        val tokens = lexer.tokenize("   \n\n" + System.lineSeparator() + "\t  \t\n  \n\r\n")
        assertEquals(1, tokens.size)
        assertEquals(TokenType.END, tokens.first().type)
    }

}