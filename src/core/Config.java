package core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import core.Gesture.*;

public abstract class Config
{
	public static int[] windows = new int[] { 5, 10, 15, 20, 25, 30 };
	public static double detectionThreshold = 0.97;
	
	public static int sampleCount = 32;
	public static Joint[] joints = new Joint[] {Joint.RIGHT_HAND, Joint.LEFT_HAND, Joint.RIGHT_ELBOW, Joint.LEFT_ELBOW};
	public static TranslationMethod translationMethod = TranslationMethod.SHOULDERS;
	public static ResamplingMethod resamplingMethod = ResamplingMethod.GLOBAL_DISTANCE;
	public static RotationMethod rotationMethod = RotationMethod.SHOULDERS;
	public static RescalingMethod rescalingMethod = RescalingMethod.ARMS;
	
	private static List<Joint> requiredJoints = null;

	static
	{
		load();
		computeRequiredJoints();
	}

	public static void load()
	{
		try
		{
			File fXmlFile = new File("gestureConfig.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			String sampleCountNode = doc.getElementsByTagName("sampleCount").item(0).getTextContent();
			sampleCount = Integer.parseInt(sampleCountNode);

			String windowsNode = doc.getElementsByTagName("windows").item(0).getTextContent();
			parseWindows(windowsNode);
			
			String jointsNode = doc.getElementsByTagName("joints").item(0).getTextContent();
			parseJoints(jointsNode);
			
			String resamplingNode = doc.getElementsByTagName("resamplingMethod").item(0).getTextContent();
			resamplingMethod = ResamplingMethod.valueOf(resamplingNode);
			
			String rotationNode = doc.getElementsByTagName("rotationMethod").item(0).getTextContent();
			rotationMethod = RotationMethod.valueOf(rotationNode);
			
			String translationNode = doc.getElementsByTagName("translationMethod").item(0).getTextContent();
			translationMethod = TranslationMethod.valueOf(translationNode);
			
			String rescalingNode = doc.getElementsByTagName("rescalingMethod").item(0).getTextContent();
			rescalingMethod = RescalingMethod.valueOf(rescalingNode);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void parseJoints(String l)
	{
		String[] split = l.split(" ");
		List<Joint> jointList = new ArrayList<Joint>();
		for (String s : split)
			jointList.add(Joint.valueOf(s));
		
		joints = new Joint[jointList.size()];
		for (int i = 0; i < joints.length; i++)
			joints[i] = jointList.get(i);
	}
	
	private static void parseWindows(String s)
	{
		String[] windowsSplit = s.split(" ");
		windows = new int[windowsSplit.length];
		for (int i = 0; i < windowsSplit.length; i++)
			windows[i] = Integer.parseInt(windowsSplit[i]);
	}

	public static void computeRequiredJoints()
	{
		boolean[] jointIsNeeded = new boolean[Joint.values().length];

		if (translationMethod == TranslationMethod.SHOULDERS)
		{
			jointIsNeeded[Joint.LEFT_SHOULDER.ordinal()] = true;
			jointIsNeeded[Joint.RIGHT_SHOULDER.ordinal()] = true;
		}

		if (rotationMethod == RotationMethod.SHOULDERS)
		{
			jointIsNeeded[Joint.LEFT_SHOULDER.ordinal()] = true;
			jointIsNeeded[Joint.LEFT_SHOULDER.ordinal()] = true;
		}

		requiredJoints = new ArrayList<Joint>();
		for (int i = 0; i < jointIsNeeded.length; i++)
		{
			if (jointIsNeeded[i])
				requiredJoints.add(Joint.values()[i]);
		}
	}

	public static boolean containsRequiredJoints(Gesture g)
	{		
		for (Joint j : requiredJoints)
		{
			if (!g.getCapture(0).joints().contains(j))
				return false;
		}
		return true;
	}
}
