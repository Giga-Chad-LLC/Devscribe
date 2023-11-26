package components.lexer


private fun Char.isIdentifierCharacter(): Boolean {
    val ch = this
    return ch.isLetterOrDigit() || (ch == '_')
}

class Lexer {
    private data class Context(
        val program: String,
        var currentIndex: Int,
        var currentPosition: Position
    )

    fun tokenize(program: String): ArrayList<Token> {
        val tokens = ArrayList<Token>()
        val context = Context(program, 0, Position(0, 0, 0))

        while (context.currentIndex < context.program.length) {
            val token = getNextToken(context)
            if (token != null) {
                tokens.add(token)
            }
        }

        val end = Token(
            type = Token.TokenType.END,
            startPosition = context.currentPosition.copy(),
            length = 0,
            lexeme = "",
        )
        tokens.add(end)

        return tokens
    }

    private fun getNextToken(context: Context): Token? {
        skipWhitespaces(context)

        context.let {
            if (it.currentIndex >= it.program.length) {
                return null
            }

            val ch = it.program[it.currentIndex]
            // Delimiters
            if (ch == ';') {
                return getSingleCharacterToken(context, Token.TokenType.SEMICOLON)
            }
            else if (ch == ',') {
                return getSingleCharacterToken(context, Token.TokenType.COMMA)
            }
            else if (ch == '{') {
                return getSingleCharacterToken(context, Token.TokenType.OPEN_CURLY)
            }
            else if (ch == '}') {
                return getSingleCharacterToken(context, Token.TokenType.CLOSE_CURLY)
            }
            else if (ch == '(') {
                return getSingleCharacterToken(context, Token.TokenType.OPEN_PAREN)
            }
            else if (ch == ')') {
                return getSingleCharacterToken(context, Token.TokenType.CLOSE_PAREN)
            }
            else if (ch == '[') {
                return getSingleCharacterToken(context, Token.TokenType.OPEN_SQUARE_BRACKET)
            }
            else if (ch == ']') {
                return getSingleCharacterToken(context, Token.TokenType.CLOSE_SQUARE_BRACKET)
            }
            else if (ch == '.') {
                return getSingleCharacterToken(context, Token.TokenType.DOT)
            }
            // Operators
            else if (ch == '+') {
                return getSingleCharacterToken(context, Token.TokenType.PLUS)
            }
            else if (ch == '-') {
                return getSingleCharacterToken(context, Token.TokenType.MINUS)
            }
            else if (ch == '/') {
                return getSingleCharacterToken(context, Token.TokenType.DIVIDE)
            }
            else if (ch == '*') {
                return getSingleCharacterToken(context, Token.TokenType.MULTIPLY)
            }
            else if (ch == '%') {
                return getSingleCharacterToken(context, Token.TokenType.MODULO)
            }
            else if (ch == '<') {
                return getSingleCharacterToken(context, Token.TokenType.LESS)
            }
            else if (ch == '>') {
                return getSingleCharacterToken(context, Token.TokenType.GREATER)
            }
            else if (ch == '!') {
                return getSingleCharacterToken(context, Token.TokenType.NOT)
            }
            else if (getValueOfLength(context, 2) == "==") {
                // must be checked before '=' operator
                return getMultiCharacterToken(context, 2, Token.TokenType.EQUALS)
            }
            else if (getValueOfLength(context, 2) == "&&") {
                return getMultiCharacterToken(context, 2, Token.TokenType.AND)
            }
            else if (getValueOfLength(context, 2) == "||") {
                return getMultiCharacterToken(context, 2, Token.TokenType.OR)
            }
            else if (ch == '=') {
                return getSingleCharacterToken(context, Token.TokenType.ASSIGN)
            }
            // Literals
            else if (ch.isDigit()) /* numeric */ {
                return getNumericLiteralToken(context)
            }
            else if (ch == '"') /* string */ {
                return getStringLiteralToken(context)
            }
            else if (getValueOfLength(context, 4) == "true") /* boolean */ {
                return getMultiCharacterToken(context, 4, Token.TokenType.BOOLEAN_TRUE_LITERAL)
            }
            else if (getValueOfLength(context, 5) == "false") /* boolean */ {
                return getMultiCharacterToken(context, 5, Token.TokenType.BOOLEAN_FALSE_LITERAL)
            }
            else if (ch.isIdentifierCharacter() && !isKeyword(getIdentifierLexeme(context))) /* identifier */ {
                return getIdentifierToken(context)
            }
            // Keywords
            else if (getValueOfLength(context, 3) == "var") {
                return getMultiCharacterToken(context, 3, Token.TokenType.VAR)
            }
            else if (getValueOfLength(context, "function".length) == "function") {
                return getMultiCharacterToken(context, "function".length, Token.TokenType.FUNCTION)
            }
            else if (getValueOfLength(context, 2) == "if") {
                return getMultiCharacterToken(context, 2, Token.TokenType.IF)
            }
            else if (getValueOfLength(context, 4) == "else") {
                return getMultiCharacterToken(context, 4, Token.TokenType.ELSE)
            }
            else if (getValueOfLength(context, 3) == "for") {
                return getMultiCharacterToken(context, 3, Token.TokenType.FOR)
            }
            else if (getValueOfLength(context, 5) == "while") {
                return getMultiCharacterToken(context, 5, Token.TokenType.WHILE)
            }
            else {
                return getMultiCharacterToken(context, 1, Token.TokenType.INVALID)
            }
        }
    }

