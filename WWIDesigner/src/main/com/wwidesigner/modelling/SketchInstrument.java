/**
 * 
 */
package com.wwidesigner.modelling;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.jidesoft.chart.style.LineStyle;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.Mouthpiece.Fipple;
import com.wwidesigner.geometry.PositionInterface;
import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.util.SortedPositionList;

/**
 * Class to plot a sketch of the physical layout of an instrument.
 */
public class SketchInstrument
{
	protected String mName;
	Mouthpiece  mouthpiece;
	PositionInterface[] borePoints;
	SortedPositionList<Hole> holes;
	
	protected static final double POINTS_PER_HOLE = 20.0;
	protected static final int PIXELS_ACROSS = 1024;

	protected double[] drawBore(Chart chart)
	{
		// Use separate lines for top and bottom profile,
		// so we can draw the whole bore in a single pass.
		DefaultChartModel modelTop = new DefaultChartModel("Interior Top");
		DefaultChartModel modelBottom = new DefaultChartModel("Interior Bottom");
		ChartStyle styleInterior = new ChartStyle(Color.black, false, true);
		float[] dashes = new float[2];
		dashes[0] = 10.0f;
		dashes[1] = 2.0f;
		BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
				10, dashes, 0.0f);
		LineStyle lineStyle = new LineStyle(Color.black, stroke);
		styleInterior.setLineStyle(lineStyle);
		double boreLength = 0.0;
		double boreWidth = 0.0;
		double boreStart = borePoints[0].getBorePosition();
		// Leave top of bore open, for window and windway.
		for (PositionInterface borePoint: borePoints)
		{
			if (borePoint instanceof BorePoint)
			{
				if (((BorePoint)borePoint).getBoreDiameter() > boreWidth)
				{
					boreWidth = ((BorePoint)borePoint).getBoreDiameter();
				}
				double y = 0.5 * ((BorePoint)borePoint).getBoreDiameter();
				boreLength = borePoint.getBorePosition();
				modelTop.addPoint(boreLength, y);
				modelBottom.addPoint(boreLength,-y);
			}
		}
		// Close the bottom of the tube.
		modelTop.addPoint(boreLength, 0.0);
		modelBottom.addPoint(boreLength, 0.0);
		chart.addModel(modelTop, styleInterior);
		chart.addModel(modelBottom, styleInterior);
		double[] boreDimensions = new double[2];
		boreDimensions[0] = boreLength - boreStart;
		boreDimensions[1] = boreWidth;
		return boreDimensions;
	}

	protected void drawHoles(Chart chart)
	{
		ChartStyle styleHole = new ChartStyle(Color.black, false, true);
		int holeNr = 1;
		for (Hole hole: holes)
		{
			String modelName;
			modelName = String.format("Hole %d", holeNr+1);
			++holeNr;
			DefaultChartModel modelHole = new DefaultChartModel(modelName);
			double radius = 0.5 * hole.getDiameter();
			for (double theta = 0.0; theta < 2.0*Math.PI - 0.01; theta += 2.0*Math.PI/POINTS_PER_HOLE)
			{
				double x = radius * Math.cos(theta); 
				double y = radius * Math.sin(theta); 
				modelHole.addPoint(hole.getBorePosition() + x, y);
			}
			// Close the circle by going back to the first point.
			modelHole.addPoint(hole.getBorePosition() + radius, 0.0);
			chart.addModel(modelHole, styleHole);
		}
	}

	protected void drawMouthpiece(Chart chart)
	{
		Fipple window = mouthpiece.getFipple();
		if (window != null)
		{
			// Draw window as a closed rectangle.
			ChartStyle styleWindow = new ChartStyle(Color.black, false, true);
			DefaultChartModel modelWindow = new DefaultChartModel("Window");
			modelWindow.addPoint(mouthpiece.getBorePosition(),
					0.5 * window.getWindowWidth());
			modelWindow.addPoint(mouthpiece.getBorePosition(),
					- 0.5 * window.getWindowWidth());
			modelWindow.addPoint(mouthpiece.getBorePosition() - window.getWindowLength(),
					- 0.5 * window.getWindowWidth());
			modelWindow.addPoint(mouthpiece.getBorePosition() - window.getWindowLength(),
					0.5 * window.getWindowWidth());
			modelWindow.addPoint(mouthpiece.getBorePosition(),
					0.5 * window.getWindowWidth());
			chart.addModel(modelWindow, styleWindow);
			if (window.getWindwayLength() != null)
			{
				// Draw windway as an open, dashed rectangle.
				ChartStyle styleWindway = new ChartStyle(Color.black, false, true);
				DefaultChartModel modelWindway = new DefaultChartModel("Windway");
				float[] dashes = new float[2];
				dashes[0] = 5.0f;
				dashes[1] = 2.0f;
				BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
						10, dashes, 0.0f);
				LineStyle lineStyle = new LineStyle(Color.black, stroke);
				styleWindway.setLineStyle(lineStyle);
				double windwayExit = mouthpiece.getBorePosition() - window.getWindowLength();
				modelWindway.addPoint(windwayExit - window.getWindwayLength(),
						0.5 * window.getWindowWidth());
				modelWindway.addPoint(windwayExit,
						0.5 * window.getWindowWidth());
				modelWindway.addPoint(windwayExit,
						- 0.5 * window.getWindowWidth());
				modelWindway.addPoint(windwayExit - window.getWindwayLength(),
						- 0.5 * window.getWindowWidth());
				chart.addModel(modelWindway, styleWindway);
			}
		}
	}

	public void draw(Instrument instrument, final boolean exitOnClose)
	{
		mName = instrument.getName();
		mouthpiece = instrument.getMouthpiece();
		borePoints = Instrument.sortList(instrument.getBorePoint());
		holes =  new SortedPositionList<Hole>(instrument.getHole());

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Sketch of " + mName);
				if (exitOnClose)
				{
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}
				Chart chart = new Chart();
				double[] boreDimensions = drawBore(chart);
				drawHoles(chart);
				drawMouthpiece(chart);
				chart.setAutoRanging(true);
				chart.setTitle("Sketch of " + mName);
				chart.getXAxis().setLabel("Length");
				chart.getYAxis().setLabel("Width");
				frame.setSize(PIXELS_ACROSS, 
						110+(int)((PIXELS_ACROSS-110)*boreDimensions[1]/boreDimensions[0]));
				frame.setContentPane(chart);
				frame.setVisible(true);
			}
		});
	}
}
