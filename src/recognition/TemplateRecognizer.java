package recognition;

import java.util.HashMap;
import java.util.List;
import core.*;

public class TemplateRecognizer extends Recognizer
{
	private HashMap<String, List<Gesture>> templates;

	@Override
	public void learn(HashMap<String, List<Gesture>> gesturesByLabel)
	{
		templates = gesturesByLabel;		
		labels = templates.keySet().toArray(new String[templates.size()]);
	}
	
	@Override
	public HashMap<String, Double> recognize(Gesture g)
	{
		HashMap<String, Double> scores = new HashMap<String, Double>();
		for (String k : templates.keySet())
		{
			List<Gesture> l = templates.get(k);
			double bestScore = Float.NEGATIVE_INFINITY;
			for (Gesture t  : l)
				bestScore = Math.max(bestScore, distance(t, g));
			
			scores.put(k, bestScore);
		}

		return scores;
	}
	
	public static double distance(Gesture a, Gesture b)
	{
		if (a.captureCount() != b.captureCount())
		{
			System.err.println("Error: TemplateRecognizer.distance(), both gestures have not been resampled.");
			System.exit(1);
		}
		
		int n = a.captureCount();
		double d = 0;
		
		for (int i = 0; i < n; i++)
			d += Capture.distance(a.getCapture(i), b.getCapture(i));
		
		double maxScore = a.captureCount() * a.jointCount() * Math.sqrt(3);

		return 1 - d / maxScore;
	}
}
