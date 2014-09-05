package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import kinect.KinectTracker;

import core.Capture;
import core.Gesture;
import core.Joint;
import core.Point;

public abstract class FormatUtils
{
	public static Gesture loadTXT(String filename)
	{
		Gesture g = new Gesture();
		int jointCount = Joint.values().length;

		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(filename));

			while ((line = br.readLine()) != null)
			{
				Capture c = new Capture();
				String[] parts = line.split(" ");
				for (int i = 0; i < jointCount; i++)
				{
					double x = Double.parseDouble(parts[i * 3 + 1]);
					double y = Double.parseDouble(parts[i * 3 + 2]);
					double z = Double.parseDouble(parts[i * 3 + 3]);
					c.put(new Point(x, y, z), Joint.values()[i]);
				}
				g.addCapture(c);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

		return g;
	}
	
	public static Gesture loadGestureWithExtension(String filename)
	{
		String extension = fileExtension(filename).toLowerCase();
		
		switch(extension)
		{
		case "oni":
			return KinectTracker.extractONIGesture(Joint.values(), filename);
		case "gst":
			return Gesture.fromFile(filename);
		case "txt":
			return FormatUtils.loadTXT(filename);
		case "csv":
			throw new UnsupportedOperationException();
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private static String fileExtension(String f)
	{
		return f.substring(f.lastIndexOf('.') + 1);
	}
}
