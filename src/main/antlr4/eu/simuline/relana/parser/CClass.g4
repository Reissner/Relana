

grammar CClass;

@parser::header {
    import eu.simuline.relana.model.CClass;
    import eu.simuline.relana.model.SClass;
    import eu.simuline.relana.model.CClassLoader;
    import eu.simuline.relana.model.ClassLocator;
    import eu.simuline.relana.model.CClassLink;
    import eu.simuline.relana.model.Package;
    import eu.simuline.relana.model.ProbDistr;
    import eu.simuline.relana.model.Deficiency;
    import eu.simuline.relana.model.MapDecl;

    import eu.simuline.relana.expressions.Type;

    import java.math.BigDecimal;

    import java.io.Reader;
    import java.io.IOException;
    import java.io.StringReader;
    import java.io.InputStream;

    import java.util.List;
    import java.util.ArrayList;
    import java.util.Map;
    import java.util.HashMap;
    import java.util.TreeMap;
    import java.util.Set;
    import java.util.HashSet;
    import java.util.Collections;
} // @header 

@parser::members {

    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    private CClassLoader classLoader;

    private ClassLocator loc;
    private CClass cClass;
    private boolean exceptionThrown;

    /* -------------------------------------------------------------------- *
     * inner classes.                                                       *
     * -------------------------------------------------------------------- */

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

    /* -------------------------------------------------------------------- *
     * constructors and creator methods.                                    *
     * -------------------------------------------------------------------- */

    private static CommonTokenStream inputStream2tokenStream(InputStream str) 
        throws IOException {
	    CharStream input = CharStreams.fromStream(str);
        CClassLexer lexer = new CClassLexer(input);
        return new CommonTokenStream(lexer);
    }

    public CClassParser(InputStream str) throws IOException {
        this(inputStream2tokenStream(str));
    }


    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

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
    private String getLocation() {
       StringBuffer result = new StringBuffer();
        result.append("[" + this.loc + "] ");
        Token token0 = this.getTokenStream().LT(-1);
        Token token1 = this.getTokenStream().LT(+1);

        //if (token1.getType() == Token.EOR_TOKEN_TYPE) {
            result.append("line "      + token0.getLine()  );
            result.append(", column "  + token0.getCharPositionInLine());
            result.append(", after \"" + token0);
            result.append("\": ");
        //} else {
        //    result.append("line "        + token1.getLine()  );
        //    result.append(", column "    + token1.getCharPositionInLine());
        //    result.append(", between \"" + token0);
         //   result.append("\" and \""    + token1);
         //   result.append("\": ");
       //}
        return result.toString();
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

    /**
     * To set the <code>CClassLoader</code>. 
     * This is needed whenever the definition of the class currently read 
     * relies on definitions of other classes such as 
     * the superclass if it is given explicitly. 
     * 
     * @param classLoader
     *    the current <code>SClassLoader</code>. 
     */
    public void setClassLoader(CClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public CClass getCClass(ClassLocator loc) {
        return cClass(loc).res;
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

//@lexer::header {
//    package eu.simuline.relana.parser;
//} // @lexer::header 

//@lexer::members {
////    // fail at first error 
//    @Override
//    public void displayRecognitionError(String[] tokenNames, 
//        RecognitionException e) {
////        if (this.failFirstError) {
//                String hdr = getErrorHeader(e);
////                String msg = getErrorMessage(e, tokenNames);
//                throw new RuntimeException(hdr + ":" + msg);
//throw e;
//        }
//                super.displayRecognitionError(tokenNames, e);
//                return;
 //   }
//} // @lexer::members



// ======================================================================== 
// Parser 
// ======================================================================== 

cClass[ClassLocator loc] returns [CClass res] throws IOException 
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
List<String> pkgPath;
CClass superClass;
Map<String,CClass.SClassDecl> effectsX;
Map<String,MapDecl> mapsX;
Map<String,CClassLink> componentsX;
Map<String,FormulaWrapper> incompEffects = 
			   new HashMap<String,FormulaWrapper>();
}
    : 
        (
            PACKAGE  pkgPathC=getPath END
            {
                pkgPath = ((GetPathContext)
                           ((CClassContext)_localctx).pkgPathC).res;
            }
            CCLASS   cClassName=NAME 
            superClassC=getSuperClass '{' 
        
            effectsXC   =effects[incompEffects] 
            mapsXC      =maps 
            componentsXC=components 
        {
                superClass  = ((GetSuperClassContext)
                    ((CClassContext)_localctx).superClassC).res;
                effectsX     = ((EffectsContext)
                  ((CClassContext)_localctx).effectsXC).effectsX;
                mapsX        = ((MapsContext)
                  ((CClassContext)_localctx).mapsXC).name2map;
                componentsX  = ((ComponentsContext)
                  ((CClassContext)_localctx).componentsXC).componentsX;
        }
            '}' EOF
        )
        {
            //System.out.println("^^^^^^^^^^^^^^^");
            if (!this.loc.getPackage().getPath().equals(pkgPath)) {
                report("Expected Package \""  
                       + this.loc.getPackage().getPathName() 
                       + "\" but found: \""  
                       + Package.getPackage(pkgPath).getPathName() + "\". ");
            }

            if (!this.loc.getName().equals($cClassName.text)) {
                report("Expected Class \""  + this.loc.getName() + 
                       "\" but found: \""  + $cClassName.text + "\". ");
            }

            // Here, the name of the of this class is as prescribed by this.loc 
            // or something has been reported. 

            if (effectsX.size() == 0 && componentsX.size() == 0) {
                // Here, neither components nor effects are defined. 
                report("Class \""  + $cClassName.text + "\" is invalid: " + 
                       "Neither components nor effects are defined. ");
            }

            this.cClass = CClass.getCClass(this.loc.getName(),
                                           this.loc.getPackage(),
                                           superClass,
                                           mapsX,
                                           componentsX,
                                           effectsX);

            // complete declarations of effects 
            if (!incompEffects.isEmpty()) {
                FormulaParser fParser = null;
                for (Map.Entry<String,FormulaWrapper> entry :
                         incompEffects.entrySet()) {
                    try {
                        CharStream input = CharStreams
                            .fromString(entry.getValue().formula);
                        FormulaLexer lexer = new FormulaLexer(input);
                        CommonTokenStream tokens = new CommonTokenStream(lexer);
                        fParser = new FormulaParser(tokens);
//fParser = new FormulaParser(str);
                        fParser.setClassLoader(this.classLoader);
                        fParser.setLocator(this.loc);
                        fParser.setCClass(this.cClass);
                        fParser.setLineColNum(entry.getValue().lineNumber,
                                              entry.getValue().colnNumber);
                        CClass.SClassDecl decl = effectsX.get(entry.getKey());
                        decl.setFormula(fParser.getFormulaStart());
                    } catch(IllegalArgumentException iaEx) {
                        //throw iaEx;// for debugging
                        fParser.report(iaEx.getMessage());
                    }
                } // for 
            }
            // Here, the entries of this class is valid 
            // or something has been reported. 

            if (this.exceptionThrown) {
                report("Recoverable errors occurred; see above. ");
            }
            $res = this.cClass;
        }
    ;


/**
 * Returns the superclass of this class. 
 * The classes that occur are truely loaded which may cause an exception. 
 * 
 * @return
 *    the superclass of the class currently parsed. 
 * @throws IOException
 *    see {@link CClassLoader#loadSClass}. 
 */
getSuperClass returns [CClass res] //throws IOException 
    :  (ext=EXTENDS superPathC = getPath())? 
        {
            List<String> superPath = $ext == null 
                ? null 
                : ((GetPathContext)
                    ((GetSuperClassContext)_localctx).superPathC).res;
            // determine the superclass 
            if (superPath == null || 
                (superPath.size() == 1 && 
                    superPath.get(0).equals(CClass.COMPONENT.getName()))) {
                // extends implicitly or explicitely Component 
                $res = CClass.COMPONENT;
            } else {
                // Here, it must be a library class. 
                try {
                    $res = this.classLoader
                        .loadCClass(ClassLocator.getLocator(superPath),
                                    this.loc.getPackage());
                } catch(IOException ioe) {
                    report(ioe.getMessage());
                }
             }
       }
    ;


/**
 * Returns a path as a list of strings. 
 * **** same as in SClassParser and for FormulaParser**** 
 * 
 * @return
 *     a path as a list of strings. 
 *     The outermost package is given first. 
 */
getPath returns [List<String> res] 
@init{List<String> res0 = new ArrayList<String>();}
@after{$res = Collections.unmodifiableList(res0);}
    :       first=NAME {res0.add($first.text);} 
        (SEP next=NAME {res0.add( $next.text);})*;



maps returns [Map<String,MapDecl> name2map] 
@init{Map<String,MapDecl> name2map0 = new HashMap<String,MapDecl>();}
@after{$name2map = name2map0;}
    : (MAPS (addMap[name2map0])*)?
    ;


addMap[Map<String,MapDecl> name2map] 
@init{
    Map<Set<Deficiency>,Deficiency> setOfSrc2targ = 
        new HashMap<Set<Deficiency>,Deficiency>();
    Set<Deficiency> idDomain = new HashSet<Deficiency>();
}
    : 
        (//<MAP> 
            (redeclare = REDECLARE)?
            nameT = NAME ':' invImgPC=getPath '-->' imgPC=getPath 
            '{' add2DefMap[setOfSrc2targ]* addToIdDom[idDomain]? '}' END
        )
        {

            List<String> invImgP = ((GetPathContext)
                    ((AddMapContext)_localctx).invImgPC).res;
            List<String>    imgP = ((GetPathContext)
                    ((AddMapContext)_localctx)   .imgPC).res;
            SClass invImgCls = null;
            SClass    imgCls = null;
            String mapName = $nameT.text;
            try {
                invImgCls = this.classLoader
                    .loadSClass(ClassLocator.getLocator(invImgP),
                                this.loc.getPackage());
            } catch (IOException ioe) {
                report("IOException while loading \"" 
                       + ClassLocator.getLocator(invImgP) 
                       + "\" in package this.loc.getPackage(): " + ioe + ". ");
            }

            try {
                imgCls = this.classLoader
                    .loadSClass(ClassLocator.getLocator(imgP),
                                this.loc.getPackage());
            } catch (IOException ioe) {
                report("IOException while loading \"" 
                       + ClassLocator.getLocator(imgP) + 
                       "\" in package this.loc.getPackage(): " + ioe + ". ");
            }
            
            setOfSrc2targ = Collections.unmodifiableMap(setOfSrc2targ);
            try {
                MapDecl newMap = new MapDecl($redeclare != null,
                                             mapName,
                                             setOfSrc2targ,
                                             invImgCls,
                                             imgCls,
                                             idDomain);
                MapDecl oldMap = name2map.put(mapName,newMap);
                if (oldMap != null) {
                    report($nameT, "Name \"" + mapName + 
                           "\" used for two maps: " + oldMap + 
                           " and " + newMap + ". ");
                }
            } catch(IllegalArgumentException iaEx) {
                // throw iaEx;// for debugging 
                report($nameT, iaEx.getMessage());
            }
        }
    ;


add2DefMap[Map<Set<Deficiency>,Deficiency> setOfSrc2targ] 
@init{
    Set<Deficiency> invImg = new HashSet<Deficiency>();
}
    : 
        ( 
            '{' addDef[invImg]* '}' MAPSTO defT=NAME 
        )
        {
            Deficiency newDef = new Deficiency($defT.text);
            invImg = Collections.unmodifiableSet(invImg);
            Deficiency oldDef = setOfSrc2targ.put(invImg,newDef);
            if (oldDef != null) {
                report("Defined image of "  + invImg + 
                       " twice: is \""  + oldDef + 
                       "\" and \""  + newDef + "\". ");
            }
        }
    ;


addDef[Set<Deficiency> defs] 
    : defT=NAME
        {
            boolean isNew = $defs.add(new Deficiency($defT.text));
            if (!isNew) {
                report("Defined deficiency "  + $defT.text + " twice. ");
            }
        }
    ;



addToIdDom[Set<Deficiency> idDomain] 
    : ( IDDOMAIN '{' addDef[idDomain]* '}' );



/**
 * Reads in all declarations of effects. 
 * 
 * @param incompEffects
 *    a map that maps names of effects to their respective declarations. 
 *    Here only incomplete declarations are mentioned: 
 *    Each formula requires a second parsing step. 
 *    Note that this method may add entries to this map while parsing. 
 * @return
 *    a map that maps names of effects to their respective declarations. 
 */
effects[Map<String,FormulaWrapper> incompEffects] 
returns [Map<String,CClass.SClassDecl> effectsX] 
@init {
Map<String,CClass.SClassDecl> effectsX0 = 
new TreeMap<String,CClass.SClassDecl>();}
@after{$effectsX = effectsX0;}
    : ( EFFECTS effect[effectsX0, incompEffects]* )?
    ;


/**
 * Reads in the declaration of a effect. 
 * Note that this method may add entries 
 * to both maps in the parameter list while parsing. 
 * 
 * @param effects
 *    a map that maps names of effects to their respective declarations. 
 * @param incompEffects
 *    a map that maps names of effects to their respective declarations. 
 *    Here only incomplete declarations are mentioned: 
 *    Each formula requires a second parsing step. 
 */
effect[Map<String,CClass.SClassDecl> effectsMap,
       Map<String,FormulaWrapper>    incompEffects] 
@init {
    Set<CClass.SClassModifier> accessModifiers = 
        new HashSet<CClass.SClassModifier>();
    SClass sClass = null;
}
    : 
        (
            (redeclare=REDECLARE)?
            addAccessModifier[accessModifiers]* 
            pathC=getPath 
            sNameT=NAME 
        ) 
        {
            List<String> path = ((GetPathContext)
                    ((EffectContext)_localctx).pathC).res;
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
            (distrC=getDistr[sClass] | formT='(' formC=skipFormula ')')?
            END
        )
        {
            ProbDistr distr = ((EffectContext)_localctx).distrC == null 
            ? null
            : ((GetDistrContext)
               ((EffectContext)_localctx).distrC).distr;
            String form = $formT == null 
                ? null 
                : ((SkipFormulaContext)
                    ((EffectContext)_localctx).formC).res;

            // read in the rest: name, modifiers, distribution, formula if any. 
            String sName = $sNameT.text;
            assert sName != null;
            CClass.SClassDecl oldDecl = effectsMap
                .put(sName,
                     new CClass.SClassDecl($redeclare != null,
                                           accessModifiers,
                                           sClass,
                                           sName,
                                           distr));
            // consistency check: duplicate declaration 
            if (oldDecl != null) {
                report("Duplicate effect declaration \"" + sName + 
                       "\"; overwrite " + oldDecl + " by " + sClass + ". " );
            }

            // store formula for later analysis 
            if (form != null) {
                assert $formT != null;
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


/**
 * Adds an access modifier to the set <code>accessModifiers</code>. 
 * Reports an error if a modifyer is duplicate. 
 * 
 * @param accessModifiers
 *    a set of access modifiers. 
 */
addAccessModifier[Set<CClass.SClassModifier> accessModifiers] 
    : ( modT=INPUT | modT=OUTPUT )
        {
//***** bad; no single source principle. 
CClass.SClassModifier mod = CClass.SClassModifier.get($modT.text);

if (!accessModifiers.add(mod)) {
   // modifier already present 
   report("Found duplicate access modifier \"" + $modT.text + "\". ");
}
// Here, the modifiers are valid in their own right. 
// Validity of combinations are not yet checked. 
        }
    ;


/**
 * 
 * 
 * @param sClass
 *    an <code>SClass</code> that describes the type 
 *    of the probability distribution to be read in. 
 * @return
 *    the probability distribution read in. 
 */
getDistr[SClass sClass] returns [ProbDistr distr]
@init {
    Map<Deficiency,BigDecimal> def2prob  = new HashMap<Deficiency,BigDecimal>();
    Map<Deficiency,ProbDistr > def2distr = new HashMap<Deficiency,ProbDistr >();
}
    : '{' (addProbAlloc[def2prob] | REPLACE replDistr[def2distr,sClass])* '}'
        {
            // check whether all is allocated 
            assert sClass.getDeclaredInnerClasses().keySet()
                .containsAll(def2distr.keySet());

            // check whether replacements are given for all "inner classes". 
            if (sClass.getDeclaredInnerClasses().keySet().size() != 
                           def2distr.keySet().size()) {
                    Set<Deficiency> minus = new HashSet<Deficiency>
                        (sClass.getDeclaredInnerClasses().keySet());
                    minus.removeAll(def2distr.keySet());
                    report("Found no partial probability distribution " + 
                        "for deficiencies " + minus + ". ");
                }

            // make sure that 
            Set<Deficiency> inter = new HashSet<Deficiency>(def2prob.keySet());
            inter.retainAll(def2distr.keySet());
            if (!inter.isEmpty()) {
                    report("For properties " + inter + 
                        " defined probability and sub-distribution. ");
                }

            //ProbDistr distr;
            Type type = sClass.getType();
            if ((sClass.getSuperClass() == null) || sClass.isInner()) {
                    // Here, the distribution is elementary, 
                    // i.e. has no subdistributions, i.e. replacements. 
                    if (!type.asSet().equals(def2prob.keySet())) {
                            report("Type with set " + type.asSet() + 
                            " does not allow distribution " + def2prob + ". ");
                        }
                    
                    $distr = new ProbDistr(type, def2prob);
                } else {
                Set<Deficiency> union = 
                    new HashSet<Deficiency>(def2prob.keySet());
                union.addAll(def2distr.keySet());
                Set<Deficiency> exp = sClass.getSuperClass().getType().asSet();
                if (!exp.equals(union)) {
                        report("Expected properties " + exp + 
                            " but found " + union + ". ");
                    }
                $distr = new ProbDistr(type,def2distr,def2prob);
            }
            try {
                $distr.validate();
            } catch(IllegalStateException e) {
                    report("Probability distribution " + e.getMessage());
            }
         }
    ;


replDistr[Map<Deficiency,ProbDistr> def2distr, SClass sClass] 
@init {  
    SClass sClassInner = null;
    Deficiency repl = null;
}
    : 
        replDefT=NAME
        {
            repl = new Deficiency($replDefT.text);
            sClassInner = sClass.getDeclaredInnerClass(repl);
            assert sClassInner != null; // thrown exception otherwise. 
        }
        distrInnerC=getDistr[sClassInner]
        {
            ProbDistr distrInner = ((GetDistrContext)
                    ((ReplDistrContext)_localctx).distrInnerC).distr;
            //replace 
            ProbDistr old = $def2distr.put(repl, distrInner);
            if (old != null) {
                    report("Overwritten replacement of property \"" 
                        + $replDefT.text + "\". ");
            }
        }
    ;


/**
 * Reads in a probability allocation. 
 * Reports an error if a value is overwritten or 
 * if the value read in	is not in <code>90,1)</code>. 
 * 
 * @param def2prob
 *    maps a deficiency to its probability. 
 *    This is successively filled with the values read in. 
 */
addProbAlloc[Map<Deficiency,BigDecimal> def2prob] 
    : (defT=NAME ':' floatT=FLOAT)
        {
            Deficiency def = new Deficiency($defT.text);
            BigDecimal newProb = new BigDecimal($floatT.text);
            if (newProb.compareTo(BigDecimal.ONE ) >= 0 || 
                newProb.compareTo(BigDecimal.ZERO) <= 0) {
                report("Assigned property \"" + $defT.text + 
                       "\" probability value " + $floatT.text + 
                       " which is not in (0,1). ");
            }
            BigDecimal oldProb = def2prob.put(def, newProb);
            if (oldProb != null) {
                // overwritten 
                report("Overwritten probability " + oldProb 
                       + " for property \"" + $defT.text + 
                       "\" by new value " + $floatT.text + ". ");
            }
        }
    ;


/**
 * Parses a formula checking its syntactic structure 
 * but without analyzing it in detail and without decomposing it 
 * nto its ingredients. 
 * Essentially delegates work to be done to {@link #appendFormula}. 
 * 
 * @return
 *    the formula read. 
 * @see FormulaParser#getFormula
 */
skipFormula returns [String res]
@init {
    StringBuffer resB  = new StringBuffer();
}
    : appendFormula["",resB] {$res = resB.toString();};



/**
 * Essentially reads a formula into the buffer <code>res</code>. 
 * 
 * @param pre
 *    a prefix string. 
 * @param res
 *    appends the prefix <code>pre</code> 
 *    and then what is parsed subsequently to <code>res</code>. 
 */
appendFormula[String pre, StringBuffer res] 
@init {
    $res.append($pre);
}
    : 
        ( 
            ('<'  appendToken["<",res,">{"] '>' 
             '{' (appendToken["" ,res,""  ])* cls = '}')      // constant 
            | 
            // ( ( (NAME INV? (COV|CONT) ) | 
            //    UNION | INTERSECT | COMPLEMENT) '(' )=>  
            (     appendOp[res] 
              '(' appendFormula["(",res] 
            ( ',' appendFormula[",",res])* cls = ')' )  // compound
            | 
            ( appendToken["" ,res,""]  
            ('.' appendToken[".",res,""])* )       // variable 
        )
        {
            if ($cls != null) {
                    $res.append($cls.text);
            }
        }
    ;


/**
 * Appends to <code>buf</code> the prefix <code>pre</code>, a name parsed 
 * and finally <code>post</code>. 
 * 
 * @param pre
 *    a prefix string. 
 * @param buf
 *    a buffer to append <code>pre</code>, a name parsed 
 *    and finally <code>post</code>. 
 * @param post
 *    a postfix string. 
 */
appendToken[String pre,StringBuffer buf,String post] 
@init {$buf.append($pre);}
    : name=NAME 
        {
            buf.append($name.text);
            buf.append($post);
        }
    ;


/**
 * Appends to <code>buf</code> 
 * the string representation of an operation parsed. 
 * 
 * @param buf
 *    a buffer to append the string representation of an operation parsed. 
 *    This may be either union, intersection or complement. 
 */
appendOp[StringBuffer buf] 
    : 
        (
            ( funT=NAME (invT=INV)? (accT=CONT | accT=COV) ) | 
            opT=UNION | 
            opT=INTERSECT | 
            opT=COMPLEMENT 
        )
        {
            if ($opT == null) {
                    assert $funT != null && $accT != null;
                    buf.append($funT.text);
                    if ($invT != null) {
                            buf.append($invT.text);
                    }
                    buf.append($accT.text);
            } else {
                assert $funT == null && $invT == null && $accT == null;
                buf.append($opT.text);
            }
        }
    ;


/**
 * Parses the subcomponents declared in this <code>CClass</code>. 
 * 
 * @return
 *    a map that maps the names of the subcomponents 
 *    to their class links. 
 */
components returns [Map<String,CClassLink> componentsX]
@init {
    Map<String,CClassLink> components0 = new TreeMap<String,CClassLink>();
}
@after {
    $componentsX = components0;
}
    : (COMPONENTS component[components0]*)?
    ;

/**
 * Parses a single subcomponent declaration. 
 * 
 * @param components
 *    a map that maps the names of the subcomponents 
 *    to their class links. 
 *    The parsed component is added to this map. 
 */
component[Map<String,CClassLink> componentsX] 
    : (cClassX=NAME cName=NAME END)
        {
            ClassLocator loc = 
            new ClassLocator($cClassX.text,this.loc.getPackage());

            CClassLink link = this.classLoader
            .resolveLocInOcc(loc,this.loc,$cName.text);

            CClassLink old = componentsX.put($cName.text, link);  
            if (old != null) {
                    report("Duplicate component \"" + $cName.text + 
                        "\"; overwrite " + old + " by " + $cClassX.text + ". " );
}
        }
    ;


// ======================================================================== 
// Lexer 
// ======================================================================== 

WS : (' ' | '\t' | '\n' | '\r' | '\f') -> skip;

SingleLineComment : '//' ~( '\r' | '\n' )* -> skip;

MultiLineComment  : '/*' .*? '*/' -> skip;

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

