package components.lexer

class Lexer {
    private data class Context(
        val program: String,
        var currentIndex: Int,
        var currentPosition: Position
    )

    // TODO: tokenize keywords
    fun tokenize(program: String): ArrayList<Token> {
        val tokens = ArrayList<Token>()
        val context = Context(program, 0, Position(0, 0, 0))

        while (context.currentIndex < context.program.length) {
            val token = getNextToken(context)
            tokens.add(token)
        }

        val end = Token(
            type = Token.TokenType.END,
            startPosition = context.currentPosition,
            length = 0,
            lexeme = "",
        )
        tokens.add(end)

        return tokens
    }

    private fun getNextToken(context: Context): Token {
        skipWhitespaces(context)


        context.let {
            // TODO: what if no tokens encountered after whitespace skipping
            if (it.currentIndex >= it.program.length) {
                throw IllegalArgumentException("Current index ${it.currentIndex} exceeds allowed range of [0, ${it.program.length})")
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
            else if (ch == '[') {
                return getSingleCharacterToken(context, Token.TokenType.OPEN_PAREN)
            }
            else if (ch == ']') {
                return getSingleCharacterToken(context, Token.TokenType.CLOSE_PAREN)
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
            else if (ch == '=') {
                return getSingleCharacterToken(context, Token.TokenType.EQUALS)
            }
            // Literals
            else if (ch.isDigit()) {
                // numeric literal must be BEFORE identifier
                return getNumericLiteralToken(context)
            }
            else if (ch == '"') {
                return getStringLiteralToken(context)
            }
            else if (isBooleanLiteral(context)) {
                return getBooleanLiteral(context)
            }
            else if (ch.isLetter()) {
                return getIdentifierToken(context)
            }
            else {
                return Token(Token.TokenType.INVALID, it.currentPosition.copy(), 1, ch.toString())
            }
        }
    }


    private fun getSingleCharacterToken(context: Context, type: Token.TokenType): Token {
        val token = Token(
            type = type,
            startPosition = context.currentPosition.copy(),
            length = 1,
            lexeme = context.program[context.currentIndex].toString()
        )
        advance(context)

        return token
    }


    private fun isBooleanLiteral(context: Context): Boolean {
        var result = false
        context.let {
            val maxLen = it.program.length

            val lexemeTrue = it.program.substring(
                it.currentIndex, (it.currentIndex + "true".length).coerceAtMost(maxLen))

            val lexemeFalse = it.program.substring(
                it.currentIndex, (it.currentIndex + "false".length).coerceAtMost(maxLen))

            if (lexemeTrue == "true" || lexemeFalse == "false") {
                result = true
            }
        }
        return result
    }

    // TODO: how to tackle newline char problem? allow/disallow newline in a string literal
    private fun getStringLiteralToken(context: Context): Token {
        context.let {
            val startPosition = context.currentPosition.copy()
            var length = 0
            val lexeme: MutableList<Char> = mutableListOf()

            var quotesCount = 0
            var ch = it.program[it.currentIndex]
            while(it.currentIndex < it.program.length && quotesCount < 2) {
                ++length
                lexeme.add(ch)

                if (ch == '"') {
                    ++quotesCount
                }

                advance(it)
                if (it.currentIndex < it.program.length) {
                    ch = it.program[it.currentIndex]
                }
            }

            if (quotesCount == 2) {
                return Token(
                    Token.TokenType.STRING_LITERAL,
                    startPosition,
                    length,
                    lexeme.joinToString()
                )
            }
            else {
                return Token(
                    Token.TokenType.INVALID,
                    startPosition,
                    length,
                    lexeme.joinToString()
                )
            }

        }
    }


    private fun getBooleanLiteral(context: Context): Token {
        context.let {
            val maxLen = it.program.length

            val lexemeTrue = it.program.substring(
                it.currentIndex, (it.currentIndex + "true".length).coerceAtMost(maxLen))

            val lexemeFalse = it.program.substring(
                it.currentIndex, (it.currentIndex + "false".length).coerceAtMost(maxLen))

            if (lexemeTrue == "true") {
                val token = Token(
                    Token.TokenType.BOOLEAN_TRUE_LITERAL,
                    it.currentPosition,
                    length = lexemeTrue.length,
                    lexemeTrue
                )
                // advancing current index of the current token in the program
                for (i in 1..lexemeTrue.length) {
                    advance(context)
                }
                return token
            }

            if (lexemeFalse == "false") {
                val token = Token(
                    Token.TokenType.BOOLEAN_FALSE_LITERAL,
                    it.currentPosition,
                    length = lexemeFalse.length,
                    lexemeFalse,
                )
                // advancing current index of the current token in the program
                for (i in 1..lexemeFalse.length) {
                    advance(context)
                }
                return token
            }

            throw IllegalArgumentException("No boolean keywords 'true'/'false' found, got '${lexemeTrue}' and '${lexemeFalse}'")
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
        return Token(type, startPosition, length, lexeme.joinToString())
    }

    // TODO: use single getIdentifierValue function to get value of 'true'/'false'/keywords/identifiers
    private fun getIdentifierToken(context: Context): Token {
        val startPosition = context.currentPosition.copy()
        var length = 0
        val lexeme: MutableList<Char> = mutableListOf()

        context.let {
            var ch = it.program[it.currentIndex]
            while(it.currentIndex < it.program.length && ch.isLetterOrDigit()) {
                ++length
                lexeme.add(ch)

                advance(it)
                if (it.currentIndex < it.program.length) {
                    ch = it.program[it.currentIndex]
                }
            }
        }
        return Token(Token.TokenType.IDENTIFIER, startPosition, length, lexeme.joinToString())
    }

    // TODO: make offset incrementation inside if/else branches?
    private fun advance(context: Context) {
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