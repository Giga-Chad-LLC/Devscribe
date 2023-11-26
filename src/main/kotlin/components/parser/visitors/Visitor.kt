package components.parser.visitors

import components.parser.nodes.*
import components.parser.nodes.auxiliary.EndNode
import components.parser.nodes.auxiliary.InvalidNode
import components.parser.nodes.constructs.*
import components.parser.nodes.literals.*
import components.parser.nodes.operators.*


interface Visitor {
    fun visit(node: ProgramNode)
    fun visit(node: StatementNode)
    fun visit(node: IdentifierNode)

    // operators
    fun visit(node: AssignmentNode)
    fun visit(node: PlusNode)
    fun visit(node: MinusNode)
    fun visit(node: DivideNode)
    fun visit(node: MultiplyNode)
    fun visit(node: ModuloNode)
    fun visit(node: LessNode)
    fun visit(node: GreaterNode)
    fun visit(node: NotNode)
    fun visit(node: EqualsNode)

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

    fun visit(node: EndNode)
    fun visit(node: InvalidNode)
}