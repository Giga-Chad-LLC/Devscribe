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
        VAR, FUNCTION, IF, FOR, WHILE,

        // Delimiters
        SEMICOLON, COMMA, OPEN_CURLY, CLOSE_CURLY, OPEN_PAREN, CLOSE_PAREN,

        // Operators
        PLUS, MINUS, DIVIDE, MULTIPLY, MODULO, EQUALS,

        // Literals
        INTEGER_LITERAL, FLOAT_LITERAL, STRING_LITERAL, BOOLEAN_TRUE_LITERAL, BOOLEAN_FALSE_LITERAL, IDENTIFIER,

        // Auxiliary
        END, INVALID,
    }
}