    private fun getValueOfLength(context: Context, len: Int): String? {
        var result = ""

        context.let {
            var index = 0
            while(it.currentIndex + index < it.program.length && result.length < len) {
                result += it.program[it.currentIndex + index]
                ++index
            }
        }

        if (result.length != len) {
            return null
        }
        return result
    }


    private fun isKeyword(identifier: String): Boolean {
        val keywords = listOf("var", "function", "if", "else", "for", "while")

        for (keyword in keywords) {
            if (identifier == keyword) {
                return true
            }
        }
        return false
    }


    private fun getSingleCharacterToken(context: Context, type: Token.TokenType): Token {
        return getMultiCharacterToken(context, 1, type)
    }


    private fun getMultiCharacterToken(context: Context, len: Int, type: Token.TokenType): Token {
        val lexeme = getValueOfLength(context, len)
        require(lexeme != null && lexeme.length == len)

        val token = Token(
            type = type,
            startPosition = context.currentPosition.copy(),
            length = len,
            lexeme = lexeme,
        )

        for (i in 0 until len) {
            advance(context)
        }

        return token
    }


    // TODO: how to tackle newline char problem? allow/disallow newline in a string literal
    private fun getStringLiteralToken(context: Context): Token {
        context.let {
            val startPosition = context.currentPosition.copy()
            var lexeme = ""
            var length = 0

            var quotesCount = 0
            var ch = it.program[it.currentIndex]
            while(it.currentIndex < it.program.length && quotesCount < 2) {
                lexeme += ch
                if (ch == '"') {
                    ++quotesCount
                }
                length += advance(it)
                if (it.currentIndex < it.program.length) {
                    ch = it.program[it.currentIndex]
                }
            }

            return Token(
                type = if (quotesCount == 2) Token.TokenType.STRING_LITERAL else Token.TokenType.INVALID,
                startPosition = startPosition,
                length = length,
                lexeme = lexeme,
            )
        }
    }


    private fun getNumericLiteralToken(context: Context): Token {
        val startPosition = context.currentPosition.copy()
        var length = 0
        val lexeme: MutableList<Char> = mutableListOf()
        var type = Token.TokenType.INTEGER_LITERAL

        context.let {
            var ch = it.program[it.currentIndex]
            while(it.currentIndex < it.program.length && (ch.isDigit() || ch == '.')) {
                ++length
                lexeme.add(ch)

                if (ch == '.') {
                    type = Token.TokenType.FLOAT_LITERAL
                }

                advance(it)
                if (it.currentIndex < it.program.length) {
                    ch = it.program[it.currentIndex]
                }
            }
        }
        return Token(type, startPosition, length, lexeme.joinToString(""))
    }


    /**
     * Method retrieves identifier lexeme without advancing current index of the program
     */
    private fun getIdentifierLexeme(context: Context): String {
        val lexeme: MutableList<Char> = mutableListOf()

        context.let {
            var index = it.currentIndex
            var ch = it.program[index]

            while(index < it.program.length && ch.isIdentifierCharacter()) {
                lexeme.add(ch)

                ++index
                if (index < it.program.length) {
                    ch = it.program[index]
                }
            }
        }

        return lexeme.joinToString("")
    }


    private fun getIdentifierToken(context: Context): Token {
        val startPosition = context.currentPosition.copy()
        val lexeme = getIdentifierLexeme(context)

        context.let {
            for (i in lexeme.indices) {
                advance(it)
            }
        }

        return Token(Token.TokenType.IDENTIFIER, startPosition, lexeme.length, lexeme)
    }

    // TODO: make offset incrementation inside if/else branches?
    /**
     * Advances current index of a program to the next symbol.
     * Line separator is skipped via advancing the index by the length of the line separator.
     *
     * @return The number of characters that were skipped (either 1 or 2 depending on the length of line separator).
     */
    private fun advance(context: Context): Int {
        val substring = context.program.substring(
            context.currentIndex, (context.currentIndex + 2).coerceAtMost(context.program.length))

        val sep = System.lineSeparator()
        val isNewline = (substring == sep)

        val len = if (isNewline) sep.length else 1

        context.currentIndex += len
        context.currentPosition.offset += len

        if (isNewline) {
            context.currentPosition.lineIndex++
            context.currentPosition.lineOffset = 0
        }
        else {
            context.currentPosition.lineOffset++
        }
        return len
    }

    private fun skipWhitespaces(context: Context) {
        context.let {
            while (it.currentIndex < it.program.length &&
                   it.program[it.currentIndex].isWhitespace()) {
                advance(it)
            }
        }
    }

}