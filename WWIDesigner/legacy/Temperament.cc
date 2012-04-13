// FluteCalc
// Copyright Dan Gordon, 2006

#include "Temperament.hh"
#include <iostream>

std::ostream & operator<<(std::ostream & os, const NominalNote & n)
{
	os <<  n.name << "[" << n.octave << "]";
	return os;
}

std::ostream & operator<<(std::ostream & os, 
		const DeviatedNominalNote & p)
{
	os << p.name << " [" << p.octave << "] ";
	if (p.cents_deviation > 0)
	{
		os << " + " << floor(0.5 + p.cents_deviation) << " cents";
	}
	if (p.cents_deviation < 0)
	{
		os << " - " << floor(0.5 - p.cents_deviation) << " cents";
	}
	os << std::endl;
	return os;
}

const double Temperament::CENT_FACTOR;	
const int Temperament::CENTS_IN_SEMITONE;
const int Temperament::CENTS_IN_OCTAVE;
const double Temperament::A440;
bool Temperament::smIsInit;
Temperament Temperament::smEqualTemp;

void Temperament::StaticInit()
{
	smEqualTemp.mNotes.push_back(Note("C", -9 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("C#", -8 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("D", -7 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("D#", -6 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("Eb", -6 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("E", -5 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("F", -4 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("F#", -3 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("Gb", -3 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("G", -2 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("G#", -1 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("Ab", -1 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("A", 0 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("A#", 1 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("Bb", 1 * CENTS_IN_SEMITONE));
	smEqualTemp.mNotes.push_back(Note("B", 2 * CENTS_IN_SEMITONE));

	smIsInit = true;
}

const Temperament & Temperament::sEqualTemp() 
{
	if (!smIsInit)
	{
		StaticInit();
	}
	return smEqualTemp;
}
		
DeviatedNominalNote Temperament::NearestNote(double freq, double ref) const
{
	assert(freq > 0);
	int n_notes = mNotes.size();

	double cents_min = mNotes.front().cents;
	double cents_from_ref = CENTS_IN_OCTAVE*log2(freq / ref);
	double cents_from_min = cents_from_ref - cents_min;

	// Round down to find the octave the freq falls within.
	int octave = (int) floor(cents_from_min/CENTS_IN_OCTAVE);

	std::list<Note>::const_iterator note_for_min = mNotes.end();
	double min_deviation = CENTS_IN_OCTAVE;

	int octave_shift = 0;
	int min_octave = octave;
	// Need to include notes on either side of the lowest and highest in the
	// octave.
	std::list<Note>::const_iterator note_it;
	for (int i = -1; i < n_notes + 1; ++i)
	{
		if (i == -1)
		{
			octave_shift = - 1;
			note_it = mNotes.end(); --note_it;
		}	
		else if (i == n_notes + 1)
		{
			octave_shift = 1;
			note_it = mNotes.begin();
		}	
		else if (i == 0)
		{
			octave_shift = 0;
			note_it = mNotes.begin();
		}
		else
		{
			octave_shift = 0;
			++note_it;
		}

		int shifted_octave = octave + octave_shift;

		double shifted_cents = cents_from_ref - CENTS_IN_OCTAVE * octave;
		double deviation = shifted_cents - 
			(note_it->cents + CENTS_IN_OCTAVE * octave_shift) ;
		if (fabs(deviation) < fabs(min_deviation))
		{
			min_octave = shifted_octave;
			min_deviation = deviation;
			note_for_min = note_it;
		}	
	}

	assert((note_for_min != mNotes.end()));

	DeviatedNominalNote result;

	result.name = note_for_min->name;
	result.octave = min_octave;
	result.cents_deviation = min_deviation;

	return result;
}

double Temperament::GetFreq(const NominalNote & nomNote, double ref) const
{
	std::list<Note>::const_iterator it;
	for (it = mNotes.begin(); it != mNotes.end(); ++it)
	{
		if (it->name == nomNote.name)
		{
			break;
		}
	}
	assert(it != mNotes.end());
	double factor = pow(CENT_FACTOR, it->cents);
	return (factor * ref) * pow(2, nomNote.octave);
}
