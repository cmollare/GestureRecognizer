package core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import utils.SerializationUtils;
import utils.StringUtils;
import learning.Matrix;

public class Gesture implements Serializable, Iterable<Capture>
{
	public static final int sampleCount = 32;
	
	private List<Capture> captures;
	private String label;

	public Gesture()
	{
		captures = new ArrayList<Capture>();
	}

	public int captureCount()
	{
		return captures.size();
	}

	public int jointCount()
	{
		return captures.get(0).jointCount();
	}

	public void addCapture(Capture c)
	{
		captures.add(c);
	}

	public Capture getCapture(int i)
	{
		return captures.get(i);
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setLabel(String s)
	{
		label = s;
		for (Capture c : this)
			c.setLabel(s);
	}
	
	public List<GestureLabel> extractLabels()
	{
		List<GestureLabel> l = new ArrayList<GestureLabel>();
		int n = this.captureCount();
		if (n == 0)
			return l;
		
		String currentLabel = getCapture(0).getLabel();
		int currentLabelStart = 0;
		
		for (int i = 1; i < n; i++)
		{
			Capture c = getCapture(i);
			String newLabel = c.getLabel();
			if (!StringUtils.equal(currentLabel, newLabel))
			{
				l.add(new GestureLabel(currentLabelStart, i - 1, currentLabel));
				currentLabel = newLabel;
				currentLabelStart = i;
			}
		}
		
		l.add(new GestureLabel(currentLabelStart, n - 1, currentLabel));
		
		return l;
	}
	
	public void applyLabels(List<GestureLabel> l)
	{
		for (GestureLabel gl : l)
		{
			for (int i = gl.start; i <= gl.end; i++)
				getCapture(i).setLabel(gl.name);
		}
	}

	public Gesture copy()
	{
		return copy(captureCount());
	}

	public Gesture copy(int size)
	{
		return copy(captureCount() - size, captureCount() - 1);
	}

	public Gesture copy(int start, int end)
	{
		Gesture g = new Gesture();
		for (int i = start; i <= end; i++)
			g.addCapture(this.captures.get(i).copy());

		return g;
	}

	public Gesture select(Joint ... joints)
	{
		Gesture r = new Gesture();
		for (Capture c : this)
			r.addCapture(c.select(joints));
		
		return r;
	}
	
	public List<Gesture> decompose()
	{
		List<Gesture> r = new ArrayList<Gesture>();
		if (getLabel() != null)
		{
			r.add(copy());
			return r;
		}
		
		int n = captureCount();
		Gesture currentGesture = new Gesture();
		
		for (int i = 0; i < n; i++)
		{
			Capture c = getCapture(i);
			String label = c.getLabel();
			if (label != null)
			{
				if (!label.equals(currentGesture.getLabel()) && currentGesture.captureCount() != 0)
				{
					r.add(currentGesture);
					currentGesture = new Gesture();
				}
				currentGesture.addCapture(c);
				currentGesture.setLabel(label);
			}
			else if (currentGesture.captureCount() != 0)
			{
				r.add(currentGesture);
				currentGesture = new Gesture();
			}
		}
		
		if (currentGesture.captureCount() != 0)
			r.add(currentGesture);
		
		return r;
	}
	
//	public static void main(String[] arg)
//	{
//		Gesture g = new Gesture();
//		
//		g.addCapture(new Capture(null));
//		g.addCapture(new Capture(null));
//		g.addCapture(new Capture("S"));
//		g.addCapture(new Capture("S"));
//		g.addCapture(new Capture("S"));
//		g.addCapture(new Capture("A"));
//		g.addCapture(new Capture("A"));
//		g.addCapture(new Capture("S"));
//		g.addCapture(new Capture(null));
//		g.addCapture(new Capture("S"));
//		
////		for (Capture c : g)
////			System.out.println(c.getLabel());
//		
//		List<Gesture> r = g.decompose();
//		for (Gesture i : r)
//			System.out.println(i.getLabel() +  " " + i.captureCount());
//	}
	
	public Matrix toMatrix(Joint ... joints)
	{
		return select(joints).toMatrix();
	}
	
	public Matrix toMatrix()
	{
		int n = captureCount();
		int m = jointCount();
		Matrix r = new Matrix(n, m * 3);

		for (int i = 0; i < n; i++)
		{
			Capture c = this.getCapture(i);
			int k = 0;
			for (Point p : c)
			{
				r.set(i, k * 3 + 0, p.x);
				r.set(i, k * 3 + 1, p.y);
				r.set(i, k * 3 + 2, p.z);
				k++;
			}
		}

		return r;
	}

	public Gesture normalize()
	{
		return normalize(captureCount());
	}

	public Gesture normalize(int window)
	{
		return normalize(window, TranslationMethod.SHOULDERS, RotationMethod.SHOULDERS, ResamplingMethod.GLOBAL_DISTANCE, RescalingMethod.ARMS);
	}

	public Gesture normalize(int window, TranslationMethod tMethod, RotationMethod rotMethod, ResamplingMethod resMethod, RescalingMethod sMethod)
	{
		Gesture g = this.copy(window);

		g.resample(resMethod);
		g.rotate(rotMethod);
		g.translate(tMethod);
		g.rescale(sMethod);

		return g;
	}

	private void resample(ResamplingMethod method)
	{
		switch (method)
		{
		case GLOBAL_DISTANCE:
			resampleGlobal();
			break;
		case LINEAR:
			resampleLinear();
			break;
		}
	}

	private void resampleLinear()
	{
		int n = Gesture.sampleCount;
		double current = 0;
		double step = ((double) captureCount()) / (n);

		List<Capture> tmp = new ArrayList<Capture>();
		tmp.add(getCapture(0));
		int next = 1;

		while (tmp.size() < n - 1)
		{
			Capture a = getCapture(next - 1);
			Capture b = getCapture(next);

			if (current + step > 1)
			{
				next++;
				current = current - 1;
			}
			else
			{
				tmp.add(Capture.interpolate(a, b, current + step));
				current += step;
			}
		}

		tmp.add(getCapture(captureCount() - 1));
		captures = tmp;
	}

	private void resampleGlobal()
	{
		int n = Gesture.sampleCount;
		double step = globalLength() / (n - 1);
		double currentDistance = 0;

		List<Capture> tmp = new ArrayList<Capture>();
		tmp.add(getCapture(0));
		Capture lastAdded = getCapture(0);
		int next = 1;
		while (tmp.size() < n - 1)
		{
			Capture a = lastAdded;
			Capture b = getCapture(next);
			double d = Capture.distance(a, b);

			if (currentDistance + step > d)
			{
				lastAdded = b;
				next++;
				currentDistance = -d;
			}
			else
			{
				lastAdded = Capture.interpolate(a, b, (currentDistance + step) / d);
				tmp.add(lastAdded);
				currentDistance = 0;
			}
		}

		tmp.add(getCapture(captureCount() - 1));
		captures = tmp;
	}

	private double globalLength()
	{
		double d = 0;
		int n = captureCount();
		for (int i = 1; i < n; i++)
			d += Capture.distance(getCapture(i - 1), getCapture(i));
		return d;
	}

	private void rotate(RotationMethod method)
	{
		switch (method)
		{
		case NONE:
			break;
		case SHOULDERS:
			for (Capture c : captures)
			{
				Point leftShoulder = c.get(Joint.LEFT_SHOULDER);
				Point rightShoulder = c.get(Joint.RIGHT_SHOULDER);
				Point center = Point.interpolate(leftShoulder, rightShoulder, 0.5);
				double theta = shoulderAngle(leftShoulder, rightShoulder);
				for (Point p : c)
					p.rotateXZ(center, -theta);
			}
			break;
		}
	}

	private void rescale(RescalingMethod method)
	{
		switch (method)
		{
		case ARMS:
			BoundingBox rightBox = new BoundingBox(this, Joint.rightArm());
			double r1 = Math.max(rightBox.xSize, Math.max(rightBox.ySize, rightBox.zSize));
			double s = 1.0;
			for (Capture c : captures)
			{
				for (Joint j : Joint.rightArm())
				{
					Point p = c.get(j);
					p.x = p.x * s / r1;
					p.y = p.y * s / r1;
					p.z = p.z * s / r1;
				}
			}
			BoundingBox leftBox = new BoundingBox(this, Joint.leftArm());
			double r2 = Math.max(leftBox.xSize, Math.max(leftBox.ySize, leftBox.zSize));
			for (Capture c : captures)
			{
				for (Joint j : Joint.leftArm())
				{
					Point p = c.get(j);
					p.x = p.x * s / r2;
					p.y = p.y * s / r2;
					p.z = p.z * s / r2;
				}
			}

			break;
		case FULL_BODY:
			BoundingBox box = new BoundingBox(this);
			double r = Math.max(box.xSize, Math.max(box.ySize, box.zSize));
			for (Capture c : captures)
			{
				for (Point p : c)
				{
					p.x /= r;
					p.y /= r;
					p.z /= r;
				}
			}
			break;
		default:
			break;
		}
	}

	private void translate(TranslationMethod method)
	{
		switch (method)
		{
		case MIN:
			BoundingBox box = new BoundingBox(this);
			for (Capture c : captures)
			{
				for (Point p : c)
				{
					p.x -= box.xMin;
					p.y -= box.yMin;
					p.z -= box.zMin;
				}
			}
			break;
		case CENTROID:
			Point centroid = centroid();
			for (Capture c : captures)
			{
				for (Point p : c)
				{
					p.x -= centroid.x;
					p.y -= centroid.y;
					p.z -= centroid.z;
				}
			}
			break;
		case SHOULDERS:
			for (Capture c : captures)
			{
				Point leftPoint = c.get(Joint.LEFT_SHOULDER).copy();
				Point rightPoint = c.get(Joint.RIGHT_SHOULDER).copy();
				for (Joint j : Joint.leftArm())
				{
					Point p = c.get(j);
					p.x -= leftPoint.x;
					p.y -= leftPoint.y;
					p.z -= leftPoint.z;
				}
				for (Joint j : Joint.rightArm())
				{
					Point p = c.get(j);
					p.x = -(p.x - rightPoint.x); // mirror
					p.y = (p.y - rightPoint.y);
					p.z = (p.z - rightPoint.z);
				}
			}
			break;
		}
	}

	public Point centroid()
	{
		double x = 0;
		double y = 0;
		double z = 0;
		double pointCount = jointCount() * captureCount();

		for (Capture c : captures)
		{
			for (Point p : c)
			{
				x += p.x;
				y += p.y;
				z += p.z;
			}
		}

		return new Point(x / pointCount, y / pointCount, z / pointCount);
	}

	private static double shoulderAngle(Point leftShoulder, Point rightShoulder)
	{
		return Math.atan2(rightShoulder.z - leftShoulder.z, rightShoulder.x - leftShoulder.x);
	}

	@Override
	public Iterator<Capture> iterator()
	{
		return captures.iterator();
	}

	public void toFile(String filename)
	{
		SerializationUtils.toFile(filename, this);
	}

	public static Gesture fromFile(String filename)
	{
		return (Gesture) SerializationUtils.fromFile(filename);
	}

	public enum TranslationMethod
	{
		MIN, CENTROID, SHOULDERS
	};

	public enum ResamplingMethod
	{
		LINEAR, GLOBAL_DISTANCE
	};

	public enum RotationMethod
	{
		NONE, SHOULDERS
	};

	public enum RescalingMethod
	{
		FULL_BODY, ARMS;
	}
}
