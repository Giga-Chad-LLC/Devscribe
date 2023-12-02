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
import components.parser.nodes.operators.*
import components.parser.visitors.Visitor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


// TODO: make single CompositeNode which stores list of nodes, others use it?
// TODO: make explicit 'ExpressionNode' node class?
class ParserTest {
    private val lexer = Lexer()
    private val parser = createParser()
    private val comparator = AstComparator()

    private fun createParser(): Parser {
        return ParserImpl()
    }

    // TODO: test UnaryOperator for ast comparator
    private class AstComparator : Visitor {
        private var treesEqual = true
        private var other: AstNode? = null

        /**
         * Returns true if trees are equivalent in terms of their structure and node types and data.
         * Otherwise, returns false.
         */
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
                if (!((peer is ProgramNode) && peer.statements.size == node.statements.size)) {
                    treesEqual = false
                    return@setOrProvideNode
                }

                var statementsEqual = true
                for (i in node.statements.indices) {
                    val first = peer.statements[i]
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

        override fun visit(node: FunctionDefinitionNode) {
            setOrProvideNode(node) { peer ->
                if (!((peer is FunctionDefinitionNode) && (peer.identifier == node.identifier) &&
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
                        compare(peer.body, node.body) &&
                        ((peer.otherwise == null && node.otherwise == null) ||
                                ((peer.otherwise != null) && (node.otherwise != null) &&
                                    compare(peer.otherwise!!, node.otherwise!!)))
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

        override fun visit(node: DeclarationNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is DeclarationNode) && compare(peer.identifier, node.identifier)
            }
        }

        override fun visit(node: DefinitionNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is DefinitionNode) &&
                            (peer.identifier == node.identifier) &&
                            compare(peer.expression, node.expression)
            }
        }

        override fun visit(node: FunctionCallNode) {
            setOrProvideNode(node) { peer ->
                if (!((peer is FunctionCallNode) && peer.arguments.size == node.arguments.size)) {
                    treesEqual = false
                    return@setOrProvideNode
                }

                var argumentsEqual = true
                for (i in node.arguments.indices) {
                    val first = peer.arguments[i]
                    val second = node.arguments[i]
                    argumentsEqual = argumentsEqual && compare(first, second)
                }

                treesEqual = argumentsEqual
            }
        }

        override fun visit(node: ReturnNode) {
            setOrProvideNode(node) { peer ->
                treesEqual = (peer is ReturnNode) && compare(peer.expression, node.expression)
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
                identifier = IdentifierNode("my_variable_123"),
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
                identifier = IdentifierNode("identifier123"),
                expression = IntegerNode("123"),
            ))
        ))

