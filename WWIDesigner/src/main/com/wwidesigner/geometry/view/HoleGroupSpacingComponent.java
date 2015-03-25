package com.wwidesigner.geometry.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.wwidesigner.optimization.HoleGroup;
import com.wwidesigner.optimization.HoleGroups;

/**
 * A component that displays and mediates the definition of hole-spacing groups:
 * sets of contiguous holes that have equal hole spacing. Used by the NAF
 * hole-grouping optimizers.
 * 
 * @author Edward Kort
 * @version 1.0
 */

public class HoleGroupSpacingComponent extends JPanel
{
	/**
	 * 
	 */
	private JRadioButton[] mHoles;
	private int mHoleLength = 27;
	private int mHoleHeight = 16;
	private int mSeparatorLength = 10;
	private int mNumHoles;
	protected float strokeWidth = NORMAL_STROKE_WIDTH;
	protected static float NORMAL_STROKE_WIDTH = 1.0f;
	protected static float BOLD_STROKE_WIDTH = 3.0f;
	protected static Color NORMAL_COLOR = Color.BLACK;
	protected static Color EDIT_COLOR = Color.RED;
	protected Color strokeColor = NORMAL_COLOR;
	private HoleGroupSeparator[] mGroupSeparators;

	public HoleGroupSpacingComponent(int numHoles)
	{
		this(numHoles, false);
	}

	public HoleGroupSpacingComponent(int numHoles, boolean isBold)
	{
		this(numHoles, isBold, false);
	}

	public HoleGroupSpacingComponent(int numHoles, boolean isBold,
			boolean isEditing)
	{
		Border border = new EmptyBorder(2, 2, 2, 2);
		setBorder(border);

		mNumHoles = numHoles;
		makeHoles();
		makeSeparators();
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
		g2.draw(new Arc2D.Double(4, 2, 68, mHoleHeight + 12, 90, 180,
				Arc2D.OPEN));
		int lineLength = mNumHoles * (mHoleLength + mSeparatorLength)
				+ mSeparatorLength;
		g2.draw(new Line2D.Double(38, 2, 38 + lineLength, 2));
		g2.draw(new Line2D.Double(38, mHoleHeight + 14, 38 + lineLength,
				mHoleHeight + 14));
		g2.draw(new Line2D.Double(38 + lineLength, 2, 38 + lineLength,
				mHoleHeight + 14));
		g2.setStroke(new BasicStroke(NORMAL_STROKE_WIDTH));
		g2.setColor(NORMAL_COLOR);
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
		// Make minimum width equal to a 4-hole flute.
		if (mNumHoles < 4)
		{
			mNumHoles = 4;
		}
		int width = mNumHoles * (mHoleLength + mSeparatorLength)
				+ mSeparatorLength + 45;
		int height = mHoleHeight + 24;
		return new Dimension(width, height);
	}

	private void addHoles() throws Exception
	{
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 15, 7, 0);
		for (int i = 0; i < mNumHoles; i++)
		{
			gbc.gridx = 2 * i;
			add(mGroupSeparators[i], gbc);
			gbc.gridx++;
			gbc.insets = new Insets(0, 0, 5, 0);
			add(mHoles[i], gbc);
			gbc.insets = new Insets(0, 0, 7, 0);
		}
		gbc.gridx = 2 * mNumHoles;
		add(mGroupSeparators[mNumHoles], gbc);
	}

	protected void makeHoles()
	{
		mHoles = new JRadioButton[mNumHoles];
		for (int i = 0; i < mNumHoles; i++)
		{
			JRadioButton hole = makeHole();
			mHoles[i] = hole;
		}
	}

	protected void makeSeparators()
	{
		mGroupSeparators = new HoleGroupSeparator[mNumHoles + 1];
		for (int i = 0; i <= mNumHoles; i++)
		{
			mGroupSeparators[i] = new HoleGroupSeparator();
		}

		HoleGroupSeparator topSeparator = mGroupSeparators[0];
		topSeparator.setSelected(true);
		topSeparator.setEnabled(false);
		HoleGroupSeparator bottomSeparator = mGroupSeparators[mNumHoles];
		bottomSeparator.setSelected(true);
		bottomSeparator.setEnabled(false);
	}

	protected JRadioButton makeHole()
	{
		JRadioButton hole = new JRadioButton();
		hole.setPreferredSize(new Dimension(mHoleLength, 16));
		hole.setBackground(Color.WHITE);
		hole.setEnabled(false);

		return hole;
	}

	public void setSingleHoleGroups()
	{
		for (HoleGroupSeparator separator : mGroupSeparators)
		{
			separator.setSelected(true);
		}
	}

	public int[][] getHoleGroups()
	{
		HoleGroups holeGroups = new HoleGroups();
		HoleGroup holeGroup = new HoleGroup();
		// The top hole is always in the first group.
		holeGroup.addHole(0);
		for (int i = 1; i < mNumHoles; i++)
		{
			if (mGroupSeparators[i].isSelected())
			{
				holeGroups.addHoleGroup(holeGroup);
				holeGroup = new HoleGroup();
			}
			holeGroup.addHole(i);
		}
		// Add last group
		holeGroups.addHoleGroup(holeGroup);
		
		return holeGroups.getHoleGroupsArray();
	}

	public static void main(String[] args)
	{
		try
		{
			final JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			HoleGroupSpacingComponent spacingComponent1 = new HoleGroupSpacingComponent(
					6);
			spacingComponent1.mGroupSeparators[3].setSelected(true);
			System.out.println(HoleGroups.printGroups(spacingComponent1.getHoleGroups()));

			spacingComponent1.setSingleHoleGroups();
			System.out.println(HoleGroups.printGroups(spacingComponent1.getHoleGroups()));

			JPanel panel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.gridx = 0;
			gbc.gridy = 0;
			panel.add(spacingComponent1, gbc);
			HoleGroupSpacingComponent spacingComponent2 = new HoleGroupSpacingComponent(
					20);
			gbc.gridy = 1;
			panel.add(spacingComponent2, gbc);
			HoleGroupSpacingComponent spacingComponent3 = new HoleGroupSpacingComponent(
					20, true);
			gbc.gridy = 2;
			panel.add(spacingComponent3, gbc);
			HoleGroupSpacingComponent spacingComponent4 = new HoleGroupSpacingComponent(
					20, true, true);
			gbc.gridy = 3;
			panel.add(spacingComponent4, gbc);
			frame.getContentPane().add(panel);
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}

}
