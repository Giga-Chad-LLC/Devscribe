import components.lexer.Lexer
import components.lexer.Token
import components.parser.Parser
import components.parser.ParserImpl
import components.parser.nodes.AstNode
import components.parser.nodes.IdentifierNode
import components.parser.nodes.ProgramNode
import components.parser.nodes.StatementNode
import components.parser.nodes.auxiliary.EndNode
import components.parser.nodes.auxiliary.InvalidNode
import components.parser.nodes.constructs.*
import components.parser.nodes.literals.*
import components.parser.nodes.operators.AssignmentNode
import components.parser.nodes.operators.BinaryOperatorNode
import components.parser.nodes.operators.UnaryOperatorNode
import components.parser.visitors.Visitor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test



// TODO: for nodes such as If/Else replace lists of AstNodes with a single AstNode (ScopeNode)
class ParserTest {
    private val lexer = Lexer()
    private val parser = createParser()

    private fun createParser(): Parser {
        return ParserImpl()
    }

    private class AstComparator : Visitor {
        private var treesEqual = true
        private var other: AstNode? = null

        fun compare(first: AstNode, second: AstNode): Boolean {
            first.accept(this)
            second.accept(this)
            return treesEqual
        }

        override fun visit(node: ProgramNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            // required to set 'other' to null to compare child nodes recursively
            other = null

            // compare other and current node
            if (!((peer is ProgramNode) && peer.statements.size == node.statements.size)) {
                treesEqual = false
                return
            }

            var result = true
            for (i in node.statements.indices) {
                val first = (other as ProgramNode).statements[i]
                val second = node.statements[i]
                result = result && compare(first, second)
            }

            if (!result) {
                treesEqual = false
            }
        }

        override fun visit(node: StatementNode) {
            if (other == null) {
                other = node
                return
            }
            val peer = other
            other = null

            treesEqual = (peer is StatementNode) && compare(peer.statement, node.statement)
        }

        override fun visit(node: IdentifierNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is IdentifierNode) && peer.identifier == node.identifier
        }

        override fun visit(node: AssignmentNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is AssignmentNode) &&
                        peer.identifier == node.identifier &&
                        compare(peer.expression, node.expression)
        }

        override fun visit(node: UnaryOperatorNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is UnaryOperatorNode) &&
                        peer.op == node.op &&
                        compare(peer.expression, node.expression)
        }

        override fun visit(node: BinaryOperatorNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is BinaryOperatorNode) &&
                        peer.op == node.op &&
                        compare(peer.lhs, node.lhs) &&
                        compare(peer.rhs, node.rhs)
        }

        override fun visit(node: IntegerNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is IntegerNode) && (peer.literal == node.literal)
        }

        override fun visit(node: FloatNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is FloatNode) && (peer.literal == node.literal)
        }

        override fun visit(node: StringNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is StringNode) && (peer.literal == node.literal)
        }

        override fun visit(node: BooleanTrueNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is BooleanTrueNode)
        }

        override fun visit(node: BooleanFalseNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is BooleanFalseNode)
        }

        override fun visit(node: FunctionNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            if (!((peer is FunctionNode) && (peer.identifier == node.identifier) &&
                (peer.arguments.size == node.arguments.size) && compare(peer.body, node.body))) {
                treesEqual = false
                return
            }

            var argumentsEqual = true
            for (i in node.arguments.indices) {
                argumentsEqual = argumentsEqual && compare(peer.arguments[i], node.arguments[i])
            }

            // TODO: treesEqual = treesEqual && argumentsEqual?
            treesEqual = argumentsEqual
        }

        override fun visit(node: IfNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is IfNode) &&
                        compare(peer.condition, node.condition) &&
                        compare(peer.body, node.body)
        }

        override fun visit(node: ElseNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is ElseNode) && compare(peer.body, node.body)
        }

        override fun visit(node: ForLoopNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is ForLoopNode) &&
                        compare(peer.enteringStatement, node.enteringStatement) &&
                        compare(peer.condition, node.condition) &&
                        compare(peer.postIterationExpression, node.postIterationExpression) &&
                        compare(peer.body, node.body)
        }

        override fun visit(node: WhileLoopNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is WhileLoopNode) &&
                        compare(peer.condition, node.condition) &&
                        compare(peer.body, node.body)
        }

        override fun visit(node: ScopeNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            if (!((peer is ScopeNode) && (peer.body.size == node.body.size))) {
                treesEqual = false
                return
            }

            var bodiesEqual = true
            for (i in node.body.indices) {
                val first = peer.body[i]
                val second = node.body[i]
                bodiesEqual = bodiesEqual && compare(first, second)
            }

            treesEqual = bodiesEqual
        }

        override fun visit(node: EndNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is EndNode)
        }

        override fun visit(node: InvalidNode) {
            if (other == null) {
                other = node
                return
            }

            val peer = other
            other = null

            treesEqual = (peer is InvalidNode) && (peer.lexeme == node.lexeme)
        }
    }

    @Test
    fun testEmptyTokensList() {
        val tokens = listOf<Token>()
        val root = parser.parse(tokens)

        assertTrue(root is ProgramNode)
        assertEquals(0, (root as ProgramNode).statements.size)
    }

    @Test
    fun testOnlyEndToken() {
        val program = ""
        val tokens = lexer.tokenize(program)

        assertEquals(1, tokens.size)
        assertEquals(Token.TokenType.END, tokens.first().type)

        val root = parser.parse(tokens)

        assertTrue(root is ProgramNode)
        assertEquals(0, (root as ProgramNode).statements.size)
    }

    @Test
    fun testVariableDeclaration() {
        val program = "var my_var_123;"
        val tokens = lexer.tokenize(program)

        // VAR, IDENTIFIER, SEMICOLON, END
        assertEquals(4, tokens.size)

        var root = parser.parse(tokens)
        assertTrue(root is ProgramNode)

        root = root as ProgramNode
        assertEquals(1, root.statements.size)

        val statement = root.statements.first().statement

        assertTrue(statement is IdentifierNode)
        assertEquals("my_var_123", (statement as IdentifierNode).identifier)
    }

    @Test
    fun testVariableDefinition() {
        val identifier = "my_var_123"
        val literal = "123.13"
        val program = "var $identifier = $literal;"
        val tokens = lexer.tokenize(program)

        // VAR, IDENTIFIER, ASSIGN, LITERAL, SEMICOLON, END
        assertEquals(6, tokens.size)

        var root = parser.parse(tokens)
        assertTrue(root is ProgramNode)

        root = root as ProgramNode
        assertEquals(1, root.statements.size)

        assertTrue(root.statements.first().statement is AssignmentNode)
        val assignment = root.statements.first().statement as AssignmentNode

        assertEquals(identifier, assignment.identifier)
        assertTrue(assignment.expression is FloatNode)
        assertEquals(literal, (assignment.expression as FloatNode).literal)
    }

    @Test
    fun testVariableDeclarationAndAssignment() {
        val identifier = "my_var_123"
        val literal = "312"
        val program = "var $identifier;\n$identifier = $literal;"
        var tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IdentifierNode(identifier)),
            StatementNode(AssignmentNode(identifier, IntegerNode(literal))),
        ))

        val got = parser.parse(tokens)


    }
}