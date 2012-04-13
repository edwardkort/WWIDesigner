/* A Bison parser, made by GNU Bison 2.1.  */

/* Skeleton parser for Yacc-like parsing with Bison,
   Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003, 2004, 2005 Free Software Foundation, Inc.

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 51 Franklin Street, Fifth Floor,
   Boston, MA 02110-1301, USA.  */

/* As a special exception, when this file is copied by Bison into a
   Bison output file, you may use that output file without restriction.
   This special exception was added by the Free Software Foundation
   in version 1.24 of Bison.  */

/* Tokens.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
   /* Put the tokens into the symbol table, so that GDB and other debuggers
      know about them.  */
   enum yytokentype {
     T_LBRACE = 258,
     T_RBRACE = 259,
     T_LSQUARE = 260,
     T_RSQUARE = 261,
     T_LBRACKET = 262,
     T_RBRACKET = 263,
     T_DOUBLE = 264,
     T_INT = 265,
     T_STRING = 266,
     T_AT = 267,
     T_COMMA = 268,
     T_EQUALS = 269,
     T_BORE = 270,
     T_EDGE_RAD_OF_CURV = 271,
     T_EMBOUCHURE = 272,
     T_EMB_CAV_LENGTH = 273,
     T_EMB_CHAR_DIM = 274,
     T_FLUTE = 275,
     T_FROM = 276,
     T_HOLES = 277,
     T_DEPTH = 278,
     T_DIAM = 279,
     T_FLANGE_DIAM = 280,
     T_PARAMETERS = 281,
     T_PAD = 282,
     T_HEIGHT = 283,
     T_TEMP = 284,
     T_LENGTH_REF = 285,
     T_LENGTH_UNITS = 286,
     T_TERMINATION = 287,
     T_MM = 288,
     T_CM = 289,
     T_M = 290,
     T_IN = 291,
     T_NOTES = 292
   };
#endif
/* Tokens.  */
#define T_LBRACE 258
#define T_RBRACE 259
#define T_LSQUARE 260
#define T_RSQUARE 261
#define T_LBRACKET 262
#define T_RBRACKET 263
#define T_DOUBLE 264
#define T_INT 265
#define T_STRING 266
#define T_AT 267
#define T_COMMA 268
#define T_EQUALS 269
#define T_BORE 270
#define T_EDGE_RAD_OF_CURV 271
#define T_EMBOUCHURE 272
#define T_EMB_CAV_LENGTH 273
#define T_EMB_CHAR_DIM 274
#define T_FLUTE 275
#define T_FROM 276
#define T_HOLES 277
#define T_DEPTH 278
#define T_DIAM 279
#define T_FLANGE_DIAM 280
#define T_PARAMETERS 281
#define T_PAD 282
#define T_HEIGHT 283
#define T_TEMP 284
#define T_LENGTH_REF 285
#define T_LENGTH_UNITS 286
#define T_TERMINATION 287
#define T_MM 288
#define T_CM 289
#define T_M 290
#define T_IN 291
#define T_NOTES 292




#if ! defined (YYSTYPE) && ! defined (YYSTYPE_IS_DECLARED)
#line 31 "parser.yy"
typedef union YYSTYPE {
	double dval;
	int ival;
	char sval[1024];
} YYSTYPE;
/* Line 1447 of yacc.c.  */
#line 118 "parser.h"
# define yystype YYSTYPE /* obsolescent; will be withdrawn */
# define YYSTYPE_IS_DECLARED 1
# define YYSTYPE_IS_TRIVIAL 1
#endif

extern YYSTYPE yylval;



