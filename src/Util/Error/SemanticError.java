package Util.Error;

import Util.Position;

public class SemanticError extends Error {
    public SemanticError(String msg, Position pos) {
        super("\033[01;31mSemantic Error\033[0m: " + msg, pos);
      }
}
