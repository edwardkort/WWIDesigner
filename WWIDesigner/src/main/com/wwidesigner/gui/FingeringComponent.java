package com.wwidesigner.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import com.wwidesigner.note.bind.NoteBindFactory;
import com.wwidesigner.util.BindFactory;

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
	private static final long serialVersionUID = 1L;
	protected JRadioButton[] mHoles;
	protected JPanel mButtonPanel = new JPanel();
	protected Image mImage;
	protected int mHighestOpenHole;
	protected JSVGCanvas svgCanvas;

	public FingeringComponent(int numHoles, int highestOpenHole)
	{
		mHighestOpenHole = highestOpenHole;
		Border border = new EmptyBorder(5, 5, 5, 5);
		setBorder(border);

		makeHoles(numHoles);
//		svgCanvas = new JSVGCanvas();
		// add(svgCanvas);
		// try
		// {
		// String imageName = ClassLoader.getSystemResource(
		// "com/wwidesigner/gui/smallFlute.svg").toString();
		// svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
		// String parser = XMLResourceDescriptor.getXMLParserClassName();
		// SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
		// Document doc = f.createDocument(imageName);
		// // svgCanvas.setURI(imageName);
		// // svgCanvas.createImage(300, 200);
		// // SVGDocument doc = svgCanvas.getSVGDocument();
		// Element root = doc.getDocumentElement();
		// // root.setAttributeNS(SVGDOMImplementation.SVG_NAMESPACE_URI,
		// "width", "800");
		// root.setAttributeNS(null, "width", "1200");
		// // root.setAttributeNS(SVGDOMImplementation.SVG_NAMESPACE_URI,
		// "height", "200");
		// root.setAttributeNS(null, "height", "300");
		// // root.setAttributeNS(null, "preserveAspectRatio", "");
		// svgCanvas.setSVGDocument((SVGDocument)doc);
		// svgCanvas.invalidate();
		// // svgCanvas.setSize(400, 500);
		// }
		// catch (Exception ex)
		// {
		// ex.printStackTrace();
		// }
		// mImage = Toolkit.getDefaultToolkit().getImage(
		// "src/main/com/wwidesigner/gui/smallFlute.gif");
		// MediaTracker mt = new MediaTracker(this);
		// try
		// {
		// mt.addImage(mImage, 0);
		// mt.waitForID(0);
		// }
		// catch (Exception e)
		// {
		// e.printStackTrace();
		// }
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.draw(new Arc2D.Double(50, 5, 70, 30, 90, 180, Arc2D.OPEN));
		g2.draw(new Line2D.Double(85, 5, 220, 5));
		g2.draw(new Line2D.Double(85, 35, 220, 35));
		g2.draw(new Line2D.Double(220, 5, 220, 35));
		// super.paintComponent(g);
		try
		{
			// Insets insets = getInsets();
			addHoles();
			// g.drawImage(mImage, insets.left, insets.top, null);
			validate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setHoles(boolean[] closedHoles) throws Exception
	{
		try
		{
			for (int i = mHighestOpenHole + 1; i < mHoles.length; i++)
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

	public boolean[] getHoles()
	{
		boolean[] closedHoles = new boolean[mHoles.length];
		for (int i = 0; i < mHoles.length; i++)
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

	// @Override
	// public Dimension getPreferredSize()
	// {
	// Insets insets = getInsets();
	// int width = svgCanvas.getWidth() + insets.left + insets.right;
	// int height = svgCanvas.getHeight() + insets.top + insets.bottom;
	// return new Dimension(width, height);
	// }

	public static void main(String[] args)
	{
		try
		{
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			FingeringComponent fingeringComponent1 = new FingeringComponent(6,
					0);
			boolean[] closedHoles = new boolean[6];
			closedHoles[1] = true;
			closedHoles[4] = true;
			fingeringComponent1.setHoles(closedHoles);
			JPanel panel = new JPanel(new GridLayout(2, 1));
			panel.add(fingeringComponent1);
			FingeringComponent fingeringComponent2 = new FingeringComponent(6,
					3);
			closedHoles[5] = true;
			closedHoles[4] = false;
			fingeringComponent2.setHoles(closedHoles);
			panel.add(fingeringComponent2);
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
		for (int i = 0; i < mHoles.length; i++)
		{
			this.add(mHoles[i]);
		}
	}

	protected void makeHoles(int numHoles)
	{
		mHoles = new JRadioButton[numHoles];
		for (int i = 0; i < numHoles; i++)
		{
			JRadioButton hole = makeHole();
			if (i < mHighestOpenHole)
			{
				hole.setSelected(true);
				hole.setEnabled(false);
			}
			else if (i == mHighestOpenHole)
			{
				hole.setEnabled(false);
			}
			mHoles[i] = hole;
		}
	}

	protected JRadioButton makeHole()
	{
		JRadioButton hole = new JRadioButton();
		hole.setPreferredSize(new Dimension(17, 21));

		return hole;
	}
}
