package Util.Error;

import Util.Position;

public class SemanticError extends Error {
    public SemanticError(String msg, Position pos) {
        super("Semantic Error: " + msg, pos);
      }
}
