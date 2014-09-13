package core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Capture implements Serializable, Iterable<Point>
{
	private HashMap<Joint, Point> points;
	private String label;
	
	public Capture()
	{
		points = new HashMap<Joint, Point>();
	}
	
	public Capture(String l)
	{
		this();
		setLabel(l);
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setLabel(String s)
	{
		label = s;
	}
	
	public Iterator<Point> iterator()
	{
		return points.values().iterator();
	}
	
	public Set<Joint> joints()
	{
		return points.keySet();
	}
	
	public Point get(Joint j)
	{
		if (!points.containsKey(j))
		{
			System.err.println("Warning: [Capture.get()] The capture does not contain the joint " + j);
			return null;
		}
		
		return points.get(j);
	}

	public int jointCount()
	{
		return points.size();
	}
	
	public Capture copy()
	{
		Capture c = new Capture();
		for (Joint j : this.points.keySet())
			c.points.put(j, this.points.get(j).copy());
			
		return c;
	}
	
	public void put(Point p, Joint j)
	{
		points.put(j, p);
	}
	
	public Capture select(Joint... joints)
	{
		Capture c = new Capture();
		
		for (Joint j : joints)
			c.put(this.get(j), j);
		
		return c;
	}
	
	public boolean isCompatible(Capture o)
	{
		return this.joints().equals(o.joints());
	}
	
	public static double distance(Capture a, Capture b)
	{
		if (!a.isCompatible(b))
		{
			System.err.println("Capture.distance(): captures do not have the same joint count");
			System.exit(1);
		}
		
		double d = 0;
		for (Joint j : a.joints())
			d += Point.distance(a.get(j), b.get(j));

		return d;

	}
	
	public static Capture interpolate(Capture a, Capture b, double v)
	{
		if (!a.isCompatible(b))
		{
			System.err.println("Capture.interpolate(): captures do not have the same joint count");
			System.exit(1);
		}
		
		int n = a.jointCount();
		Capture r = new Capture();
		for (Joint j : a.joints())
			r.put(Point.interpolate(a.get(j), b.get(j), v), j);
		
		return r;
	}
}
