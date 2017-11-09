package eu.simuline.relana.expressions;

import eu.simuline.relana.model.Deficiency;
import eu.simuline.relana.model.DeficiencyMap;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Represents the operations as intersection, union, complement, 
 * covariant and contravariant functors and maps. 
 *
 *
 * Created: Fri Apr 29 01:57:48 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public abstract class Operation {

    /**
     * Comprises the basic set theoretic operations 
     * union, intersection and complement. 
     */
    public enum BaseOps {

	/* ---------------------------------------------------------------- *
	 * constructor constants.                                           *
	 * ---------------------------------------------------------------- */

	Intersection(new IntsOp()) {
	    public String toString() {
		return "&";
	    }
	},
	Union(new UnionOp()) {
	    public String toString() {
		return "|";
	    }
	},
	Complement(new CompOp()) {
	    public String toString() {
		return "~";
	    }
	};

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	BaseOps(Operation oper) {
	    this.oper = oper;
	}

	/* ---------------------------------------------------------------- *
	 * class constants and initializer.                                 *
	 * ---------------------------------------------------------------- */

	private static final Map<String, BaseOps> KEY2OP;

	static {
	    KEY2OP = new HashMap<String, BaseOps>();
	    KEY2OP.put(Intersection.toString(), Intersection);
	    KEY2OP.put(Union       .toString(), Union       );
	    KEY2OP.put(Complement  .toString(), Complement  );
	}

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	private Operation oper;

	/* ---------------------------------------------------------------- *
	 * methods.                                                          *
	 * ---------------------------------------------------------------- */

	Operation getOperation() {
	    return this.oper;
	}

	public static Operation getOperation(String str) {
	    BaseOps oper = KEY2OP.get(str);
	    if (oper == null) {
		throw new IllegalStateException
		    ("Unknown operation \"" + str + "\". ");
	    }
	    return oper.getOperation();
	}
    } // enum BaseOps 

    /**
     * Defines the basic set theoretic intersection. 
     */
    static final class IntsOp extends Operation implements Eval {
	//private Type type;
	IntsOp() {
	    //this.type = type;
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                          *
	 * ---------------------------------------------------------------- */

	boolean arity1() {
	    return false;
	}

	public Type retType(Set<FormulaDecl> args) {
	    Iterator<FormulaDecl> iter = args.iterator();
	    if (!iter.hasNext()) {
		throw new IllegalArgumentException
		    ("Expected at least one argument. ");
	    }
	    Type proto = iter.next().retType();
	    //Type cand;
	    while (iter.hasNext()) {
		if (!proto.equals(iter.next().retType())) {
		    // prepare types 
		    List<Type> argTypes = new ArrayList<Type>();
		    for (FormulaDecl form : args) {
			argTypes.add(form.retType());
		    }
		    throw new IllegalArgumentException
			("Expected all the same types; found " + args + 
			 " with types " + argTypes + ". ");
		}	
	    }
	    return proto;
	}

	public Set<Deficiency> eval(Set<Set<Deficiency>> param) {
	    Iterator<Set<Deficiency>> iter = param.iterator();
	    assert iter.hasNext();
	    Set<Deficiency> result = new HashSet<Deficiency>(iter.next());
	    while (iter.hasNext()) {
		result.retainAll(iter.next());
	    }
	    return result;
	}

	public Eval getEval(Type type) {
	    return this;
	}

	public Operation getOperation() {
	    return this;
	}

	boolean isIsoAntitone() {
	    return true;
	}

	public String toString() {
	    return "&";
	}
    } // class IntsOp 

    /**
     * Defines the basic set theoretic union complement. 
     */
    static final class UnionOp extends Operation implements Eval {
	boolean arity1() {
	    return false;
	}
	public Type retType(Set<FormulaDecl> args) {
	    Iterator<FormulaDecl> iter = args.iterator();
	    if (!iter.hasNext()) {
		throw new IllegalArgumentException
		    ("Expected at least one argument. ");
	    }
	    Type proto = iter.next().retType();
//	    Type cand;
	    while (iter.hasNext()) {
		if (!proto.equals(iter.next().retType())) {
		    // prepare types 
		    List<Type> argTypes = new ArrayList<Type>();
		    for (FormulaDecl form : args) {
			argTypes.add(form.retType());
		    }
		    throw new IllegalArgumentException
			("Expected all the same types; found " + args + 
			 " with types " + argTypes + ". ");
		}	
	    }
	    return proto;
	}
	public Set<Deficiency> eval(Set<Set<Deficiency>> param) {
	    Set<Deficiency> result = new HashSet<Deficiency>();
	    for (Set<Deficiency> set : param) {
		result.addAll(set);
	    }
	    return result;
	}
	public Eval getEval(Type type) {
	    return this;
	}
	public Operation getOperation() {
	    return this;
	}

	boolean isIsoAntitone() {
	    return true;
	}
	public String toString() {
	    return "|";
	}
    } // class UnionOp 

    /**
     * Defines the basic set theoretic complement. 
     */
    static final class CompOp extends Operation {
	boolean arity1() {
	    return true;
	}
	public Type retType(Set<FormulaDecl> args) {
	    Iterator<FormulaDecl> iter = args.iterator();
	    if (!iter.hasNext()) {
		throw new IllegalArgumentException
		    ("Expected at least one argument. ");
	    }
	    Type proto = iter.next().retType();
	    if (iter.hasNext()) {
		throw new IllegalArgumentException
		    ("Expected no more than one argument. ");
	    }
	    return proto.getInverse();

	}

	public Eval getEval(final Type type) {
	    return new Eval() {
		    public Set<Deficiency> eval(Set<Set<Deficiency>> param) {
			Set<Deficiency> result = type.asSet();
			Iterator<Set<Deficiency>> iter = param.iterator();
			result.removeAll(iter.next());
			assert !iter.hasNext();
			return result;
		    }
		    public Operation getOperation() {
			return CompOp.this;
		    }

		};
	}

	boolean isIsoAntitone() {
	    return false;
	}
	public String toString() {
	    return "~";
	}
    } // class CompOp 

    public enum Functor {

	/* ---------------------------------------------------------------- *
	 * constructor constants.                                           *
	 * ---------------------------------------------------------------- */

	Covariant() {
	    public boolean isAllowed(DeficiencyMap map) {
		return map.isTwistIsotone();
	    }
	    public String twistIsotone() {
		return "twist-isotone";
	    }
	    Set<Deficiency> eval(DeficiencyMap map, Set<Deficiency> defs) {
		return map.cov(defs);
	    }
	    Type source(DeficiencyMap map) {
		return map.source.getType();
	    }
	    Type target(DeficiencyMap map) {
		return map.target.getType();
	    }
	    String getSymbol() {
		return ",";
	    }
	},
	Contravariant() {
	    public boolean isAllowed(DeficiencyMap map) {
		return map.isIsotone();
	    }
	    public String twistIsotone() {
		return "isotone";
	    }
	    Set<Deficiency> eval(DeficiencyMap map, Set<Deficiency> defs) {
		return map.cont(defs);
	    }
	    Type source(DeficiencyMap map) {
		return map.target.getType();
	    }
	    Type target(DeficiencyMap map) {
		return map.source.getType();
	    }
	    String getSymbol() {
		return "'";
	    }
	};

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	public abstract boolean isAllowed(DeficiencyMap map);
	public abstract String twistIsotone();
	abstract Type source(DeficiencyMap map);
	abstract Type target(DeficiencyMap map);

	Set<Deficiency> eval(Set<Set<Deficiency>> param,
			     DeficiencyMap map) {
	    assert param.size() == 1;
	    return eval(map, param.iterator().next());
	}
	abstract Set<Deficiency> eval(DeficiencyMap map,
				      Set<Deficiency> defs);

	Type retType(Set<FormulaDecl> args, Maps mapOper) {
	    Type retType = args.iterator().next().retType();
	    if (!source(mapOper.getMap()).equals(retType)) {
		throw new IllegalArgumentException
		    ("Cannot apply \"" + mapOper + 
		     "\" to formula \"" + args.iterator().next() + 
		     "\" with return type " + retType + ". ");
	    }
	    return target(mapOper.getMap());
	}
	//abstract Operation getOperation(DeficiencyMap map);
	abstract String getSymbol();

	private static final Map<String, Operation.Functor> ACC2FUNCT;

	static { 
	    ACC2FUNCT = new HashMap<String, Operation.Functor>();
	    ACC2FUNCT.put(Operation.Functor.Covariant    .getSymbol(),
			  Operation.Functor.Covariant);
	    ACC2FUNCT.put(Operation.Functor.Contravariant.getSymbol(),
			  Operation.Functor.Contravariant);
	}

	public static Functor covCont(String acc) {
	    return ACC2FUNCT.get(acc);
	}
    } // enum Functor 

    public final static class Maps extends Operation implements Operation.Eval {

	/* ---------------------------------------------------------------- *
	 * fields.                                                          *
	 * ---------------------------------------------------------------- */

	private final String funName;
	private final boolean isInverted;
	private final DeficiencyMap map;
	private final Functor funct;

	/* ---------------------------------------------------------------- *
	 * constructors.                                                    *
	 * ---------------------------------------------------------------- */

	public Maps(String funName,
		    boolean isInverted,
		    DeficiencyMap map, 
		    Functor funct) {
	    this.funName = funName;
	    this.isInverted = isInverted;
	    this.map = map;
	    this.funct = funct;
	    if (!funct.isAllowed(map)) {
		throw new IllegalArgumentException
		    ("Map \"" + this.funName + 
		     "\" is not " + funct.twistIsotone() + 
		     " and so corresponding functor \"" + this + 
		     "\" is not defined. ");
	    }
	}

	/* ---------------------------------------------------------------- *
	 * methods.                                                         *
	 * ---------------------------------------------------------------- */

	private DeficiencyMap getMap() {
	    return this.map;
	}

	boolean arity1() {
	    return true;
	}
	public Type retType(Set<FormulaDecl> args) {
	    return this.funct.retType(args, this);
	}
	public Set<Deficiency> eval(Set<Set<Deficiency>> param) {
	    return this.funct.eval(param, this.map);
	}

	public Operation.Eval getEval(Type type) {
	    return this;
	}

	public Operation getOperation() {
	    return this;
	}

	// **** caution: if false: 
	// then the operation is assumed to be antitone
	public boolean isIsoAntitone() {
	    return true;
	}
	public String toString() {
	    StringBuffer res = new StringBuffer();
	    res.append(this.funName);
	    if (this.isInverted) {
		res.append('!');
	    }
	    res.append(this.funct.getSymbol());
	    return res.toString();
	}
    } // class Maps

    /**
     *
     */
    public interface Eval {
	// length == 1 if arity1() 
	Set<Deficiency> eval(Set<Set<Deficiency>> param);
	Operation getOperation();
    } // interface Eval 

    /* -------------------------------------------------------------------- *
     * constructors.                                                        *
     * -------------------------------------------------------------------- */

    public Operation() {
		    // is empty. 
    } // Operation constructor

    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    // whether arity of parameter list must be one; 
    // otherwise it is an arbitrary but POSITIVE number. 
    abstract boolean arity1();
    // **** caution: if false: then the operation is assumed to be antitone
    abstract boolean isIsoAntitone();

    public abstract Type retType(Set<FormulaDecl> args);
    public abstract Eval getEval(Type type);



    public static Operation getOperation(BaseOps baseOps) {
	return baseOps.getOperation();
    }

    public static Operation getOperation(String funName,
					 boolean isInverted,
					 DeficiencyMap map, 
					 Functor funct) {
	return new Maps(funName, isInverted, map, funct);
    }

} // Operation
