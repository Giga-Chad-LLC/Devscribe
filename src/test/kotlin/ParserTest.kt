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
import components.parser.nodes.auxiliary.NoOperationNode
import components.parser.nodes.constructs.*
import components.parser.nodes.literals.*
import components.parser.nodes.operators.AssignmentNode
import components.parser.nodes.operators.BinaryOperatorNode
import components.parser.nodes.operators.BinaryOperatorType
import components.parser.nodes.operators.UnaryOperatorNode
import components.parser.visitors.Visitor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test



class ParserTest {
    private val lexer = Lexer()
    private val parser = createParser()
    private val comparator = AstComparator()

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

        /**
         * This function checks whether 'other' is set:
         * 1. If it is set then execute the callback with the value of 'other' and set 'other' to null.
         * 2. If 'other' is null, then set it to be the provided node.
         */
        private fun setOrProvideNode(node: AstNode, callback: (AstNode) -> Unit) {
            if (other != null) {
                val peer = other!!
                other = null
                callback(peer)
            }
            else {
                other = node
            }
        }

        override fun visit(node: ProgramNode) {
            setOrProvideNode(node) { peer ->
                // compare other and current node
                if (!((peer is ProgramNode) && peer.statements.size == node.statements.size)) {
                    treesEqual = false
                    return@setOrProvideNode
                }

                var statementsEqual = true
                for (i in node.statements.indices) {
                    val first = (other as ProgramNode).statements[i]
                    val second = node.statements[i]
                    statementsEqual = statementsEqual && compare(first, second)
                }

                treesEqual = statementsEqual
            }
        }

