package eu.simuline.relana.expressions;

import eu.simuline.relana.model.Deficiency;
import eu.simuline.relana.model.CClass;
import eu.simuline.relana.model.CInstance;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a formula declaration which is either a constant, a variable 
 * or composed via an operator. 
 * Note the difference to a {@link Formula}. 
 *
 *
 * Created: Fri Apr 29 10:56:37 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public abstract class FormulaDecl {

    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    /**
     * Represents atomic formulae consisting of a constant only. 
     */
    static final class Const extends FormulaDecl {
	private final Set<Deficiency> val;
	private final Type type;
	Const(Type type, Set<Deficiency> val) {
	    this.type = type;
	    this.val  = val;
	}
	public Type retType() {
	    return this.type;
	}
	public Formula resolve(CInstance cInst) {
	    return new Formula.Const(this.val, this.type);
	}

	public String toString() {
	    StringBuffer res = new StringBuffer();
	    res.append('<');
	    res.append(this.type);
	    res.append(">{");
	    res.append(this.val);
	    res.append('}');
	    return res.toString();
	}
    } // class Const 

    /**
     * Represents atomic formulae consisting of a variable only. 
     */
    static final class Var extends FormulaDecl {
	private final CClass.SClassDecl decl;
	// from variable where the root formula is defined to this variable 
	// which is "more interior". 
	private final List<String> path;

	Var(CClass.SClassDecl decl, List<String> path) {
	    this.decl = decl;
	    this.path = path;
	}
	public Type retType() {
	    return this.decl.getSClass().getType();
	}
	public Formula resolve(CInstance cInst) {
	    return new Formula.Var(cInst.getEffect(this.path),
				   this.decl.getName());
	}
	public String toString() {
	    StringBuffer res = new StringBuffer();
	    res.append("$" + this.decl.getName() + "$");
	    return res.toString();
	}
    } // class Var 

    /**
     * Represents composite formulae 
     * consisting of a operation {@link #oper} 
     * and a set of arguments {@link #args}. 
     */
    static final class Comp extends FormulaDecl {

	private final Operation oper;
	private final Set<FormulaDecl> args;

	Comp(Operation oper, Set<FormulaDecl> args) {
	    this.oper = oper;
	    this.args = args;
	    retType(); // for tests only 
	}

	public Type retType() {
	    return this.oper.retType(this.args);
	}

	public Formula resolve(CInstance cInst) {
	    Set<Formula> fArgs = new HashSet<Formula>();
	    for (FormulaDecl decl : this.args) {
		fArgs.add(decl.resolve(cInst));
	    }

	    return Formula.getFormula(this.oper.getEval(retType()), fArgs);
	}

	public String toString() {
	    StringBuffer res = new StringBuffer();
	    // remove the enclosing brackets from argument list 
	    String argsStr = this.args.toString();
	    argsStr = argsStr.substring(1, argsStr.length() - 2);

	    res.append(this.oper);
	    res.append('(');
	    res.append(argsStr);
	    res.append(')');
	    return res.toString();
	}
    } // class Var 


    /* -------------------------------------------------------------------- *
     * static creator methods used by CClassParser.                         *
     * -------------------------------------------------------------------- */

    private FormulaDecl() {
    }

    public static FormulaDecl getConst(Type type, Set<Deficiency> val) {
	return new Const(type, val);
    }

    public static FormulaDecl getVar(CClass.SClassDecl decl,
				     List<String> path) {
	return new Var(decl, path);
    }

    public static FormulaDecl getComp(Operation oper, Set<FormulaDecl> args) {
	return new Comp(oper, args);
    }

    /* -------------------------------------------------------------------- *
     * further methods.                                                     *
     * -------------------------------------------------------------------- */

    public abstract Type retType();

    public abstract Formula resolve(CInstance cInst);

    public final boolean equals(Object obj) {
	return super.equals(obj);
    }

    public final int hashCode() {
	return super.hashCode();
    }


} // FormulaDecl
