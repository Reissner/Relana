
/**
 * A parser for relana-SClasses. 
 */
grammar SClass;

@parser::header {
    import eu.simuline.relana.model.ClassLocator;
    import eu.simuline.relana.model.SClass;
    import eu.simuline.relana.model.Deficiency;
    import eu.simuline.relana.model.DeficiencyNode;
    import eu.simuline.relana.model.SClassLoader;
    import eu.simuline.relana.model.Package;

    import java.util.Map;
    import java.util.HashMap;
    import java.util.Set;
    import java.util.HashSet;
    import java.util.Iterator;
    import java.util.Collections;

    import java.io.Reader;
    import java.io.IOException;
} // @parser::header 

//@lexer::header {
//    package eu.simuline.relana.parser;
//} // @lexer::header 

@parser::members {

    /* -------------------------------------------------------------------- *
     * fields.                                                              *
     * -------------------------------------------------------------------- */

    private SClassLoader classLoader;
    private ClassLocator loc;
    private Map<ClassLocator,ClassLocator> subclassDep;

    /* -------------------------------------------------------------------- *
     * constructors and creator methods.                                    *
     * -------------------------------------------------------------------- */

    private static CommonTokenStream reader2tokenStream(Reader reader)  
		throws IOException {
        ANTLRInputStream antlrStream = new ANTLRInputStream(reader);
        SClassLexer lexer = new SClassLexer(antlrStream);
        return new CommonTokenStream(lexer);
    }

    public SClassParser(Reader reader) throws IOException {
        this(reader2tokenStream(reader));
    }


    /* -------------------------------------------------------------------- *
     * methods.                                                             *
     * -------------------------------------------------------------------- */

    /**
     * Reports an error and also the location where it occurred. 
     * 
     * @param msg 
     *    the message to be displayed. 
     */
    void report(String msg) throws RuntimeException {// **** was ParseException
        System.out.print("[" + loc + "] ");
        //System.out.println(msg);
        RuntimeException pe = new RuntimeException(msg);
        //System.out.println(pe.getMessage());
        throw pe;
    } // report

    /**
     * To set the <code>SClassLoader</code>. 
     * This is needed whenever the definition of the class currently read 
     * relies on definitions of other classes such as 
     * the superclass if it is given explicitly. 
     * 
     * @param classLoader
     *    the current <code>SClassLoader</code>. 
     */
    public void setClassLoader(SClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public SClass getSClass(ClassLocator loc,
                            Map<ClassLocator,ClassLocator> subclassDep) {
        return sClass(loc,subclassDep).res;
    }

    /**
     * Throws an exception if the class currently parsed 
     * is involved in a cyclic definition. 
     * 
     * @param needed
     *    points to a class to be loaded by {@link #classLoader}. 
     * @throws Pars eException 
     *    if this class is parsed in order to declare a class <code>C</code>
     *    and <code>C</code> in turn is adressed in this class. 
     */
    private void checkDependencies(ClassLocator needed) 
        throws RuntimeException {
//        throws ParseException {
        ClassLocator needs = this.subclassDep.get(needed);
        if (needs != null) {
            // dependency: "needed" is really needed by "needs"
            List<ClassLocator> trace = new ArrayList<ClassLocator>();
            ClassLocator stop = needed;
            while (needs != null && !needs.equals(stop)) {
                trace.add(needed);
                needed = needs;
                needs = this.subclassDep.get(needed);
            }
            report("Cyclic subclass-dependency: trace \""  + trace + "\". ");
        } // if 
        // 
        this.subclassDep.put(needed, this.loc);
    } // checkDependencies

} // @parser::members 

// ======================================================================== 
// Parser 
// ======================================================================== 


/**
 * Parses an <code>SClass</code>. 
 * The main ingredients is its package, its name, the superclasses, 
 * the declared deficiencies, 
 * the inner classes and finally the relations. 
 * 
 * @param loc
 *    the location of the class currently parsed. 
 * @param subclassDep
 *    the subclass dependencies as defined for 
 *    {@link SClassLoader#loadSClass
(ClassLocator,Package,Map<ClassLocator,ClassLocator>)}
 * @return
 *    the <code>SClass</code> described by the document parsed. 
 * @throws IOException
 *    see {@link #getSuperClass}. 
 * @see #getSuperClass
 * @see #getDeficiencies
 * @see #getInnerCls
 * @see #addRelations
 */
sClass[ClassLocator loc,
       Map<ClassLocator,ClassLocator> subclassDep] 
returns [SClass res] 
@init {
    this.loc = loc;
    this.subclassDep = subclassDep;
    Map<List<String>,SClass> paths2innerCls = 
        new HashMap<List<String>,SClass>();
List<String> pkgPath;
SClass superclass;
Map<Deficiency,DeficiencyNode> deficiency2ordering;
Map<Deficiency,SClass> old2innerCls;
}
    : 
        ( 
            PACKAGE pkgPathC=getPath END
            {
                pkgPath = ((GetPathContext)
                           ((SClassContext)_localctx).pkgPathC).res;
            }
           SCLASS  sClassName=NAME 
            superclassC=getSuperClass[paths2innerCls] 
            '{' 
            deficiency2orderingC = getDeficiencies 
            {
                superclass = ((GetSuperClassContext)
                    ((SClassContext)_localctx).superclassC).res;
                deficiency2ordering = ((GetDeficienciesContext)
                     ((SClassContext)_localctx).deficiency2orderingC)
                .deficiency2ordering;
            }
            old2innerClsC = getInnerCls[superclass,
			   new HashSet<Deficiency>(deficiency2ordering.keySet())]
        {
            old2innerCls = ((GetInnerClsContext)
                            ((SClassContext)_localctx).old2innerClsC)
                .old2innerCls;
        }
            addRelations[deficiency2ordering] 
            '}' EOF
        )
        {
            //System.out.println("^^^^^^^^^^^^^^^"+this.loc);
            if (!this.loc.getPackage().getPath().equals(pkgPath)) {
                report("Expected Package \""  
                       + this.loc.getPackage().getPathName() 
                       + "\" but found: \""  
                       + Package.getPackage(pkgPath).getPathName() + "\". ");
            }

            if (!this.loc.getName().equals($sClassName.text)) {
                report("Expected Class \""  + this.loc.getName() + 
                       "\" but found: \""  + $sClassName.text + "\". ");
            }

            //System.out.println("--------paths2innerCls: "+paths2innerCls);
            //System.out.println("--------old2innerCls: "+old2innerCls);

            // Here, the the of this class is as prescribed by this.loc 
            // or something has been reported. 

            $res = SClass.getSClass(this.loc.getName(),
                                    this.loc.getPackage(),
                                    superclass,
                                    old2innerCls,
                                    deficiency2ordering);
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


/**
 * Adds a path to the given set of paths. 
 * 
 * @param innerPaths
 *    a set of paths. 
 */
addPath[Set<List<String>> innerPaths] 
    : pkgPathC=getPath
        {
            List<String> path = ((GetPathContext)
                                 ((AddPathContext)_localctx).pkgPathC).res;
            $innerPaths.add(path);
        }
    ;


/**
 * Returns the superclass of this class 
 * and also puts the classes that overwrite Properties 
 * into the parameter map. 
 * The classes that occur are truely loaded which may cause an exception. 
 * 
 * @param paths2innerCls
 *    maps paths to inner classes. 
 * @return
 *    the superclass of the class currently parsed. 
 * @throws IOException
 *    see {@link SClassLoader#loadSClass}. 
 */
getSuperClass[Map<List<String>,SClass > paths2innerCls] 
returns[SClass res] 
//throws IOException 
@init {
    //List<String> superPath = null;
    ClassLocator needed;
    Set<List<String> > innerPaths = new HashSet<List<String> >();
List<String> superPath = null;
}
    :  
        (EXTENDS superPathC=getPath
            {
                superPath = ((GetPathContext)
                             ((GetSuperClassContext)_localctx).superPathC).res;

            } 
            ('[' addPath[innerPaths] (',' addPath[innerPaths])* ']')?
        )? 
        {
            if (superPath == null) {
                // extends implicitly BooleanEffect 
((GetSuperClassContext)_localctx).res = SClass.BOOLEAN;
                return _localctx;
            }
            // Here, we have a valid path. 

            if (superPath.size() == 1 && 
                (superPath.get(0).equals(SClass.BOOLEAN.getName()))) {
                // Here it must be BooleanEffect, the overall base class. 
((GetSuperClassContext)_localctx).res = SClass.BOOLEAN;
                return _localctx;
            }   
            // Here, it must be a library class. 

            //System.out.println("getSuperClass------innerPaths: "+innerPaths);

            try {
                for (List<String> innerPath : innerPaths) {
                    needed = ClassLocator.getLocator(superPath);
                    checkDependencies(needed);
                    paths2innerCls.put(innerPath,
                                       this.classLoader
                                       .loadSClass(needed,
                                                   this.loc.getPackage(),
                                                   this.subclassDep));

                } // for 
            } catch(IOException ioe) {
                  throw new IllegalStateException
                        ("Thrown exception while loading class: \"" + 
                         ioe + "\". ");
            }

            needed = ClassLocator.getLocator(superPath);
            checkDependencies(needed);

           try {
            $res = this.classLoader.loadSClass(needed,
                                               this.loc.getPackage(),
                                               this.subclassDep);
            } catch(IOException ioe) {
               $res = null;
                  throw new IllegalStateException
                        ("Thrown exception while loading class: \"" + 
                         ioe + "\". ");
            }
        }
    ;


/**
 * Returns a map describing a set of deficiencies 
 * and their (ordering) relations. 
 * 
 * @return
 *    see {@link Type#deficiency2ordering}. 
 */// old *********************************
//getDeficiencies returns[Map<Deficiency,DeficiencyNode> res]
//@init {
//    Map<Deficiency,DeficiencyNode> deficiency2ordering = 
//        new HashMap<Deficiency,DeficiencyNode>();
//}
//    : (PROPERTIES addDeficiency[deficiency2ordering]*)?
//        {$res=deficiency2ordering;}
//    ;

getDeficiencies returns[Map<Deficiency,DeficiencyNode> deficiency2ordering]
@init {
    Map<Deficiency,DeficiencyNode> deficiency2ordering0 = new HashMap<Deficiency,DeficiencyNode>();
}
@after {
$deficiency2ordering = deficiency2ordering0;
}
    : (PROPERTIES addDeficiency[deficiency2ordering0]*)?
        //{$res=deficiency2ordering;}
    ;


/**
 * Adds a new deficiency to the ordered set 
 * given by <code>deficiency2ordering</code> 
 * without relating it to the rest. 
 * 
 * @param deficiency2ordering
 *    an ordered set as described for {@link Type#deficiency2ordering}. 
 */
addDeficiency[Map<Deficiency,DeficiencyNode> deficiency2ordering] 
    : (defT=NAME)
        {
            Deficiency def = new Deficiency($defT.text);
            DeficiencyNode old = deficiency2ordering
            .put(def, new DeficiencyNode(def));
            if (old != null) {
                    // duplicate deficiency 
                    report("Property \""  + $defT.text + "\" is duplicate. ");
                }
        }
    ;


/**
 * Parses the subtype map. 
 * 
 * @param superclass
 *    
 * @param newDefs
 *    
 * @return
 *    a map that maps each overwritten deficiency 
 *    to the <code>SClass</code> overwriting it. 
 * @see #addMap
 */
getInnerCls[SClass superclass, Set<Deficiency> newDefs] 
    returns [Map<Deficiency,SClass> old2innerCls]
@init {
    Map<Deficiency,SClass> old2innerCls0 = new HashMap<Deficiency,SClass>();
    Set<Deficiency> oldDefs = superclass.getType().asSet();
}
    : (MAP addMap[oldDefs,old2innerCls0,newDefs]*)?
        {
            if (!newDefs.isEmpty()) {
                // **** this should be merely a warning. 
                report("Found properties nowhere referenced: " 
                       + newDefs + ". ");
            }
$old2innerCls = old2innerCls0;
        }
    ;

/**
 * Adds a single old property and its inverse image 
 * to the current subtype map. 
 * 
 * @param oldDefs
 *    set of deficiencies of the superclass. 
 *    These are the candidates that may be overwritten. 
 * @param old2innerCls
 *    maps each overwritten deficiency 
 *    to the <code>SClass</code> overwriting it. 
 *    The keys are out of <code>oldDefs</code>. 
 * @param newDefs
 *    An <code>SClass</code> overwriting an element 
 *    of <code>oldDefs</code> may be given 
 *    either by its classname or explicitly declaring its deficiencies 
 *    and the ordering relation. 
 *    In the latter case, 
 *    the deficiencies must have been declared above, 
 *    and hence must be in <code>newDefs</code>. 
 * @return
 *    <code>old2innerCls</code>. **** seems superfluous to me. **** 
 */
addMap[Set<Deficiency> oldDefs, 
       Map<Deficiency,SClass> old2innerCls, 
       Set<Deficiency> newDefs] returns [Map<Deficiency,SClass> res]
//throws RecognitionException 
@init {
Map<Deficiency,DeficiencyNode> deficiency2ordering = null;
List<String> clsPath = null;
}
    : 
        ( 
            REPLACE oldD=NAME 
            ( 
                '{' 
                    deficiency2orderingC=getCheckedDeficiencies[newDefs] 
                {
                    deficiency2ordering = ((GetCheckedDeficienciesContext)
                            ((AddMapContext)_localctx).deficiency2orderingC)
                    .deficiency2ordering;
                }
                    RELATIONS
                    addRelation[deficiency2ordering]* 
                '}' 
                |     pathC=getPath 
                    {   
                        clsPath = ((GetPathContext)
                           ((AddMapContext)_localctx).pathC).res;
                    }
            )
        )
        {
            Deficiency oldDef = new Deficiency($oldD.text);
            SClass sClass = null;
            assert deficiency2ordering == null ^ clsPath == null;
            if (deficiency2ordering != null) {
                // inner class is given directly 
                sClass = SClass.getSClass(oldDef.getName(), 
                                          Package
                                          .getPackage(this.loc.getPath()),
                                          SClass.BOOLEAN,
                                          new HashMap<Deficiency,SClass>(),
                                          deficiency2ordering);
                sClass.verifyInner();
             } else {
                // inner class is loaded 
                try {
                    sClass = this.classLoader
                        .loadSClass(ClassLocator.getLocator(clsPath),
                                    this.loc.getPackage());
                } catch(IOException e) {
                    throw new IllegalStateException
                        ("Thrown exception " 
                         +"which should have been loaded before: \"" + 
                         e + "\". ");
                }
            }
            SClass overWrittenDef = old2innerCls.put(oldDef,sClass);
            //System.out.println();
            if (overWrittenDef != null) {
                report("Tried to overwrite " + oldDef + ". ");
            }
            $res = old2innerCls;// **** superfluous? 
        }
    ;


/**
 * Reads in declared deficiencies in <code>newDefs</code> 
 * and returns an ordered set consisting of a subset of them 
 * with trivial ordering. 
 * Reports an error 
 * if deficiencies outside <code>newDefs</code> are addressed. 
 * 
 * @param newDefs
 *    the set of declared deficiencies. 
 *    These may be used to declare inner classes. 
 * @return
 *    an ordered subset of deficiencies out of <code>newDefs</code> 
 *    with trivial ordering. 
 */
getCheckedDeficiencies[Set<Deficiency> newDefs] 
    returns [Map<Deficiency,DeficiencyNode> deficiency2ordering] 
@init {
   Map<Deficiency,DeficiencyNode> deficiency2ordering0 = 
        new HashMap<Deficiency,DeficiencyNode>();
}

    : 
        (addDeficiency[deficiency2ordering0]+) 
        {
            if (!newDefs.containsAll(deficiency2ordering0.keySet())) {
                    // undeclared deficiencies. 
                    Set<Deficiency> undeclared = 
                        new HashSet<Deficiency>(deficiency2ordering0.keySet());
                    undeclared.removeAll(newDefs);
                    report("Found properties: " + undeclared + 
                        " either undeclared or used above. ");
                }
            // declared properties may not occur twice. 
            newDefs.removeAll(deficiency2ordering0.keySet());
$deficiency2ordering = deficiency2ordering0;
       }
    ;

/**
 * Adds relations to the (outer) class parsed. 
 * The main work is delegated to {@link #addRelation}. 
 * After having read all relations, 
 * the isolated entries in <code>deficiency2ordering</code> are removed. 
 * 
 * @param deficiency2ordering
 *    Represents the ordering between the declared deficiencies 
 *    which are not given by inner classes. 
 *    Declarations of relations are added to this parameter. 
 *    See also {@link Type#deficiency2ordering}. 
 */
addRelations[Map<Deficiency,DeficiencyNode> deficiency2ordering] 
    : (RELATIONS addRelation[deficiency2ordering]*)?
        {
            Iterator<Deficiency> iter = deficiency2ordering.keySet().iterator();
            Deficiency def;
            DeficiencyNode node;
            while (iter.hasNext()) {
                    def = iter.next();
                    node = deficiency2ordering.get(def);
                    if (node.getPredecessors().isEmpty() && 
                        node.getSuccessors()  .isEmpty()) {
                            iter.remove();
                    }
                } // for 
        }
    ;

/**
 * Adds a relation to an explicitly declared inner class. 
 * 
 * @param deficiency2ordering
 *    an ordered set as described for {@link Type#deficiency2ordering}. 
 */
addRelation[Map<Deficiency,DeficiencyNode> deficiency2ordering] 
    : (defT1=NAME IMPLIES defT2=NAME END)
        {
            DeficiencyNode defN1 = deficiency2ordering
            .get(new Deficiency($defT1.text));
            if (defN1 == null) {
                    report("Deficiency \""  + $defT1.text + "\" is unknown. ");
                }
            DeficiencyNode defN2 = deficiency2ordering
            .get(new Deficiency($defT2.text));
            if (defN2 == null) {
                    report("Deficiency \""  + $defT2.text + "\" is unknown. ");
                }

            defN1.addSuccessor  (defN2);
            defN2.addPredecessor(defN1);
        }
    ;

// ======================================================================== 
// Lexer 
// ======================================================================== 

WS : (' ' | '\t' | '\n' | '\r' | '\f') -> skip;

SingleLineComment : '//' ~( '\r' | '\n' )* -> skip;

MultiLineComment  : '/*'  .*? '*/' -> skip;

PACKAGE:          'package';
SCLASS:           'sclass';
EXTENDS:          'extends';
PROPERTIES:       'properties';
MAP:              'map';
REPLACE:          'replace';
RELATIONS:	      'relations';
IMPLIES:	      '==>';
END:              ';' ;
SEP:              '.' ;
NAME:             LETTER IDENTIFIER*;
fragment IDENTIFIER:      (LETTER | DIGIT | '_') ;
fragment LETTER:          (CAPITAL_LETTER | SMALL_LETTER) ;
fragment DIGIT:           '0'..'9';
fragment SMALL_LETTER:    'a'..'z';
fragment CAPITAL_LETTER:  'A'..'Z';

