package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.JFrame;

import recognition.OnlineRecognizer;

public class ScoreGraph extends JFrame
{
	private int count;
	private LinkedList<double[]> scores;
	private static Color[] colors = { Color.BLACK, Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE };
	private int size;
	private static int maxSize = 300;
	private static int width = 400;
	private static int height = 320;
	private static double minScore = 0.90;

	public ScoreGraph(int templateCount, String title)
	{
		count = templateCount;
		scores = new LinkedList<double[]>();

		Component comp = new GraphComponent();
		this.add("Center", comp);
		this.pack();
		this.setTitle(title);
//		this.setLocation(640, 0);
		this.setSize(width, height);
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void update(double[] s)
	{
		scores.add(s);
		size++;
		if (size > maxSize)
		{
			scores.remove();
			size--;
		}
		this.repaint();
	}

	private class GraphComponent extends Component
	{
		@Override
		public void paint(Graphics g)
		{
//			System.out.println("repaiting");
			
			int j = 0;
			double[] prev = null;
			float xRatio = (float) width / maxSize;
			float yRatio = (float) (height / (1 - minScore));
			g.setColor(Color.BLACK);
			int threshold = (int) ((1 - OnlineRecognizer.detectionThreshold) * yRatio);
			g.drawLine(0, threshold, width, threshold);

			for (double[] s : scores)
			{
				if (prev != null)
				{
					for (int i = 0; i < count; i++)
					{
						double scale = 1;
						
						Color c = colors[i % colors.length];
						int x = (int) (j * xRatio);
						int y = (int) ((1 - s[i] / scale) * yRatio);
						int prevX = (int) ((j - 1) * xRatio);
						int prevY = (int) ((1 - prev[i] / scale) * yRatio);
						g.setColor(c);
						g.drawLine(prevX, prevY, x, y);
					}
				}
				prev = s;
				j++;
			}
		}
	}
	
	public static ScoreGraph[] bulkCreate(int size, int[] windows)
	{
		ScoreGraph[] graphs = new ScoreGraph[windows.length];

		for (int i = 0; i < windows.length; i++)
		{
			graphs[i] = new ScoreGraph(size, "Window: " + windows[i]);
			graphs[i].setLocation(1920 - i * width, 0);
		}

		return graphs;
	}
}
