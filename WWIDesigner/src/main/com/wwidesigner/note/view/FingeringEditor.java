package com.wwidesigner.note.view;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.wwidesigner.note.Fingering;

public class FingeringEditor extends AbstractCellEditor implements
		TableCellEditor
{
	Fingering fingering;
	FingeringComponent fingeringComp;

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column)
	{
		fingeringComp = new FingeringComponent((Fingering) value, true, true);
		fingering = (Fingering) value;

		return fingeringComp;
	}

	@Override
	public boolean stopCellEditing()
	{
		fingeringComp.updateFingering(fingering);

		return super.stopCellEditing();
	}

	@Override
	public Object getCellEditorValue()
	{
		return fingering;
	}

}
