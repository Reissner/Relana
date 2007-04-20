package eu.simuline.relana.expressions;

import eu.simuline.relana.model.Deficiency;
//import eu.simuline.relana.model.CClass;
import eu.simuline.relana.model.SInstance;

import java.util.Set;
import java.util.HashSet;

/**
 * Formula
 *
 *
 * Created: Fri Apr 29 10:56:37 2005
 *
 * @author <a href="mailto:ernst@local">Ernst Reissner</a>
 * @version 1.0
 */
public abstract class Formula {

    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

    public final static Formula EMPTY_EXPRESSION = 
    Const.create(new HashSet<Deficiency>(),
		 Type.getEmpty());

    public static class Const extends Formula {
	private final Set<Deficiency> val;
	private final Type type;
	public Const(Set<Deficiency> val,Type type) {
	    this.val  = val;
	    this.type = type;
	    if (this.type != null && !this.type.isValid(val)) {
		throw new IllegalArgumentException
		    ("invalid set " +this.val + 
		     " for type " + this.type + ". ");
	    }

	}
	private final static Const create(Set<Deficiency> val,Type type) {
	    return new Const(val,type);
	}

	public Formula remove(SInstance serv, Deficiency def) {
	    return Const.this;
	}
	public Formula add(SInstance serv, Deficiency def) {
	    return Const.this;
	}
	public Formula substitute(SInstance serv, Formula form) {
	    return Const.this;
	}
	
	public Set<SInstance> getVars() {
	    return new HashSet<SInstance>();
	}
	public Set<Deficiency> getConst() {
	    return this.val;
	}
	public Set<Deficiency> getMax() {
	    return this.val;
	}
	public Set<Deficiency> getMin() {
	    return this.val;
	}
	public String toString() {
	    return "<>{"+this.val+"}";
	}
    } // class Const 

    public static class Var extends Formula {
	private final SInstance varS;
	String name;
	public Var(SInstance varS,String name) {
	    this.varS = varS;
	    this.name = name;
	}
	public Formula remove(SInstance serv, Deficiency def) {
	    throw new eu.simuline.util.NotYetImplementedException();
	    // return new Var(var);
	}
	public Formula add(SInstance serv, Deficiency def) {
	    throw new eu.simuline.util.NotYetImplementedException();
	    // return new Var(var);
	}
	public Formula substitute(SInstance serv, Formula form) {
//System.out.println("-----serv : "+serv);
//System.out.println("this.var : "+this.var);
//System.out.println(" (serv == this.var): "+(serv == this.var));

	    return (serv == this.varS) ? form : Var.this;
	}
	public Set<SInstance> getVars() {
	    Set<SInstance> res = new HashSet<SInstance>();
	    res.add(this.varS);
	    return res;
	}
	public Set<Deficiency> getConst() {
	    return null;
	}
	public Set<Deficiency> getMax() {
	    return this.varS.getType().asSet();
	}
	public Set<Deficiency> getMin() {
	    return new HashSet<Deficiency>();
	}
	public String toString() {
	    StringBuffer res = new StringBuffer();
	    res.append("$" + this.name + "$");
	    return res.toString();
	}
    } // class Var 

    public static class Comp extends Formula {

	protected Set<Deficiency> min;
	protected Set<Deficiency> max;

	private final Operation.Eval oper;
	private final Set<Formula> args;

	private Comp(Operation.Eval oper, Set<Formula> args) {
	    this.oper = oper;
	    this.args = args;

	    Set<Set<Deficiency>> param = new HashSet<Set<Deficiency>>();
	    for (Formula arg : args) {
		param.add(arg.getMin());
	    }
	    this.min = oper.eval(param);

	    param = new HashSet<Set<Deficiency>>();
	    for (Formula arg : args) {
		param.add(arg.getMax());
	    }
	    this.max = this.oper.eval(param);

	    if (this.oper.getOperation().isIsoAntitone()) {
		// nothing to do 
		this.max = this.max;
		this.min = this.min;
	    } else {
		// exchange 
		Set<Deficiency> inter;
		inter = this.max;
		this.max = this.min;
		this.min = inter;
	    }
	}

	private final static Comp create(Operation.Eval oper, 
					 Set<Formula> args) {
	    return new Comp(oper, args);
	}

	public Formula remove(SInstance serv, Deficiency def) {
	    Set<Formula> newArgs = new HashSet<Formula>();
	    for (Formula form : args) {
		newArgs.add(form.remove(serv, def));
	    }
	    return getFormula(this.oper,newArgs);
	}
	public Formula add(SInstance serv, Deficiency def) {
	    Set<Formula> newArgs = new HashSet<Formula>();
	    for (Formula form : args) {
		newArgs.add(form.add(serv, def));
	    }
	    return getFormula(this.oper,newArgs);
	}

	public Formula substitute(SInstance serv, Formula form) {
	    Set<Formula> newArgs = new HashSet<Formula>();
	    for (Formula arg : this.args) {
		newArgs.add(arg.substitute(serv, form));
	    }
	    return getFormula(this.oper,newArgs);
	}
	public Set<SInstance> getVars() {
	    Set<SInstance> res = new HashSet<SInstance>();
	    for (Formula arg : args) {
		res.addAll(arg.getVars());
	    }
	    return res;
	}
	public Set<Deficiency> getConst() {
	    return null;
	}
	public Set<Deficiency> getMax() {
	    return this.max;
	}
	public Set<Deficiency> getMin() {
	    return this.min;
	}

	public String toString() {
	    StringBuffer res = new StringBuffer();
	    res.append("" + this.oper + "(" + this.args + ")");
	    return res.toString();
	}
    } // class Comp 


    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */


    /* -------------------------------------------------------------------- *
     * static creator methods.                                              *
     * -------------------------------------------------------------------- */

    public static Formula getFormula(Operation.Eval oper, Set<Formula> args) {
	Formula res = Comp.create(oper,args);
	return (res.getMin().size() == res.getMax().size())
	    ? Const.create(res.getMin(),null)
	    : res;
    }

    /* -------------------------------------------------------------------- *
     * further methods.                                                     *
     * -------------------------------------------------------------------- */

    public abstract Formula remove(SInstance serv, Deficiency def);
    public abstract Formula    add(SInstance serv, Deficiency def);
    public abstract Formula substitute(SInstance serv, Formula form);

    public abstract Set<SInstance> getVars();

    /**
     * Returns the constant represented by this formula, if any. 
     * If this is not a constant formula, 
     * i.e. an instance of {@link Formula.Const}, 
     * then <code>null</code> is returned. 
     *
     * @return
     *    <ul>
     *    <li>
     *    The constant represented by this formula 
     *    if this is an instance of {@link Formula.Const}. 
     *    <li>
     *    <code>null</code> otherwise. 
     *    </ul>
     */
    public abstract Set<Deficiency> getConst();
    public abstract Set<Deficiency> getMin();
    public abstract Set<Deficiency> getMax();
} // FormulaDecl
