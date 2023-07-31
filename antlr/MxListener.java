// Generated from Mx.g4 by ANTLR 4.13.0
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MxParser}.
 */
public interface MxListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MxParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(MxParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(MxParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by the {@code var}
	 * labeled alternative in {@link MxParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterVar(MxParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code var}
	 * labeled alternative in {@link MxParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitVar(MxParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code func}
	 * labeled alternative in {@link MxParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterFunc(MxParser.FuncContext ctx);
	/**
	 * Exit a parse tree produced by the {@code func}
	 * labeled alternative in {@link MxParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitFunc(MxParser.FuncContext ctx);
	/**
	 * Enter a parse tree produced by the {@code class}
	 * labeled alternative in {@link MxParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterClass(MxParser.ClassContext ctx);
	/**
	 * Exit a parse tree produced by the {@code class}
	 * labeled alternative in {@link MxParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitClass(MxParser.ClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(MxParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(MxParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#blockStmt}.
	 * @param ctx the parse tree
	 */
	void enterBlockStmt(MxParser.BlockStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#blockStmt}.
	 * @param ctx the parse tree
	 */
	void exitBlockStmt(MxParser.BlockStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#exprStmt}.
	 * @param ctx the parse tree
	 */
	void enterExprStmt(MxParser.ExprStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#exprStmt}.
	 * @param ctx the parse tree
	 */
	void exitExprStmt(MxParser.ExprStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#ifStmt}.
	 * @param ctx the parse tree
	 */
	void enterIfStmt(MxParser.IfStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#ifStmt}.
	 * @param ctx the parse tree
	 */
	void exitIfStmt(MxParser.IfStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code while}
	 * labeled alternative in {@link MxParser#loopStmt}.
	 * @param ctx the parse tree
	 */
	void enterWhile(MxParser.WhileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code while}
	 * labeled alternative in {@link MxParser#loopStmt}.
	 * @param ctx the parse tree
	 */
	void exitWhile(MxParser.WhileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code for}
	 * labeled alternative in {@link MxParser#loopStmt}.
	 * @param ctx the parse tree
	 */
	void enterFor(MxParser.ForContext ctx);
	/**
	 * Exit a parse tree produced by the {@code for}
	 * labeled alternative in {@link MxParser#loopStmt}.
	 * @param ctx the parse tree
	 */
	void exitFor(MxParser.ForContext ctx);
	/**
	 * Enter a parse tree produced by the {@code continue}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 */
	void enterContinue(MxParser.ContinueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code continue}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 */
	void exitContinue(MxParser.ContinueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code break}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 */
	void enterBreak(MxParser.BreakContext ctx);
	/**
	 * Exit a parse tree produced by the {@code break}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 */
	void exitBreak(MxParser.BreakContext ctx);
	/**
	 * Enter a parse tree produced by the {@code return}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 */
	void enterReturn(MxParser.ReturnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code return}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 */
	void exitReturn(MxParser.ReturnContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDecl(MxParser.VarDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDecl(MxParser.VarDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#varInitDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarInitDecl(MxParser.VarInitDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#varInitDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarInitDecl(MxParser.VarInitDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#nonArrayType}.
	 * @param ctx the parse tree
	 */
	void enterNonArrayType(MxParser.NonArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#nonArrayType}.
	 * @param ctx the parse tree
	 */
	void exitNonArrayType(MxParser.NonArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(MxParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(MxParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#funcDecl}.
	 * @param ctx the parse tree
	 */
	void enterFuncDecl(MxParser.FuncDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#funcDecl}.
	 * @param ctx the parse tree
	 */
	void exitFuncDecl(MxParser.FuncDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#parameterDeclList}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclList(MxParser.ParameterDeclListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#parameterDeclList}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclList(MxParser.ParameterDeclListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#parameterDecl}.
	 * @param ctx the parse tree
	 */
	void enterParameterDecl(MxParser.ParameterDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#parameterDecl}.
	 * @param ctx the parse tree
	 */
	void exitParameterDecl(MxParser.ParameterDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void enterClassDecl(MxParser.ClassDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#classDecl}.
	 * @param ctx the parse tree
	 */
	void exitClassDecl(MxParser.ClassDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#memberDecl}.
	 * @param ctx the parse tree
	 */
	void enterMemberDecl(MxParser.MemberDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#memberDecl}.
	 * @param ctx the parse tree
	 */
	void exitMemberDecl(MxParser.MemberDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#constructDecl}.
	 * @param ctx the parse tree
	 */
	void enterConstructDecl(MxParser.ConstructDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#constructDecl}.
	 * @param ctx the parse tree
	 */
	void exitConstructDecl(MxParser.ConstructDeclContext ctx);
	/**
	 * Enter a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNewExpr(MxParser.NewExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNewExpr(MxParser.NewExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identifier}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(MxParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identifier}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(MxParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code memberAccess}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMemberAccess(MxParser.MemberAccessContext ctx);
	/**
	 * Exit a parse tree produced by the {@code memberAccess}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMemberAccess(MxParser.MemberAccessContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(MxParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(MxParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constant}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterConstant(MxParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constant}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitConstant(MxParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterSubscript(MxParser.SubscriptContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitSubscript(MxParser.SubscriptContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignment}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(MxParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignment}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(MxParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(MxParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(MxParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpr(MxParser.BinaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpr(MxParser.BinaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code postfix}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPostfix(MxParser.PostfixContext ctx);
	/**
	 * Exit a parse tree produced by the {@code postfix}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPostfix(MxParser.PostfixContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterSubExpr(MxParser.SubExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitSubExpr(MxParser.SubExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ternary}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterTernary(MxParser.TernaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ternary}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitTernary(MxParser.TernaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(MxParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(MxParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code array}
	 * labeled alternative in {@link MxParser#newItem}.
	 * @param ctx the parse tree
	 */
	void enterArray(MxParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code array}
	 * labeled alternative in {@link MxParser#newItem}.
	 * @param ctx the parse tree
	 */
	void exitArray(MxParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nonArray}
	 * labeled alternative in {@link MxParser#newItem}.
	 * @param ctx the parse tree
	 */
	void enterNonArray(MxParser.NonArrayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nonArray}
	 * labeled alternative in {@link MxParser#newItem}.
	 * @param ctx the parse tree
	 */
	void exitNonArray(MxParser.NonArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(MxParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(MxParser.LiteralContext ctx);
}