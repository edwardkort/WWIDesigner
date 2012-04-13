// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef TEMPERAMENT_DOT_HH
#define TEMPERAMENT_DOT_HH

#include <cmath>
#include <list>
#include <string>

/**
 * Representation of a note in an octave - it's name, and its deviation from
 * the reference note. For example, in equal temperment, A# is 100 cents 
 * above the reference note A.
 */
class Note 
{
	public:

		Note(std::string nm, double cts) : name(nm), cents(cts)
		{
			// Empty.
		}

		std::string	name;
		double cents;
};

/**
 * The name of a note plus its octave.
 */
class NominalNote
{
	public:

		NominalNote() : name(""), octave(0)
		{
			// Empty.
		}

		NominalNote(std::string nm, int oct) : name(nm), octave(oct)
		{
			// Empty.
		}

		friend int operator==(const NominalNote & lhs, 
				const NominalNote & rhs)
		{
			return lhs.name == rhs.name && lhs.octave == rhs.octave;
		}

		friend int operator<(const NominalNote & lhs, const NominalNote rhs)
		{
			return (lhs.name < rhs.name ||
					(lhs.name == rhs.name) && (lhs.octave < rhs.octave));
		}
		
		friend std::ostream & 
			operator<<(std::ostream & os, const NominalNote & n);

		std::string name;
		int octave;
};

/**
 * The name of a note plus its octave, plus a cents deviation from its 
 * (unspecified) nominal pitch.
 */
class DeviatedNominalNote : public NominalNote
{
	public:

		DeviatedNominalNote() : NominalNote(), cents_deviation(0.0)
		{
			// Empty.
		}

		DeviatedNominalNote(std::string nm, int oct, double cents) : 
			NominalNote(nm, oct), cents_deviation(cents)
		{
			// Empty.
		}

		DeviatedNominalNote(const NominalNote & noteSpec, double cents) : 
			NominalNote(noteSpec), cents_deviation(cents)
		{
			// Empty.
		}

		friend std::ostream & operator<<(std::ostream & os, 
				const DeviatedNominalNote & n);

		double cents_deviation;
};

/**
 * Representation of a temperament. Basically, a list of \a Notes. Extra
 * information in the form of a reference pitch is needed to find the
 * actual frequency of the notes.
 */
class Temperament
{
	public:

		static const double CENT_FACTOR = 1.00057778951;	
		// Multiply freq by CENT_FACTOR^r to raise by r cents.
		static const int CENTS_IN_SEMITONE = 100;
		static const int CENTS_IN_OCTAVE = 1200;
		static const double A440 = 440.0;

		static const Temperament & sEqualTemp();

		/**
		 * The \a Notes of the \a Temperament.
		 */
		std::list<Note> & Notes()
		{
			return mNotes;
		}

		/**
		 * Utility function giving the note nearest to \a freq, if we are
		 * at a reference pitch of \a ref.
		 */
		DeviatedNominalNote NearestNote(double freq, double ref) const;

		/**
		 * Return the frequency of a given nominal note, given that we are 
		 * at a reference pitch of \a ref.
		 */
		double GetFreq(const NominalNote & note, double ref) const;

	private:

		static void Temperament::StaticInit();

		static bool smIsInit;
		static std::string smDiatonicNotes[12];
		static Temperament smEqualTemp;

		std::list<Note> mNotes;
};

#endif // TEMPERAMENT_DOT_HH
