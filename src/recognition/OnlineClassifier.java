package recognition;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ui.LabelWriter;
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
	
	public void labelize(KinectTracker tracker, Recognizer recognizer, LabelWriter output)
	{		
		List<double[]> results = new ArrayList<double[]>();
		double[][] windowResults = new double[windows.length][recognizer.labelCount()];
		
		Gesture g = new Gesture();
		tracker.start();
		
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
						output.write(label);
						tracker.writeLabel(label);
						g = new Gesture();
						results.clear();
					}
				}
			}
		}
		
		tracker.stop();
	}
	
	private void evaluateWindows(Gesture g, Recognizer recognizer, double[][] res)
	{
		int n = g.captureCount();
		String[] labels = recognizer.labels();
		
		for (int i = 0; i < windows.length; i++)
		{
			if (n < windows[i])
			{
				for (int j = 0; j < recognizer.labelCount(); j++)
					res[i][j] = Double.NEGATIVE_INFINITY;
				continue;
			}
			
			Gesture gt = g.copy(windows[i]).normalize();
			HashMap<String, Double> r = recognizer.recognize(gt);
			for (int j = 0; j < recognizer.labelCount(); j++)
				res[i][j] = r.get(labels[j]);
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
}
