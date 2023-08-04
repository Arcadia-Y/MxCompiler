import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;
import Parser.MxLexer;
import Parser.MxParser;
import Semantic.SemanticChecker;
import Semantic.SymbolCollector;
import Util.Error.Error;
import Util.Error.MxErrorListener;
import AST.*;

public class test {
    private static boolean check(String path) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        boolean ans = true;
        while (reader.ready()) {
            String line = reader.readLine();
            if (line.contains("Verdict")) {
                if (line.contains("Success"))
                    ans = true;
                else 
                    ans = false;
                break;
            }
        }
        reader.close();
        try {
            InputStream is = new FileInputStream(path);
            MxLexer lexer = new MxLexer(CharStreams.fromStream(is));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MxErrorListener());
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MxParser parser = new MxParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new MxErrorListener());
            ParseTree tree = parser.program();
            //System.out.println(tree.toStringTree(parser));
            ASTBuilder astBuilder = new ASTBuilder();
            Program prog = (Program) astBuilder.visit(tree);
            //System.out.println("Finish building AST.");
            SymbolCollector collector = new SymbolCollector();
            collector.visit(prog);
            //System.out.println("Finish symbol collection.");
            SemanticChecker checker = new SemanticChecker();
            checker.visit(prog);
            return ans;
        } catch (Error e) {
            System.out.println(e.toString());
            return !ans;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader r = new BufferedReader(new FileReader("testcases/sema/judgelist.txt"));
        while (true) {
            String path = r.readLine();
            if (path == null) break;
            path = "testcases/sema/" + path;
            if (check(path))
                System.out.println("PASS: " + path + "\n");
            else {
                System.out.println("WRONG ANSWER: " + path);
                break;
            }
        }
        r.close();
    }
}
