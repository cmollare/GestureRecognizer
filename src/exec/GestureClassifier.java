package exec;

import java.util.List;

import kinect.KinectTracker;
import recognition.OfflineClassifier;
import recognition.OnlineClassifier;
import recognition.Recognizer;
import ui.OSCWriter;
import ui.OutputWriter;
import ui.StdoutWriter;
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
	output: filename
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

		Gesture g = null;
		Recognizer recognizer = Recognizer.fromFile(model);
		OutputWriter ow = getRequestedWriter(output);

		if (type.equals("online"))
		{
			OnlineClassifier c = new OnlineClassifier(Config.detectionThreshold, Config.windows);
			boolean useUI = ui.equals("yes");
			KinectTracker kt = new KinectTracker(Joint.values(), gestureFile, useUI);
			g = c.labelize(kt, recognizer, ow);
		}
		else if (type.equals("offline"))
		{
			g = FormatUtils.loadGestureWithExtension(gestureFile);
			OfflineClassifier c = new OfflineClassifier(Config.windows);
			c.labelize(g, recognizer, ow);

			for (GestureLabel l : g.extractLabels())
				ow.write(l);
		}
		else
			throw new UnsupportedOperationException();		
	}
	
	private static OutputWriter getRequestedWriter(String arg)
	{
		if (arg.equals("stdout"))
			return new StdoutWriter();
		String[] splitArgs = arg.split(" ");
		if (splitArgs.length == 3)
			return new OSCWriter(splitArgs[0], Integer.parseInt(splitArgs[1]), splitArgs[2]);
		else
			throw new UnsupportedOperationException();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
