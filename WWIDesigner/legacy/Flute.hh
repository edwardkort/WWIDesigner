// FluteCalc
// Copyright Dan Gordon, 2006

#ifndef FLUTE_DOT_HH
#define FLUTE_DOT_HH

#include <string>
#include <list>
#include "Common.hh"
#include "Temperament.hh"

class BoreSect;
class Hole;
class Component;
class Termination;
class Embouchure;

/**
 * Configuration of open and closed holes corresponding to a given nominal
 * note.
 */
class NoteConfig : public NominalNote
{
	public:

		NoteConfig(const NominalNote & nomNote, const std::list<bool> & conf) :
			NominalNote(nomNote), config(conf)
		{
			// Empty.
		}

		std::list<bool> config;
};

/**
 * Class representing a flute. Contains a linear chain of \a Components,
 * terminated by a \a Termination.
 */
class Flute
{
	public:

		// ------------------------------------------------------------------
		// Life cycle:
		//

		Flute() :
			mComponents(),
			mHoles(),
			mpTermination(0),
			mTemp(Temperament::sEqualTemp()),
			mPitchStandard(440.0)
		{
			// Empty.
		}

		virtual ~Flute()
		{
			// Empty.
		}

		virtual void Validate();

		// ------------------------------------------------------------------
		// Accessor member functions:
		//

		/**
		 * The nominal temperament of the flute.
		 */
		const Temperament & GetTemp() const
		{
			return mTemp;
		}
		/**
		 * The nominal temperament of the flute.
		 */
		void SetTemp(const Temperament & temp)
		{
			mTemp = temp;
		}

		/**
		 * The nominal pitch of the flute, in Hz.
		 */
		const double & GetPitchStandard() const
		{
			return mPitchStandard;
		}
		/**
		 * The nominal pitch of the flute, in Hz.
		 */
		void SetPitchStandard(const double & pitchSt)
		{
			mPitchStandard = pitchSt;
		}

		/**
		 * The Embouchure component.
		 */
		const Embouchure & GetEmbouchure() const
		{
			return *mpEmbouchure;
		}

		/**
		 * The bore sections in order of from left (head) to right (foot).
		 */
		const std::list<BoreSect *> & GetBore() const
		{
			return mBoreSects;
		}

		/**
		 * The finger/tone holes in order of from left (head) to right (foot).
		 */
		const std::list<Hole *> & GetHoles() const
		{
			return mHoles;
		}

		/**
		 * The Termination (load impedance) at the foot end.
		 */
		const Termination & GetTermination() const
		{
			return *mpTermination;
		}

		// ------------------------------------------------------------------
		// Other member functions:
		//

		/**
		 * Add a nominal note and the configuration of open and closed holes
		 * that produce it.
		 */
		void AddNote(const NominalNote & nomNote, const std::string xoConfig);

		/**
		 * Set the open and closed states of the holes in order to produce
		 * the given note.
		 */
		void SetNote(const NominalNote & nomNote);

		/**
		 * Return the list of hole configurations for the notes.
		 */
		const std::list< NoteConfig > & NoteConfigs()
		{
			return mNoteConfigs;
		}

		/**
		 * Add a \a BoreSect to the right (foot) end.
		 */
		void AddBore(BoreSect * pBore);

		/**
		 * Add a \a Hole to the right (foot) end.
		 */
		void AddHole(Hole * pHole);

		/**
		 * Set the \a Termination.
		 */
		void SetTermination(Termination * pTerm);

		/**
		 * Set the \a Embouchure.
		 */
		void SetEmbouchure(Embouchure * pEmb);

		/**
		 * Calculate the impedance at frequency \a freq.
		 */
		complex CalcZ(double freq) const;

		/**
		 * Stream output operator.
		 */
		friend std::ostream & operator<<(std::ostream & os,
				const Flute & f)
		{
			return f.print(os);
		}

	private:

		// ------------------------------------------------------------------
		// Private member functions:
		//

		virtual std::ostream & print(std::ostream & os) const;

		// ------------------------------------------------------------------
		// Private member data:
		//

		std::list<Component *> mComponents;
		Embouchure * mpEmbouchure;
		std::list<BoreSect *> mBoreSects;
		std::list<Hole *> mHoles;
		Termination * mpTermination;

		std::list< NoteConfig > mNoteConfigs;

		Temperament mTemp;

		double mPitchStandard;
};

#endif // FLUTE_DOT_HH
