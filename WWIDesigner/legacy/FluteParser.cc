// FluteCalc
// Copyright Dan Gordon, 2006

#include "FluteParser.hh"
#include "EndBoreSect.hh"
#include "Embouchure.hh"
#include "SilverFluteHole.hh"

FluteParser * FluteParser::smpFluteParser;

FluteParser & FluteParser::GetFluteParser()
{
	if (smpFluteParser == 0)
	{
		smpFluteParser = new FluteParser();
	}
	return *smpFluteParser;
}

void FluteParser::CreateFluteFromInput(Flute & rFlute, const FILE * pf)
{
	mpFlute = &rFlute;
	yyin = pf;
	//gpFluteParser = this;
	yyparse();
	ProcessParsedData();
}

void FluteParser::SetCurLengthRef(std::string label)
{
	double pos = 0;
	if (label != "")
	{
		std::map<std::string, double>::iterator ref_it = 
			mLengthRefs.find(label);
		if (ref_it == mLengthRefs.end())
		{
			// The reference doesn't exist.
			std::string msg = 
				"The length reference " + label + " doesn't exist.";
			yyerror(msg.c_str());
	}
		else
		{
			pos = ref_it->second;	
		}
	}
	mCurrentLengthRef = pos;
}

void FluteParser::AddLengthRef(std::string label, double pos)
{
	pos += mCurrentLengthRef;
	mLengthRefs[label] = pos;
}

void FluteParser::AddBorePoint(double pos, double diam)
{
	pos += mCurrentLengthRef;
	BorePoint & bp = mBorePoints[pos];
	bp.diam = diam;
}

void FluteParser::AddHole(double pos, double diam, double length, 
		double edgeRC)
{
	double r_ext = (0.5 * diam);
	Hole * p_hole = new Hole(mParams, 0.0, r_ext, length, false, edgeRC);
	pos += mCurrentLengthRef;
	BorePoint & bp = mBorePoints[pos];
	bp.p_hole = p_hole;
}

void FluteParser::AddSilverFluteHole(double pos, double diam, double length, 
		double padHeight, double padDiam, double edgeRC)
{
	double r_ext = (0.5 * diam);
	double r_pad = (0.5 * padDiam);
	Hole * p_hole = 
		new SilverFluteHole(mParams, 0.0, r_ext, length, false, edgeRC,
				padHeight, r_pad);
	pos += mCurrentLengthRef;
	BorePoint & bp = mBorePoints[pos];
	bp.p_hole = p_hole;
}

void FluteParser::SetEmbouchure(double lChar, double lCav)
{
	mpEmbouchure = new Embouchure(mParams, 0.0, lChar, lCav);
}

void FluteParser::SetTermination(double flangeDiam)
{
	mFlangeRad = 0.5 * flangeDiam;
}

void FluteParser::ProcessParsedData()
{
	if (!mBorePoints.empty())
	{
		// Deal with the Embouchure and the start of the bore:
		{
			std::map<double, BorePoint>::iterator it = mBorePoints.find(0.0);
			if (it == mBorePoints.end())
			{
				yyerror("There must be a bore specification at position 0.0");
			}
			if (mpEmbouchure != NULL)
			{
				mpEmbouchure->SetRB(0.5 * it->second.diam);
				mpFlute->SetEmbouchure(mpEmbouchure);
			}
		}

		// Set the diameter at the holes.
		{
			std::map<double, BorePoint>::iterator prev_it = mBorePoints.end();
			std::map<double, BorePoint>::iterator cur_it;
			for (cur_it = mBorePoints.begin(); 
					cur_it != mBorePoints.end(); 
					++cur_it)
			{
				Hole * p_hole = cur_it->second.p_hole;
				if (p_hole != NULL) 
				{
					std::map<double, BorePoint>::iterator next_it;
					for (next_it = cur_it; 
							next_it->second.diam == 0.0; 
							++next_it)
					{
						;
					}
					if (prev_it == mBorePoints.end() || 
							next_it == mBorePoints.end())
					{
						yyerror("The flute must not begin or end with a hole");
					}

					double L = next_it->first - prev_it->first;
					double l = cur_it->first - prev_it->first;
					double d1 = prev_it->second.diam;
					double d3 = next_it->second.diam;
					double d2 = d1 + (d3 - d1)*l/L;
					cur_it->second.diam = d2;
					p_hole->SetRB(0.5 * d2);
					prev_it = cur_it;
				}
				else
				{
					prev_it = cur_it;
				}
			}
		}

		// Make the BoreSections.
		{
			std::map<double, BorePoint>::iterator cur_it = mBorePoints.begin();
			std::map<double, BorePoint>::iterator next_it = cur_it; ++next_it;
			for (; next_it != mBorePoints.end(); ++cur_it, ++next_it)
			{
				double l_b = (next_it->first - cur_it->first);
				double r_b_l = (0.5 * cur_it->second.diam);
				double r_b_r = (0.5 * next_it->second.diam);
				BoreSect * p_bore = new BoreSect(mParams, l_b, r_b_l, r_b_r);
				cur_it->second.p_bore = p_bore;
			}

		}

		// Add the components.
		{
			std::map<double, BorePoint>::iterator cur_it;
			for (cur_it = mBorePoints.begin(); 
					cur_it != mBorePoints.end(); 
					++cur_it)
			{
				Hole * p_hole = cur_it->second.p_hole;
				BoreSect * p_bs = cur_it->second.p_bore;
				if (p_hole)
				{
					mpFlute->AddHole(p_hole);
				}
				if (p_bs)
				{
					mpFlute->AddBore(p_bs);	
				}
			}	
		}

		// Add the termination.
		{
			std::map<double, BorePoint>::iterator last_it = mBorePoints.end();
			--last_it;
			--last_it;
			BoreSect * p_bs = last_it->second.p_bore;
			if (p_bs == NULL)
			{
				yyerror("The flute must end with a bore section.");
			}
			Termination * p_term = new EndBoreSect(*p_bs, mFlangeRad);
			mpFlute->SetTermination(p_term);
		}
	} // !mBorePoints.empty()
	mpFlute->Validate();
}
