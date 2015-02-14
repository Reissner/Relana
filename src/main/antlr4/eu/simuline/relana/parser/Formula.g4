
/**
 * A parser for relana-formulae. 
 * The main method of the parser is {@link #getFormula(CClass)}. 
 */
grammar Formula;

@parser::header {
    import eu.simuline.relana.model.CClass;
    import eu.simuline.relana.model.SClass;
    import eu.simuline.relana.model.CClassLoader;
    import eu.simuline.relana.model.MapDecl;
    import eu.simuline.relana.model.Deficiency;
    import eu.simuline.relana.model.DeficiencyMap;
    import eu.simuline.relana.model.ClassLocator;

    import eu.simuline.relana.expressions.FormulaDecl;
    import eu.simuline.relana.expressions.Operation;
    import eu.simuline.relana.expressions.Type;

    import java.io.Reader;
    import java.io.IOException;

    import java.util.Set;
    import java.util.HashSet;
    import java.util.Stack;
} // @parser::header 

@parser::members {

    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    private CClassLoader classLoader;
    private CClass cClass;
    private ClassLocator loc;
    private boolean exceptionThrown;

    private int lineNumber;
    private int colnNumber;

    // for data exchange between nodes **** 

    /**
     * The stack of incompletely parsed levels of formulae. 
     * Created by getCompFormula, populated by addFormula
     */
    Stack<Set<FormulaDecl>> argsStack = new Stack<Set<FormulaDecl>>();

    /**
     * Represents a path. 
     * Required by constFormula and varFormula, set by path. 
     */
    List<String> path;

    /**
     * 'Returned by constFormula, varFormula and compFormula. 
     * For recursive parsing of nested formulae: 
     * Added as an argument of an enclosing formula to {@link #argsStack} 
     * by rule addFormula. 
     */
    FormulaDecl fDecl;

    /* -------------------------------------------------------------------- *
     * constructors and creator methods.                                    *
     * -------------------------------------------------------------------- */

//    private static org.antlr.v4.runtime.CommonTokenStream 
//        reader2tokenStream(Reader reader)   
//		throws IOException {
//        org.antlr.v4.runtime.ANTLRInputStream antlrStream = 
//            new org.antlr.v4.runtime.ANTLRInputStream(reader);
//        FormulaLexer lexer = new FormulaLexer(antlrStream);
//        return new CommonTokenStream(lexer);
//    }

//    FormulaParser(Reader reader) {
//        this(reader2tokenStream(reader));
//    }


    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    void setLineColNum(int lineNumber, int colnNumber) {
        this.lineNumber = lineNumber;
        this.colnNumber = colnNumber;
    }

    /**
     * To set the <code>CClassLoader<code>. 
     * This is needed whenever the definition of the class currently read 
     * relies on definitions of other classes such as 
     * the superclass if it is given explicitly. 
     * 
     * @param classLoader
     *    the current <code>SClassLoader<code>. 
     */
    public void setClassLoader(CClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets the locator for the class currently parsed. 
     * This is needed to analyze formulae in a second parsing step 
     * when all <code>SClass</code> declarations are clarified. 
     * 
     * @param loc
     *    the location of the class currently parsed. 
     */
    void setLocator(ClassLocator loc) {
        this.loc = loc;
    }

    /**
     * Sets the <code>CClass</code> for the class currently parsed 
     * but without formulae. 
     * This is needed to analyze formulae in a second parsing step 
     * when all <code>SClass</code> declarations are clarified. 
     * 
     * @param cClass
     *    
     */
    void setCClass(CClass cClass) {
        this.cClass = cClass;
    }

    /**
     * Returns the all in all formula invoking rule 'formula'. 
     */
    FormulaDecl getFormulaStart() {
        formula();
        return this.fDecl;
    }

    /**
     * Returns a string comprising the current class, 
     * the number of the current line and column, 
     * the last token successfully read and the token tp be read next. 
     *
     * @return
     *    a <code>String</code> of the form 
     *    <code>[{@link #loc}] line ##, column ## 
     *    between tokenRead and tokenToBeRead</code>. 
     */
    String getLocation() {
        StringBuffer result = new StringBuffer();
        Token token = this.getTokenStream().LT(0);
        result.append("[" + this.loc + "] ");
        if (token == null) {
            result.append("no token ");
        } else {
            result.append("line "     + 
                          (token.getLine()-1            +this.lineNumber));
            result.append(", column " + 
                          (token.getCharPositionInLine()+this.colnNumber));
        result.append(", after \"" + token);
        }
        result.append("\": ");
        
        return result.toString();
    }

    /**
     * Reports an error and also the location where it occurred. 
     * **** same as in SClassParser **** 
     * 
     * @param msg 
     *    the message to be displayed. 
     */
//    void report(String msg) throws ParseException {
    void report(String msg) throws RuntimeException {
        System.out.print(getLocation());
        RuntimeException pe = new RuntimeException(msg);
        //System.out.println(pe.getMessage());
        this.exceptionThrown = true;
        throw pe;
    } // report

//    public static void main(String[] args) throws Exception {
//        Reader str = new java.io.StringReader(args[0]);
//System.out.println("str: "+str);
//java.io.StringWriter wr = new java.io.StringWriter();
//while (true) {
//int ch = str.read();
//if (ch == -1) {System.out.println("wr: "+wr);break;}
//wr.write(ch);
//}
//        FormulaParser fParser = new FormulaParser((Reader)null);
//fParser.setCClass(CClass.COMPONENT);
//        fParser.ReInit(str);
//        //fParser.setLineColNum(entry.getValue().lineNumber,
//        //                      entry.getValue().colnNumber);
//       fParser.getFormula();
//    }

} // @members 

@rulecatch {
catch (RecognitionException e) {
//        if (this.failFirstError) {
                throw e;
//        }
//        reportError(e);
//        recover(input, e);
} // catch 
} // @rulecatch 

//@lexer::header {
//    package eu.simuline.relana.parser;
//} // @lexer::header 

/*
@lexer::members {
    // fail at first error 
    @Override
    public void displayRecognitionError(String[] tokenNames, 
        RecognitionException e) {
//        if (this.failFirstError) {
                String hdr = getErrorHeader(e);
                String msg = getErrorMessage(e, tokenNames);
                throw new RuntimeException(hdr + ":" + msg);
//throw e;
//        }
//                super.displayRecognitionError(tokenNames, e);
//                return;
    }
} // @lexer::members
*/

// ======================================================================== 
// Lexer 
// ======================================================================== 

WS : (' ' | '\t' | '\n' | '\r' | '\f') -> skip;

SingleLineComment : '//' ~( '\r' | '\n' )* -> skip;

MultiLineComment  : //'/*' (options {greedy=false;} : .)* '*/' -> skip;
'/*' (~'*' | '*' ~'/')* '*/' -> skip;

//PACKAGE:          'package';
//CLASS:            'class';
//MAPS:             'maps';
//MAP:              'map';
//MAPSTO:           '|-->';
//IDDOMAIN:         'id:';
//COMPONENTS:       'components';
//EFFECTS:          'effects';
//INPUT:            'input';
//OUTPUT:           'output';
//REPLACE:          'replace'; 
INV:              '!' ;
COV:              ',' ;
CONT:             '\'' ;
END:              ';' ;
SEP:              '.' ;
UNION:            '|' ;
INTERSECT:        '&' ;
COMPLEMENT:       '~' ;
NAME:         LETTER IDENTIFIER*;
//FLOAT:        NUMBER ('.' NUMBER)? (('E' | 'e') ('+' | '-')? NUMBER)?; 
//fragment NUMBER:           (DIGIT)+ ; 
fragment IDENTIFIER:      (LETTER | DIGIT | '_') ;
fragment LETTER:          (CAPITAL_LETTER | SMALL_LETTER) ;
fragment DIGIT:           '0'..'9';
fragment SMALL_LETTER:    'a'..'z';
fragment CAPITAL_LETTER:  'A'..'Z';


// ======================================================================== 
// Parser 
// ======================================================================== 


/**
 * Parses a formula. 
 * A formula is either 
 * a constant as defined by {@link #getConstFormula()}, 
 * a variable as defined by {@link #getVarFormula()}, 
 * a compound formula as defined by {@link #getCompFormula()}. 
 * 
 * @return
 *    a declaration of a formula. 
 * @see CClassParser#skipFormula
 */
formula //returns [FormulaDecl res] 
    : 
{this.fDecl = null;}

        (
            constFormula | 
            compFormula | 
            varFormula
        ) 
{assert this.fDecl != null;}
 //   {
            //$res=decl;
            //this.formulaStack.push(decl);
 //       }
;



/**
 * Parses a constant formula which has the form 
 * <code>&lt;path>{defi}</code>, 
 * where <code>path</code> points to ?an effect via components? 
 * and <code>defi</code> is the name of a deficiency. 
 * 
 * @return
 *    the declaration of the constant formula parsed. 
 */
constFormula //returns [FormulaDecl decl] 
@init{
    Set<Deficiency> defs = new HashSet<Deficiency>();
    assert this.fDecl == null;
    Type type = null;
}
    : '<' path '>' 
        {
            SClass sClass = null;
            try {
                sClass = this.classLoader.loadSClass
                    (ClassLocator.getLocator(this.path),
					 this.loc.getPackage());
            } catch (IOException ioe) {
                report("IOException while loading \"" 
                       + ClassLocator.getLocator(this.path) 
                       + "\" in package " + this.loc.getPackage() 
                       + ": " + ioe + ". ");
                sClass = null; // never reached. ****
            } catch (Exception e) {
					assert false;// **** to be removed 
					// after transition to v4: v4 and v3 exception. 
				    }
            assert sClass != null;
            type = sClass.getType();

            defs = new HashSet<Deficiency>();
        }
'{' (name=NAME {defs.add(new Deficiency($name.text));})* '}' 
        {
            if (!type.isValid(defs)) {
                report("Set " + defs + " does not conform with type " +
                       type + ". ");
            }
           this.fDecl = FormulaDecl.getConst(type, defs);
assert this.fDecl != null;
        }
    ;

/**
 * Parses a formula consisting of a single variable, 
 * the name of which is a path according to {@link #path()}. 
 * 
 * @return
 *    the declaration of the formula parsed. 
 */
varFormula //returns [FormulaDecl decl] 
    :  path
        {
            assert fDecl == null;
            CClass.SClassDecl declS = this.cClass.getEffectDecl(this.path);
            if (declS == null) {
                report("Found name " + this.path + 
                       " leading to an unknown effect. ");
            }
            this.fDecl = FormulaDecl.getVar(declS, this.path);
assert this.fDecl != null;
        };


/**
 * Parses a compound formula which has at least one operation recursively. 
 * A compound formula is written in prefix notation 
 * <code>oper(args)</code>, where <code>oper</code> is an operation 
 * and <code>args</code> is a comma sepatated list of arguments, 
 * which are in turn formulae. 
 * The operation is either union, intersection complement 
 * or a functor. 
 * A functor consists of a function name and of a sign 
 * which determines whether the functor is covariant or contravariant. 
 * 
 * @return
 *    the declaration of the formula parsed. 
 */
compFormula //returns [FormulaDecl decl] 
@init{
    this.argsStack.push(new HashSet<FormulaDecl>());
    assert this.fDecl == null;
    Operation oper = null;
}
    : 
        ( 
            opT=UNION | 
            opT=INTERSECT | 
            opT=COMPLEMENT | 
            (opT=NAME (invT=INV)? (accT=CONT | accT=COV) )
        ) 
        {
            String key = $opT.text;
            if ($accT == null) {
                // opT = <UNION> | <INTERSECT> | <COMPLEMENT> 
                assert $invT == null;
                oper = Operation.BaseOps.getOperation(key);
            } else {
                // opT = f, | opt = f'
                String funName = key;
                MapDecl mapDecl = this.cClass.getMapDecl(funName);
                if (mapDecl == null) {
                    report("Declared no map \"" + funName + "\". " );
                }
                DeficiencyMap map = mapDecl.getMap();
                boolean isInverted = $invT != null;
                if (isInverted) {
                    // replace map by its inverse 
                    map = map.getInverse();
                }
                oper = Operation.getOperation(funName,
                                              isInverted,
                                              map,
                                              Operation.Functor
                                              .covCont($accT.text));
            }
            assert oper != null;
// Here, the operation is read. 
        }
        (
            '(' addFormula (',' addFormula)* ')'
        )
        {
            this.fDecl = FormulaDecl.getComp(oper, this.argsStack.pop());
assert this.fDecl != null;
        };

/**
 * Adds a formula to a set of formulae. 
 * This is needed to read in the parameters of a compound formula. 
 * 
 * @param args
 *    a set of formulae: the arguments of a compound formula read so  far. 
 * @see #getCompFormula
 */
addFormula//[Set<FormulaDecl> args]
    : 
  formula 
{this.argsStack.peek().add(this.fDecl);};


/**
 * Returns a path as a list of strings. 
 * Used in getVarFormula and getConstFormula, 
 * **** same as in SClassParser **** 
 * 
 * @return
 *     a path as a list of strings. 
 *     The outermost package is given first. 
 */
path //returns [List<String> res] 
@init{this.path = new ArrayList<String>();}
    :       first=NAME {this.path.add($first.text);} 
        (SEP next=NAME {this.path.add( $next.text);})*;
