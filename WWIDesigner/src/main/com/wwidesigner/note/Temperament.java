/**
 * 
 */
package com.wwidesigner.note;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kort
 * 
 */
public class Temperament
{
	protected String name;
	protected String comment;
	protected List<Double> ratio;

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the comment
	 */
	public String getComment()
	{
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

	/**
	 * @return the ratio
	 */
	public List<Double> getRatio()
	{
		if (ratio == null)
		{
			ratio = new ArrayList<Double>();
		}
		return this.ratio;
	}

	/**
	 * @param ratio
	 *            the ratio to set
	 */
	public void setRatio(List<Double> ratio)
	{
		this.ratio = ratio;
	}

	public void addRatio(Double newRatio)
	{
		getRatio();
		ratio.add(newRatio);
	}

	public static Temperament makeTET_12()
	{
		Temperament temp = new Temperament();
		temp.setName("12-Tone Equal Temperament");
		temp.setComment("12-tone, equal temperament in three octaves.");

		for (int i = 0; i < 37; i++)
		{
			double ratio = Math.pow(2., (double) i / 12.);
			temp.addRatio(ratio);
		}
		return temp;
	}

	public static Temperament makeJI_12()
	{
		double[] octaveMultiplier = { 1., 2., 4. };
		Temperament temp = new Temperament();
		temp.setName("12-Tone Just Intonation");
		temp.setComment("12-tone, just intonation with 7-limit tritone in three octaves.");

		temp.addRatio(1.);
		for (int i = 0; i < octaveMultiplier.length; i++)
		{
			double mult = octaveMultiplier[i];
			temp.addRatio(mult * 16. / 15);
			temp.addRatio(mult * 9. / 8);
			temp.addRatio(mult * 6. / 5);
			temp.addRatio(mult * 5. / 4);
			temp.addRatio(mult * 4. / 3);
			temp.addRatio(mult * 7. / 5);
			temp.addRatio(mult * 3. / 2);
			temp.addRatio(mult * 8. / 5);
			temp.addRatio(mult * 5. / 3);
			temp.addRatio(mult * 9. / 5);
			temp.addRatio(mult * 15. / 8);
			temp.addRatio(mult * 2.);
		}

		return temp;
	}

	public enum StandardTemperament
	{
		TET_12("12-tone equal temperament"), JI_12("12-tone just intonation");
		private String description;

		private StandardTemperament(String description)
		{
			this.description = description;
		}

		public String toString()
		{
			return description;
		}

	}

	public static Temperament makeStandardTemperament(StandardTemperament type)
	{
		switch (type)
		{
			case TET_12:
				return makeTET_12();
			case JI_12:
				return makeJI_12();
			default:
				return null;
		}
	}

	public void deleteNulls()
	{
		// Do nothing with name
		if (comment != null && comment.trim().length() == 0)
		{
			comment = null;
		}
		if (ratio != null)
		{
			for (int i = ratio.size() - 1; i >= 0; i--)
			{
				if (ratio.get(i) == null)
				{
					ratio.remove(i);
				}
			}
		}

	}

}
