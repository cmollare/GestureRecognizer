package exec;

import kinect.KinectTracker;
import recognition.OfflineClassifier;
import recognition.OnlineClassifier;
import recognition.Recognizer;
import ui.OutputWriter;
import ui.StdoutWriter;
import utils.FormatUtils;
import core.Gesture;
import core.Joint;

public class GestureClassifier
{
	
	/***
	
	Usage: input recognizer type output

	input: filename (.oni, .gst, .csv, .txt)
	classifier: filename
	output: filename
	
	***/
	
	public static void main(String[] args)
	{
		if (args.length < 4)
		{
			System.out.println("Missing arguments");
			System.exit(1);
		}

		String recognizerFile = args[0];
		String gestureFile = args[1];
		String classifier = args[2];
		String output = args[3];

		Gesture g = null;
		Recognizer recognizer = Recognizer.fromFile(recognizerFile);
		OutputWriter ow = new StdoutWriter();

		if (classifier.equals("online"))
		{
			int[] windows = new int[] { 5, 10, 15, 20, 25, 30, 35, 40, 50, 60 };
			double threshold = 0.97;
			Joint[] joints = Joint.arms();

			OnlineClassifier c = new OnlineClassifier(threshold, windows);
			KinectTracker kt = new KinectTracker(joints, gestureFile);
			g = c.labelize(kt, recognizer, ow);
		}
		else if (classifier.equals("offline"))
		{
			g = FormatUtils.loadGestureWithExtension(gestureFile);
			OfflineClassifier c = new OfflineClassifier();
			c.labelize(g, recognizer, ow);
		}
		else
			throw new UnsupportedOperationException();
		
		
	}
}
