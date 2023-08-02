// Generated from Mx.g4 by ANTLR 4.13.0
 
package Parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MxParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MxVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MxParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MxParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(MxParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(MxParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#blockStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStmt(MxParser.BlockStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#exprStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprStmt(MxParser.ExprStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#ifStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStmt(MxParser.IfStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code While}
	 * labeled alternative in {@link MxParser#loopStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile(MxParser.WhileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code For}
	 * labeled alternative in {@link MxParser#loopStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor(MxParser.ForContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Continue}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinue(MxParser.ContinueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Break}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreak(MxParser.BreakContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Return}
	 * labeled alternative in {@link MxParser#jumpStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn(MxParser.ReturnContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#varDeclStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclStmt(MxParser.VarDeclStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#varDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDecl(MxParser.VarDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#varInitDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarInitDecl(MxParser.VarInitDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#nonArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonArray(MxParser.NonArrayContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayType}
	 * labeled alternative in {@link MxParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayType(MxParser.ArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nonArrayType}
	 * labeled alternative in {@link MxParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNonArrayType(MxParser.NonArrayTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#funcDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncDecl(MxParser.FuncDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#parameterDeclList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDeclList(MxParser.ParameterDeclListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#parameterDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDecl(MxParser.ParameterDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#classDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassDecl(MxParser.ClassDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#memberDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberDecl(MxParser.MemberDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#constructDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstructDecl(MxParser.ConstructDeclContext ctx);
	/**
	 * Visit a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewExpr(MxParser.NewExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code memberAccess}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberAccess(MxParser.MemberAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(MxParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code constant}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(MxParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subscript}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubscript(MxParser.SubscriptContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignment}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(MxParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCall}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(MxParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code variable}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(MxParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryExpr(MxParser.BinaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code postfix}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfix(MxParser.PostfixContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subExpr}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubExpr(MxParser.SubExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ternary}
	 * labeled alternative in {@link MxParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTernary(MxParser.TernaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(MxParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code newArray}
	 * labeled alternative in {@link MxParser#newItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewArray(MxParser.NewArrayContext ctx);
	/**
	 * Visit a parse tree produced by the {@code newNonArray}
	 * labeled alternative in {@link MxParser#newItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNewNonArray(MxParser.NewNonArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link MxParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(MxParser.LiteralContext ctx);
}