package exec;

import java.util.List;

import kinect.KinectTracker;

import utils.FormatUtils;
import utils.SerializationUtils;
import core.Gesture;
import core.GestureLabel;
import core.Joint;

public class GestureRecorder
{
	/***
	
	Usage: input duration labels output

	input: filename (.oni, .gst, .csv, .txt)
	labels: (optional) filename
	output: filename
	
	***/
	
	public static void main(String[] args)
	{
		if (args.length < 3)
		{
			System.err.println("Error: Missing arguments.");
			System.err.println("Usage: input labels output");
			System.exit(1);
		}

		String input = args[0];
		String labels = args[2];
		String output = args[3];
		
		Gesture g = null;
		
		if (input.startsWith("kinect#"))
		{
			double duration = getKinectRecordingDuration(input);
			g = KinectTracker.recordGesture(Joint.values(), null, duration);
		}
		else
			g = FormatUtils.loadGestureWithExtension(input);
		
		if (!labels.isEmpty())
		{
			if (SerializationUtils.fileExists(labels))
			{
				List<GestureLabel> l = GestureLabel.fromFile(labels);
				g.applyLabels(l);
			}
			else
				g.setLabel(labels);
		}
		
		g.toFile(output);
	}
	
	public static double getKinectRecordingDuration(String arg)
	{
		return Double.parseDouble(arg.substring(7));
	}
}
