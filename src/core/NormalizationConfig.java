package core;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import core.Gesture.*;

public abstract class NormalizationConfig
{
	public static int sampleCount = 32;
	public static Joint[] joints = Joint.arms();
	public static TranslationMethod transMethod = TranslationMethod.SHOULDERS;
	public static ResamplingMethod resamplingMethod = ResamplingMethod.GLOBAL_DISTANCE;
	public static RotationMethod rotMethod = RotationMethod.SHOULDERS;
	public static RescalingMethod rescalingMethod = RescalingMethod.ARMS;
	
	static
	{
		load();
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
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
