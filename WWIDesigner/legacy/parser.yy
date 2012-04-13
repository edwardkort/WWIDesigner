%{
#include<iostream>
#include "FluteParser.hh"
#include "Embouchure.hh"
#include "PhysParams.hh"

extern "C" 
{
	int yylex(void);
	int yywrap() 
	{ 
		return 1; 
	} 
}

int yyparse(void); 

int line_num = 1;

void yyerror(const char *str) 
{ 
	std::cerr << "line " << line_num << ": " << str << std::endl;
	exit(1); 
} 

int yydebug;

%}

%union
{
	double dval;
	int ival;
	char sval[1024];
};

%token T_LBRACE
%token T_RBRACE
%token T_LSQUARE
%token T_RSQUARE
%token T_LBRACKET
%token T_RBRACKET
%token <dval> T_DOUBLE
%token <ival> T_INT
%token <sval> T_STRING
%token T_AT
%token T_COMMA
%token T_EQUALS
%token T_BORE
%token T_EDGE_RAD_OF_CURV 
%token T_EMBOUCHURE
%token T_EMB_CAV_LENGTH
%token T_EMB_CHAR_DIM
%token T_FLUTE
%token T_FROM
%token T_HOLES
%token T_DEPTH
%token T_DIAM
%token T_FLANGE_DIAM
%token T_PARAMETERS
%token T_PAD
%token T_HEIGHT
%token T_TEMP
%token T_LENGTH_REF
%token T_LENGTH_UNITS
%token T_TERMINATION
%token <dval> T_MM
%token <dval> T_CM
%token <dval> T_M
%token <dval> T_IN
%token T_NOTES

%type <dval> int_or_double
%type <dval> length
%type <dval> position_statement
%type <dval> unit
%type <dval> opt_edge_rc

%%

flute_spec
:
	T_FLUTE 
	T_LBRACE
		notes_statement
		parameters_statement
		opt_length_ref_statement
		bore_statement
		opt_embouchure_statement
		opt_holes_statement
		termination_statement
	T_RBRACE
;

length
:
	int_or_double { $$ = FluteParser::GetFluteParser().Metres($1); }
|
	int_or_double unit { $$ = $1 * $2; }
;


int_or_double
:
	T_INT {$$ = $1;}
|
	T_DOUBLE {$$ = $1;}
;
	
notes_statement
:
	T_NOTES 
	T_LBRACE
		note_list
	T_RBRACE
;

note_list
:
	/* Empty */
|
	note_list note
;

note
:
	T_STRING T_LSQUARE T_INT T_RSQUARE T_STRING
{
	FluteParser::GetFluteParser().AddNoteConfig($1, $3, $5);
}
;
parameters_statement
:
	T_PARAMETERS 
	T_LBRACKET
		T_TEMP T_EQUALS int_or_double T_COMMA  T_LENGTH_UNITS T_EQUALS unit
	T_RBRACKET
	{
		FluteParser::GetFluteParser().SetLengthFactor($9);
		FluteParser::GetFluteParser().SetPhysParams($5);
	}
;

unit
:
	T_MM {$$ = 0.001;}
|	T_CM {$$ = 0.01;}
|	T_M {$$ = 1.0;}
|	T_IN {$$ = 0.0254;}
;

opt_length_ref_statement
: 
	/* Empty. */
| 
	length_ref_statement
;	
	
length_ref_statement
:
	T_LENGTH_REF
	T_LBRACE
		length_ref_list
	T_RBRACE
;

length_ref_list
:
	/* Empty. */
|
	length_ref_list length_ref_item
;

length_ref_item
:
	position_statement T_STRING
	{
   		FluteParser::GetFluteParser().AddLengthRef($2, $1);
	}
;

bore_statement
:
	T_BORE
	T_LBRACE
		bore_list
	T_RBRACE
;

bore_list
:
	/* Empty. */
|
	bore_list bore_spec_statement
;

bore_spec_statement
:
	position_statement length
	{
		FluteParser::GetFluteParser().AddBorePoint($1, $2);
	}
;

position_statement
:
	T_LSQUARE T_AT length opt_from_statement T_RSQUARE
	{
		$$ = $3;
	}
;

opt_from_statement
:
	/* Empty. */
	{
		FluteParser::GetFluteParser().SetCurLengthRef("");
	}
|
	T_FROM T_STRING
	{
		FluteParser::GetFluteParser().SetCurLengthRef($2);
	}
;

opt_embouchure_statement
:
	/* Empty. */
|
	embouchure_statement
;

embouchure_statement
:
	T_EMBOUCHURE
	T_LBRACKET
		T_EMB_CAV_LENGTH T_EQUALS length T_COMMA
		T_EMB_CHAR_DIM T_EQUALS length
	T_RBRACKET
	{
		FluteParser::GetFluteParser().SetEmbouchure($9, $5);
	}
;

termination_statement
:
	T_TERMINATION
	T_LBRACKET
		T_FLANGE_DIAM T_EQUALS length
	T_RBRACKET
	{
		FluteParser::GetFluteParser().SetTermination($5);
	}
;

opt_holes_statement
:
	/* Empty. */
|
	holes_statement
;

holes_statement
:
	T_HOLES
	T_LBRACE
		hole_list
	T_RBRACE
;


hole_list
:
	/* Empty. */
|
	hole_list hole_or_silver_flute_hole
;

hole_or_silver_flute_hole
:
	hole
|
	silver_flute_hole
;

hole
:
	position_statement 
	T_LBRACKET
		T_DIAM T_EQUALS length T_COMMA
		T_DEPTH T_EQUALS length 
		opt_edge_rc
	T_RBRACKET
	{
		FluteParser::GetFluteParser().AddHole($1, $5, $9, $10);
	}
;

silver_flute_hole
:
	position_statement 
	T_LBRACKET
		T_DIAM T_EQUALS length T_COMMA
		T_DEPTH T_EQUALS length T_COMMA
		T_PAD T_LBRACKET T_HEIGHT T_EQUALS length 
			T_COMMA T_DIAM T_EQUALS length T_RBRACKET
		opt_edge_rc
	T_RBRACKET
	{
		FluteParser::GetFluteParser().AddSilverFluteHole(
			$1, $5, $9, $15, $19, $21);
	}
;

opt_edge_rc
:
	/* Empty. */
	{
		const double DEFAULT_EDGE_RC = 0.0005;
		$$ = DEFAULT_EDGE_RC;
	}
|
	T_COMMA T_EDGE_RAD_OF_CURV length
{
	$$ = $3;
}
;

%%
