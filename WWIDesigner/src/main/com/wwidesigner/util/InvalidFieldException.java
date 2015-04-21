/* Exception used to signal an invalid data in a model object such as Instrument or Tuning. */
package com.wwidesigner.util;

public class InvalidFieldException extends Exception
{
	private String label;

	/**
	 * Construct an exception to signal an error in a data structure of type <code>modelType</code>.
	 * @param modelType - The type of the data structure tested.
	 * @param message - Message describing the error to the end-user.
	 */
	public InvalidFieldException(String modelType, String message)
	{
		super(message);
		this.label = "Invalid " + modelType + " field";
	}

	public String getLabel()
	{
		return label;
	}

}
