package recognition;

import gui.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import kinect.KinectTracker;

import learning.HMM;
import core.Capture;
import core.Gesture;

public class OnlineRecognizer
{
	public static final double detectionThreshold = 0.97;
	private static final int scoreHistorySize = 10;

	public static void main(String[] args)
	{
		System.out.println("Starting...");

		OutputWriter output = new UDPWriter("127.0.0.1", 9991);
		String filename = args.length > 0 ? args[0] : null;
		filename = "/hdd/corpus/p1.oni";
		
		SimpleRecognizer.templates = new ArrayList<List<Gesture>>();
		SimpleRecognizer.templateLabels = new ArrayList<Word>();
		SimpleRecognizer.templateLabels.add(Word.PLAY);
		SimpleRecognizer.templateLabels.add(Word.WG);
		String[] templateFolders = {"play_mangou_bis_train", "wg"};
		for (String s : templateFolders)
		{
			List<Gesture> set = new ArrayList<Gesture>();
			SimpleRecognizer.loadFolder(set, "res/templates/" + s);
			SimpleRecognizer.templates.add(set);
		}
		
		int classCount = SimpleRecognizer.templates.size();
		List<List<int[]>> labels = new ArrayList<List<int[]>>();
		for (int i = 0; i < classCount; i++)
		{
			List<int[]> setLabels = new ArrayList<int[]>();
			for (Gesture g : SimpleRecognizer.templates.get(i))
				setLabels.add(HMM.generateStateLabels(false, 1, 1, 1));
			labels.add(setLabels);
		}
		

		List<double[]> results = new LinkedList<double[]>();

		int[] windows = { 40, 45, 50, 55, 60, 65};
		double[][] windowResults = new double[windows.length][classCount];

		ScoreGraph bestGraph = new ScoreGraph(classCount, "Best");
		KinectTracker tracker = new KinectTracker(filename);
		Gesture g = new Gesture("Test", null);
		Viewer v = new Viewer(tracker);
		
		while (true)
		{
			v.update();
			Capture capture = tracker.getCapture();
			if (capture != null)
			{
				g.add(capture);
				if (g.size() > 10)
				{
					evaluateWindows(g, windows, windowResults);
					double[] frame_res = bestResults(windowResults);
					results.add(frame_res);
//					System.out.println("res; " + Arrays.toString(frame_res));
					bestGraph.update(frame_res);
					
					Word w = detectWord(results);
					if (w != null)
					{
						System.out.println("Detected " + w);
						output.write(w);
						g = new Gesture("Test", null);
						results.clear();
					}
				}
			}
		}
	}

	public static Word detectWord(List<double[]> res)
	{
		int last = res.size() - 1;
		int tCount = SimpleRecognizer.templates.size();;
		double bestScore = -1;
		int bestIndex = -1;
		for (int i = 0; i < tCount; i++)
		{
			double score = res.get(last)[i];
			if (bestScore < score)
			{
				bestScore = score;
				bestIndex = i;
			}
		}
		
		if (bestScore < detectionThreshold)
			return null;
		
		return SimpleRecognizer.templateLabels.get(bestIndex);
	}
	
	public static void evaluateWindows(Gesture g, int[] windows, double[][] res)
	{
		for (int i = 0; i < windows.length; i++)
		{
			Gesture gt = g.copy(windows[i]).normalize();

			double[] r = SimpleRecognizer.recognize(gt);
//			System.out.println("window res: " + Arrays.toString(r));
			for (int j = 0; j < r.length; j++)
				res[i][j] = r[j];
		}
	}
	
	public static double[] bestResults(double[][] res)
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
