package com.wwidesigner.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;


/**
 * <p>Title: NAF Designer</p>
 * <p>Description: </p>
 * @author Edward Kort
 * @version 1.0
 */

public class FingeringComponent extends JPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JRadioButton[] mHoles;
    protected JPanel mButtonPanel = new JPanel();
    protected Image mImage;
    protected int mHighestOpenHole;

    public FingeringComponent( int numHoles, int highestOpenHole )
    {
        mHighestOpenHole = highestOpenHole;
        makeHoles( numHoles );
        mImage = Toolkit.getDefaultToolkit().getImage( "src/main/com/edkort/flute/impedance/gui/smallFlute.gif" );
        MediaTracker mt = new MediaTracker( this );
        try
        {
            mt.addImage( mImage, 0 );
            mt.waitForID( 0 );
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        try
        {
            Insets insets = getInsets();
            addHoles();
            g.drawImage( mImage, insets.left, insets.top, null );
            validate();
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }
    }

    public void setHoles( boolean[] closedHoles ) throws Exception
    {
        try
        {
            for ( int i = mHighestOpenHole + 1; i < mHoles.length; i++ )
            {
                JRadioButton hole = mHoles[i];
                hole.setSelected( closedHoles[i] );
            }
        }
        catch ( Exception ex )
        {
            throw new Exception( "Incorrect number of closedHoles in setHoles()." );
        }
    }

    public boolean[] getHoles()
    {
        boolean[] closedHoles = new boolean[mHoles.length];
        for ( int i = 0; i < mHoles.length; i++ )
        {
            closedHoles[i] = mHoles[i].isSelected();
        }

        return closedHoles;
    }
    
    @Override
    public Dimension getSize()
    {
    	return getPreferredSize();
    }
    
    @Override
    public Dimension getMinimumSize()
    {
    	return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize()
    {
        Insets insets = getInsets();
        int width = mImage.getWidth( null ) + insets.left + insets.right;
        int height = mImage.getHeight( null ) + insets.top + insets.bottom;
        return new Dimension( width, height );
    }

    public static void main(String[] args)
    {
        try
        {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
            FingeringComponent fingeringComponent1 = new FingeringComponent( 6, 0 );
            boolean[] closedHoles = new boolean[6];
            closedHoles[1] = true;
            closedHoles[4] = true;
            fingeringComponent1.setHoles( closedHoles );
            JPanel panel = new JPanel( new GridLayout( 2, 1 ) );
            panel.add( fingeringComponent1 );
            FingeringComponent fingeringComponent2 = new FingeringComponent( 6, 3 );
            closedHoles[5] = true;
            closedHoles[4] = false;
            fingeringComponent2.setHoles( closedHoles );
            panel.add( fingeringComponent2 );
            frame.getContentPane().add( panel );
            frame.pack();
            frame.setVisible(true);
        }
        catch ( Exception ex )
        {
            System.out.println( ex.getMessage() );
        }
    }
    private void addHoles() throws Exception
    {
        for ( int i = 0; i < mHoles.length; i++ )
        {
            this.add( mHoles[i] );
        }
    }

    protected void makeHoles( int numHoles )
    {
        mHoles = new JRadioButton[numHoles];
        for ( int i = 0; i < numHoles; i++ )
        {
            JRadioButton hole = makeHole();
            if ( i < mHighestOpenHole )
            {
                hole.setSelected( true );
                hole.setEnabled( false );
            }
            else if ( i == mHighestOpenHole )
            {
                hole.setEnabled( false );
            }
            mHoles[i] = hole;
        }
    }

    protected JRadioButton makeHole()
    {
        JRadioButton hole = new JRadioButton();
        hole.setPreferredSize( new Dimension( 17, 21 ) );

        return hole;
    }
}
