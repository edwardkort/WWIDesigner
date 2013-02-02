package com.wwidesigner.gui.util;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class IntegerDocument extends PlainDocument
{
	@Override
	public void insertString(int offset, String s, AttributeSet attributeSet)
			throws BadLocationException
	{
		try
		{
			Integer.parseInt(s);
		}
		catch (Exception ex)
		{
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		super.insertString(offset, s, attributeSet);
	}
}
