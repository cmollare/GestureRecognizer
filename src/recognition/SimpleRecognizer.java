package recognition;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.Gesture;

public class SimpleRecognizer
{
	public static List<List<Gesture>> templates;
	public static List<Word> templateLabels;

	public static double[] recognize(Gesture g)
	{
		double scores[] = new double[templates.size()];
		for (int i = 0; i < templates.size(); i++)
		{
			for (Gesture t : templates.get(i))
				scores[i] = Math.max(scores[i], Gesture.compare(t, g, Gesture.ScoreMethod.ARMS));
		}

		return scores;
	}

	public static void loadFolder(List<Gesture> set, String path)
	{
		File folder = new File(path);
		File[] files = folder.listFiles();
		Arrays.sort(files);
		for (File f : files)
		{
			Gesture g = Gesture.load(f.getPath(), null).normalize();
			set.add(g);
		}
	}
}
