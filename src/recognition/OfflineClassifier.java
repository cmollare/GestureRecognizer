package recognition;

import java.util.HashMap;
import learning.Matrix;
import ui.OutputWriter;
import core.*;

public class OfflineClassifier
{
	public OfflineClassifier()
	{
	}
	
	public void labelize(Gesture g, Recognizer recognizer, OutputWriter output)
	{
		int[] windows = new int[] { 5, 6, 7, 8, 9, 10, 20, 30, 40 };
		int captureCount = g.captureCount();
		int classCount = recognizer.labelCount();
		String[] classes = recognizer.labels();
		Matrix scores = new Matrix(captureCount, classCount, Double.NEGATIVE_INFINITY);

		for (int i = 0; i < captureCount; i++)
		{
			for (int k = 0; k < windows.length; k++)
			{
				int w = windows[k];
				int i0 = i;
				int i1 = i + w;

				if (i0 >= 0 && i1 < captureCount)
				{
					Gesture current = g.copy(i0, i1).normalize();
					HashMap<String, Double> r = recognizer.recognize(current);
					

					for (int l = i0; l <= i1; l++)
					{
						for (int j = 0; j < classCount; j++)
							scores.set(l, j, Math.max(r.get(j), scores.get(l, j)));
					}
				}
			}
		}

		for (int i = 0; i < captureCount; i++)
		{
			String bestClass = null;
			double bestScore = Double.NEGATIVE_INFINITY;
			for (int j = 0; j < classes.length; j++)
			{
				double s = scores.get(i, j);
				if (s > bestScore)
				{
					bestScore = s;
					bestClass = classes[j];
				}
			}

			g.getCapture(i).setLabel(bestClass);
		}
	}
}
