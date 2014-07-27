package core;

public class Capture
{
	public static final int jointCount = 24;
	public long timestamp;
	public Point[] points;
	
	public Capture()
	{
		points = new Point[Capture.jointCount];
	}
	
	public Capture(String s)
	{
		String[] parts = s.split(" ");
		timestamp = Long.parseLong(parts[0]);
		points = new Point[Capture.jointCount];
		for (int i = 0; i < points.length; i++)
		{
			double x = Double.parseDouble(parts[i * 3 + 1]);
			double y = Double.parseDouble(parts[i * 3 + 2]);
			double z = Double.parseDouble(parts[i * 3 + 3]);
			points[i] = new Point(x, y, z);
		}
	}
	
	public Capture(long timestamp, Point[] p)
	{
		points = p;
		this.timestamp = timestamp;
	}
	
	public static Capture fromChalearn(String line)
	{
//		String zeroline = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
//		if (line.equals(zeroline))
//			return null;
		String[] sl = line.split(",");
		String[] slp = new String[Capture.jointCount * 3];
		for (int i = 0; i < sl.length; i += 9)
			slp[i / 9] = sl[i + 0] + " " + sl[i + 1] + " " + sl[i + 2] + " "; 
		
		String s = "0 ";
		s += slp[3];
		s += slp[2];
		s += slp[1];
		s += slp[0];
		s += "0 0 0 ";
		s += slp[4];
		s += slp[5];
		s += slp[6];
		s += slp[7];
		s += "0 0 0 ";
		s += "0 0 0 ";
		s += slp[8];
		s += slp[9];
		s += slp[10];
		s += slp[11];
		s += "0 0 0 ";
		s += slp[12];
		s += slp[13];
		s += slp[14];
		s += slp[15];
		s += slp[16];
		s += slp[17];
		s += slp[18];
		s += slp[19];
		
		return new Capture(s);
	}
}
