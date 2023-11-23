import components.lexer.Lexer
import components.lexer.Token.TokenType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class LexerTest {
    private val lexer = Lexer()

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
            Pair('=', TokenType.ASSIGN),
            Pair('!', TokenType.NOT),
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
            Pair('=', TokenType.ASSIGN),
            Pair('!', TokenType.NOT),
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

    @Test
    fun testFindSingleCharLiteral() {
        val testCases: List<Pair<TokenType, Char>> = listOf(
            Pair(TokenType.INTEGER_LITERAL, '2'),
            Pair(TokenType.IDENTIFIER, 'a'),
        )

        for ((type, literal) in testCases) {
            val program = literal.toString()
            val tokens = lexer.tokenize(program)

            // LITERAL/IDENTIFIER, END
            assertEquals(2, tokens.size)

            val tok = tokens[0]
            assertEquals(type, tok.type)
            assertEquals(1, tok.length)
            assertEquals(literal.toString(), tok.lexeme)
            assertEquals(0, tok.startPosition.offset)
            assertEquals(0, tok.startPosition.lineIndex)
            assertEquals(0, tok.startPosition.lineOffset)
        }
    }

    @Test
    fun testFindSingleCharLiteralWithWhitespaces() {
        val testCases: List<Pair<TokenType, Char>> = listOf(
            Pair(TokenType.INTEGER_LITERAL, '2'),
            Pair(TokenType.IDENTIFIER, 'a'),
        )

        val whitespaceBlock = "\t\t \n\r\n \r  \n\n \t  "

        for ((type, literal) in testCases) {
            val program = whitespaceBlock + literal.toString() + whitespaceBlock
            val tokens = lexer.tokenize(program)

            // LITERAL/IDENTIFIER, END
            assertEquals(2, tokens.size)

            val tok = tokens[0]
            assertEquals(type, tok.type)
            assertEquals(1, tok.length)
            assertEquals(literal.toString(), tok.lexeme)
            assertEquals(whitespaceBlock.length, tok.startPosition.offset)
        }
    }

    @Test
    fun testFindEqualsOperator() {
        val eq = "=="
        val tokens = lexer.tokenize(eq)

        // EQUALS, END
        assertEquals(2, tokens.size)

        val tok = tokens[0]
        assertEquals(TokenType.EQUALS, tok.type)
        assertEquals(eq.length, tok.length)
        assertEquals(eq, tok.lexeme)
        assertEquals(0, tok.startPosition.offset)
        assertEquals(0, tok.startPosition.lineIndex)
        assertEquals(0, tok.startPosition.lineOffset)
    }

    @Test
    fun testFindMultiCharLiteral() {
        val testCases: List<Pair<TokenType, String>> = listOf(
            Pair(TokenType.INTEGER_LITERAL, "1234324"),
            Pair(TokenType.FLOAT_LITERAL, "123.3213"),
            Pair(TokenType.FLOAT_LITERAL, "1.0"),
            Pair(TokenType.STRING_LITERAL, "\"\""), // empty string literal
            Pair(TokenType.STRING_LITERAL, "\"non-empty string literal\""),
            Pair(TokenType.BOOLEAN_TRUE_LITERAL, "true"),
            Pair(TokenType.BOOLEAN_FALSE_LITERAL, "false"),
            Pair(TokenType.IDENTIFIER, "variableName"),
            Pair(TokenType.IDENTIFIER, "variable_name"),
            Pair(TokenType.IDENTIFIER, "_variable_name_"), // TODO: currently '_' not supported by lexer, need to support
            Pair(TokenType.IDENTIFIER, "var123"),
            Pair(TokenType.IDENTIFIER, "x"),
            Pair(TokenType.IDENTIFIER, "_"),
        )

        for ((type, literal) in testCases) {
            val tokens = lexer.tokenize(literal)

            // LITERAL, END
            assertEquals(2, tokens.size)

            val tok = tokens[0]
            assertEquals(type, tok.type)
            assertEquals(literal.length, tok.length)
            assertEquals(literal, tok.lexeme)
            assertEquals(0, tok.startPosition.offset)
            assertEquals(0, tok.startPosition.lineIndex)
            assertEquals(0, tok.startPosition.lineOffset)
        }
    }

    @Test
    fun testFindMultiCharLiteralWithWhitespaces() {
        val testCases: List<Pair<TokenType, String>> = listOf(
            Pair(TokenType.INTEGER_LITERAL, "1234324"),
            Pair(TokenType.FLOAT_LITERAL, "123.3213"),
            Pair(TokenType.FLOAT_LITERAL, "1.0"),
            Pair(TokenType.STRING_LITERAL, "\"\""), // empty string literal
            Pair(TokenType.STRING_LITERAL, "\"non-empty string literal\""),
            Pair(TokenType.BOOLEAN_TRUE_LITERAL, "true"),
            Pair(TokenType.BOOLEAN_FALSE_LITERAL, "false"),
            Pair(TokenType.IDENTIFIER, "variableName"),
            Pair(TokenType.IDENTIFIER, "variable_name"),
            Pair(TokenType.IDENTIFIER, "_variable_name_"),
            Pair(TokenType.IDENTIFIER, "var123"),
            Pair(TokenType.IDENTIFIER, "x"),
            Pair(TokenType.IDENTIFIER, "_"),
        )

        val whitespaceBlock = "\t\t \n\r\n \r  \n\n \t  "

        for ((type, literal) in testCases) {
            val program = whitespaceBlock + literal + whitespaceBlock
            val tokens = lexer.tokenize(program)

            // LITERAL, END
            assertEquals(2, tokens.size)

            val tok = tokens[0]
            assertEquals(type, tok.type)
            assertEquals(literal.length, tok.length)
            assertEquals(literal, tok.lexeme)
            assertEquals(whitespaceBlock.length, tok.startPosition.offset)
        }
    }


    @Test
    fun testFindKeyword() {
        val testCases: List<Pair<TokenType, String>> = listOf(
            Pair(TokenType.VAR, "var"),
            Pair(TokenType.FUNCTION, "function"),
            Pair(TokenType.IF, "if"),
            Pair(TokenType.FOR, "for"),
            Pair(TokenType.WHILE, "while"),
        )

        for ((type, keyword) in testCases) {
            val tokens = lexer.tokenize(keyword)

            // KEYWORD, END
            assertEquals(2, tokens.size)

            val tok = tokens[0]
            assertEquals(type, tok.type)
            assertEquals(keyword.length, tok.length)
            assertEquals(keyword, tok.lexeme)
            assertEquals(0, tok.startPosition.offset)
            assertEquals(0, tok.startPosition.lineIndex)
            assertEquals(0, tok.startPosition.lineOffset)
        }
    }

    @Test
    fun testFindKeywordWithWhitespaces() {
        val testCases: List<Pair<TokenType, String>> = listOf(
            Pair(TokenType.VAR, "var"),
            Pair(TokenType.FUNCTION, "function"),
            Pair(TokenType.IF, "if"),
            Pair(TokenType.FOR, "for"),
            Pair(TokenType.WHILE, "while"),
        )

        val whitespaceBlock = "\t\t \n\r\n \r  \n\n \t  "

        for ((type, keyword) in testCases) {
            val program = whitespaceBlock + keyword + whitespaceBlock
            val tokens = lexer.tokenize(program)

            // KEYWORD, END
            assertEquals(2, tokens.size)

            val tok = tokens[0]
            assertEquals(type, tok.type)
            assertEquals(keyword.length, tok.length)
            assertEquals(keyword, tok.lexeme)
            assertEquals(whitespaceBlock.length, tok.startPosition.offset)
        }
    }

    @Test
    fun testVariableDefinition() {
        val keyword = "var"
        val identifier = "my_variable_123"
        val op = "="

        val valueLiterals: List<Pair<TokenType, String>> = listOf(
            Pair(TokenType.INTEGER_LITERAL, "123"),
            Pair(TokenType.FLOAT_LITERAL, "12.213"),
            Pair(TokenType.STRING_LITERAL, "\"my string value\""),
            Pair(TokenType.BOOLEAN_TRUE_LITERAL, "true"),
            Pair(TokenType.BOOLEAN_FALSE_LITERAL, "false"),
        )

        for ((type, value) in valueLiterals) {
            val program = "$keyword $identifier$op$value;"
            val tokens = lexer.tokenize(program)

            // VAR, IDENTIFIER, EQUALS, LITERAL, SEMICOLON, END
            assertEquals(6, tokens.size)

            // asserting tokens' types
            assertEquals(TokenType.VAR, tokens[0].type)
            assertEquals(TokenType.IDENTIFIER, tokens[1].type)
            assertEquals(TokenType.ASSIGN, tokens[2].type)
            assertEquals(type, tokens[3].type)
            assertEquals(TokenType.SEMICOLON, tokens[4].type)
            assertEquals(TokenType.END, tokens[5].type)
        }
    }

    @Test
    fun testVariableDeclarationAndFurtherAssignment() {
        val keyword = "var"
        val identifier = "my_var_123"
        val values: List<Pair<TokenType, String>> = listOf(
            Pair(TokenType.INTEGER_LITERAL, "123"),
            Pair(TokenType.FLOAT_LITERAL, "12.213"),
            Pair(TokenType.STRING_LITERAL, "\"my string value\""),
            Pair(TokenType.BOOLEAN_TRUE_LITERAL, "true"),
            Pair(TokenType.BOOLEAN_FALSE_LITERAL, "false"),
        )

        for ((type, literal) in values) {
            val program = "$keyword $identifier;\n\n$identifier=$literal;"
            val tokens = lexer.tokenize(program)

            // KEYWORD, IDENTIFIER, SEMICOLON, IDENTIFIER, EQUALS, LITERAL SEMICOLON
            val types = listOf(
                TokenType.VAR,
                TokenType.IDENTIFIER,
                TokenType.SEMICOLON,
                TokenType.IDENTIFIER,
                TokenType.ASSIGN,
                type,
                TokenType.SEMICOLON,
            )

            assertEquals(types.size, tokens.size)

            for (i in types.indices) {
                assertEquals(types[i], tokens[i].type)
            }
        }
    }

    @Test
    fun testFunctionDefinition() {
        val keyword = "function"
        val identifier = "my_function_123"
        val arg1 = "arg1"
        val arg2 = "arg2"

        val program = "$keyword $identifier ($arg1, $arg2) {\n}"
        val tokens = lexer.tokenize(program)

        // KEYWORD, IDENTIFIER, OPEN_PAREN, IDENTIFIER, COMMA, IDENTIFIER, CLOSE_PAREN, OPEN_CURLY, CLOSE_CURLY, END
        val types = listOf(
            TokenType.FUNCTION,
            TokenType.IDENTIFIER,
            TokenType.OPEN_PAREN,
            TokenType.IDENTIFIER,
            TokenType.COMMA,
            TokenType.IDENTIFIER,
            TokenType.CLOSE_PAREN,
            TokenType.OPEN_CURLY,
            TokenType.CLOSE_CURLY,
            TokenType.END,
        )

        assertEquals(types.size, tokens.size)

        for (i in types.indices) {
            assertEquals(types[i], tokens[i].type)
        }
    }

    @Test
    fun testConditionsWithIfElse() {
        val program = "if (2+3==4){\n}\n" +
                      "else if (true) {\n}\n" +
                      " else {\n}"
        val tokens = lexer.tokenize(program)

        // IF, OPEN_PAREN, LITERAL, PLUS, LITERAL, EQUALS, LITERAL, CLOSE_PAREN, OPEN_CURLY, CLOSE_CURLY
        // ELSE, IF, OPEN_PAREN, BOOL, CLOSE_PAREN, OPEN_CURLY, CLOSE_CURLY,
        // ELSE OPEN_CURLY, CLOSE_CURLY

        val types = listOf(
            TokenType.IF,
            TokenType.OPEN_PAREN,
            TokenType.INTEGER_LITERAL,
            TokenType.PLUS,
            TokenType.INTEGER_LITERAL,
            TokenType.EQUALS,
            TokenType.INTEGER_LITERAL,
            TokenType.ELSE,
            TokenType.IF,
            TokenType.OPEN_PAREN,
            TokenType.BOOLEAN_TRUE_LITERAL,
            TokenType.CLOSE_PAREN,
            TokenType.OPEN_CURLY,
            TokenType.CLOSE_CURLY,
            TokenType.ELSE,
            TokenType.OPEN_CURLY,
            TokenType.CLOSE_CURLY,
        )

        assertEquals(types.size, tokens.size)

        for (i in types.indices) {
            assertEquals(types[i], tokens[i].type)
        }
    }

    @Test
    fun testForLoopDefinition() {
        val program = "for(var i = 0; i < N; i = i + 1)\n{\n}\n"
        val tokens = lexer.tokenize(program)

        // FOR, OPEN_PAREN, VAR, IDENTIFIER, ASSIGN, LITERAL, SEMICOLON,
        // IDENTIFIER, LESS, IDENTIFIER, SEMICOLON,
        // IDENTIFIER, ASSIGN, IDENTIFIER, PLUS, LITERAL, CLOSE_PAREN
        // OPEN_CURLY, CLOSE_CURLY
        val types = listOf(
            TokenType.FOR,
            TokenType.OPEN_PAREN,
            TokenType.VAR,
            TokenType.IDENTIFIER,
            TokenType.ASSIGN,
            TokenType.INTEGER_LITERAL,
            TokenType.SEMICOLON,
            TokenType.IDENTIFIER,
            TokenType.LESS,
            TokenType.IDENTIFIER,
            TokenType.SEMICOLON,
            TokenType.IDENTIFIER,
            TokenType.ASSIGN,
            TokenType.IDENTIFIER,
            TokenType.PLUS,
            TokenType.INTEGER_LITERAL,
            TokenType.CLOSE_PAREN,
            TokenType.OPEN_CURLY,
            TokenType.CLOSE_CURLY
        )

        assertEquals(types.size, tokens.size)

        for (i in types.indices) {
            assertEquals(types[i], tokens[i].type)
        }
    }

    @Test
    fun whileLoopDefinition() {
        val program = "  while(i > 10) {\n}\n"
        val tokens = lexer.tokenize(program)

        // WHILE, OPEN_PAREN, IDENTIFIER, GREATER, INTEGER, CLOSE_PAREN, OPEN_CURLY, CLOSE_CURLY
        val types = listOf(
            TokenType.WHILE,
            TokenType.OPEN_PAREN,
            TokenType.IDENTIFIER,
            TokenType.GREATER,
            TokenType.INTEGER_LITERAL,
            TokenType.CLOSE_PAREN,
            TokenType.OPEN_CURLY,
            TokenType.CLOSE_CURLY,
        )

        assertEquals(types.size, tokens.size)

        for (i in types.indices) {
            assertEquals(types[i], tokens[i].type)
        }
    }
}
