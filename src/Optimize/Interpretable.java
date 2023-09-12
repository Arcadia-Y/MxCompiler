package Optimize;

import IR.Node.Var;
import Optimize.ConstantPropagation.CPInfo;

public interface Interpretable {
    public CPInfo interpret();
    public Var getRes();
}