        override fun visit(node: StatementNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is StatementNode) && compare(peer.statement, node.statement)
            }
        }

        override fun visit(node: IdentifierNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is IdentifierNode) && peer.identifier == node.identifier
            }
        }

        override fun visit(node: AssignmentNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is AssignmentNode) &&
                        peer.identifier == node.identifier &&
                        compare(peer.expression, node.expression)
            }
        }

        override fun visit(node: UnaryOperatorNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is UnaryOperatorNode) &&
                        peer.op == node.op &&
                        compare(peer.expression, node.expression)
            }
        }

        override fun visit(node: BinaryOperatorNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is BinaryOperatorNode) &&
                        peer.op == node.op &&
                        compare(peer.lhs, node.lhs) &&
                        compare(peer.rhs, node.rhs)
            }
        }

        override fun visit(node: IntegerNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is IntegerNode) && (peer.literal == node.literal)
            }
        }

        override fun visit(node: FloatNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is FloatNode) && (peer.literal == node.literal)
            }
        }

        override fun visit(node: StringNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is StringNode) && (peer.literal == node.literal)
            }
        }

        override fun visit(node: BooleanTrueNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is BooleanTrueNode)
            }
        }

        override fun visit(node: BooleanFalseNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is BooleanFalseNode)
            }
        }

        override fun visit(node: FunctionNode) {
            setOrProvideNode(node) { peer ->
                if (!((peer is FunctionNode) && (peer.identifier == node.identifier) &&
                            (peer.arguments.size == node.arguments.size) && compare(peer.body, node.body))) {
                    treesEqual = false
                    return@setOrProvideNode
                }

                var argumentsEqual = true
                for (i in node.arguments.indices) {
                    argumentsEqual = argumentsEqual && compare(peer.arguments[i], node.arguments[i])
                }

                // TODO: treesEqual = treesEqual && argumentsEqual?
                treesEqual = argumentsEqual
            }
        }

        override fun visit(node: IfNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is IfNode) &&
                        compare(peer.condition, node.condition) &&
                        compare(peer.body, node.body)
            }
        }

        override fun visit(node: ElseNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is ElseNode) && compare(peer.body, node.body)
            }
        }

        override fun visit(node: ForLoopNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is ForLoopNode) &&
                        compare(peer.enteringStatement, node.enteringStatement) &&
                        compare(peer.condition, node.condition) &&
                        compare(peer.postIterationExpression, node.postIterationExpression) &&
                        compare(peer.body, node.body)
            }
        }

        override fun visit(node: WhileLoopNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is WhileLoopNode) &&
                        compare(peer.condition, node.condition) &&
                        compare(peer.body, node.body)
            }
        }

        override fun visit(node: ScopeNode) {
            setOrProvideNode(node) { peer ->
                if (!((peer is ScopeNode) && (peer.body.size == node.body.size))) {
                    treesEqual = false
                    return@setOrProvideNode
                }

                var bodiesEqual = true
                for (i in node.body.indices) {
                    val first = peer.body[i]
                    val second = node.body[i]
                    bodiesEqual = bodiesEqual && compare(first, second)
                }

                treesEqual = bodiesEqual
            }
        }

        override fun visit(node: DefinitionNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is DefinitionNode) &&
                            (peer.identifier == node.identifier) &&
                            compare(peer.expression, node.expression)
            }
        }

        override fun visit(node: EndNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is EndNode)
            }
        }

        override fun visit(node: InvalidNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is InvalidNode) && (peer.lexeme == node.lexeme)
            }
        }

        override fun visit(node: NoOperationNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is NoOperationNode)
            }
        }
    }

    /**
     * Testing AstComparator component.
     */

    @Test
    fun testAstComparatorOnEmptyASTs() {
        val root1 = ProgramNode(listOf())
        val root2 = ProgramNode(listOf())

        val got = comparator.compare(root1, root2)
        assertEquals(true, got)
    }

    @Test
    fun testAstComparatorOnSingleStatementAST() {
        val root1 = ProgramNode(listOf(
            StatementNode(NoOperationNode())
        ))
        val root2 = ProgramNode(listOf(
            StatementNode(NoOperationNode())
        ))

        val got = comparator.compare(root1, root2)
        assertEquals(true, got)
    }

    @Test
    fun testAstComparatorOnTheSameAST() {
        /**
         The considered program is:
         ```
            var my_variable_123 = 1 + 2;
         ```
         */
        val root = ProgramNode(listOf(
            StatementNode(DefinitionNode(
                identifier = "my_variable_123",
                expression = BinaryOperatorNode(
                    lhs = IntegerNode("1"),
                    rhs = IntegerNode("2"),
                    op = BinaryOperatorType.PLUS,
                )
            ))
        ))

        val got = comparator.compare(root, root)
        assertEquals(true, got)
    }

    @Test
    fun testAstComparatorOnDistinctVariableDefinitionASTs() {
        /**
         Program 1 (integer variable):
         ```
         var identifier123 = 123;
         ```

         Program 2 (float variable):
         ```
         var identifier123 = 123.0;
         ```
        */

        // program1
        val root1 = ProgramNode(listOf(
            StatementNode(DefinitionNode(
                identifier = "identifier123",
                expression = IntegerNode("123"),
            ))
        ))

        // program2
        val root2 = ProgramNode(listOf(
            StatementNode(AssignmentNode(
                identifier = "identifier123",
                expression = FloatNode("123.0"),
            ))
        ))

        val got = comparator.compare(root1, root2)
        assertEquals(false, got)
    }


    @Test
    fun testAstComparatorOnSameFunctionDefinitionASTs() {
        /**
         * Program:
         * ```
         * function my_function(arg1, arg2) {}
         * ```
         */

        val root = ProgramNode(listOf(
            StatementNode(FunctionNode(
                identifier = "my_function",
                arguments = listOf(IdentifierNode("arg1"), IdentifierNode("arg2")),
                body = ScopeNode(listOf(NoOperationNode()))
            ))
        ))

        val got = comparator.compare(root, root)
        assertEquals(true, got)
    }

    // TODO: make single CompositeNode which stores list of nodes, others use it

    @Test
    fun testAstComparatorOnForLoopASTs() {
        /**
         * Both programs:
         * ```
         * for (var i = 0; i < 10; i = i + 1) {}
         * ```
         */

        val root1 = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = DefinitionNode("i", IntegerNode("0")),
                condition = BinaryOperatorNode(
                    lhs = IdentifierNode("i"),
                    rhs = IntegerNode("10"),
                    op = BinaryOperatorType.LESS,
                ),
                postIterationExpression = AssignmentNode(
                    identifier = "i",
                    BinaryOperatorNode(
                        lhs = IdentifierNode("i"),
                        rhs = IntegerNode("1"),
                        op = BinaryOperatorType.PLUS,
                    ),
                ),
                body = ScopeNode(listOf())
            ))
        ))

        val root2 = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = DefinitionNode("i", IntegerNode("0")),
                condition = BinaryOperatorNode(
                    lhs = IdentifierNode("i"),
                    rhs = IntegerNode("10"),
                    op = BinaryOperatorType.LESS,
                ),
                postIterationExpression = AssignmentNode(
                    identifier = "i",
                    BinaryOperatorNode(
                        lhs = IdentifierNode("i"),
                        rhs = IntegerNode("1"),
                        op = BinaryOperatorType.PLUS,
                    ),
                ),
                body = ScopeNode(listOf())
            ))
        ))

        val got = comparator.compare(root1, root2)
        assertEquals(true, got)
    }



    /**
     * Testing implemented of Parser interface.
     */

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