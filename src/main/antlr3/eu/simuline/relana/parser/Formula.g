
grammar Formula;

@header {
    package eu.simuline.ralana.parser;

    import eu.simuline.relana.model.CClass;
    import eu.simuline.relana.model.Deficiency;

    import eu.simuline.relana.expressions.FormulaDecl;

    import java.util.Set;
    import java.util.HashSet;
} // @header 


@lexer::header {
    package eu.simuline.ralana.parser;
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

getFormula[CClass cClass] returns [FormulaDecl res] 
    : 
        (decl=getConstFormula | 
        ( ((NAME INV? (COV | CONT) ) | 
            UNION | INTERSECT | COMPLEMENT) '(')=> 
decl=getCompFormula[cClass] | 
decl=getVarFormula) {$res=decl;};


getConstFormula returns [FormulaDecl decl] 
@init{Set<Deficiency> defs = new HashSet<Deficiency>();}
    : '<' path=getPath '>' 
        {
            throw new eu.simuline.util.NotYetImplementedException();
        }
'{' name=NAME {defs.add(new Deficiency($name.text));} '}';

getVarFormula returns [FormulaDecl decl] 
    :  path = getPath
        {
            throw new eu.simuline.util.NotYetImplementedException();
        };


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

addFormula[Set<FormulaDecl> args, CClass cClass]
    : arg=getFormula[cClass] {args.add(arg);};


getPath returns [List<String> res] 
@init{$res = new ArrayList<String>();}
    :       first=NAME {res.add($first.text);} 
        (SEP next=NAME {res.add( $next.text);})*;




