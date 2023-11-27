package components.parser.visitors

import components.parser.nodes.*
import components.parser.nodes.auxiliary.EndNode
import components.parser.nodes.auxiliary.InvalidNode
import components.parser.nodes.auxiliary.NoOperationNode
import components.parser.nodes.constructs.*
import components.parser.nodes.literals.*
import components.parser.nodes.operators.*


interface Visitor {
    fun visit(node: ProgramNode)
    fun visit(node: StatementNode)
    fun visit(node: IdentifierNode)

    // operators
    fun visit(node: AssignmentNode)
    fun visit(node: UnaryOperatorNode)
    fun visit(node: BinaryOperatorNode)

    // literals
    fun visit(node: IntegerNode)
    fun visit(node: FloatNode)
    fun visit(node: StringNode)
    fun visit(node: BooleanTrueNode)
    fun visit(node: BooleanFalseNode)

    // constructs
    fun visit(node: FunctionNode)
    fun visit(node: IfNode)
    fun visit(node: ElseNode)
    fun visit(node: ForLoopNode)
    fun visit(node: WhileLoopNode)
    fun visit(node: ScopeNode)
    fun visit(node: DefinitionNode)
    fun visit(node: FunctionCallNode)

    fun visit(node: EndNode)
    fun visit(node: InvalidNode)
    fun visit(node: NoOperationNode)
}