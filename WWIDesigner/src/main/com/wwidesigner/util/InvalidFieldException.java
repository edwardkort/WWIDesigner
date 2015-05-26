/* Exception used to signal an invalid data in a model object such as Instrument or Tuning. */
package com.wwidesigner.util;

import java.util.ArrayList;
import java.util.List;

public class InvalidFieldException extends Exception
{
	protected String label;
	protected List<String> messages;

	/**
	 * Construct an exception to signal an error in a data structure of type <code>modelType</code>.
	 * @param modelType - The type of the data structure tested.
	 * @param message - Message describing the error to the end-user.
	 */
	public InvalidFieldException(String modelType, String message)
	{
		super(message);
		this.label = "Invalid " + modelType + " field";
		this.messages = new ArrayList<String>();
		this.messages.add(message);
	}

	/**
	 * Construct an exception to signal an error in a data structure of type <code>modelType</code>.
	 * @param modelType - The type of the data structure tested.
	 * @param message - Message describing the error to the end-user.
	 * @param messageList - Complete list of errors found in the data structure.
	 */
	public InvalidFieldException(String modelType, String message, List<String> messageList)
	{
		super(message);
		this.label = "Invalid " + modelType + " field";
		this.messages = messageList;
	}

	public String getLabel()
	{
		return label;
	}
	
	public List<String> getMessages()
	{
		return messages;
	}

	public void printMessages()
	{
		if (messages.size() > 1)
		{
			System.out.println();
		}
		for (String message: messages)
		{
			System.out.println(label + ": " + message);
		}
	}
}
