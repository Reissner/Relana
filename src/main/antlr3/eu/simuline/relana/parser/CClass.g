

grammar CClass;

@header {
    package eu.simuline.relana.parser;

    import eu.simuline.relana.model.CClass;
    import eu.simuline.relana.model.SClass;
    import eu.simuline.relana.model.CClassLoader;
    import eu.simuline.relana.model.ClassLocator;
    import eu.simuline.relana.model.CClassLink;
    import eu.simuline.relana.model.ProbDistr;
    import eu.simuline.relana.model.Deficiency;
    import eu.simuline.relana.model.MapDecl;

    import java.math.BigDecimal;

    import java.util.TreeMap;
    import java.util.Set;
    import java.util.HashSet;

    import java.io.Reader;
    import java.io.IOException;
} // @header 

@members {
    private CClassLoader classLoader;

    private ClassLocator loc;
    private boolean exceptionThrown;

    // *** only for reentry parsing formulae: should be separate parser. 
    static class FormulaWrapper {
        private int lineNumber;
        private int colnNumber;
        private String formula;

        FormulaWrapper(int lineNumber,
                       int colnNumber,
                       String formula) {
            this.lineNumber = lineNumber;
            this.colnNumber = colnNumber;
            this.formula = formula;
        }
    } // class FormulaWrapper 

    /*
     * Returns a string comprising the current class, 
     * the number of the current line and column, 
     * the last token successfully read and the token tp be read next. 
     *
     * @return
     *    a <code>String</code> of the form 
     *    <code>[{@link #loc}] line ##, column ## 
     *    between tokenRead and tokenToBeRead</code>. 
     */
//    private String getLocation() {
//       return getLocation(this.token);
//    }

//    private String getLocation(Token token) {
//        StringBuffer result = new StringBuffer();
//        result.append("[" + this.loc + "] ");

//        if (this.token.next == null) {
//            result.append("line "      + token.beginLine  );
//            result.append(", column "  + token.beginColumn);
//            result.append(", after \"" + token);
//            result.append("\": ");
//        } else {
//            result.append("line "        + token.next.beginLine  );
//            result.append(", column "    + token.next.beginColumn);
//            result.append(", between \"" + token);
//            result.append("\" and \""    + token.next);
//            result.append("\": ");
//       }
//        return result.toString();
//    }

    private static CommonTokenStream reader2tokenStream(Reader reader)   
		throws IOException {
        ANTLRReaderStream antlrStream = new ANTLRReaderStream(reader);
        CClassLexer lexer = new CClassLexer(antlrStream);
        return new CommonTokenStream(lexer);
    }

    public CClassParser(Reader reader)   
		throws IOException {
        this(reader2tokenStream(reader));
    }

    public CClassParser(CommonTokenStream stream) {
        super(stream);
    }

    public void ReInit(Reader reader)   
		throws IOException {
        setTokenStream(reader2tokenStream(reader));
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
     * Reports an error and also the location where it occurred. 
     * **** same as in SClassParser **** 
     * 
     * @param msg 
     *    the message to be displayed. 
     */
//    private void report(String msg) throws ParseException {
    private void report(String msg) throws RuntimeException {
//        System.out.print(getLocation());
        RuntimeException pe = new RuntimeException(msg);
//        ParseException pe = new ParseException(msg);
        //System.out.println(pe.getMessage());
        this.exceptionThrown = true;
        throw pe;
    } // report

//    private void report(Token token,String msg) throws ParseException {
    private void report(Token token,String msg) throws RuntimeException {
//        System.out.print(getLocation(token));
        RuntimeException pe = new RuntimeException(msg);
//        ParseException pe = new ParseException(msg);
        //System.out.println(pe.getMessage());
        this.exceptionThrown = true;
        throw pe;
    } // report
/*
private void report(Exception exc) throws ParseException {
       System.out.print(getLocation());
       throw exc;
} // report
*/

} // @members 

@lexer::header {
    package eu.simuline.relana.parser;
} // @lexer::header 


// ======================================================================== 
// Lexer 
// ======================================================================== 

WS : (' ' | '\t' | '\n' | '\r' | '\f') {skip();};

SingleLineComment : '//' ~( '\r' | '\n' )* {skip();};

MultiLineComment  : '/*' (options {greedy=false;} : .)* '*/' {skip();};

PACKAGE:          'package';
CCLASS:           'class';
EXTENDS:          'extends';
REDECLARE:        'redeclare';
MAPS:             'maps';
MAP:              'map';
MAPSTO:           '|-->';
IDDOMAIN:         'id:';
COMPONENTS:       'components';
EFFECTS:          'effects';
INPUT:            'input';
OUTPUT:           'output';
REPLACE:          'replace'; 
INV:              '!' ;
COV:              ',' ;
CONT:             '\'' ;
END:              ';' ;
SEP:              '.' ;
UNION:            '|' ;
INTERSECT:        '&' ;
COMPLEMENT:       '~' ;
NAME:         LETTER IDENTIFIER*;
FLOAT:        NUMBER ('.' NUMBER)? (('E' | 'e') ('+' | '-')? NUMBER)?; 
fragment NUMBER:           (DIGIT)+ ; 
fragment IDENTIFIER:      (LETTER | DIGIT | '_') ;
fragment LETTER:          (CAPITAL_LETTER | SMALL_LETTER) ;
fragment DIGIT:           '0'..'9';
fragment SMALL_LETTER:    'a'..'z';
fragment CAPITAL_LETTER:  'A'..'Z';


// ======================================================================== 
// Parser 
// ======================================================================== 

cClass[ClassLocator loc] returns [CClass res] 
@init {
//System.out.println("------------"+loc);
this.exceptionThrown = false;
//List<String> pkgPath;
//Token cClassName;
//CClass superClass;
//Map<String,MapDecl> mapsX;
//Map<String,CClass.SClassDecl> effectsX;
//Map<String,CClassLink> componentsX;
this.loc = loc;
Map<String,FormulaWrapper> incompEffects = 
			   new HashMap<String,FormulaWrapper>();
}
    : 
        PACKAGE  pkgPath = getPath END
        CCLASS   cClassName = NAME 
        superClass = getSuperClass 
        
        effectsX   = effects[incompEffects] 
        mapsX       = maps 
        componentsX = components 
        '}' EOF
    ;


getSuperClass returns [CClass res] 
    :  (EXTENDS superPath = getPath())? '{' ;


/**
 * Returns a path as a list of strings. 
 * **** same as in SClassParser and for FormulaParser**** 
 * 
 * @return
 *     a path as a list of strings. 
 *     The outermost package is given first. 
 */
getPath returns [List<String> res] 
@init{$res = new ArrayList<String>();}
    :       first=NAME {res.add($first.text);} 
        (SEP next=NAME {res.add( $next.text);})*;



maps returns [Map<String,MapDecl> res] 
@init{Map<String,MapDecl> name2map = new HashMap<String,MapDecl>();}
    : (MAPS (addMap[name2map])*)?
    ;

addMap[Map<String,MapDecl> name2map] 
@init{
    Map<Set<Deficiency>,Deficiency> setOfSrc2targ = 
        new HashMap<Set<Deficiency>,Deficiency>();
    Set<Deficiency> idDomain = new HashSet<Deficiency>();
}
    : (//<MAP> 
            (redeclare = REDECLARE)?
            nameT = NAME ':' invImgP = getPath '-->' imgP = getPath 
            '{' add2DefMap[setOfSrc2targ]* addToIdDom[idDomain]? '}' END)
    ;


add2DefMap[Map<Set<Deficiency>,Deficiency> setOfSrc2targ] 
@init{
    Set<Deficiency> invImg = new HashSet<Deficiency>();
}
    : ( '{' addDef[invImg]* '}' MAPSTO defT=NAME )
    ;


addDef[Set<Deficiency> defs] 
    : defT = NAME;



addToIdDom[Set<Deficiency> idDomain] 
    : ( IDDOMAIN '{' addDef[idDomain]* '}' );


effects[Map<String,FormulaWrapper> incompEffects] 
returns [Map<String,CClass.SClassDecl> res] 
@init {
Map<String,CClass.SClassDecl> effects = 
			      new TreeMap<String,CClass.SClassDecl>();
}
    : ( EFFECTS (effect[effects,incompEffects])* )?
    ;


effect[Map<String,CClass.SClassDecl> effects,
       Map<String,FormulaWrapper>    incompEffects] 
@init {
    Set<CClass.SClassModifier> accessModifiers = 
        new HashSet<CClass.SClassModifier>();
    SClass sClass = null;
}
    : 
        ( (redeclare = REDECLARE)?
            (addAccessModifier[accessModifiers])* 
            path=getPath 
            sNameT=NAME 
        ) 
        {
            // get class of effect 
            if (path.size() == 1 && 
                path.get(0).equals(SClass.BOOLEAN.getName())) {
                sClass = SClass.BOOLEAN;
            } else {
                try {
                    sClass = this.classLoader
                        .loadSClass(ClassLocator.getLocator(path),
                                    this.loc.getPackage());
                } catch (IOException ioe) {
                    report("IOException while loading \"" 
                           + ClassLocator.getLocator(path) 
                           + "\" in package this.loc.getPackage(): " 
                           + ioe + ". ");
                    sClass = null; // never reached. ****
                }
            }
            assert sClass != null;

//Token formT = null;
        }
        (
            (distr=getDistr[sClass] | ( formT='(' form=skipFormula ')') )?
            END
        )
        {
            // read in the rest: name, modifiers, distribution, formula if any. 
            String sName = sNameT.toString();
            CClass.SClassDecl oldDecl = effects
                .put(sName,
                     new CClass.SClassDecl(redeclare != null,
                                           accessModifiers,
                                           sClass,
                                           sNameT.toString(),
                                           distr));
            // consistency check: duplicate declaration 
            if (oldDecl != null) {
                report("Duplicate effect declaration \"" + sNameT + 
                       "\"; overwrite " + oldDecl + " by " + sClass + ". " );
            }

            // store formula for later analysis 
            if (form != null) {
                assert formT != null;
                // for use of second pass parsing formulae 
                int lineNumber = $formT.line;//beginLine;
                int colnNumber = $formT.pos;//beginColumn;

                // form comes from skipFormula and comes from res.toString 
                // if skipFormula is invoked, it is not null; otherwise it is.
                incompEffects.put(sName,
                                  new FormulaWrapper(lineNumber,
                                                     colnNumber,
                                                     form));
            }
        }
    ;

addAccessModifier[Set<CClass.SClassModifier> accessModifiers] 
    : ( modT=INPUT | modT=OUTPUT )
    ;


getDistr[SClass sClass] returns [ProbDistr res]
@init {
    Map<Deficiency,BigDecimal> def2prob  = new HashMap<Deficiency,BigDecimal>();
    Map<Deficiency,ProbDistr > def2distr = new HashMap<Deficiency,ProbDistr >();
}
    : '{' (addProbAlloc[def2prob] | REPLACE replDistr[def2distr,sClass])* '}'
    ;


replDistr[Map<Deficiency,ProbDistr> def2distr, SClass sClass] 
@init {
    SClass sClassInner = null;
}
    : 
        replDefT = NAME
        {
            throw new eu.simuline.util.NotYetImplementedException();
        }
        distrInner = getDistr[sClassInner]
        {
            throw new eu.simuline.util.NotYetImplementedException();
        }
    ;

addProbAlloc[Map<Deficiency,BigDecimal> def2prob] 
    : (defT = NAME ':' floatT = FLOAT)
        {
            throw new eu.simuline.util.NotYetImplementedException();
        }
    ;

skipFormula returns [String res]
@init {
    StringBuffer resB  = new StringBuffer();
}
    : appendFormula["",resB] {$res = resB.toString();};

appendFormula[String pre, StringBuffer res] 
    : 
        ( 
            ('<'  appendToken["<",res,">{"] '>' 
                '{' (appendToken["" ,res,""  ])* cls = '}')      // constant 
        | 
            ( ( (NAME INV? (COV|CONT) ) | 
                    UNION | INTERSECT | COMPLEMENT) '(' )=>  
            ( appendOp[res] 
          '(' appendFormula["(",res] 
        ( ',' appendFormula[",",res])* cls = ')' )  // compound
| 
   ( appendToken["" ,res,""]  
	     ('.' appendToken[".",res,""])* )       // variable 
);


appendToken[String pre,StringBuffer buf,String post] 
    : name = NAME;


appendOp[StringBuffer buf] 
    : 
        (
            ( funT=NAME (invT=INV)? (accT=CONT | accT=COV) ) | 
            opT=UNION | 
            opT=INTERSECT | 
            opT=COMPLEMENT 
        )
    ;


components returns [Map<String,CClassLink> res]
@init {
    Map<String,CClassLink> components = new TreeMap<String,CClassLink>();
}
    : (COMPONENTS component[components]*)?
    ;

component[Map<String,CClassLink> components] 
    : (cClassX=NAME cName=NAME END)
    ;


