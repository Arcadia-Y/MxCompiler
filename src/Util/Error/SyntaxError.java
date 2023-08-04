package Util.Error;

import Util.Position;

public class SyntaxError extends Error{
    public SyntaxError(String msg, Position pos) {
        super("\033[01;31mSyntax Error\033[0m: " + msg, pos);
    }
}
