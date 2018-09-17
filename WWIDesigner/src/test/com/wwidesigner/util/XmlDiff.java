/**
 * 
 */
package com.wwidesigner.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.FloatingPointTolerantDifferenceListener;
import org.w3c.dom.Node;

/**
 * @author kort
 * 
 *         Changes default XML comparison by: 1. Ignoring formatting whitespace
 *         2. Ignoring differences in namespace prefix 3. Ignoring number
 *         differences of less than 0.001
 */
public class XmlDiff extends Diff
{
	private double numberTolerance = 0.001d;

	public XmlDiff(File controlFile, File testFile) throws Exception
	{
		super(createFileReader(controlFile), createFileReader(testFile));
		XMLUnit.setNormalizeWhitespace(true);
		SimpleDiffListener sdiff = new SimpleDiffListener();
		FloatingPointTolerantDifferenceListener fdiff = new FloatingPointTolerantDifferenceListener(
				sdiff, numberTolerance);
		overrideDifferenceListener(fdiff);
	}

	private static FileReader createFileReader(File testFile)
			throws FileNotFoundException
	{
		return new FileReader(testFile);
	}

	class SimpleDiffListener implements DifferenceListener
	{
		public int differenceFound(Difference difference)
		{
			if (DifferenceConstants.NAMESPACE_URI_ID == difference.getId()
					|| DifferenceConstants.SCHEMA_LOCATION_ID == difference
							.getId()
					|| DifferenceConstants.NAMESPACE_PREFIX_ID == difference
							.getId())
			{
				return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			}

			return RETURN_ACCEPT_DIFFERENCE;
		}

		public void skippedComparison(Node control, Node test)
		{
		}
	}
}
