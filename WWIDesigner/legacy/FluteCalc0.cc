// FluteCalc
// Copyright Dan Gordon, 2006

#include "Flute.hh"
#include "FluteParser.hh"
#include "Util.hh"
#include "ImpedanceSpectrum.hh"
#include <iostream>
#include <fstream>
#include <string>
#include <iomanip>
#include <cmath>
#include <vector>

void GetT(TransferMatrix & t, const Component & c, double freq)
{
	c.CalcT(t, freq);
}

void PrintBore(Flute & flute, const std::string & ofname)
{
	std::ofstream * p_ofile = 0;
	assert(ofname != "");
	const std::string ofname_br = ofname + std::string(".fcb");
	p_ofile = new std::ofstream(ofname_br.c_str());
	output_bore(flute, *p_ofile, '	');
}

void WriteImpedanceSpectrum(Flute & flute, const NominalNote & nomNote,
		double freq_start, double freq_end, int nfreq,
		const std::string & ofname)
{
	std::ofstream * p_ofile = 0;
	if (ofname != "")
	{
		const std::string ofname_imp = ofname + std::string(".fci");
		p_ofile = new std::ofstream(ofname_imp.c_str());
	}

	flute.SetNote(nomNote);

	std::cout << std::endl;
	std::cout << "Calculating the impedance spectrum for note " << nomNote << ":"
		<< std::endl;

	ImpedanceSpectrum imp;
	CalcImpedance(imp, flute, freq_start, freq_end, nfreq);

	std::cout << std::endl;
	std::cout << "Impedance minima :" << std::endl;
	std::cout << std::endl;
	for (std::vector<double>::iterator it = imp.Minima().begin();
			it != imp.Minima().end(); ++it)
	{
		DeviatedNominalNote dnn =
			flute.GetTemp().NearestNote(*it, flute.GetPitchStandard());
		if (dnn == nomNote)
		{
			std::cout << "Match: Impedance min: " << dnn << std::endl;
		}
		else
		{
			std::cout << "       Impedance min: " << dnn << std::endl;
		}
	}

	if (p_ofile != NULL)
	{


		for (std::map<double, complex>::iterator it =
				imp.Spectrum().begin() ; it != imp.Spectrum().end(); ++it)
		{
			*p_ofile
				<< (it->first) << " "
				<< abs(it->second) << " "
				<< (it->second.real()) << " "
				<< (it->second.imag()) << " "
				<< '\n';
		}

		delete p_ofile;
	}
}

void CalcTuning(Flute & flute, int nFreq, const std::string & ofname)
{

	std::ofstream * p_ofile = 0;
	if (ofname != "")
	{
		const std::string ofname_tun = ofname + std::string(".fct");
		p_ofile = new std::ofstream(ofname_tun.c_str());
	}

	const Temperament & temp = flute.GetTemp();
	const double & pitch_st = flute.GetPitchStandard();

	const std::list< NoteConfig > notes = flute.NoteConfigs();
	std::list< NoteConfig >::const_iterator it;
	for (it = notes.begin(); it != notes.end(); ++it)
	{
		const NominalNote nom_note = *it;
		flute.SetNote(nom_note);
		double matching_freq = temp.GetFreq(nom_note, pitch_st);

		std::cout << nom_note << " @ " <<  floor(matching_freq + 0.5)
			<< " Hz (nominal) ";

		double delta_factor =
			pow(Temperament::CENT_FACTOR, Temperament::CENTS_IN_SEMITONE * 4.0);
		double freq_start = matching_freq / delta_factor;
		double freq_end = matching_freq * delta_factor;

		ImpedanceSpectrum imp;
		CalcImpedance(imp, flute, freq_start, freq_end, nFreq);

		double min_deviation_freq = 0;
		double min_deviation = 1e10;

		std::vector<double>::const_iterator it;
		for (it = imp.Minima().begin(); it != imp.Minima().end(); ++it)
		{
			double deviation = fabs(*it - matching_freq);
			if (deviation < min_deviation)
			{
				min_deviation = deviation;
				min_deviation_freq = *it;
			}
		}

		if (min_deviation < 1e10)
		{
			double cents_from_nominal = Temperament::CENTS_IN_OCTAVE *
				log2(min_deviation_freq / matching_freq);

			char plus_or_null = (cents_from_nominal >= 0.0) ? '+' : '\0';
			std::cout << plus_or_null << floor(cents_from_nominal + 0.5)
				<< " cents."
				<< std::endl;
			if (p_ofile != NULL)
			{
				*p_ofile << nom_note << " " << matching_freq
					<< " " << cents_from_nominal << std::endl;
			}
		}
		else
		{
			std::cout << "Flute does not produce a note within an octave of the nominal note "
				<< nom_note << "." << std::endl;
		}
	}
	if (p_ofile != NULL)
	{
		delete p_ofile;
	}
}

int main(int argc, char**argv)
{
	assert(argc == 11);
	const char* ifname = argv[1];
	const std::string ofname = argv[2];
	double pitch_standard = atof(argv[3]);
	const bool do_bore = atoi(argv[4]);
	bool do_tuning = atoi(argv[5]);
	const std::string note = argv[6];
	const int octave = atoi(argv[7]);
	double freq_start = atof(argv[8]);
	double freq_end = atof(argv[9]);
	int nfreq = atoi(argv[10]);

	std::cout << "Reading flute params from file " << ifname << std::endl;
	std::cout << "Writing output to files " << ofname << ".fc..." << std::endl;

	std::cout << std::endl;
	std::cout << "Loading flute from file..." << std::endl;
	std::cout << std::endl;

	Flute flute;
	FILE * p_input_file = fopen(ifname,"r");

	FluteParser::GetFluteParser().CreateFluteFromInput(flute, p_input_file);
	flute.SetPitchStandard(pitch_standard);
	std::cout << "...Finished loading flute from file." << std::endl;

	if (do_bore)
	{
		PrintBore(flute, ofname);
	}
	if (do_tuning)
	{
		CalcTuning(flute, nfreq, ofname);
	}
	if (note != "")
	{
		WriteImpedanceSpectrum(flute, NominalNote(note, octave),
				freq_start, freq_end, nfreq, ofname);
	}
}
