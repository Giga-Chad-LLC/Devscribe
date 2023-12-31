package components.parser

import components.lexer.Token
import components.lexer.Token.TokenType.*
import components.parser.nodes.AstNode
import components.parser.nodes.IdentifierNode
import components.parser.nodes.ProgramNode
import components.parser.nodes.StatementNode
import components.parser.nodes.constructs.FunctionDefinitionNode
import components.parser.nodes.constructs.ScopeNode



class ParserImpl : Parser {
    private data class Context(
        var currentIndex: Int
    )

    override fun parse(tokens: List<Token>): AstNode {
        val statements = mutableListOf<StatementNode>()

        val context = Context(0)

        while (context.currentIndex < tokens.size) {
            val token = tokens[context.currentIndex]

            when (token.type) {
                VAR -> parseFunctionDefinitionOrDeclaration(context, tokens)
                else -> {}
                /*
                FUNCTION -> parseFunctionDefinition(iterator, statements, tokens)
                SEMICOLON -> parseSemicolon(iterator, statements)
                FUNCTION -> TODO()
                IF -> TODO()
                ELSE -> TODO()
                FOR -> TODO()
                WHILE -> TODO()
                SEMICOLON -> TODO()
                COMMA -> TODO()
                OPEN_CURLY -> TODO()
                CLOSE_CURLY -> TODO()
                OPEN_PAREN -> TODO()
                CLOSE_PAREN -> TODO()
                PLUS -> TODO()
                MINUS -> TODO()
                DIVIDE -> TODO()
                MULTIPLY -> TODO()
                MODULO -> TODO()
                ASSIGN -> TODO()
                LESS -> TODO()
                GREATER -> TODO()
                NOT -> TODO()
                EQUALS -> TODO()
                INTEGER_LITERAL -> TODO()
                FLOAT_LITERAL -> TODO()
                STRING_LITERAL -> TODO()
                BOOLEAN_TRUE_LITERAL -> TODO()
                BOOLEAN_FALSE_LITERAL -> TODO()
                IDENTIFIER -> TODO()
                END -> TODO()
                INVALID -> TODO()
                */
            }
        }

        return ProgramNode(statements)
    }

    private fun parseFunctionDefinitionOrDeclaration(context: Context, tokens: List<Token>) {
        val varIndex = context.currentIndex
        val nameIndex = context.currentIndex + 1
        val semicolonIndex = context.currentIndex + 2

        require(semicolonIndex <  tokens.size)

        requireTypeAt(varIndex, Token.TokenType.VAR, tokens)
        requireTypeAt(nameIndex, Token.TokenType.IDENTIFIER, tokens)

        // var name [= value];
        if (tokens[semicolonIndex].type == Token.TokenType.SEMICOLON) {
            // variable declaration

        }
        else {
            // variable definition
        }

    }

    /*private fun parseSemicolon(iterator: Iterator, statements: MutableList<StatementNode>) {
        statements.add(parseStatement(iterator.statementTokens))
        iterator.statementTokens.clear()
        iterator.currentIndex++
    }

    private fun parseFunctionDefinition(
        iterator: Iterator, statements: MutableList<StatementNode>, tokens: List<Token>) {
        // assert the structure of: 'function identifier([...arg,])'
        requireFunctionDeclaration(iterator.currentIndex, tokens)

        val identifier = tokens[iterator.currentIndex + 1].lexeme

        // skip identifier & opening parenthesis
        iterator.currentIndex += 2

        // parse arguments
        val arguments = mutableListOf<AstNode>()
        while(tokens[iterator.currentIndex].type != CLOSE_PAREN) {
            if (tokens[iterator.currentIndex].type == IDENTIFIER) {
                arguments.add(IdentifierNode(tokens[iterator.currentIndex].lexeme))
            }
            ++iterator.currentIndex
        }

        // skip closing parenthesis
        iterator.currentIndex++

        val scope = parseScope(iterator, statements)
        statements.add(StatementNode(
                FunctionDefinitionNode(IdentifierNode(identifier), arguments, scope)
            ))
    }

    private fun parseScope(iterator: Iterator, statements: MutableList<StatementNode>): ScopeNode {
        TODO()
    }

    private fun parseStatement(tokens: List<Token>) : StatementNode {
        TODO()
    }

    private fun requireFunctionDeclaration(index: Int, tokens: List<Token>) {
        requireTypeAt(index, FUNCTION, tokens)
        requireTypeAt(index + 1, IDENTIFIER, tokens)
        requireTypeAt(index + 2, OPEN_PAREN, tokens)
        // arguments
        var shift = 3
        while(index + shift < tokens.size && tokens[index + shift].type != CLOSE_PAREN) {
            val token = tokens[index + shift]
            require(token.type == IDENTIFIER || token.type == COMMA)
            shift++
        }
        require(index + shift < tokens.size && tokens[index + shift].type == CLOSE_PAREN)
    }*/

    private fun requireTypeAt(index: Int, type: Token.TokenType, tokens: List<Token>) {
        require(index < tokens.size && tokens[index].type == type)
    }
}