        // program2
        val root2 = ProgramNode(listOf(
            StatementNode(DefinitionNode(
                identifier = IdentifierNode("identifier123"),
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
            StatementNode(FunctionDefinitionNode(
                identifier = IdentifierNode("my_function"),
                arguments = listOf(IdentifierNode("arg1"), IdentifierNode("arg2")),
                body = ScopeNode(listOf(StatementNode(NoOperationNode())))
            ))
        ))

        val got = comparator.compare(root, root)
        assertEquals(true, got)
    }


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
                enteringStatement = StatementNode(DefinitionNode(IdentifierNode("i"), IntegerNode("0"))),
                condition = BinaryOperatorNode(
                    lhs = IdentifierNode("i"),
                    rhs = IntegerNode("10"),
                    op = BinaryOperatorType.LESS,
                ),
                postIterationExpression = AssignmentNode(
                    identifier = IdentifierNode("i"),
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
                enteringStatement = StatementNode(DefinitionNode(IdentifierNode("i"), IntegerNode("0"))),
                condition = BinaryOperatorNode(
                    lhs = IdentifierNode("i"),
                    rhs = IntegerNode("10"),
                    op = BinaryOperatorType.LESS,
                ),
                postIterationExpression = AssignmentNode(
                    identifier = IdentifierNode("i"),
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

    @Test
    fun testAstComparatorOnWhileLoopASTs() {
        /**
         * Program:
         * ```
         * var i = 0;
         * var N = 10;
         * while(i < N) {
         *  print(i)
         * }
         * ```
         */

        val root = ProgramNode(listOf(
            StatementNode(DefinitionNode(IdentifierNode("i"), IntegerNode("0"))),
            StatementNode(DefinitionNode(IdentifierNode("N"), IntegerNode("10"))),
            StatementNode(WhileLoopNode(
                condition = BinaryOperatorNode(
                    lhs = IdentifierNode("i"),
                    rhs = IdentifierNode("N"),
                    op = BinaryOperatorType.LESS,
                ),
                body = ScopeNode(listOf(
                    StatementNode(FunctionCallNode(
                        identifier = IdentifierNode("print"),
                        arguments = listOf(IdentifierNode("i"))
                    ))
                ))
            ))
        ))

        val got = comparator.compare(root, root)
        assertEquals(true, got)
    }

    @Test
    fun testAstComparatorWithForAndWhileLoopsASTs() {
        /**
         * Program 1:
         * ```
         * for (var i = 0; i < 10; i = i + 1) {}
         * ```
         *
         * Program 2:
         * ```
         * while (i < 10) {}
         * ```
         */

        // program 1
        val root1 = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = StatementNode(DefinitionNode(IdentifierNode("i"), IntegerNode("0"))),
                condition = BinaryOperatorNode(
                    lhs = IdentifierNode("i"),
                    rhs = IntegerNode("10"),
                    op = BinaryOperatorType.LESS,
                ),
                postIterationExpression = AssignmentNode(
                    identifier = IdentifierNode("i"),
                    BinaryOperatorNode(
                        lhs = IdentifierNode("i"),
                        rhs = IntegerNode("1"),
                        op = BinaryOperatorType.PLUS,
                    ),
                ),
                body = ScopeNode(listOf())
            ))
        ))

        // program 2
        val root2 = ProgramNode(listOf(
            StatementNode(WhileLoopNode(
                condition = BinaryOperatorNode(
                    lhs = IdentifierNode("i"),
                    rhs = IdentifierNode("10"),
                    op = BinaryOperatorType.LESS,
                ),
                body = ScopeNode(listOf())
            ))
        ))

