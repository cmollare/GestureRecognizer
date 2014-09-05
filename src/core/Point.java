package core;

import java.io.Serializable;

public class Point implements Serializable
{
	public double x;
	public double y;
	public double z;
	
	public Point(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public boolean isZero()
	{
		return x == 0 && y == 0 && z == 0;
	}
	
	public Point copy()
	{
		return new Point(this.x, this.y, this.z);
	}
	
	public void rotateXZ(Point o, double angle)
	{
		x = Math.cos(angle) * (x - o.x) - Math.sin(angle) * (z - o.z) + o.x;
		z = Math.sin(angle) * (x - o.x) + Math.cos(angle) * (z - o.z) + o.z;
	}
	
	public static double distance(Point a, Point b)
	{
		double dx = a.x - b.x;
		double dy = a.y - b.y;
		double dz = a.z - b.z;
		
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public static Point interpolate(Point a, Point b, double r)
	{
		double nx = a.x + r * (b.x - a.x);
		double ny = a.y + r * (b.y - a.y);
		double nz = a.z + r * (b.z - a.z);
		
		return new Point(nx, ny, nz);
	}
	
	@Override
	public String toString()
	{
		return "(" + x + " " + y + " " + z + ")";
	}
}
