package exec;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import recognition.Recognizer;
import recognition.HMMRecognizer;
import recognition.TemplateRecognizer;

import core.*;

public class RecognizerTrainer
{
	/***
	
	Usage: input type output
	
	input: list of files and directories (eg. "res/g1.gst res/g2.gst res/dir1/"
	type: "hmm" or "template"
	output: recognizer file
	
	***/
	
	public static void main(String[] args)
	{		
		if (args.length < 3)
		{
			System.err.println("Missing argument.");
			System.err.println("Usage: input type output");
			System.exit(1);
		}
		
		String input = args[0];
		String recognizerType = args[1];
		String output = args[2];
		List<String> inputFileNames = getFileNames(input); 
		List<Gesture> gestures = loadGestures(inputFileNames);
		List<Gesture> decomposedGestures = decomposeByLabel(gestures);
		HashMap<String, List<Gesture>> gesturesByLabel = groupByLabel(decomposedGestures);
		
		Recognizer gr = null;
		if (recognizerType.equals("template"))
			gr = new TemplateRecognizer();
		else if (recognizerType.equals("hmm"))
			gr = new HMMRecognizer();
		else
		{
			System.err.println("Error: the recognizer type \"" + recognizerType + "\" is unkown");
			System.exit(1);
		}
		
		gr.learn(gesturesByLabel);
		gr.toFile(output);
	}
	
	private static List<String> getFileNames(String arg)
	{
		List<String> files = new ArrayList<String>();
		String[] inputArray = arg.split(" ");
		
		for (int i = 0; i < inputArray.length; i++)
		{
			File f = new File(inputArray[i]);
			if (!f.exists())
			{
				System.err.println("File/Directory " + inputArray[i] + " does not exist");
				System.exit(1);
			}
			if (f.isDirectory())
			{
				System.err.println("Directory loading not implemented");
				throw new UnsupportedOperationException();
			}
			else
				files.add(inputArray[i]);
		}
		
		return files;
	}
	
	private static List<Gesture> loadGestures(List<String> filenames)
	{
		List<Gesture> gestures = new ArrayList<Gesture>();
		
		for (String s : filenames)
			gestures.add(Gesture.fromFile(s));
		
		return gestures;
	}
	
	private static List<Gesture> decomposeByLabel(List<Gesture> set)
	{
		List<Gesture> set2 = new ArrayList<Gesture>();
		for (Gesture g : set)
			set2.addAll(g.decompose());
		
		return set2;
	}
	
	private static HashMap<String, List<Gesture>> groupByLabel(List<Gesture> set)
	{
		HashMap<String, List<Gesture>> map = new HashMap<String, List<Gesture>>();
		
		for (Gesture g : set)
		{
			String label = g.getLabel();
			List<Gesture> l = null;
			
			if (map.containsKey(label))
				l = map.get(label);
			else
			{
				l = new ArrayList<Gesture>();
				map.put(label, l);
			}
			l.add(g);
		}
		
		return map;
	}
}
