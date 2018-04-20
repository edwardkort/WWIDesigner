package com.wwidesigner.gui.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

/**
 * TransferHandler specifically for JTextComponents that turns off support for
 * drag and drop.
 * 
 * @author Edward N. Kort
 *
 */
public class NoDropTextTransferHandler extends TransferHandler
{
	/**
	 * For the source, only support cut and copy.
	 */
	@Override
	public int getSourceActions(JComponent comp)
	{
		return COPY_OR_MOVE;
	}

	/**
	 * @return null if source is not a JTextComponent, an empty StringSelection
	 *         if no text is selected.
	 */
	@Override
	public Transferable createTransferable(JComponent comp)
	{
		if (!(comp instanceof JTextComponent))
		{
			return null;
		}

		JTextComponent textComp = (JTextComponent) comp;
		return new StringSelection(textComp.getSelectedText());
	}

	/**
	 * If the transfer action is a move, deletes the selected text from the
	 * source JTextComponent.
	 */
	@Override
	public void exportDone(JComponent comp, Transferable trans, int action)
	{
		if (!(comp instanceof JTextComponent))
		{
			return;
		}

		if (action == MOVE)
		{
			JTextComponent textComp = (JTextComponent) comp;
			textComp.replaceSelection(null);
		}
	}

	/**
	 * @return True only if the transfer is not a drop, the target is a
	 *         JTextComponent, and the data to be transfered is a String.
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		return !info.isDrop()
				|| !info.isDataFlavorSupported(DataFlavor.stringFlavor)
				|| !(info.getComponent() instanceof JTextComponent);
	}

	/**
	 * Only transfers String data to a JTextComponent in a paste action.
	 * 
	 * @return True only if all the preconditions are satisfied and the transfer
	 *         is successful.
	 */
	@Override
	public boolean importData(TransferHandler.TransferSupport info)
	{
		if (!canImport(info))
		{
			return false;
		}

		// Get source String
		Transferable trans = info.getTransferable();
		String str = null;
		try
		{
			str = (String) trans.getTransferData(DataFlavor.stringFlavor);
		}
		catch (Exception ex)
		{
			return false;
		}

		// Delete selected text in target, then write String at caret.
		JTextComponent textComp = (JTextComponent) info.getComponent();
		try
		{
			int startIdx = textComp.getSelectionStart();
			int endIdx = textComp.getSelectionEnd();
			textComp.getDocument().remove(startIdx, endIdx - startIdx);
			textComp.getDocument().insertString(textComp.getCaretPosition(),
					str, null);
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}

	/**
	 * A no-op replacement.
	 */
	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action)
	{
	}
}
