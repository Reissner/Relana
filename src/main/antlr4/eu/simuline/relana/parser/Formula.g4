
/**
 * A parser for relana-formulae. 
 * The main method of the parser is {@link #getFormula(CClass)}. 
 */
grammar Formula;

@header {
    package eu.simuline.relana.parser;

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
} // @header 

@members {

    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    private CClassLoader classLoader;
    private CClass cClass;
    private ClassLocator loc;
    private boolean exceptionThrown;

    private int lineNumber;
    private int colnNumber;

    /* -------------------------------------------------------------------- *
     * constructors and creator methods.                                    *
     * -------------------------------------------------------------------- */

    private static CommonTokenStream reader2tokenStream(Reader reader)   
		throws IOException {
        ANTLRReaderStream antlrStream = new ANTLRReaderStream(reader);
        FormulaLexer lexer = new FormulaLexer(antlrStream);
        return new CommonTokenStream(lexer);
    }

    public FormulaParser(Reader reader)   
		throws IOException {
        this(reader2tokenStream(reader));
    }

    public FormulaParser(CommonTokenStream stream) {
        super(stream);
    }

    public void ReInit(Reader reader) throws IOException {
        setTokenStream(reader2tokenStream(reader));
    }

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
        result.append("line "     + (token.getLine()-1            +this.lineNumber));
        result.append(", column " + (token.getCharPositionInLine()+this.colnNumber));
        result.append(", after \"" + token);
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

    public static void main(String[] args) throws Exception {
        Reader str = new java.io.StringReader(args[0]);
System.out.println("str: "+str);
//java.io.StringWriter wr = new java.io.StringWriter();
//while (true) {
//int ch = str.read();
//if (ch == -1) {System.out.println("wr: "+wr);break;}
//wr.write(ch);
//}
        FormulaParser fParser = new FormulaParser((Reader)null);
fParser.setCClass(CClass.COMPONENT);
        fParser.ReInit(str);
        //fParser.setLineColNum(entry.getValue().lineNumber,
        //                      entry.getValue().colnNumber);
       fParser.getFormula();
    }

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

@lexer::header {
    package eu.simuline.relana.parser;
} // @lexer::header 

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

// ======================================================================== 
// Lexer 
// ======================================================================== 

WS : (' ' | '\t' | '\n' | '\r' | '\f') {skip();};

SingleLineComment : '//' ~( '\r' | '\n' )* {skip();};

MultiLineComment  : '/*' (options {greedy=false;} : .)* '*/' {skip();};

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
 * Start rule just wrapping {@link #getFormula(CClass)}. 
 * Purely formally, to avoid warning on missing start rule 
 * coming from the fact, that no eof is specified 
 * and that {@link #getFormula(CClass)} is defined recursively. 
 */
getFormulaStart returns [FormulaDecl res] 
    : res1=getFormula {$res=res1;};

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
getFormula returns [FormulaDecl res] 
    : 
        (
            decl=getConstFormula | 
            decl=getCompFormula | 
            decl=getVarFormula
        ) {$res=decl;};


/**
 * Parses a constant formula which has the form 
 * <code>&lt;path>{defi}</code>, 
 * where <code>path</code> points to ?an effect via components? 
 * and <code>defi</code> is the name of a deficiency. 
 * 
 * @return
 *    the declaration of the constant formula parsed. 
 */
getConstFormula returns [FormulaDecl decl] 
@init{
    Set<Deficiency> defs = new HashSet<Deficiency>();
    Type type = null;
}
    : '<' path=getPath '>' 
        {
            SClass sClass = null;
            try {
                sClass = this.classLoader.loadSClass
                    (ClassLocator.getLocator(path),
					 this.loc.getPackage());
            } catch (IOException ioe) {
                report("IOException while loading \"" 
                       + ClassLocator.getLocator(path) 
                       + "\" in package " + this.loc.getPackage() 
                       + ": " + ioe + ". ");
                sClass = null; // never reached. ****
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
            $decl = FormulaDecl.getConst(type,defs);
assert $decl != null;
        }
    ;

/**
 * Parses a formula consisting of a single variable, 
 * the name of which is a path according to {@link #getPath()}. 
 * 
 * @return
 *    the declaration of the formula parsed. 
 */
getVarFormula returns [FormulaDecl decl] 
    :  path = getPath
        {
            CClass.SClassDecl declS = this.cClass.getEffectDecl(path);
            if (declS == null) {
                report("Found name " + path + 
                       " leading to an unknown effect. ");
            }
            $decl = FormulaDecl.getVar(declS,path);
            //throw new eu.simuline.util.NotYetImplementedException();
assert $decl != null;
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
getCompFormula returns [FormulaDecl decl] 
@init{
    Set<FormulaDecl> args = new HashSet<FormulaDecl>();
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
            '('      addFormula[args] 
                (',' addFormula[args])* ')'
        )
        {
            $decl = FormulaDecl.getComp(oper,args);
assert $decl != null;
        };

/**
 * Adds a formula to a set of formulae. 
 * This is needed to read in the parameters of a compound formula. 
 * 
 * @param args
 *    a set of formulae: the arguments of a compound formula read so  far. 
 * @see #getCompFormula
 */
addFormula[Set<FormulaDecl> args]
    : arg=getFormula {args.add(arg);};


/**
 * Returns a path as a list of strings. 
 * **** same as in SClassParser **** 
 * 
 * @return
 *     a path as a list of strings. 
 *     The outermost package is given first. 
 */
getPath returns [List<String> res] 
@init{$res = new ArrayList<String>();}
    :       first=NAME {res.add($first.text);} 
        (SEP next=NAME {res.add( $next.text);})*;




