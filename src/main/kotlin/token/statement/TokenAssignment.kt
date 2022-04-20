package token.statement

import evaluation.FunctionEvaluation.toVariable
import lexer.Parser
import lexer.PositionalException
import properties.Type
import table.SymbolTable
import token.Token
import token.TokenIdentifier
import token.operator.TokenIndexing
import token.TokenLink
import token.operator.TokenOperator

class TokenAssignment(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?,
    children: MutableList<Token> = mutableListOf()
) : TokenOperator(symbol, value, position, bindingPower, nud, led, std) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    var parent: Type? = null
    val name: String get() = left.value

    fun canEvaluate(): Boolean = right.find("(IDENT)") == null
            && right.find("parent") == null

    override fun evaluate(symbolTable: SymbolTable): Any {
        val value = right.evaluate(symbolTable)
        assignLValue(left, value, symbolTable.parent, symbolTable)
        return value
    }

//    override fun copy(): TokenAssignment = TokenAssignment(
//        symbol,
//        value,
//        position,
//        bindingPower,
//        nud,
//        led,
//        std,
//        children.map { it.copy() }.toMutableList()
//    )


    private fun assignLValue(token: Token, value: Any, parent: SymbolTable.Type?, symbolTable: SymbolTable) {
        if (token is TokenIdentifier) {
            symbolTable.addVariable(token.value, value.toVariable(token, parent))
            return
        }
        // all variables inside PArray property of type won't have such type as parent
        if (token is TokenIndexing) {
            val (array, index) = token.getArrayAndIndex(symbolTable)
            array.getPValue()[index] = value.toVariable(right, null)
            return
        }
        var importTable = symbolTable
        var current = token
        while (current is TokenLink) {
            // left is type
            if (importTable.getVariableOrNull(current.left) != null) {
                val type = importTable.getVariableOrNull(current.left)
                if (type is Type) {
                    importTable = type.symbolTable
                    current = current.right
                } else throw PositionalException("primitive does not contain properties", current.left)
            } else if (importTable.getObjectOrNull(current.left) != null) {
                importTable = importTable.getObjectOrNull(current.left)!!.symbolTable
                current = current.right
            } else if (importTable.getImportOrNull(current.left) != null) {
                importTable = SymbolTable(currentFile = current.left.value)
                current = current.right
            }
        }
        if (current is TokenIdentifier)
            importTable.addVariable(current.value, value.toVariable(current, parent))
        else throw PositionalException("expected identifier or link", current)
    }
}