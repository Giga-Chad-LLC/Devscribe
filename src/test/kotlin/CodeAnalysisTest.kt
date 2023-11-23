import components.lexer.Lexer
import components.lexer.Token
import components.lexer.Token.TokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class CodeAnalysisTest {
    private val lexer = Lexer()

    // Whitespaces
    @Test
    fun testEmptyInput() {
        val program = ""
        val tokens = lexer.tokenize(program)

        assertEquals(1, tokens.size)
        assertEquals(TokenType.END, tokens.first().type)
        assertEquals(program.length, tokens.first().startPosition.offset)
        assertEquals(0, tokens.first().startPosition.lineIndex)
        assertEquals(0, tokens.first().startPosition.lineOffset)
    }

    @Test
    fun testEmptyInputWithWhitespaces() {
        val program = "   \n\n" + System.lineSeparator() + "\t  \t\n  \n\r\n"
        val lineIndex = program.count { ch -> ch == '\n' }
        val tokens = lexer.tokenize(program)

        assertEquals(1, tokens.size)

        val token = tokens.first()
        assertEquals(TokenType.END, token.type)
        assertEquals(program.length, token.startPosition.offset)
        assertEquals(lineIndex, token.startPosition.lineIndex)
    }

    // General
    @Test
    fun testSingleCharTokenAtZeroIndexPosition() {
        val program = "+  \t\n  \r\n \t"
        val tokens = lexer.tokenize(program)

        // PLUS, END
        assertEquals(2, tokens.size)
        val plus = tokens[0]
        val end = tokens[1]

        // plus
        assertEquals(TokenType.PLUS, plus.type)
        assertEquals(1, plus.length)
        assertEquals("+", plus.lexeme)
        assertEquals(0, plus.startPosition.offset)
        assertEquals(0, plus.startPosition.lineIndex)
        assertEquals(0, plus.startPosition.lineOffset)

        // end
        assertEquals(TokenType.END, end.type)
        assertEquals(program.length, end.startPosition.offset)

        val correctLineIndex = program.count { ch -> ch == '\n' }
        val lastLineLength = program.length - program.lastIndexOf('\n')

        assertEquals(correctLineIndex, end.startPosition.lineIndex)
        assertEquals(lastLineLength, end.startPosition.lineOffset)
    }

    @Test
    fun testSingleCharTokenAtLastIndexPosition() {
        val program = "  \t\n  \r\n \t  +"
        val tokens = lexer.tokenize(program)

        // PLUS, END
        assertEquals(2, tokens.size)
        val plus = tokens[0]
        val end = tokens[1]

        val correctLineIndex = program.count { ch -> ch == '\n' }
        val lastLineLength = program.length - program.lastIndexOf('\n')

        // plus
        assertEquals(TokenType.PLUS, plus.type)
        assertEquals(1, plus.length)
        assertEquals("+", plus.lexeme)
        assertEquals(program.lastIndex, plus.startPosition.offset)
        assertEquals(correctLineIndex, plus.startPosition.lineIndex)
        assertEquals(lastLineLength - 1 /* because '+' takes the last position in the line */,
            plus.startPosition.lineOffset)

        // end
        assertEquals(TokenType.END, end.type)
        assertEquals(program.length, end.startPosition.offset)

        assertEquals(correctLineIndex, end.startPosition.lineIndex)
        assertEquals(lastLineLength, end.startPosition.lineOffset)
    }

    @Test
    fun testTwoConsecutiveSingleCharTokens() {
        val testCases: List<Pair<Char, TokenType>> = listOf(
            Pair(';', TokenType.SEMICOLON),
            Pair(',', TokenType.COMMA),
            Pair('{', TokenType.OPEN_CURLY),
            Pair('}', TokenType.CLOSE_CURLY),
            Pair('[', TokenType.OPEN_PAREN),
            Pair(']', TokenType.CLOSE_PAREN),
            Pair('+', TokenType.PLUS),
            Pair('-', TokenType.MINUS),
            Pair('/', TokenType.DIVIDE),
            Pair('*', TokenType.MULTIPLY),
            Pair('%', TokenType.MODULO),
            Pair('=', TokenType.EQUALS),
        )

        for ((delimiter, type) in testCases) {
            val program = delimiter.toString() + delimiter.toString()
            val tokens = lexer.tokenize(program)

            // DELIMITER, DELIMITER, END
            assertEquals(3, tokens.size)

            assertEquals(type, tokens[0].type)
            assertEquals(0, tokens[0].startPosition.offset)
            assertTrue(tokens[0].length == tokens[0].lexeme.length)
            assertTrue(tokens[0].startPosition.lineOffset == tokens[0].startPosition.offset)

            assertEquals(type, tokens[1].type)
            assertEquals(1, tokens[1].startPosition.offset)
            assertTrue(tokens[1].length == tokens[1].lexeme.length)
            assertTrue(tokens[1].startPosition.lineOffset == tokens[1].startPosition.offset)

            assertEquals(TokenType.END, tokens[2].type)
            assertEquals(2, tokens[2].startPosition.offset)
        }
    }

    @Test
    fun testCorrectSingleCharTokensPositions() {
        val testCases: List<Pair<Char, TokenType>> = listOf(
            Pair(';', TokenType.SEMICOLON),
            Pair(',', TokenType.COMMA),
            Pair('{', TokenType.OPEN_CURLY),
            Pair('}', TokenType.CLOSE_CURLY),
            Pair('[', TokenType.OPEN_PAREN),
            Pair(']', TokenType.CLOSE_PAREN),
            Pair('+', TokenType.PLUS),
            Pair('-', TokenType.MINUS),
            Pair('/', TokenType.DIVIDE),
            Pair('*', TokenType.MULTIPLY),
            Pair('%', TokenType.MODULO),
            Pair('=', TokenType.EQUALS),
        )

        val whitespaceBlock = "  \t\r\n \n\t  "

        for ((lexeme, type) in testCases) {
            val program = whitespaceBlock + lexeme
            val tokens = lexer.tokenize(program)

            // DELIMITER, END
            assertEquals(2, tokens.size)

            val delimiter = tokens[0]
            val end = tokens[1]

            // delimiter
            assertEquals(type, delimiter.type)
            assertEquals(1, delimiter.length)
            assertEquals(lexeme.toString(), delimiter.lexeme)
            assertEquals(whitespaceBlock.length, delimiter.startPosition.offset)

            // end
            assertEquals(TokenType.END, end.type)
            assertEquals(whitespaceBlock.length + 1, end.startPosition.offset)
        }
    }

    // Delimiters
    @Test
    fun testBracketPairsWithWhitespaces() {
        data class Bracket(
            val lexeme: Char,
            val type: TokenType,
        )

        val testCases: List<Pair<Bracket, Bracket>> = listOf(
            Pair(
                Bracket('{', TokenType.OPEN_CURLY),
                Bracket('}', TokenType.CLOSE_CURLY),
            ),
            Pair(
                Bracket('[', TokenType.OPEN_PAREN),
                Bracket(']', TokenType.CLOSE_PAREN),
            )
        )

        val whitespaceBlock = "   \n\t\t \r\n  \t "

        for ((openBracket, closeBracket) in testCases) {
            val program = whitespaceBlock + openBracket.lexeme.toString() +
                          whitespaceBlock + closeBracket.lexeme.toString() + whitespaceBlock

            val tokens = lexer.tokenize(program)

            // OPEN_BRACKET, CLOSE_BRACKET, END
            assertEquals(3, tokens.size)
            val open = tokens[0]
            val close = tokens[1]
            val end = tokens[2]

            assertEquals(openBracket.type, open.type)
            assertEquals(openBracket.lexeme.toString(), open.lexeme)

            assertEquals(closeBracket.type, close.type)
            assertEquals(closeBracket.lexeme.toString(), close.lexeme)

            assertEquals(TokenType.END, end.type)
        }
    }

    // Operators

    // Literals
    @Test
    fun testFindSingleDigitIntegerLiteral() {
        val program = "2"
        val tokens = lexer.tokenize(program)

        // INTEGER_LITERAL, END
        assertEquals(2, tokens.size)

        val integer = tokens[0]
        assertEquals(TokenType.INTEGER_LITERAL, integer.type)
        assertEquals(1, integer.length)
        assertEquals("2", integer.lexeme)
        assertEquals(0, integer.startPosition.offset)
        assertEquals(0, integer.startPosition.lineIndex)
        assertEquals(0, integer.startPosition.lineOffset)
    }

    @Test
    fun testFindSingleDigitIntegerLiteralWithWhitespaces() {
        val whitespaceBlock = "\t\t \n\r\n \r  \n\n \t  "
        val program = whitespaceBlock + "2" + whitespaceBlock
        val tokens = lexer.tokenize(program)

        // INTEGER_LITERAL, END
        assertEquals(2, tokens.size)

        val integer = tokens[0]
        assertEquals(TokenType.INTEGER_LITERAL, integer.type)
        assertEquals(1, integer.length)
        assertEquals("2", integer.lexeme)
        assertEquals(whitespaceBlock.length, integer.startPosition.offset)
    }

    @Test
    fun testFindMultiDigitIntegerLiteral() {
        val literal = "232423109"
        val tokens = lexer.tokenize(literal)

        // INTEGER_LITERAL, END
        assertEquals(2, tokens.size)

        val integer = tokens[0]
        assertEquals(TokenType.INTEGER_LITERAL, integer.type)
        assertEquals(literal.length, integer.length)
        assertEquals(literal, integer.lexeme)
        assertEquals(0, integer.startPosition.offset)
        assertEquals(0, integer.startPosition.lineIndex)
        assertEquals(0, integer.startPosition.lineOffset)
    }

    @Test
    fun testFindMultiDigitIntegerLiteralWithWhitespaces() {
        val whitespaceBlock = "\t\t \n\r\n \r  \n\n \t  "
        val literal = "232423109"
        val program = whitespaceBlock + literal + whitespaceBlock
        val tokens = lexer.tokenize(program)

        // INTEGER_LITERAL, END
        assertEquals(2, tokens.size)

        val integer = tokens[0]
        assertEquals(TokenType.INTEGER_LITERAL, integer.type)
        assertEquals(literal.length, integer.length)
        assertEquals(literal, integer.lexeme)
        assertEquals(whitespaceBlock.length, integer.startPosition.offset)
    }

    @Test
    fun testFindFloatLiteral() {
        val literal = "12.230"
        val tokens = lexer.tokenize(literal)

        // FLOAT_LITERAL, END
        assertEquals(2, tokens.size)

        val integer = tokens[0]
        assertEquals(TokenType.FLOAT_LITERAL, integer.type)
        assertEquals(literal.length, integer.length)
        assertEquals(literal, integer.lexeme)
        assertEquals(0, integer.startPosition.offset)
        assertEquals(0, integer.startPosition.lineIndex)
        assertEquals(0, integer.startPosition.lineOffset)
    }

    /*@Test
    fun testFindFloatLiteralWithWhitespace() {
        val literal = "12.230"
        val tokens = lexer.tokenize(literal)

        // FLOAT_LITERAL, END
        assertEquals(2, tokens.size)

        val integer = tokens[0]
        assertEquals(TokenType.FLOAT_LITERAL, integer.type)
        assertEquals(literal.length, integer.length)
        assertEquals(literal, integer.lexeme)
        assertEquals(0, integer.startPosition.offset)
        assertEquals(0, integer.startPosition.lineIndex)
        assertEquals(0, integer.startPosition.lineOffset)
    }*/

    // Keywords

}
/*
    // Keywords
    VAR, FUNCTION, IF, FOR, WHILE,

    // Delimiters +
    SEMICOLON, COMMA, OPEN_CURLY, CLOSE_CURLY, OPEN_PAREN, CLOSE_PAREN,

    // Operators +
    PLUS, MINUS, DIVIDE, MULTIPLY, MODULO, EQUALS,

    // Literals
    INTEGER_LITERAL, FLOAT_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL, IDENTIFIER,

    // Auxiliary
    END, INVALID,
*/