        val got = comparator.compare(root1, root2)
        assertEquals(false, got)
    }

    @Test
    fun testAstComparatorIfElseBranchesASTs() {
        /**
         * Program:
         * ```
         * var i = 22;
         * if (10 + 20 == 30 && i == 10) {}
         * else if (i == 0) {}
         * else {}
         * ```
         */

        val root = ProgramNode(listOf(
            StatementNode(DefinitionNode(IdentifierNode("i"), IntegerNode("22"))),
            StatementNode(IfNode(
                condition = BinaryOperatorNode(
                    BinaryOperatorNode(
                        BinaryOperatorNode(IntegerNode("10"), IntegerNode("20"), BinaryOperatorType.PLUS),
                        IntegerNode("30"),
                        BinaryOperatorType.EQUALS,
                    ),
                    BinaryOperatorNode(IdentifierNode("i"), IntegerNode("10"), BinaryOperatorType.EQUALS),
                    BinaryOperatorType.AND,
                ),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                otherwise = ScopeNode(listOf(
                    StatementNode(IfNode(
                        condition = BinaryOperatorNode(IdentifierNode("i"), IntegerNode("0"), BinaryOperatorType.EQUALS),
                        body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                        otherwise = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                    ))
                ))
            ))
        ))

        val got = comparator.compare(root, root)
        assertEquals(true, got)
    }


    @Test
    fun testAstComparatorWithDistinctIfElseBranchesASTs() {
        /**
         * Program 1:
         * ```
         * if (10 + 20 == 30 && i == 10) {}
         * else if (i == 0) {}
         * else {}
         * ```
         *
         * Program 2:
         * ```
         * if (10 + 20 == 30 && i == 10) {}
         * else if (i == 0) {}
         * ```
         */

        val root1 = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BinaryOperatorNode(
                    BinaryOperatorNode(
                        BinaryOperatorNode(IntegerNode("10"), IntegerNode("20"), BinaryOperatorType.PLUS),
                        IntegerNode("30"),
                        BinaryOperatorType.EQUALS,
                    ),
                    BinaryOperatorNode(IdentifierNode("i"), IntegerNode("10"), BinaryOperatorType.EQUALS),
                    BinaryOperatorType.AND,
                ),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                otherwise = ScopeNode(listOf(
                    StatementNode(IfNode(
                        // TODO: make 'condition' be an expression node
                        condition = BinaryOperatorNode(IdentifierNode("i"), IntegerNode("0"), BinaryOperatorType.EQUALS),
                        body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                        otherwise = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                    ))
                ))
            ))
        ))

        val root2 = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BinaryOperatorNode(
                    BinaryOperatorNode(
                        BinaryOperatorNode(IntegerNode("10"), IntegerNode("20"), BinaryOperatorType.PLUS),
                        IntegerNode("30"),
                        BinaryOperatorType.EQUALS,
                    ),
                    BinaryOperatorNode(IdentifierNode("i"), IntegerNode("10"), BinaryOperatorType.EQUALS),
                    BinaryOperatorType.AND,
                ),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                otherwise = ScopeNode(listOf(
                    StatementNode(IfNode(
                        condition = BinaryOperatorNode(IdentifierNode("i"), IntegerNode("0"), BinaryOperatorType.EQUALS),
                        body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                        otherwise = null,
                    ))
                ))
            ))
        ))

        val got = comparator.compare(root1, root2)
        assertEquals(false, got)
    }



    /**
     * Testing implemented of Parser interface.
     */

    @Test
    fun testEmptyTokensList() {
        val tokens = listOf<Token>()
        val root = parser.parse(tokens)

        val expected = ProgramNode(listOf())
        assertEquals(true, comparator.compare(expected, root))
    }

    @Test
    fun testOnlyEndToken() {
        val program = ""
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf())
        val root = parser.parse(tokens)

        assertEquals(true, comparator.compare(expected, root))
    }

    @Test
    fun testVariableDeclaration() {
        val identifier = "my_variable_123"
        val program = "var $identifier;"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DeclarationNode(IdentifierNode(identifier)))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testIntegerVariableDefinition() {
        val identifier = "my_var_123"
        val literal = "123"
        val program = "var $identifier = $literal;"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DefinitionNode(IdentifierNode(identifier), IntegerNode(literal)))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testFloatVariableDefinition() {
        val identifier = "my_var_123"
        val literal = "123.13"
        val program = "var $identifier = $literal;"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DefinitionNode(IdentifierNode(identifier), FloatNode(literal)))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testStringVariableDefinition() {
        val identifier = "my_var_123"
        val literal = "\"my str\""
        val program = "var $identifier = $literal;"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DefinitionNode(IdentifierNode(identifier), StringNode(literal)))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testBooleanTrueVariableDefinition() {
        val identifier = "my_var_123"
        val program = "var $identifier = true;"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DefinitionNode(IdentifierNode(identifier), BooleanTrueNode()))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }


    @Test
    fun testBooleanFalseVariableDefinition() {
        val identifier = "my_var_123"
        val program = "var $identifier = false;"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DefinitionNode(IdentifierNode(identifier), BooleanFalseNode()))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }


    @Test
    fun testVariableDeclarationAndAssignment() {
        val identifier = "my_var_123"
        val literal = "312"
        val program = "var $identifier;\n$identifier = $literal;"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DeclarationNode(IdentifierNode(identifier))),
            StatementNode(AssignmentNode(
                IdentifierNode(identifier),
                IntegerNode(literal)
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testVariableDefinitionInsideScopeBlock() {
        val identifier = "value"
        val literal = "10"
        val program = "{" +
                    "var $identifier = $literal;" +
                "}"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(ScopeNode(listOf(
                StatementNode(DefinitionNode(
                    IdentifierNode(identifier),
                    IntegerNode(literal),
                ))
            )))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testNestedScopeBlocks() {
        val program = """
            {
                {} {}
                {
                    {
                        {}
                    }
                }
            }
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        // TODO: 'ScopeNode(listOf())' or 'ScopeNode(listOf(StatementNode(NoOperationNode())))'?
        val expected = ProgramNode(listOf(
            StatementNode(ScopeNode(listOf(
                StatementNode(ScopeNode(listOf())),
                StatementNode(ScopeNode(listOf())),
                StatementNode(ScopeNode(listOf(
                    StatementNode(ScopeNode(listOf(
                        StatementNode(ScopeNode(listOf()))
                    )))
                )))
            )))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testAssignmentToAnotherVariable() {
        val identifier1 = "a"
        val identifier2 = "b"
        val literal = "10"

        val program = """
            var $identifier1;
            var $identifier2 = $literal;
            $identifier1 = $identifier2;
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DeclarationNode(IdentifierNode(identifier1))),
            StatementNode(DefinitionNode(IdentifierNode(identifier2), IntegerNode(literal))),
            StatementNode(AssignmentNode(IdentifierNode(identifier1), IdentifierNode(identifier2)))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testEmptyStatements() {
        val program = ";;;;"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(NoOperationNode()),
            StatementNode(NoOperationNode()),
            StatementNode(NoOperationNode()),
            StatementNode(NoOperationNode()),
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testIfConstructDefinition() {
        val program = "if (true) {}"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BooleanTrueNode(),
                // TODO: 'listOf()' or 'listOf(StatementNode(NoOperationNode()))'?
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                otherwise = null,
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testIfConstructDefinitionWithNoCurlyBraces() {
        val program = "if (true) callback(1, 2, 3);"
        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BooleanTrueNode(),
                body = ScopeNode(listOf(
                    StatementNode(FunctionCallNode(
                        identifier = IdentifierNode("callback"),
                        arguments = listOf(IntegerNode("1"), IntegerNode("2"), IntegerNode("3"))
                    ))
                )),
                otherwise = null,
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testIfElseConstructDefinition() {
        val program = """
            if (false) {
                var value = 10;
                print(value);
            }
            else {
                var value = "string";
            }
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BooleanFalseNode(),
                body = ScopeNode(listOf(
                    StatementNode(DefinitionNode(IdentifierNode("value"), IntegerNode("10"))),
                    StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IdentifierNode("value"))))
                )),
                otherwise = ScopeNode(listOf(
                    StatementNode(DefinitionNode(IdentifierNode("value"), StringNode("\"string\"")))
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testIfElseConstructDefinitionWithNoCurlyBraces() {
        val program = """
            if (false) print(10);
            else print(20);
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BooleanFalseNode(),
                body = ScopeNode(listOf(
                    StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("10"))))
                )),
                otherwise = ScopeNode(listOf(
                    StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("20"))))
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }


    @Test
    fun testIfElseIfConstructDefinition() {
        val program = """
            if (false) { print(10); }
            else if (true) { print(20); }
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BooleanFalseNode(),
                body = ScopeNode(listOf(
                    StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("10"))))
                )),
                otherwise = ScopeNode(listOf(
                    StatementNode(IfNode(
                        condition = BooleanTrueNode(),
                        body = ScopeNode(listOf(
                            StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("20"))))
                        )),
                        otherwise = null
                    ))
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testIfElseIfConstructDefinitionWithNoCurlyBraces() {
        val program = """
            if (false) print(10);
            else if (true) print(20);
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BooleanFalseNode(),
                body = ScopeNode(listOf(
                    StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("10"))))
                )),
                otherwise = ScopeNode(listOf(
                    StatementNode(IfNode(
                        condition = BooleanTrueNode(),
                        body = ScopeNode(listOf(
                            StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("20"))))
                        )),
                        otherwise = null,
                    ))
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testIfElseIfElseConstructDefinition() {
        val program = """
            if (false) { print(1); }
            else if (false) { print(2); }
            else { print(3); }
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BooleanFalseNode(),
                body = ScopeNode(listOf(
                    StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("1"))))
                )),
                otherwise = ScopeNode(listOf(
                    StatementNode(IfNode(
                        condition = BooleanFalseNode(),
                        body = ScopeNode(listOf(
                            StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("2"))))
                        )),
                        otherwise = ScopeNode(listOf(
                            StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("3"))))
                        ))
                    ))
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testIfElseIfElseConstructDefinitionWithNoCurlyBraces() {
        val program = """
            if (false);
            else if (false) print(2);
            else print(3);
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(IfNode(
                condition = BooleanFalseNode(),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
                otherwise = ScopeNode(listOf(
                    StatementNode(IfNode(
                        condition = BooleanFalseNode(),
                        body = ScopeNode(listOf(
                            StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("2"))))
                        )),
                        otherwise = ScopeNode(listOf(
                            StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IntegerNode("3"))))
                        )),
                    ))
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testForLoopDefinition() {
        val program = """
            for (var i = 0; i < 10; i = i + 1) {}
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = StatementNode(DefinitionNode(IdentifierNode("i"), IntegerNode("0"))),
                condition = BinaryOperatorNode(IdentifierNode("i"), IntegerNode("10"), BinaryOperatorType.LESS),
                postIterationExpression = AssignmentNode(
                    IdentifierNode("i"),
                    BinaryOperatorNode(IdentifierNode("i"), IntegerNode("1"), BinaryOperatorType.PLUS)
                ),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testForLoopDefinitionWithEmptyBlocks() {
        val program = """
            for (;;) {}
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = StatementNode(NoOperationNode()),
                condition = BooleanTrueNode(), // emptiness implies true expression
                postIterationExpression = NoOperationNode(),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testForLoopDefinitionWithNoVariableDefinition() {
        val program = """
            for (;i<10;i=i+1) {}
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = StatementNode(NoOperationNode()),
                condition = BinaryOperatorNode(IdentifierNode("i"), IntegerNode("10"), BinaryOperatorType.LESS),
                postIterationExpression = AssignmentNode(
                    IdentifierNode("i"),
                    BinaryOperatorNode(IdentifierNode("i"), IntegerNode("1"), BinaryOperatorType.PLUS)
                ),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testForLoopDefinitionWithNoPostIterationExpression() {
        val program = """
            for (var my_var = 0; my_var < N;) {}
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = StatementNode(DefinitionNode(IdentifierNode("my_var"), IntegerNode("0"))),
                condition = BinaryOperatorNode(IdentifierNode("my_var"), IdentifierNode("N"), BinaryOperatorType.LESS),
                postIterationExpression = NoOperationNode(),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testForLoopDefinitionWithNoConditionExpression() {
        val program = """
            for (var my_var = 0;;my_var = 2 * my_var) {}
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = StatementNode(DefinitionNode(IdentifierNode("my_var"), IntegerNode("0"))),
                condition = BooleanTrueNode(),
                postIterationExpression = AssignmentNode(
                    IdentifierNode("my_var"),
                    BinaryOperatorNode(IntegerNode("2"), IdentifierNode("my_var"), BinaryOperatorType.MULTIPLY)
                ),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testForLoopDefinitionNoCurlyBraces() {
        val program = """
            for (var v = 0; !(v == 10); v = v + 1);
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        // TODO: add support of '!=' operator
        val expected = ProgramNode(listOf(
            StatementNode(ForLoopNode(
                enteringStatement = StatementNode(DefinitionNode(IdentifierNode("v"), IntegerNode("0"))),
                condition = UnaryOperatorNode(
                    BinaryOperatorNode(IdentifierNode("v"), IntegerNode("10"), BinaryOperatorType.EQUALS),
                    UnaryOperatorType.NOT,
                ),
                postIterationExpression = AssignmentNode(
                    IdentifierNode("i"),
                    BinaryOperatorNode(IdentifierNode("i"), IntegerNode("1"), BinaryOperatorType.PLUS)
                ),
                body = ScopeNode(listOf(StatementNode(NoOperationNode()))),
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testWhileLoopDefinition() {
        val program = """
            while(i < 10) {}
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(WhileLoopNode(
                condition = BinaryOperatorNode(IdentifierNode("i"), IntegerNode("10"), BinaryOperatorType.LESS),
                body = ScopeNode(listOf(
                    StatementNode(NoOperationNode())
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testWhileLoopDefinitionNoCurlyBraces() {
        val program = """
            while(i < 10);
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(WhileLoopNode(
                condition = BinaryOperatorNode(IdentifierNode("i"), IntegerNode("10"), BinaryOperatorType.LESS),
                body = ScopeNode(listOf(
                    StatementNode(NoOperationNode())
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testFunctionDefinitionWithNoArguments() {
        val program = """
            function my_function() {
                var i = 0;
                print(i);
                return i;
            }
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(FunctionDefinitionNode(
                identifier = IdentifierNode("my_function"),
                arguments = listOf(),
                body = ScopeNode(listOf(
                    StatementNode(DefinitionNode(IdentifierNode("i"), IntegerNode("0"))),
                    StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IdentifierNode("i")))),
                    StatementNode(ReturnNode(IdentifierNode("i"))),
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testFunctionDefinitionWithSingleArgument() {
        val program = """
            function my_function(val) {
                print(val);
                return val * 2;
            }
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(FunctionDefinitionNode(
                identifier = IdentifierNode("my_function"),
                arguments = listOf(IdentifierNode("val")),
                body = ScopeNode(listOf(
                    StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IdentifierNode("val")))),
                    StatementNode(ReturnNode(
                        BinaryOperatorNode(
                            IdentifierNode("val"),
                            IntegerNode("2"),
                            BinaryOperatorType.MULTIPLY,
                        )
                    )),
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testFunctionDefinitionWithThreeArguments() {
        val program = """
            function my_func(val1, val2, val3) {
                return val1 + val2 + val3;
            }
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(FunctionDefinitionNode(
                identifier = IdentifierNode("my_function"),
                arguments = listOf(IdentifierNode("val1"), IdentifierNode("val2"), IdentifierNode("val3")),
                body = ScopeNode(listOf(
                    StatementNode(ReturnNode(
                        BinaryOperatorNode(
                            IdentifierNode("val1"),
                            BinaryOperatorNode(
                                IdentifierNode("val2"),
                                IdentifierNode("val3"),
                                BinaryOperatorType.PLUS
                            ),
                            BinaryOperatorType.PLUS,
                        )
                    )),
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testFunctionDefinitionInsideAnotherFunction() {
        val program = """
            function outer() {
                function inner(val1) {
                    print(val1);
                }
                
                inner(10);
            }
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(FunctionDefinitionNode(
                identifier = IdentifierNode("outer"),
                arguments = listOf(),
                body = ScopeNode(listOf(
                    StatementNode(FunctionDefinitionNode(
                        identifier = IdentifierNode("inner"),
                        arguments = listOf(IdentifierNode("val1")),
                        body = ScopeNode(listOf(
                            StatementNode(FunctionCallNode(IdentifierNode("print"), listOf(IdentifierNode("val1"))))
                        ))
                    )),
                    StatementNode(FunctionCallNode(IdentifierNode("inner"), listOf(IntegerNode("10")))),
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testFunctionCallWithNoArguments() {
        val program = """
            my_function();
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(FunctionCallNode(IdentifierNode("my_function"), listOf()))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testFunctionCallWithSingleArgument() {
        val program = """
            my_function_single_arg(1000);
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(FunctionCallNode(IdentifierNode("my_function"), listOf(
                IntegerNode("1000")
            )))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testFunctionCallWithThreeArguments() {
        val program = """
            my_func(123, "str", false);
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(FunctionCallNode(IdentifierNode("my_function"), listOf(
                IntegerNode("123"), StringNode("\"str\""), BooleanFalseNode()
            )))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }

    @Test
    fun testAssignmentToFunctionCallResult() {
        val program = """
            var variable = my_function(1, 2, 3);
        """.trimIndent()

        val tokens = lexer.tokenize(program)

        val expected = ProgramNode(listOf(
            StatementNode(DefinitionNode(
                IdentifierNode("variable"),
                FunctionCallNode(IdentifierNode("my_function"), listOf(
                    IntegerNode("1"), IntegerNode("2"), IntegerNode("3")
                ))
            ))
        ))

        val root = parser.parse(tokens)

        val got = comparator.compare(expected, root)
        assertEquals(true, got)
    }
}