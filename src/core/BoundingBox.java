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
		xMin = Double.POSITIVE_INFINITY;
		xMax = Double.NEGATIVE_INFINITY;
		yMin = Double.POSITIVE_INFINITY;
		yMax = Double.NEGATIVE_INFINITY;
		zMin = Double.POSITIVE_INFINITY;
		zMax = Double.NEGATIVE_INFINITY;

		for (Joint joint : joints)
		{
			for (Capture c : g)
			{
				Point p = c.get(joint);
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
