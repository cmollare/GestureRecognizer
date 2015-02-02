package recognition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import utils.SerializationUtils;

import core.Gesture;

/**
 * class
 */

public abstract class Recognizer implements Serializable
{
	protected String[] labels;
	
	public abstract HashMap<String, Double> recognize(Gesture g);
	public abstract void learn(HashMap<String, List<Gesture>> gesturesByLabel);
	
	public int labelCount()
	{
		return labels.length;
	}
	
	public String[] labels()
	{
		return labels;
	}
	
	public void toFile(String filename)
	{
		SerializationUtils.toFile(filename, this);
	}
	
	public static Recognizer fromFile(String filename)
	{
		return (Recognizer) SerializationUtils.fromFile(filename);
	}
}
