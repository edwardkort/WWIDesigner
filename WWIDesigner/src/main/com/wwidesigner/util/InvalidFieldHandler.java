/* Class to support validity checking in a model object such as Instrument or Tuning. */
package com.wwidesigner.util;

import java.util.ArrayList;
import java.util.List;

public class InvalidFieldHandler
{
	protected String  modelType;
	protected int nrOfErrors;
	protected List<String> messages;

	/**
	 * Construct a handler to accumulate errors in a data structure of type
	 * <code>modelType</code>.
	 * 
	 * @param modelType
	 *            - The type of the data structure tested.
	 */
	public InvalidFieldHandler(String modelType)
	{
		this.modelType = modelType;
		this.nrOfErrors = 0;
		this.messages = new ArrayList<String>();
	}

	/**
	 * Caller has detected a field error.  Add it to the list.
	 * @param message
	 */
	public void logError(String message)
	{
		messages.add(message);
		++ nrOfErrors;
	}
	
	/**
	 * Report any errors detected. If logError has been called, throw an
	 * exception to report the error.
	 * 
	 * @param firstErrorOnly
	 *            If true, report only the first logged error in the exception
	 *            message. If false, report a summary message in the exception,
	 *            and expect the exception handler to call
	 *            InvalidFieldException.printMessages() for the exception.
	 * @throws InvalidFieldException
	 */
	public void reportErrors(boolean firstErrorOnly)
			throws InvalidFieldException
	{
		if (nrOfErrors > 0)
		{
			InvalidFieldException exception;
			if (nrOfErrors == 1 || firstErrorOnly)
			{
				exception = new InvalidFieldException(modelType,
						messages.get(0), messages);
			}
			else
			{
				exception = new InvalidFieldException(modelType,
						"Invalid data in " + modelType
								+ ". See console log for details.",
						messages);
			}
			throw exception;
		}
	}

}
