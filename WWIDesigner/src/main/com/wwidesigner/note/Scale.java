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
public class Scale
{
	protected String name;
	protected String comment;
	protected List<Note> note;

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
	 * @return the note
	 */
	public List<Note> getNote()
	{
		if (note == null)
		{
			note = new ArrayList<Scale.Note>();
		}
		return this.note;
	}

	/**
	 * @param note
	 *            the note to set
	 */
	public void setNote(List<Note> note)
	{
		this.note = note;
	}

	public void addNote(Note newNote)
	{
		getNote();
		note.add(newNote);
	}

	// TODO Add logic that validates requirement for a frequency.
	public static class Note extends com.wwidesigner.note.bind.Note
	{

	}

}
