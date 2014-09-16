package exec;

import kinect.KinectTracker;
import recognition.OfflineClassifier;
import recognition.OnlineClassifier;
import recognition.Recognizer;
import ui.FileLabelWriter;
import ui.OSCLabelWriter;
import ui.LabelWriter;
import ui.StdoutLabelWriter;
import utils.FormatUtils;
import core.Config;
import core.Gesture;
import core.GestureLabel;
import core.Joint;

public class GestureClassifier
{
	/***
	
	Usage: input recognizer type output ui

	input: filename (.oni, .gst, .csv, .txt)
	classifier: filename
	output: filename or stdout or "ip port osc-address" (OSC)
	ui: yes / no
	
	***/
	
	public static void main(String[] args)
	{
		if (args.length < 5)
		{
			System.out.println("Missing arguments");
			System.exit(1);
		}

		String gestureFile = args[0];
		String model = args[1];
		String type = args[2];
		String output = args[3];
		String ui = args[4];

		Recognizer recognizer = Recognizer.fromFile(model);
		LabelWriter writer = getRequestedWriter(output);

		if (type.equals("online"))
		{
			OnlineClassifier classifier = new OnlineClassifier(Config.detectionThreshold, Config.windows);
			boolean useUI = ui.equals("yes");
			KinectTracker kt = new KinectTracker(Joint.niteJoints(), gestureFile, useUI);
			classifier.labelize(kt, recognizer, writer);
		}
		else if (type.equals("offline"))
		{
			Gesture g = FormatUtils.loadGestureWithExtension(gestureFile);
			OfflineClassifier c = new OfflineClassifier(Config.windows);
			c.labelize(g, recognizer, writer);

			for (GestureLabel l : g.extractLabels())
				writer.write(l);
		}
		else
			throw new UnsupportedOperationException();
		
		writer.close();
	}
	
	private static LabelWriter getRequestedWriter(String arg)
	{
		if (arg.equals("stdout"))
			return new StdoutLabelWriter();
		
		String[] splitArgs = arg.split(" ");
		
		if (splitArgs.length == 3)
			return new OSCLabelWriter(splitArgs[0], Integer.parseInt(splitArgs[1]), splitArgs[2]);
		else
			return new FileLabelWriter(arg);
	}

	
}
