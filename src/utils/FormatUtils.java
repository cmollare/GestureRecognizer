package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import recognition.Chalearn;

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
	
	public static void writeTXT(Gesture g, String filename)
	{
		try
		{
			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			for (Capture c : g)
			{
				String content = "0 ";
				for (Joint j : Joint.values())
				{
					if (c.joints().contains(j))
					{
						Point p = c.get(j);
						content += p.x + " " + p.y + " " + p.z + " ";
					}
					else
						content += "0 0 0 ";
				}
				content += "\n";
				bw.write(content);
			}
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static Gesture loadGestureWithExtension(String filename)
	{
		String extension = fileExtension(filename);
		
		switch(extension)
		{
		case "oni":
			return KinectTracker.extractONIGesture(Joint.niteJoints(), filename);
		case "gst":
			return Gesture.fromFile(filename);
		case "txt":
			return FormatUtils.loadTXT(filename);
		case "csv":
			return Chalearn.loadChalearnGesture(filename);
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	public static void writeGestureWithExtension(Gesture g, String filename)
	{
		String extension = fileExtension(filename);
		
		switch(extension)
		{
		case "gst":
			g.toFile(filename);
			break;
		case "txt":
			FormatUtils.writeTXT(g, filename);
			break;
		case "csv":
			throw new UnsupportedOperationException();
		case "oni":
			throw new UnsupportedOperationException();
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private static String fileExtension(String f)
	{
		return f.substring(f.lastIndexOf('.') + 1).toLowerCase();
	}
	
	public static boolean inputIsKinect(String f)
	{
		return f.equals("kinect") || fileExtension(f).equals("oni");
	}
}
