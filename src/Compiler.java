import java.io.InputStream;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import ASM.ASMBuilder;
import ASM.Node.ASMModule;
import Parser.MxLexer;
import Parser.MxParser;
import Semantic.SemanticChecker;
import Semantic.SymbolCollector;
import Util.Error.Error;    
import Util.Error.MxErrorListener;
import AST.*;
import IR.IRBuilder;
import Optimize.*;

public class Compiler {
    private static void checkSema() throws Exception {
     try {
            InputStream is = System.in;
            MxLexer lexer = new MxLexer(CharStreams.fromStream(is));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MxParser parser = new MxParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());
            ParseTree tree = parser.program();
            ASTBuilder astBuilder = new ASTBuilder();
            Program prog = (Program) astBuilder.visit(tree);
            SymbolCollector collector = new SymbolCollector();
            collector.visit(prog);
            SemanticChecker checker = new SemanticChecker();
            checker.visit(prog);
            System.out.println("Semantic: \033[01;32mPASS\033[0m");
        } catch (Error e) {
            throw e;
        }
    }

    private static void generateASM() throws Exception {
        InputStream is = System.in;
        MxLexer lexer = new MxLexer(CharStreams.fromStream(is));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new MxErrorListener());
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MxParser parser = new MxParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new MxErrorListener());
        ParseTree tree = parser.program();


        ASTBuilder astBuilder = new ASTBuilder();
        Program prog = (Program) astBuilder.visit(tree);
        SymbolCollector collector = new SymbolCollector();
        collector.visit(prog);
        SemanticChecker checker = new SemanticChecker();
        checker.visit(prog);

        IRBuilder irBuilder = new IRBuilder();
        IR.Node.Module mod = irBuilder.getIR(prog);

        var funcInline = new FunctionInliner();
        SSAOptimizer ssa = new SSAOptimizer();
        var dce = new DeadCodeElimination();
        var cp = new ConstantPropagation();
        var globalOpt = new GlobalVariableOptimizer(mod);
        funcInline.run(mod);
        ssa.constructSSA(mod);
        for (var f : mod.funcDefs) {
            boolean changed = true;
            while (changed) {
                changed = false;
                changed |= cp.SimpleConstantPropagation(f);
                changed |= cp.dealAddSub(f);
                changed |= dce.codeElimination(f);
                changed |= dce.jumpElimination(f);
                changed |= dce.eliminateUnreachable(f);
                globalOpt.dealFunc(f);
            }
        }
        if (mod.funcDefs.size() > 25) {
            funcInline.run(mod);
            ssa.constructSSA(mod);
            for (var f : mod.funcDefs) {
                boolean changed = true;
                while (changed) {
                    changed = false;
                    changed |= cp.SimpleConstantPropagation(f);
                    changed |= cp.dealAddSub(f);
                    changed |= dce.codeElimination(f);
                    changed |= dce.jumpElimination(f);
                    changed |= dce.eliminateUnreachable(f);
                    globalOpt.dealFunc(f);
                }
            }
        }

        ssa.destroySSA(mod);
        new LivenessAnalyzer().run(mod);
        LinearScanRegisterAllocator regAlloc = new LinearScanRegisterAllocator();
        regAlloc.run(mod);

        ASMBuilder asmBuilder = new ASMBuilder(regAlloc.regSet);
        ASMModule asm = asmBuilder.generateASM(mod);
        System.out.print(asm.toString());
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java Compiler <filename>");
            return;
        } else if (args[0].equals("-fsyntax-only")) {
            System.out.println("Semantic check");
            checkSema();
        } else if (args[0].equals("-S")) {
            generateASM();
            return;
        } else {
            System.out.println("Unknown option");
            return;
        }
    }
}
