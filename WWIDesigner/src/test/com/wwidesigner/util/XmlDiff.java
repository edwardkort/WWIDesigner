/**
 * 
 */
package com.wwidesigner.util;

import java.io.File;
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
		super(new FileReader(controlFile), new FileReader(testFile));
		XMLUnit.setNormalizeWhitespace(true);
		SimpleDiffListener sdiff = new SimpleDiffListener();
		FloatingPointTolerantDifferenceListener fdiff = new FloatingPointTolerantDifferenceListener(
				sdiff, numberTolerance);
		overrideDifferenceListener(fdiff);
	}

	class SimpleDiffListener implements DifferenceListener
	{
		public int differenceFound(Difference difference)
		{
			return DifferenceConstants.NAMESPACE_PREFIX_ID == difference
					.getId() ? RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL
					: RETURN_ACCEPT_DIFFERENCE;
		}

		public void skippedComparison(Node control, Node test)
		{
		}
	}
}
