package com.wwidesigner.note.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.wwidesigner.gui.util.ComponentPainter;
import com.wwidesigner.note.Fingering;

/**
 * <p>
 * Title: NAF Designer
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author Edward Kort
 * @version 1.0
 */

public class FingeringComponent extends JPanel
{
	/**
	 * 
	 */
	protected JRadioButton[] mHoles;
	protected JPanel mButtonPanel = new JPanel();
	protected Image mImage;
	protected int mHoleLength = 16;
	protected float strokeWidth = NORMAL_STROKE_WIDTH;
	protected static float NORMAL_STROKE_WIDTH = 1.0f;
	protected static float BOLD_STROKE_WIDTH = 3.0f;
	protected static Color NORMAL_COLOR = Color.BLACK;
	protected static Color EDIT_COLOR = Color.RED;
	protected Color strokeColor = NORMAL_COLOR;

	public FingeringComponent(Fingering fingering)
	{
		this(fingering, false);
	}

	public FingeringComponent(Fingering fingering, boolean isBold)
	{
		this(fingering, isBold, false);
	}

	public FingeringComponent(Fingering fingering, boolean isBold,
			boolean isEditing)
	{
		this(fingering.getOpenHole().size(), isBold, isEditing);
		try
		{
			setFingering(fingering);
		}
		catch (Exception e)
		{

		}
	}

	public FingeringComponent(int numHoles)
	{
		this(numHoles, false);
	}

	public FingeringComponent(int numHoles, boolean isBold)
	{
		this(numHoles, isBold, false);
	}

	public FingeringComponent(int numHoles, boolean isBold, boolean isEditing)
	{
		Border border = new EmptyBorder(2, 2, 2, 2);
		setBorder(border);

		makeHoles(numHoles);
		if (isBold)
		{
			strokeWidth = BOLD_STROKE_WIDTH;
		}
		if (isEditing)
		{
			strokeColor = EDIT_COLOR;
		}
		try
		{
			addHoles();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(Color.WHITE);
		g2.fill(new Rectangle(getPreferredSize()));
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setColor(strokeColor);
		g2.draw(new Arc2D.Double(5, 2, 70, 22, 90, 180, Arc2D.OPEN));
		int lineLength = mHoles.length * mHoleLength + 40;
		g2.draw(new Line2D.Double(40, 2, lineLength, 2));
		g2.draw(new Line2D.Double(40, 24, lineLength, 24));
		g2.draw(new Line2D.Double(lineLength, 2, lineLength, 24));
		g2.setStroke(new BasicStroke(NORMAL_STROKE_WIDTH));
		g2.setColor(NORMAL_COLOR);
	}

	public void setHoles(boolean[] closedHoles) throws Exception
	{
		try
		{
			for (int i = 0; i < mHoles.length; i++)
			{
				JRadioButton hole = mHoles[i];
				hole.setSelected(closedHoles[i]);
			}
		}
		catch (Exception ex)
		{
			throw new Exception(
					"Incorrect number of closedHoles in setHoles().");
		}
	}

	public void setFingering(Fingering fingering) throws Exception
	{
		List<Boolean> openHoles = fingering.getOpenHole();
		boolean[] closedHoles = new boolean[openHoles.size()];
		int i = 0;
		for (boolean open : openHoles)
		{
			closedHoles[i++] = !open;
		}

		setHoles(closedHoles);
	}

	public Fingering updateFingering(Fingering fingering)
	{
		boolean[] openHoles = getOpenHoles();
		fingering.setOpenHoles(openHoles);

		return fingering;
	}

	public boolean[] getHoles()
	{
		boolean[] closedHoles = new boolean[mHoles.length];
		for (int i = 0; i < mHoles.length; i++)
		{
			closedHoles[i] = mHoles[i].isSelected();
		}

		return closedHoles;
	}

	public boolean[] getOpenHoles()
	{
		boolean[] holes = getHoles();
		for (int i = 0; i < mHoles.length; i++)
		{
			holes[i] = !holes[i];
		}

		return holes;
	}

	public Fingering getFingering()
	{
		Fingering fingering = new Fingering();
		boolean[] closedHoles = getHoles();

		for (boolean closedHole : closedHoles)
		{
			fingering.addOpenHole(!closedHole);
		}

		return fingering;
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
	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}

	@Override
	public Dimension getPreferredSize()
	{
		int width = mHoles.length * mHoleLength + 45;
		int height = 28;
		return new Dimension(width, height);
	}

	public static void main(String[] args)
	{
		try
		{
			final JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			FingeringComponent fingeringComponent1 = new FingeringComponent(6);
			boolean[] closedHoles = new boolean[6];
			closedHoles[1] = true;
			closedHoles[4] = true;
			fingeringComponent1.setHoles(closedHoles);
			JPanel panel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.gridx = 0;
			gbc.gridy = 0;
			panel.add(fingeringComponent1, gbc);
			FingeringComponent fingeringComponent2 = new FingeringComponent(20);
			gbc.gridy = 1;
			panel.add(fingeringComponent2, gbc);
			FingeringComponent fingeringComponent3 = new FingeringComponent(20,
					true);
			gbc.gridy = 2;
			panel.add(fingeringComponent3, gbc);
			FingeringComponent fingeringComponent4 = new FingeringComponent(20,
					true, true);
			gbc.gridy = 3;
			panel.add(fingeringComponent4, gbc);
			ComponentPainter.paintComponent(panel, 90, 30);
			frame.getContentPane().add(panel);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}

	private void addHoles() throws Exception
	{
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy = 0;
		for (int i = 0; i < mHoles.length; i++)
		{
			if (i == 0)
			{
				gbc.insets = new Insets(0, 15, 0, 0);
			}
			else
			{
				gbc.insets = new Insets(0, 0, 0, 0);
			}
			gbc.gridx = i;
			add(mHoles[i], gbc);
		}
	}

	protected void makeHoles(int numHoles)
	{
		mHoles = new JRadioButton[numHoles];
		for (int i = 0; i < numHoles; i++)
		{
			JRadioButton hole = makeHole();
			mHoles[i] = hole;
		}
	}

	protected JRadioButton makeHole()
	{
		JRadioButton hole = new JRadioButton();
		hole.setPreferredSize(new Dimension(mHoleLength, 13));

		return hole;
	}
}
