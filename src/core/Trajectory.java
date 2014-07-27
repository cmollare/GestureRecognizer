package core;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Trajectory extends ArrayList<Point>
{
	@Override
	public boolean add(Point p)
	{
		return super.add(p);
	}
	
	public Trajectory copy()
	{
		return this.copy(this.size());
	}
	
	public Trajectory copy(int size)
	{
		Trajectory c = new Trajectory();
		for (int i = Math.max(this.size() - size, 0); i < this.size(); i++)
			c.add(this.get(i).copy());
		return c;
	}

	public void resampleTime(int n)
	{
		int s = this.size();
		double step = ((double) (s - 1)) / ((double)(n - 1));
		Trajectory newTraj = new Trajectory();
		newTraj.add(this.get(0));
		double current = step;
		int s2 = 1;
		while (s2 < n - 1)
		{
			int i = (int) Math.floor(current);
			Point a = this.get(i);
			Point b = this.get(i + 1);
			double r = current - Math.floor(current);
			
			double nx = a.x + r * (b.x - a.x);
			double ny = a.y + r * (b.y - a.y);
			double nz = a.z + r * (b.z - a.z);
			
			Point p = new Point(nx, ny, nz);
			newTraj.add(p);
			s2++;
			
			current += step;
		}

		newTraj.add(this.get(this.size() - 1));

		this.clear();
		this.addAll(newTraj);
	}
	
	public void resampleDistance(int n)
	{		
		double step = pathLength() / n;
		Trajectory newTraj = new Trajectory();
		newTraj.add(this.get(0));
		double currentDistance = 0;
		Point current = this.get(0);
		Point next = this.get(1);
		int nextIndex = 1;

		while (newTraj.size() < n - 1)
		{
			double d = Point.distance(current, next);
			if (currentDistance + d >= step)
			{
				double r = (step - currentDistance) / d;
				double nx = current.x + r * (next.x - current.x);
				double ny = current.y + r * (next.y - current.y);
				double nz = current.z + r * (next.z - current.z);

				Point p = new Point(nx, ny, nz);
				newTraj.add(p);
				current = p;
				currentDistance = 0;
			}
			else
			{
				currentDistance += d;
				nextIndex++;
				current = next;
				next = this.get(nextIndex);
			}
		}

		newTraj.add(this.get(this.size() - 1));

		this.clear();
		this.addAll(newTraj);
	}

	public double pathLength()
	{
		float d = 0;
		for (int i = 1; i < this.size(); i++)
			d += Point.distance(this.get(i), this.get(i - 1));
		return d;
	}

	public static double compare(Trajectory a, Trajectory b)
	{			
		if (a.size() != b.size())
		{
			System.err.println("Error: trying to compare 2 tracjectories with different sizes.");
			return 0;
		}
		
		int l = a.size();
		double d = 0;
		for (int i = 0; i < l; i++)
			d += Point.distance(a.get(i), b.get(i));

		return d;
	}
}
