package com.wwidesigner.impedance.gui;

import javax.swing.*;
import java.awt.*;


/**
 * <p>Title: NAF Designer</p>
 * <p>Description: </p>
 * @author Edward Kort
 * @version 1.0
 */

public class NoteComponent extends JPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JComboBox<Object> mNoteCombo = new JComboBox<Object>();
    protected GridBagLayout gridBagLayout1 = new GridBagLayout();
    protected JTextField mCentsField = new JTextField();
    protected JLabel jLabel1 = new JLabel();

    public NoteComponent()
    {
        try
        {
            jbInit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        frame.setSize( 300, 300 );
        NoteComponent noteComponent1 = new NoteComponent();
        noteComponent1.mNoteCombo.addItem("A");
        noteComponent1.mNoteCombo.addItem("Bb");
        noteComponent1.mNoteCombo.addItem("C");
        noteComponent1.mCentsField.setText("440.03");
        frame.getContentPane().add( noteComponent1 );
        frame.setVisible(true);
    }
    private void jbInit() throws Exception
    {
        this.setLayout(gridBagLayout1);
        mCentsField.setPreferredSize(new Dimension(50, 21));
        mCentsField.setText("0");
        mCentsField.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setText("cents");
        this.add(mNoteCombo,     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(mCentsField,  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel1,   new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
    }

}
