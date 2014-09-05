package recognition;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ui.OutputWriter;
import kinect.KinectTracker;

import core.Capture;
import core.Gesture;

public class OnlineClassifier
{
	private static final int minGestureSize = 10;
	
	private double detectionThreshold;
	private int[] windows;

	public OnlineClassifier(double detectionThreshold, int[] windows)
	{
		this.detectionThreshold = detectionThreshold;
		this.windows = windows;
	}
	
	public Gesture labelize(KinectTracker tracker, Recognizer recognizer, OutputWriter output)
	{		
		List<double[]> results = new ArrayList<double[]>();
		double[][] windowResults = new double[windows.length][];
		
		Gesture g = new Gesture();
		
		while (!tracker.isDone())
		{
			tracker.update();
			Capture lastCapture = tracker.getCapture();
			
			if (lastCapture != null)
			{
				g.addCapture(lastCapture);
				if (g.captureCount() > minGestureSize)
				{
					evaluateWindows(g, recognizer, windowResults);
					double[] frame_res = bestResults(windowResults);
					results.add(frame_res);
					
					String label = detectLabel(recognizer, results);
					if (label != null)
					{
						System.out.println("Detected: " + label);
						output.write(label);
						g = new Gesture();
						results.clear();
					}
				}
			}
		}
		
		return g;
	}

	private String detectLabel(Recognizer recognizer, List<double[]> res)
	{
		int last = res.size() - 1;
		int classCount = recognizer.labelCount();
		double bestScore = -1;
		String bestClass = null;
		for (int i = 0; i < classCount; i++)
		{
			double score = res.get(last)[i];
			if (bestScore < score)
			{
				bestScore = score;
				bestClass = recognizer.labels()[i];
			}
		}
		
		if (bestScore < detectionThreshold)
			return null;
		
		return bestClass;
	}
	
	private void evaluateWindows(Gesture g, Recognizer recognizer, double[][] res)
	{
		for (int i = 0; i < windows.length; i++)
		{
			Gesture gt = g.copy(windows[i]).normalize();

			HashMap<String, Double> r = recognizer.recognize(gt);
			int j = 0;
			for (String l : recognizer.labels())
			{
				res[i][j] = r.get(l);
				j++;
			}
		}
	}
	
	private double[] bestResults(double[][] res)
	{
		int n = res[0].length;
		double[] best = new double[n];
		
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < res.length; j++)
			{
				if (best[i] < res[j][i])
					best[i] = res[j][i];
			}
		}
		
		return best;
	}
}
