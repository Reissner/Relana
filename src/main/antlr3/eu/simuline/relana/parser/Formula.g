
/**
 * A parser for relana-formulae. 
 * The main method of the parser is {@link #getFormula(CClass)}. 
 */
grammar Formula;

@header {
    package eu.simuline.relana.parser;

    import eu.simuline.relana.model.CClass;
    import eu.simuline.relana.model.Deficiency;

    import eu.simuline.relana.expressions.FormulaDecl;

    import java.util.Set;
    import java.util.HashSet;
} // @header 


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
CLASS:            'class';
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
getFormula[CClass cClass] returns [FormulaDecl res] 
    : 
        (decl=getConstFormula | 
        ( ((NAME INV? (COV | CONT) ) | 
            UNION | INTERSECT | COMPLEMENT) '(')=> 
decl=getCompFormula[cClass] | 
decl=getVarFormula) {$res=decl;};


/**
 * Parses a constant formula. 
 * 
 * @return
 *    the declaration of the constant formula parsed. 
 */
getConstFormula returns [FormulaDecl decl] 
@init{Set<Deficiency> defs = new HashSet<Deficiency>();}
    : '<' path=getPath '>' 
        {
            throw new eu.simuline.util.NotYetImplementedException();
        }
'{' name=NAME {defs.add(new Deficiency($name.text));} '}';

/**
 * Parses a formula consisting of a single variable. 
 * 
 * @return
 *    the declaration of the formula parsed. 
 */
getVarFormula returns [FormulaDecl decl] 
    :  path = getPath
        {
            throw new eu.simuline.util.NotYetImplementedException();
        };


/**
 * Parses a compound formula which has at least one operation recursively. 
 * 
 * @return
 *    the declaration of the formula parsed. 
 */
getCompFormula[CClass cClass] returns [FormulaDecl decl] 
@init{Set<FormulaDecl> args = new HashSet<FormulaDecl>();}
    : 
        ( 
            opT=UNION | 
            opT=INTERSECT | 
            opT=COMPLEMENT | 
            (opT=NAME (invT=INV)? (accT=CONT | accT=COV) )
        ) 
        {
            throw new eu.simuline.util.NotYetImplementedException();
        }
        ('(' addFormula[args,cClass] (',' addFormula[args,cClass])* ')'
        );

/**
 * Adds a formula to a set of formulae. 
 * This is needed to read in the parameters of a compound formula. 
 * 
 * @param args
 *    a set of formulae: the arguments of a compound formula read so  far. 
 * @see #getCompFormula
 */
addFormula[Set<FormulaDecl> args, CClass cClass]
    : arg=getFormula[cClass] {args.add(arg);};


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




