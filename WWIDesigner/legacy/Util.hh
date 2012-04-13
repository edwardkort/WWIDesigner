// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef UTIL_DOT_HH
#define UTIL_DOT_HH

#include <iostream>

class Flute;

std::ostream & output_bore(const Flute & flute, std::ostream & os, 
		const char sep);

#endif
