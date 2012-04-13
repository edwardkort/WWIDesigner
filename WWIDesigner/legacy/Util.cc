// FluteCalc
// Copyright Dan Gordon, 2006

#include "Util.hh"
#include "Flute.hh"
#include "BoreSect.hh"

std::ostream & output_bore(const Flute & flute, std::ostream & os, 
		const char sep)
{
	const std::list<BoreSect *> & bore = flute.GetBore();
	bool first = true;
	float length = 0.0;
	for (std::list<BoreSect *>::const_iterator it = bore.begin(); 
			it != bore.end(); ++it)
	{
		if (first)
		{
			os << length << sep << 2.0 * (**it).GetRBL() << std::endl;
		}
		length += (**it).GetLB();
		os << length << sep << 2.0 * (**it).GetRBR() << std::endl;
		first = false;
	}
	return os;
}
