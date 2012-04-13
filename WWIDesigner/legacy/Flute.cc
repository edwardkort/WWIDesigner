// FluteCalc
// Copyright Dan Gordon, 2006

#include "Flute.hh"
#include "Termination.hh"
#include "BoreSect.hh"
#include "Hole.hh"
#include "Embouchure.hh"

#include <iostream>
#include <fstream>
#include <string>
#include <algorithm>

void Flute::Validate()
{
	assert(mpTermination);

	std::list<Component *>::iterator comp_it;
	for (comp_it = mComponents.begin(); 
			comp_it != mComponents.end(); 
			++comp_it)
	{
		assert(*comp_it);
		(**comp_it).Validate();
	}
}

void Flute::AddNote(const NominalNote & note, const std::string xoConfig)
{
	std::list<bool> config;
	const char * c_config = xoConfig.c_str();
	for (const char * c = c_config; *c != '\0'; ++c)
	{
		if (*c == 'x')
		{
			config.push_back(1);
		}
		else if (*c == 'o')
		{
			config.push_back(0);
		}
	}
	mNoteConfigs.push_back(NoteConfig(note, config));
}

void Flute::SetNote(const NominalNote & nomNote)
{
	std::list< NoteConfig >::const_iterator note_it = 
		find(mNoteConfigs.begin(), mNoteConfigs.end(), nomNote);

	if (note_it == mNoteConfigs.end())
	{
		std::cerr << "Note " << nomNote 
			<< " not found in note configurations. Exiting." << std::endl;
		exit(1);
	}

	const std::list<bool> & found_note = note_it->config;
	std::list<bool>::const_iterator config_it;
	std::list<Hole *>::iterator hole_it;
	for	(config_it	= found_note.begin(), hole_it = mHoles.begin(); 
			config_it != found_note.end() && hole_it != mHoles.end(); 
			++config_it, ++hole_it)
	{
		(**hole_it).SetIsClosed(*config_it);
		(**hole_it).Validate();
	}
}

void Flute::AddBore(BoreSect * pBore)
{
	mComponents.push_back(pBore);
	mBoreSects.push_back(pBore);
}

void Flute::AddHole(Hole * pHole)
{
	mComponents.push_back(pHole);
	mHoles.push_back(pHole);
}

void Flute::SetTermination(Termination * pTerm)
{
	assert(mpTermination == 0);
	mpTermination = pTerm;
}

void Flute::SetEmbouchure(Embouchure * pEmb)
{
	static bool added = false;
	assert (!added);
	added = true;

	mComponents.push_front(pEmb);
	mpEmbouchure = pEmb;
}

complex Flute::CalcZ(double freq) const
{
	TransferMatrix flute_t(ONE, ZERO, ZERO, ONE);
	TransferMatrix comp_t(ONE, ZERO, ZERO, ONE);
	for (std::list<Component *>::const_iterator it = mComponents.begin();
			it != mComponents.end();
			++it)
	{
		const Component & comp = **it;
		comp.CalcT(comp_t, freq);
		flute_t = flute_t * comp_t;
	}
	complex z_l = mpTermination->CalcZL(freq);
	complex result = 
		(z_l * flute_t.mPP + flute_t.mPU) / (z_l * flute_t.mUP + flute_t.mUU);
	return result;
}

std::ostream & Flute::print(std::ostream & os) const
{
	os << "Flute: " << std::endl;
	for (std::list<Component *>::const_iterator it = mComponents.begin();
			it != mComponents.end();
			++it)
	{
		const Component & comp = **it;
		os << comp;	
	}
	return os;
}
