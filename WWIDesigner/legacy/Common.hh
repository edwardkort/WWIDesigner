// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef COMMON_DOT_HH
#define COMMON_DOT_HH

#define Extern extern

#include <iostream>
#include <complex>
#include <cmath>

typedef std::complex<double> complex;

// Mathematical Constants:
const double PI = 3.14159265358979;		// pi
const complex I = complex(0.0,1.0);		// i
const complex J = complex(0.0,-1.0);	// j
const complex ONE = complex(1.0,0.0);	// 1 
const complex ZERO = complex(0.0,0.0);	// 0

#endif // COMMON_DOT_HH
