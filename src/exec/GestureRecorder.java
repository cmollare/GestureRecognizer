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
	ui: yes / no
	
	***/
	
	public static void main(String[] args)
	{
		if (args.length < 4)
		{
			System.err.println("Error: Missing arguments.");
			System.err.println("Usage: input labels output ui");
			System.exit(1);
		}

		String input = args[0];
		String labels = args[1];
		String output = args[2];
		String ui = args[3];
		
		Gesture g = null;
		
		if (FormatUtils.inputIsKinect(input))
		{
			boolean useUI = ui.equals("yes");
			g = KinectTracker.recordGesture(Joint.niteJoints(), input, useUI);
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
			{
				g.setLabel(labels);
			}
		}
	
		g = g.normalize();
		FormatUtils.writeGestureWithExtension(g, output);
	}
	
	public static double getKinectRecordingDuration(String arg)
	{
		return Double.parseDouble(arg.substring(7));
	}
}
