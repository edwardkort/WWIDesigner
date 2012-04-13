%{
	#include "parser.h"
	#include <math.h> 
	#include <string.h>

	extern int line_num;
%}

%%
"{"                 { return T_LBRACE; }
"}"                 { return T_RBRACE; }
"["                 { return T_LSQUARE; }
"]"                 { return T_RSQUARE; }
"("                 { return T_LBRACKET; }
")"                 { return T_RBRACKET; }
"@"                 { return T_AT; }
","                 { return T_COMMA; }
"="                 { return T_EQUALS; }
"bore"              { return T_BORE; }
"embouchure"    	{ return T_EMBOUCHURE; }
"emb_cav_length"    { return T_EMB_CAV_LENGTH; }
"emb_char_dim"      { return T_EMB_CHAR_DIM; }
"from"				{ return T_FROM; }
"flute"             { return T_FLUTE; }
"holes"             { return T_HOLES; }
"depth"             { return T_DEPTH; }
"diam"              { return T_DIAM; }
"edge_rad_of_curv"  { return T_EDGE_RAD_OF_CURV; }
"parameters"        { return T_PARAMETERS; }
"pad"               { return T_PAD; }
"height"            { return T_HEIGHT; }
"temp"        		{ return T_TEMP; }
"termination"		{ return T_TERMINATION; }
"flange_diam"		{ return T_FLANGE_DIAM; }
"length_ref"      	{ return T_LENGTH_REF; }
"length_units"     	{ return T_LENGTH_UNITS; }
"mm"     			{ return T_MM; }
"cm"     			{ return T_CM; }
"m"     			{ return T_M; }
"in"     			{ return T_IN; }
"notes"				{ return T_NOTES; }

[+-]?[0-9]e?[0-9]*  			{yylval.ival = atoi(yytext); return T_INT;}
[+-]?[0-9]+\.[0-9]*e?[0-9]*    {yylval.dval = atof(yytext); return T_DOUBLE;}
[A-Za-z0-9_#]+	    			{strcpy(yylval.sval, yytext); return T_STRING;}
\n								{++line_num;}
[ \t]+							{}
\%.*							{}
%%

/* EOF */
