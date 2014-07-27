package core;

public class BoundingBox
{
	public double xMin;
	public double xMax;
	public double xSize;

	public double yMin;
	public double yMax;
	public double ySize;

	public double zMin;
	public double zMax;
	public double zSize;

	public BoundingBox(Gesture g)
	{
		this(g, Joint.values());
	}
	
	public BoundingBox(Gesture g, Joint[] joints)
	{
		Point first = g.trajectories[joints[0].ordinal()].get(0);
		xMin = first.x;
		xMax = first.x;
		yMin = first.y;
		yMax = first.y;
		zMin = first.z;
		zMax = first.z;

		for (Joint joint : joints)
		{
			if (joint.isDetected())
			{
				for (Point p : g.trajectories[joint.ordinal()])
				{
					if (p.x < xMin)
						xMin = p.x;
					if (p.x > xMax)
						xMax = p.x;
					if (p.y < yMin)
						yMin = p.y;
					if (p.y > yMax)
						yMax = p.y;
					if (p.z < zMin)
						zMin = p.z;
					if (p.z > zMax)
						zMax = p.z;
				}
			}
		}

		xSize = xMax - xMin;
		ySize = yMax - yMin;
		zSize = zMax - zMin;
	}
	
	@Override
	public String toString()
	{
		return xSize + " " + ySize + " " + zSize;
	}
}
