

grammar SClass;

@header {
    package eu.simuline.relana.parser;

    import eu.simuline.relana.model.ClassLocator;
    import eu.simuline.relana.model.SClass;
    import eu.simuline.relana.model.Deficiency;
    import eu.simuline.relana.model.DeficiencyNode;
    import eu.simuline.relana.model.SClassLoader;

    import java.util.Map;
    import java.util.HashMap;
    import java.util.Set;
    import java.util.HashSet;

    import java.io.Reader;
    import java.io.IOException;
} // @header 

@lexer::header {
    package eu.simuline.relana.parser;
} // @lexer::header 

@members {

    private SClassLoader classLoader;

    private static CommonTokenStream reader2tokenStream(Reader reader)  
		throws IOException {
        ANTLRReaderStream antlrStream = new ANTLRReaderStream(reader);
        SClassLexer lexer = new SClassLexer(antlrStream);
        return new CommonTokenStream(lexer);
    }

    public SClassParser(Reader reader) throws IOException {
        this(reader2tokenStream(reader));
    }

    public SClassParser(CommonTokenStream stream) {
        super(stream);
    }

    public void ReInit(Reader reader) throws IOException {
        setTokenStream(reader2tokenStream(reader));
    }


    /**
     * To set the <code>SClassLoader<code>. 
     * This is needed whenever the definition of the class currently read 
     * relies on definitions of other classes such as 
     * the superclass if it is given explicitly. 
     * 
     * @param classLoader
     *    the current <code>SClassLoader<code>. 
     */
    public void setClassLoader(SClassLoader classLoader) {
        this.classLoader = classLoader;
    }


} // @members 

// ======================================================================== 
// Lexer 
// ======================================================================== 

WS : (' ' | '\t' | '\n' | '\r' | '\f') {skip();};

SingleLineComment : '//' ~( '\r' | '\n' )* {skip();};

MultiLineComment  : '/*' (options {greedy=false;} : .)* '*/' {skip();};

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

// ======================================================================== 
// Parser 
// ======================================================================== 

 sClass[ClassLocator loc,
        Map<ClassLocator,ClassLocator> subclassDep] returns [SClass res] 
@init {
Map<List<String>,SClass > paths2innerCls = new HashMap<List<String>,SClass >();
}
    : 
        ( 
            PACKAGE pkgPath=getPath END
            SCLASS  sClassName=NAME 
            superclass=getSuperClass[paths2innerCls] 
            '{' 
            deficiency2ordering = getDeficiencies 
            old2innerCls = getInnerCls[superclass,
			   new HashSet<Deficiency>(deficiency2ordering.keySet())]
            addRelations[deficiency2ordering] 
            '}' EOF)
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
@init{$res = new ArrayList<String>();}
    :       first=NAME {res.add($first.text);} 
        (SEP next=NAME {res.add( $next.text);})*;

addPath[Set<List<String> > innerPaths] 
    : path = getPath 
        {$innerPaths.add(path);}
    ;

getSuperClass[Map<List<String>,SClass > paths2innerCls] returns[SClass res]
@init {
//List<String> superPath = null;
ClassLocator needed;
Set<List<String> > innerPaths = new HashSet<List<String> >();
}
    :  ( EXTENDS superPath = getPath 
            ('[' addPath[innerPaths] (',' addPath[innerPaths])* ']')?)? 
    ;


getDeficiencies returns[Map<Deficiency,DeficiencyNode>  res]
@init {
Map<Deficiency,DeficiencyNode> deficiency2ordering = 
			   new HashMap<Deficiency,DeficiencyNode>();
}
    : (PROPERTIES addDeficiency[deficiency2ordering]*)?
    ;


addDeficiency[Map<Deficiency,DeficiencyNode> deficiency2ordering] 
    : (defT = NAME)
    ;

getInnerCls[SClass superclass,
            Set<Deficiency> newDefs] returns [Map<Deficiency,SClass> res]
@init {
Map<Deficiency,SClass> old2innerCls = new HashMap<Deficiency,SClass>();
Set<Deficiency> oldDefs = superclass.getType().asSet();
}
    : (MAP addMap[oldDefs,old2innerCls,newDefs]*)?
    ;

addMap[Set<Deficiency> oldDefs, 
       Map<Deficiency,SClass> old2innerCls, 
       Set<Deficiency> newDefs] returns [Map<Deficiency,SClass> res]
    : 
        ( 
            REPLACE oldD=NAME 
            ( '{' 
                deficiency2ordering=getCheckedDeficiencies[newDefs] 
                RELATIONS
                addRelation[deficiency2ordering]* 
                '}' | 
                clsPath=getPath 
            )
        )
    ;

getCheckedDeficiencies[Set<Deficiency> newDefs] 
    returns [Map<Deficiency,DeficiencyNode> res] 
@init {
Map<Deficiency,DeficiencyNode> deficiency2ordering = 
			       new HashMap<Deficiency,DeficiencyNode>();
}
    : addDeficiency[deficiency2ordering]+ 
    ;

addRelations[Map<Deficiency,DeficiencyNode> deficiency2ordering] 
    : (RELATIONS addRelation[deficiency2ordering]*)?
    ;

addRelation[Map<Deficiency,DeficiencyNode> deficiency2ordering] 
    : (defT1=NAME IMPLIES defT2=NAME END)
    ;
