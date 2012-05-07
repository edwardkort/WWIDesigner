package com.wwidesigner.gui;

import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * <p>Title: NAF Designer</p>
 * <p>Description: </p>
 * @author Edward Kort
 * @version 1.0
 */

public class FingeringGroup extends JPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int mNextRow = 1;
    protected ArrayList<FingeringComponent> mFingerings = new ArrayList<FingeringComponent>();
    protected int mNumHoles;
    protected int mHighestOpenHole;

    public FingeringGroup( int numHoles, int highestOpenHole )
    {
        mNumHoles = numHoles;
        mHighestOpenHole = highestOpenHole;
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
        frame.setSize( 400, 400 );
        FingeringGroup fingeringGroup1 = new FingeringGroup( 6, 3 );
        frame.getContentPane().add( fingeringGroup1 );
        //frame.pack();
        frame.setVisible(true);
    }

    private void jbInit() throws Exception
    {
        this.setLayout( new GridBagLayout() );
        Font buttonFont = new java.awt.Font( "Dialog", 1, 14 );
        JButton addFingeringButton = new JButton( "+" );
        addFingeringButton.setFont( buttonFont );
        addFingeringButton.setPreferredSize(new Dimension(18, 18));
        addFingeringButton.setToolTipText("Add fingering");
        addFingeringButton.setMargin(new Insets(2, 2, 2, 2));
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add( addFingeringButton, gbc );

        final JButton removeFingeringButton = new JButton( "-" );
        removeFingeringButton.setFont( buttonFont );
        removeFingeringButton.setPreferredSize( new Dimension( 18, 18 ) );
        removeFingeringButton.setToolTipText( "Remove fingering" );
        removeFingeringButton.setMargin( new Insets( 1, 1, 3, 3 ) );

        FingeringComponent fc = new FingeringComponent( mNumHoles, mHighestOpenHole );
        mFingerings.add( fc );
        gbc.gridx = 1;
        gbc.gridy = mNextRow++;
//        this.add( fc, gbc );
        this.add( fc);

        addFingeringButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent ev )
            {
                FingeringGroup fg = FingeringGroup.this;
                FingeringComponent fc = new FingeringComponent( mNumHoles, mHighestOpenHole );
                mFingerings.add( fc );
                if ( mNextRow > 2 )
                {
                    fg.remove( removeFingeringButton );
                }
                gbc.gridx = 1;
                gbc.gridy = mNextRow++;
                fg.add( fc, gbc );
                gbc.gridx = 0;
                gbc.gridy = mNextRow;
                fg.add( removeFingeringButton, gbc );
                fg.updateUI();
            }
        } );

        removeFingeringButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent ev )
            {
                FingeringGroup fg = FingeringGroup.this;
                FingeringComponent fc = (FingeringComponent)mFingerings.remove( mFingerings.size() - 1 );
                fg.remove( fc );
                fg.remove( removeFingeringButton );
                if ( mNextRow > 3 )
                {
                    gbc.gridx = 0;
                    gbc.gridy = mNextRow;
                    fg.add( removeFingeringButton, gbc );
                }
                else
                {
                    fg.remove( removeFingeringButton );
                }
                mNextRow--;
                fg.updateUI();
            }
        } );
    }
}
