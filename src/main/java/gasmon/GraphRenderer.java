package gasmon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import javax.swing.*;

@SuppressWarnings("serial")
public class GraphRenderer extends JPanel {
	private static final int PREF_W = 600;
	private static final int PREF_H = 600;
	private static final Stroke POINT_STROKE = new BasicStroke(3f);

	private static final int MAX_POINT_POS_X = 1000;
	private static final int MAX_POINT_POS_Y = 1000;
	private ArrayList<SensorPoint> readings;
	private ArrayList<SensorPoint> estimate;
	
	private JFrame frame;

	public GraphRenderer(ArrayList<SensorPoint> readings, ArrayList<SensorPoint> estimate) {
		this.readings = readings;
		this.estimate = estimate;
		this.frame = new JFrame("Gas Monitor");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.getContentPane().add(this);
		this.frame.pack();
		this.frame.setLocationByPlatform(true);
		this.frame.setVisible(true);
	}

	public void updateValues(ArrayList<SensorPoint> readings, ArrayList<SensorPoint> estimate) {
		this.readings = readings;
		this.estimate = estimate;
		this.frame.repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setStroke(POINT_STROKE);		
		for (SensorPoint p : readings) {
			g2.setColor(p.colour);
			int x1 = (int) (p.x * getWidth() / MAX_POINT_POS_X);
			int y1 = (int) (p.y * getHeight() / MAX_POINT_POS_Y);
			g2.fillOval(x1 - (p.w/2), y1 - (p.w/2), p.w, p.w);
			g2.setColor(Color.BLACK);
			g2.drawString(p.getPosAsString(), x1-25, y1-5);
			g2.drawString(p.getValueAsString(), x1-10, y1+5);
		}
		
		g2.setColor(Color.RED);
		for (SensorPoint s : estimate) {
			int x1 = (int) (s.x * getWidth() / MAX_POINT_POS_X);
			int y1 = (int) (s.y * getHeight() / MAX_POINT_POS_Y);
			g2.drawRect(x1 - 2, y1 - 2, 4, 4);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREF_W, PREF_H);
	}
}