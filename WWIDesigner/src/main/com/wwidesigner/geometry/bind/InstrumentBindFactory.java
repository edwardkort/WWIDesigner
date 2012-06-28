/**
 * 
 */
package com.wwidesigner.geometry.bind;

import com.wwidesigner.geometry.Instrument;

/**
 * @author kort
 * 
 */
public class InstrumentBindFactory
{
	public InstrumentBindFactory()
	{

	}

	public Object createDomainObject(Object xmlObject, boolean convertToMm)
	{
		Object domainObject = null;
		String xmlObjectName = xmlObject.getClass().getSimpleName();

		switch (xmlObjectName)
		{
			case "XmlInstrument":
				domainObject = createInstrument((XmlInstrument) xmlObject);
				break;
		}

		return domainObject;
	}

	public Instrument createInstrument(XmlInstrument xmlInstrument)
	{
		Instrument instrument = new Instrument();
		
		instrument.setName(xmlInstrument.getName());
		instrument.setDescription(xmlInstrument.getDescription());
		
		boolean convertToMm = xmlInstrument.getLengthType().equals(XmlLengthType.IN);
		
		instrument.setMouthpiece((MouthPiece)createDomainObject(xmlInstrument.getMouthpiece(), convertToMm));

		return instrument;
	}
}
