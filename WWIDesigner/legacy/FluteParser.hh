// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef FLUTE_PARSER_DOT_HH
#define FLUTE_PARSER_DOT_HH

#include <map>
#include <string>
#include "Flute.hh"
#include "Hole.hh"
#include "BoreSect.hh"

class FluteParser;

extern const FILE * yyin;

void yyerror(const char * msg);

extern "C" int yylex();

/**
 * Creates a \a Flute from a formatted input file.
 */
class FluteParser
{
	public:

		friend int yyparse();

		static FluteParser & GetFluteParser();

		virtual ~FluteParser()
		{
			// Empty.
		}

		void CreateFluteFromInput(Flute & rFlute, const FILE * pf);

	private:

		class BorePoint
		{
			public:

				BorePoint() : diam(0.0), p_hole(NULL), p_bore(NULL)
				{
					// Empty.
				}

				double diam;
				Hole * p_hole;
				BoreSect * p_bore;
		};

		// ------------------------------------------------------------------
		// Private member functions:
		//

		FluteParser() : mCurrentLengthRef(0), mLineNum(1)
		{
			// Empty.
		}

		void SetPhysParams(double tc)
		{
			mParams.set(tc);
		}

		void AddNoteConfig(std::string name, int octave, std::string holeConfig)
		{
			mpFlute->AddNote(NominalNote(name, octave), holeConfig);
		}

		void SetCurLengthRef(std::string label);

		void SetLengthFactor(double factor)
		{
			mLengthFactor = factor;
		}

		double Metres(double length)
		{
			return mLengthFactor * length;
		}

		void AddLengthRef(std::string label, double pos);

		void AddBorePoint(double pos, double diam);

		void AddHole(double pos, double diam, double length, double edgeRC);
		
		void AddSilverFluteHole(double pos, double diam, double length, 
				double padHeight, double padDiam, double edgeRC);

		void SetEmbouchure(double lChar, double lCav);

		void SetTermination(double flangeDiam);

		void ProcessParsedData();

		// ------------------------------------------------------------------
		// Private member data:
		//

		static FluteParser * smpFluteParser;

		Flute * mpFlute;

		Embouchure * mpEmbouchure;

		std::map<std::string, double> mLengthRefs;
		double mCurrentLengthRef;

		std::map<double, BorePoint> mBorePoints;

		PhysParams mParams;

		double mLengthFactor;

		double mFlangeRad;

		int mLineNum;
};

#endif // FLUTE_PARSER_DOT_HH
