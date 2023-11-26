package components.lexer


data class Position(
    var offset: Int, // global offset inside a string
    var lineIndex: Int,
    var lineOffset: Int, // offset inside current line
)


class Token(
    val type: TokenType,
    val startPosition: Position,
    val length: Int,
    val lexeme: String
) {
    enum class TokenType {
        // Keywords
        VAR /* var */, FUNCTION /* function */, IF /* if */, ELSE /* else */, FOR /* for */, WHILE /* while */,

        // Delimiters
        SEMICOLON, COMMA, DOT, OPEN_CURLY, CLOSE_CURLY, OPEN_PAREN, CLOSE_PAREN, OPEN_SQUARE_BRACKET, CLOSE_SQUARE_BRACKET,

        // Operators
        PLUS, MINUS, DIVIDE, MULTIPLY, MODULO, ASSIGN /* = */, LESS /* < */, GREATER /* > */, NOT /* ! */, EQUALS /* == */,
        AND /* && */, OR /* || */,

        // Literals
        INTEGER_LITERAL, FLOAT_LITERAL, STRING_LITERAL, BOOLEAN_TRUE_LITERAL, BOOLEAN_FALSE_LITERAL, IDENTIFIER,

        // Auxiliary
        END, INVALID,
    }
}