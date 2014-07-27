package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import learning.Matrix;

import recognition.ChalearnRecognizer;
import recognition.HMMRecognizer;
import recognition.Word;

public class Gesture
{
	public static final int sampleCount = 32;

	public Trajectory[] trajectories;
	public String name;
	public Word meaning;

	public Gesture(String name, Word meaning)
	{
		trajectories = new Trajectory[Capture.jointCount];
		for (Joint j : Joint.values())
		{
			if (j.isDetected())
				trajectories[j.ordinal()] = new Trajectory();
		}

		this.meaning = meaning;
		this.name = name;
	}

	public int size()
	{
		return trajectories[Joint.LEFT_HAND.ordinal()].size();
	}

	public void add(Capture c)
	{
		for (int i = 0; i < Capture.jointCount; i++)
		{
			if (trajectories[i] != null)
				trajectories[i].add(c.points[i]);
		}
	}

	public Gesture copy()
	{
		Gesture c = new Gesture(this.name, this.meaning);
		for (int i = 0; i < trajectories.length; i++)
		{
			if (trajectories[i] != null)
				c.trajectories[i] = this.trajectories[i].copy();
			else
				c.trajectories[i] = null;
		}

		return c;
	}

	public Gesture copy(int start, int end)
	{
		Gesture g = new Gesture(null, null);
		for (int i = start; i <= end; i++)
		{
			for (Joint j : Joint.values())
			{
				if (this.trajectories[j.ordinal()] != null)
					g.trajectories[j.ordinal()].add(this.trajectories[j.ordinal()].get(i));
			}
		}

		return g;
	}

	public Gesture copy(int size)
	{
		Gesture c = new Gesture(this.name, this.meaning);
		for (int i = 0; i < trajectories.length; i++)
		{
			if (this.trajectories[i] != null)
				c.trajectories[i] = this.trajectories[i].copy(size);
			else
				c.trajectories[i] = null;
		}

		return c;
	}

	public Matrix toMatrix(Joint... joints)
	{
		boolean useDelta = false;
		int n = this.size();
		int m = joints.length;
		Matrix r = new Matrix(n, m * (useDelta ? 6 : 3));

		for (int j = 0; j < m; j++)
		{
			Trajectory t = trajectories[joints[j].ordinal()];
			for (int i = 0; i < n; i++)
			{
				Point p = t.get(i);

				if (useDelta)
				{
					double dx;
					double dy;
					double dz;

					if (i == 0)
					{
						Point next = t.get(i + 1);
						dx = next.x - p.x;
						dy = next.y - p.y;
						dz = next.z - p.z;
					}
					else
					{
						Point prev = t.get(i - 1);
						dx = p.x - prev.x;
						dy = p.y - prev.y;
						dz = p.z - prev.z;
					}

					r.set(i, j * 6 + 0, p.x);
					r.set(i, j * 6 + 1, p.y);
					r.set(i, j * 6 + 2, p.z);
					r.set(i, j * 6 + 3, dx);
					r.set(i, j * 6 + 4, dy);
					r.set(i, j * 6 + 5, dz);
				}
				else
				{
					r.set(i, j * 3 + 0, p.x);
					r.set(i, j * 3 + 1, p.y);
					r.set(i, j * 3 + 2, p.z);
				}
			}
		}

		return r;
	}

	public Matrix toMatrixWithContext(Integer context, Matrix hands, Joint... joints)
	{
		// context = 1 only
		boolean useDelta = false;

		int n = this.size(); // time = rows
		int m = joints.length; // features
		int n_h = hands.rows();
		int h = 6; // 2 aires + 4 bary
		int shift = 3 * m + h;

		if (n_h != n)
		{
			System.err.println("Error: Gesture.toMatrix(): different number of lines.");
			return null;
		}

		int nb_cols = (m * (useDelta ? 6 : 3) + h) * (context * 2 + 1);

		Matrix r = new Matrix(n, nb_cols);

		// a completer!!!

		// System.out.println("n=" + n + " m=" + m);

		if (context == 1)
		{
			// first line: t0:t0:t1
			{
				int i = 0; // t0
				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i);
						r.set(i, j * 3 + 0, p.x);
						r.set(i, j * 3 + 1, p.y);
						r.set(i, j * 3 + 2, p.z);

					}
				}
				// air blobs:
				r.set(i, 3 * m, Math.sqrt(hands.get(i, 2)));
				r.set(i, 3 * m + 1, Math.sqrt(hands.get(i, 38)));
				// barycentres:
				r.set(i, 3 * m + 2, hands.get(i, 3));
				r.set(i, 3 * m + 3, hands.get(i, 4));
				r.set(i, 3 * m + 4, hands.get(i, 39));
				r.set(i, 3 * m + 5, hands.get(i, 40));

				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i);
						r.set(i, j * 3 + shift + 0, p.x);
						r.set(i, j * 3 + shift + 1, p.y);
						r.set(i, j * 3 + shift + 2, p.z);
					}
				}
				// air blobs:
				r.set(i, 3 * m + shift, Math.sqrt(hands.get(i, 2)));
				r.set(i, 3 * m + shift + 1, Math.sqrt(hands.get(i, 38)));
				// barycentres:
				r.set(i, 3 * m + shift + 2, hands.get(i, 3));
				r.set(i, 3 * m + shift + 3, hands.get(i, 4));
				r.set(i, 3 * m + shift + 4, hands.get(i, 39));
				r.set(i, 3 * m + shift + 5, hands.get(i, 40));

				// t1
				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i + 1);
						r.set(i, j * 3 + 2 * shift + 0, p.x);
						r.set(i, j * 3 + 2 * shift + 1, p.y);
						r.set(i, j * 3 + 2 * shift + 2, p.z);
					}
				}

				// air blobs:
				r.set(i, 3 * m + 2 * shift, Math.sqrt(hands.get(i + 1, 2)));
				r.set(i, 3 * m + 2 * shift + 1, Math.sqrt(hands.get(i + 1, 38)));
				// barycentres:
				r.set(i, 3 * m + 2 * shift + 2, hands.get(i + 1, 3));
				r.set(i, 3 * m + 2 * shift + 3, hands.get(i + 1, 4));
				r.set(i, 3 * m + 2 * shift + 4, hands.get(i + 1, 39));
				r.set(i, 3 * m + 2 * shift + 5, hands.get(i + 1, 40));

			}
			// end of print 1st line

			// line i: ti-1:ti:ti+1
			{
				for (int i = 1; i < n - 1; i++)
				{
					for (int j = 0; j < m; j++)
					{
						Trajectory t = trajectories[joints[j].ordinal()];
						{
							// ti-1
							Point p = t.get(i - 1);
							r.set(i, j * 3 + 0, p.x);
							r.set(i, j * 3 + 1, p.y);
							r.set(i, j * 3 + 2, p.z);

						}
					}
					// air blobs:
					r.set(i, 3 * m, Math.sqrt(hands.get(i - 1, 2)));
					r.set(i, 3 * m + 1, Math.sqrt(hands.get(i - 1, 38)));
					// barycentres:
					r.set(i, 3 * m + 2, hands.get(i - 1, 3));
					r.set(i, 3 * m + 3, hands.get(i - 1, 4));
					r.set(i, 3 * m + 4, hands.get(i - 1, 39));
					r.set(i, 3 * m + 5, hands.get(i - 1, 40));

					for (int j = 0; j < m; j++)
					{
						Trajectory t = trajectories[joints[j].ordinal()];
						{
							// ti
							Point p = t.get(i);
							r.set(i, j * 3 + shift + 0, p.x);
							r.set(i, j * 3 + shift + 1, p.y);
							r.set(i, j * 3 + shift + 2, p.z);
						}
					}
					// air blobs:
					r.set(i, 3 * m + shift, Math.sqrt(hands.get(i, 2)));
					r.set(i, 3 * m + shift + 1, Math.sqrt(hands.get(i, 38)));
					// barycentres:
					r.set(i, 3 * m + shift + 2, hands.get(i, 3));
					r.set(i, 3 * m + shift + 3, hands.get(i, 4));
					r.set(i, 3 * m + shift + 4, hands.get(i, 39));
					r.set(i, 3 * m + shift + 5, hands.get(i, 40));

					for (int j = 0; j < m; j++)
					{
						Trajectory t = trajectories[joints[j].ordinal()];
						{
							// ti+1
							Point p = t.get(i + 1);
							r.set(i, j * 3 + 2 * shift + 0, p.x);
							r.set(i, j * 3 + 2 * shift + 1, p.y);
							r.set(i, j * 3 + 2 * shift + 2, p.z);
						}
					}
					// air blobs:
					r.set(i, 3 * m + 2 * shift, Math.sqrt(hands.get(i + 1, 2)));
					r.set(i, 3 * m + 2 * shift + 1, Math.sqrt(hands.get(i + 1, 38)));
					// barycentres:
					r.set(i, 3 * m + 2 * shift + 2, hands.get(i + 1, 3));
					r.set(i, 3 * m + 2 * shift + 3, hands.get(i + 1, 4));
					r.set(i, 3 * m + 2 * shift + 4, hands.get(i + 1, 39));
					r.set(i, 3 * m + 2 * shift + 5, hands.get(i + 1, 40));

				}
			}

			// last line: tN-2:tN-1:tN-1
			{
				int i = n - 1; // tN-2
				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i - 1);
						r.set(i, j * 3 + 0, p.x);
						r.set(i, j * 3 + 1, p.y);
						r.set(i, j * 3 + 2, p.z);
					}
				}
				// air blobs:
				r.set(i, 3 * m, Math.sqrt(hands.get(i - 1, 2)));
				r.set(i, 3 * m + 1, Math.sqrt(hands.get(i - 1, 38)));
				// barycentres:
				r.set(i, 3 * m + 2, hands.get(i - 1, 3));
				r.set(i, 3 * m + 3, hands.get(i - 1, 4));
				r.set(i, 3 * m + 4, hands.get(i - 1, 39));
				r.set(i, 3 * m + 5, hands.get(i - 1, 40));

				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i);
						r.set(i, j * 3 + shift + 0, p.x);
						r.set(i, j * 3 + shift + 1, p.y);
						r.set(i, j * 3 + shift + 2, p.z);
					}
				}
				// air blobs:
				r.set(i, 3 * m + shift, Math.sqrt(hands.get(i, 2)));
				r.set(i, 3 * m + shift + 1, Math.sqrt(hands.get(i, 38)));
				// barycentres:
				r.set(i, 3 * m + shift + 2, hands.get(i, 3));
				r.set(i, 3 * m + shift + 3, hands.get(i, 4));
				r.set(i, 3 * m + shift + 4, hands.get(i, 39));
				r.set(i, 3 * m + shift + 5, hands.get(i, 40));

				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i);
						r.set(i, j * 3 + 2 * shift + 0, p.x);
						r.set(i, j * 3 + 2 * shift + 1, p.y);
						r.set(i, j * 3 + 2 * shift + 2, p.z);
					}
				}
				// air blobs:
				r.set(i, 3 * m + 2 * shift, Math.sqrt(hands.get(i, 2)));
				r.set(i, 3 * m + 2 * shift + 1, Math.sqrt(hands.get(i, 38)));
				// barycentres:
				r.set(i, 3 * m + 2 * shift + 2, hands.get(i, 3));
				r.set(i, 3 * m + 2 * shift + 3, hands.get(i, 4));
				r.set(i, 3 * m + 2 * shift + 4, hands.get(i, 39));
				r.set(i, 3 * m + 2 * shift + 5, hands.get(i, 40));

			}
		}
		// System.out.println("R: ");
		// r.print();

		return r;
	}

	public Matrix toMatrixWithContextAndDerivative(Integer context, Joint... joints)
	{

		context = 1;// no choice!
		boolean useDelta = true;
		int n = this.size();// taille des samples (32)
		int m = joints.length; // nb de joints. meaningfull = 6
		int shift = m * 6;
		Matrix r = new Matrix(n, m * (useDelta ? 6 : 3) * (context * 2 + 1));

		// System.out.println("n=" + n + " m=" + m);
		double dx;
		double dy;
		double dz;

		if (context == 1)
		{
			// first line: t0:t0:t1
			{
				int i = 0; // t0: on prend la derivee de t1 a la place de la der de t0. Better choice? Missing value?
				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i);
						Point next = t.get(i + 1);
						dx = next.x - p.x;
						dy = next.y - p.y;
						dz = next.z - p.z;
						r.set(i, j * 6 + 0, p.x);
						r.set(i, j * 6 + 1, p.y);
						r.set(i, j * 6 + 2, p.z);
						r.set(i, j * 6 + 3, dx);
						r.set(i, j * 6 + 4, dy);
						r.set(i, j * 6 + 5, dz);

					}
				}
				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i);
						Point next = t.get(i + 1);
						dx = next.x - p.x;
						dy = next.y - p.y;
						dz = next.z - p.z;
						r.set(i, j * 6 + shift + 0, p.x);
						r.set(i, j * 6 + shift + 1, p.y);
						r.set(i, j * 6 + shift + 2, p.z);
						r.set(i, j * 6 + shift + 3, dx);
						r.set(i, j * 6 + shift + 4, dy);
						r.set(i, j * 6 + shift + 5, dz);
					}
				}

				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i + 1);
						Point next = t.get(i + 2);
						dx = next.x - p.x;
						dy = next.y - p.y;
						dz = next.z - p.z;
						r.set(i, j * 6 + 2 * shift + 0, p.x);
						r.set(i, j * 6 + 2 * shift + 1, p.y);
						r.set(i, j * 6 + 2 * shift + 2, p.z);
						r.set(i, j * 6 + 2 * shift + 3, dx);
						r.set(i, j * 6 + 2 * shift + 4, dy);
						r.set(i, j * 6 + 2 * shift + 5, dz);
					}
				}
			}
			// end of print 1st line

			// line i: ti-1:ti:ti+1
			{
				for (int i = 1; i < n - 1; i++)
				{
					for (int j = 0; j < m; j++)
					{
						Trajectory t = trajectories[joints[j].ordinal()];
						{
							// ti-1
							Point p = t.get(i - 1);
							Point next = t.get(i);
							dx = next.x - p.x;
							dy = next.y - p.y;
							dz = next.z - p.z;
							r.set(i, j * 6 + 0, p.x);
							r.set(i, j * 6 + 1, p.y);
							r.set(i, j * 6 + 2, p.z);
							r.set(i, j * 6 + 3, dx);
							r.set(i, j * 6 + 4, dy);
							r.set(i, j * 6 + 5, dz);
						}
					}

					for (int j = 0; j < m; j++)
					{
						Trajectory t = trajectories[joints[j].ordinal()];
						{
							// ti
							Point p = t.get(i);
							Point next = t.get(i + 1);
							dx = next.x - p.x;
							dy = next.y - p.y;
							dz = next.z - p.z;
							r.set(i, j * 6 + shift + 0, p.x);
							r.set(i, j * 6 + shift + 1, p.y);
							r.set(i, j * 6 + shift + 2, p.z);
							r.set(i, j * 6 + shift + 3, dx);
							r.set(i, j * 6 + shift + 4, dy);
							r.set(i, j * 6 + shift + 5, dz);
						}
					}

					for (int j = 0; j < m; j++)
					{
						Trajectory t = trajectories[joints[j].ordinal()];
						{
							// ti+1
							if (i < n - 2)
							{
								Point p = t.get(i + 1);
								Point next = t.get(i + 2);
								dx = next.x - p.x;
								dy = next.y - p.y;
								dz = next.z - p.z;
								r.set(i, j * 6 + 2 * shift + 0, p.x);
								r.set(i, j * 6 + 2 * shift + 1, p.y);
								r.set(i, j * 6 + 2 * shift + 2, p.z);
								r.set(i, j * 6 + 2 * shift + 3, dx);
								r.set(i, j * 6 + 2 * shift + 4, dy);
								r.set(i, j * 6 + 2 * shift + 5, dz);
							}
							else
							{
								// i=n-2 on repete la derivee du point n-2
								Point p = t.get(i);
								Point next = t.get(i + 1);
								dx = next.x - p.x;
								dy = next.y - p.y;
								dz = next.z - p.z;
								r.set(i, j * 6 + 2 * shift + 0, p.x);
								r.set(i, j * 6 + 2 * shift + 1, p.y);
								r.set(i, j * 6 + 2 * shift + 2, p.z);
								r.set(i, j * 6 + 2 * shift + 3, dx);
								r.set(i, j * 6 + 2 * shift + 4, dy);
								r.set(i, j * 6 + 2 * shift + 5, dz);
							}

						}
					}

				}
			}

			// last line: tN-2:tN-1:tN-1. on prend la derivee de tN-2 a la place de la der de tN-1. Better choice? Missing value?
			{
				int i = n - 1;
				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i - 1);
						Point next = t.get(i);
						dx = next.x - p.x;
						dy = next.y - p.y;
						dz = next.z - p.z;
						r.set(i, j * 6 + 0, p.x);
						r.set(i, j * 6 + 1, p.y);
						r.set(i, j * 6 + 2, p.z);
						r.set(i, j * 6 + 3, dx);
						r.set(i, j * 6 + 4, dy);
						r.set(i, j * 6 + 5, dz);
					}
				}
				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i);
						Point previous = t.get(i - 1);
						dx = p.x - previous.x;
						dy = p.y - previous.y;
						dz = p.z - previous.z;
						r.set(i, j * 6 + shift + 0, p.x);
						r.set(i, j * 6 + shift + 1, p.y);
						r.set(i, j * 6 + shift + 2, p.z);
						r.set(i, j * 6 + shift + 3, dx);
						r.set(i, j * 6 + shift + 4, dy);
						r.set(i, j * 6 + shift + 5, dz);
					}
				}
				for (int j = 0; j < m; j++)
				{
					Trajectory t = trajectories[joints[j].ordinal()];
					{
						Point p = t.get(i);
						Point previous = t.get(i - 1);
						dx = p.x - previous.x;
						dy = p.y - previous.y;
						dz = p.z - previous.z;
						r.set(i, j * 6 + 2 * shift + 0, p.x);
						r.set(i, j * 6 + 2 * shift + 1, p.y);
						r.set(i, j * 6 + 2 * shift + 2, p.z);
						r.set(i, j * 6 + 2 * shift + 3, dx);
						r.set(i, j * 6 + 2 * shift + 4, dy);
						r.set(i, j * 6 + 2 * shift + 5, dz);
					}
				}
			}
		}
		// System.out.println("R: ");
		// r.print(); System.out.println("nb_rows:" + r.rows());
		// r.print();

		return r;
	}

	public Matrix toMatrixOneVectorPerGesture(Joint... joints)
	{
		// returns a single row matrix with all the 32 captures
		int n = this.size();
		int m = joints.length;
		int sizePoint = 3;
		Matrix r = new Matrix(1, n * m * 3);

		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
			{
				Trajectory t = trajectories[joints[j].ordinal()];
				Point p = t.get(i);
				r.set(0, i * m * sizePoint + j * sizePoint + 0, p.x);
				r.set(0, i * m * sizePoint + j * sizePoint + 1, p.y);
				r.set(0, i * m * sizePoint + j * sizePoint + 2, p.z);
			}
		}

		return r;
	}

	// public int[] labels()
	// {
	// int stateCount = 3;
	// int s = this.size();
	// int[] l = new int[s];
	//
	// for (int i = 0; i < s; i++)
	// l[i] = (int) (i * ((double) stateCount) / s);
	// return l;
	// }

	public Gesture normalize()
	{
		return normalize(-1);
	}

	public Gesture normalize(int window)
	{
		return normalize(window, ScalingMethod.ARMS, TranslationMethod.SHOULDERS, RotationMethod.SHOULDERS, ResamplingMethod.GLOBAL_DISTANCE);
	}

	public Gesture normalize(int window, ScalingMethod scalingMethod, TranslationMethod tMethod, RotationMethod rotMethod, ResamplingMethod resMethod)
	{
		Gesture g = window == -1 ? this.copy() : this.copy(window);

		g.resample(resMethod);
		g.rotate(rotMethod);
		g.translate(tMethod);
		g.rescale(scalingMethod);
		g.name = g.name + "_";

		return g;
	}

	private void resample(ResamplingMethod method)
	{
		for (Joint joint : Joint.values())
		{
			if (joint.isDetected())
			{
				switch (method)
				{
				case GLOBAL_DISTANCE:
					resampleGlobal();
					break;
				case DISTANCE:
					trajectories[joint.ordinal()].resampleDistance(sampleCount);
					break;
				case TIME:
					trajectories[joint.ordinal()].resampleTime(sampleCount);
					break;
				}
			}
		}
	}

	private void resampleGlobal()
	{
		int n = sampleCount;
		double step = globalLength() / n;
		Trajectory[] newTraj = new Trajectory[Capture.jointCount];
		for (Joint j : Joint.arms())
		{
			newTraj[j.ordinal()] = new Trajectory();
			newTraj[j.ordinal()].add(trajectories[j.ordinal()].get(0));
		}

		double currentDistance = 0;
		Trajectory[] currentTraj = this.trajectories;
		Trajectory[] nextTraj = this.trajectories;
		int current = 0;
		int next = 1;
		int count = 1;

		while (count < n - 1)
		{
			double d = captureDistance(currentTraj, nextTraj, current, next);
			if (currentDistance + d >= step)
			{
				double r = (step - currentDistance) / d;

				for (Joint j : Joint.arms())
				{
					Point a = currentTraj[j.ordinal()].get(current);
					Point b = nextTraj[j.ordinal()].get(next);
					Point p = Point.interpolate(a, b, r);
					newTraj[j.ordinal()].add(p);
				}

				current = count;
				count++;
				currentTraj = newTraj;
				currentDistance = 0;
			}
			else
			{
				currentDistance += d;
				current = next;
				next++;
				currentTraj = this.trajectories;
			}
		}

		for (Joint j : Joint.arms())
		{
			Trajectory t = this.trajectories[j.ordinal()];
			newTraj[j.ordinal()].add(t.get(t.size() - 1));
		}

		this.trajectories = newTraj;
	}

	public boolean isLeftHanded()
	{
		return trajectories[Joint.LEFT_HAND.ordinal()].pathLength() > trajectories[Joint.RIGHT_HAND.ordinal()].pathLength();
	}

	public boolean is21()
	{
		double threshold = 60;

		// System.out.println("left pathlength: " + trajectories[Joint.LEFT_HAND.ordinal()].pathLength());
		// System.out.println("right pathlength: " + trajectories[Joint.RIGHT_HAND.ordinal()].pathLength());

		return ((trajectories[Joint.LEFT_HAND.ordinal()].pathLength() < threshold) && (trajectories[Joint.RIGHT_HAND.ordinal()].pathLength() < threshold));
	}

	private double globalLength()
	{
		double d = 0;
		for (Joint j : Joint.arms())
			d += trajectories[j.ordinal()].pathLength();
		return d;
	}

	private double captureDistance(Trajectory[] aTraj, Trajectory[] bTraj, int a, int b)
	{
		double d = 0;
		for (Joint j : Joint.arms())
			d += Point.distance(aTraj[j.ordinal()].get(a), bTraj[j.ordinal()].get(b));

		return d;
	}

	private void rotate(RotationMethod method)
	{
		switch (method)
		{
		case NONE:
			break;
		case SHOULDERS:
			for (int i = 0; i < sampleCount; i++)
			{
				Point leftShoulder = trajectories[Joint.LEFT_SHOULDER.ordinal()].get(i);
				Point rightShoulder = trajectories[Joint.RIGHT_SHOULDER.ordinal()].get(i);
				Point center = Point.interpolate(leftShoulder, rightShoulder, 0.5);
				double theta = shoulderAngle(leftShoulder, rightShoulder);

				for (Joint j : Joint.arms())
					trajectories[j.ordinal()].get(i).rotateXZ(center, -theta);
			}
			break;
		}
	}

	private void rescale(ScalingMethod method)
	{
		switch (method)
		{
		case ARMS:
			BoundingBox rightBox = new BoundingBox(this, Joint.rightArm());
			double r1 = Math.max(rightBox.xSize, Math.max(rightBox.ySize, rightBox.zSize));
			double s = 1.0;
			for (Joint j : Joint.rightArm())
			{
				for (Point p : this.trajectories[j.ordinal()])
				{
					p.x = p.x * s / r1;
					p.y = p.y * s / r1;
					p.z = p.z * s / r1;
				}
			}
			BoundingBox leftBox = new BoundingBox(this, Joint.leftArm());
			double r2 = Math.max(leftBox.xSize, Math.max(leftBox.ySize, leftBox.zSize));
			for (Joint j : Joint.leftArm())
			{
				for (Point p : this.trajectories[j.ordinal()])
				{
					p.x = p.x * s / r2;
					p.y = p.y * s / r2;
					p.z = p.z * s / r2;
				}
			}

			break;
		case FULL_BODY:
			BoundingBox box = new BoundingBox(this);
			double r = Math.max(box.xSize, Math.max(box.ySize, box.zSize));
			for (Trajectory t : trajectories)
			{
				for (Point p : t)
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
			for (Trajectory t : trajectories)
			{
				for (Point p : t)
				{
					p.x -= box.xMin;
					p.y -= box.yMin;
					p.z -= box.zMin;
				}
			}
			break;
		case CENTROID:
			Point centroid = centroid();
			for (Trajectory t : trajectories)
			{
				for (Point p : t)
				{
					p.x -= centroid.x;
					p.y -= centroid.y;
					p.z -= centroid.z;
				}
			}
			break;
		case SHOULDERS:

			for (int i = 0; i < sampleCount; i++)
			{
				Point leftPoint = trajectories[Joint.LEFT_SHOULDER.ordinal()].get(i).copy();
				Point rightPoint = trajectories[Joint.RIGHT_SHOULDER.ordinal()].get(i).copy();
				for (Joint j : Joint.leftArm())
				{
					Point p = trajectories[j.ordinal()].get(i);
					p.x -= leftPoint.x;
					p.y -= leftPoint.y;
					p.z -= leftPoint.z;
				}
				for (Joint j : Joint.rightArm())
				{
					Point p = trajectories[j.ordinal()].get(i);
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
		double count = 0;

		for (Joint joint : Joint.values())
		{
			if (joint.isDetected())
			{
				for (Point p : trajectories[joint.ordinal()])
				{
					count++;
					x += p.x;
					y += p.y;
					z += p.z;
				}
			}
		}

		if (count == 0)
			return new Point(0, 0, 0);

		return new Point(x / count, y / count, z / count);
	}

	public void printArms()
	{
		for (int i = 0; i < sampleCount; i++)
		{
			String s = "";
			for (Joint j : Joint.leftArm())
			{
				s += trajectories[j.ordinal()].get(i) + " ";
			}
			for (Joint j : Joint.rightArm())
			{
				s += trajectories[j.ordinal()].get(i) + " ";
			}
			System.out.println(s);
		}
		System.out.println();
	}

	public void print(Joint... joints)
	{
		for (int i = 0; i < sampleCount; i++)
		{
			String s = "";
			for (Joint j : joints)
			{
				s += trajectories[j.ordinal()].get(i) + " ";
			}
			System.out.println(s);
		}
		System.out.println();
	}

	public static double compare(Gesture a, Gesture b, ScoreMethod method)
	{
		double d = 0;
		double maxScore = 1;

		switch (method)
		{
		case ARMS:
			double normalScore = compareArms(a, b, false);
			double reversedScore = compareArms(a, b, true);
			d = Math.min(normalScore, reversedScore);
			maxScore = Gesture.sampleCount * 4.0 * Math.sqrt(3);
//			System.out.println("max: " + maxScore + " normal: " + normalScore + " reversed: " + reversedScore);
//			System.out.println("AleftBox: " + new BoundingBox(a, Joint.leftArm()));
//			System.out.println("ArightBox: " + new BoundingBox(a, Joint.rightArm()));
//			System.out.println("BleftBox: " + new BoundingBox(b, Joint.leftArm()));
//			System.out.println("BrightBox: " + new BoundingBox(b, Joint.rightArm()));
//			a.printArms();
//			b.printArms();
			break;
		case FULL_BODY:
			for (Joint joint : Joint.values())
			{
				if (joint.isDetected())
					d += Trajectory.compare(a.trajectories[joint.ordinal()], b.trajectories[joint.ordinal()]) * joint.weight();
			}

			maxScore = Gesture.sampleCount * Joint.effectiveJointCount * Math.sqrt(3);
			break;
		}
		
//		System.out.println("d: " + d + " max: " + maxScore + " " + (1 - d / maxScore));
		return 1 - d / maxScore;
	}

	public static double compareArms(Gesture a, Gesture b, boolean reversed)
	{
		double d = 0;
		for (int i = 1; i < 3; i++)
		{
			Joint aJoint = reversed ? Joint.rightArm()[i] : Joint.leftArm()[i];
			Joint bJoint = Joint.leftArm()[i];
			d += Trajectory.compare(a.trajectories[aJoint.ordinal()], b.trajectories[bJoint.ordinal()]);
		}
		for (int i = 1; i < 3; i++)
		{
			Joint aJoint = reversed ? Joint.leftArm()[i] : Joint.rightArm()[i];
			Joint bJoint = Joint.rightArm()[i];
			d += Trajectory.compare(a.trajectories[aJoint.ordinal()], b.trajectories[bJoint.ordinal()]);
		}
		
//		System.out.println("compareArms: " + d);
		return d;
	}

	public static Gesture load(String filename, Word meaning)
	{
		Gesture g = new Gesture(pathToName(filename), meaning);
		BufferedReader br = null;

		try
		{
			String line;
			br = new BufferedReader(new FileReader(filename));

			while ((line = br.readLine()) != null)
			{
				Capture c = new Capture(line);
				g.add(c);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

		// System.out.println("Loaded gesture " + g.name + ". Capture count: " +
		// g.trajectories[0].size());

		return g;
	}

	public static Gesture load_chalearn(String filename, Word meaning)
	{
		Gesture g = new Gesture(filename, meaning);
		BufferedReader br = null;

		try
		{
			String line;
			br = new BufferedReader(new FileReader(filename));

			while ((line = br.readLine()) != null)
			{
				// System.out.println("New capture: " + line);
				Capture c = Capture.fromChalearn(line);
				// if(c != null)
				g.add(c);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

		// System.out.println("Loaded gesture " + g.name + ". Capture count: " +
		// g.trajectories[0].size());

		return g;
	}

	public static Gesture load_chalearn(String filename, InputStream is, Word meaning)
	{
		Gesture g = new Gesture(filename, meaning);
		BufferedReader br = null;

		try
		{
			String line;
			int nb_lines = 0;

			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			while ((line = br.readLine()) != null)
			{
				// System.out.println("New capture: " + line);
				Capture c = Capture.fromChalearn(line);

				g.add(c);
				nb_lines++;

			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

		// System.out.println("Loaded gesture " + g.name + ". Capture count: " +
		// g.trajectories[0].size());

		return g;
	}

	public boolean isClean()
	{

		for (int i = 0; i < this.size(); i++)
		{
			boolean zeroline = true;
			for (Joint j : Joint.values())
			{
				if (this.trajectories[j.ordinal()] != null && !this.trajectories[j.ordinal()].get(i).isZero())
				{
					zeroline = false;
					break;
				}
			}
			if (zeroline)
				return false;
		}

		return true;
	}

	public void write(String filename)
	{
		try
		{
			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			int s = trajectories[8].size();
			for (int i = 0; i < s; i++)
			{
				String content = "";
				content += "0 ";
				for (Joint j : Joint.values())
				{
					if (trajectories[j.ordinal()] != null)
						content += trajectories[j.ordinal()].get(i).x + " " + trajectories[j.ordinal()].get(i).y + " " + trajectories[j.ordinal()].get(i).z + " ";
					else
						content += "0 0 0 ";
				}
				content += "\n";
				bw.write(content);
			}

			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeARMS(String filename)
	{
		try
		{
			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < trajectories[8].size(); i++)
			{
				String content = "";
				content += "0 ";
				// for (Joint j : Joint.values())
				for (Joint j : Joint.rightArm())
				{
					if (j.isDetected() && j != Joint.RIGHT_SHOULDER)
						content += trajectories[j.ordinal()].get(i).x + " " + trajectories[j.ordinal()].get(i).y + " " + trajectories[j.ordinal()].get(i).z + " ";
				}

				for (Joint j : Joint.leftArm())
				{
					if (j.isDetected() && j != Joint.LEFT_SHOULDER)
						content += trajectories[j.ordinal()].get(i).x + " " + trajectories[j.ordinal()].get(i).y + " " + trajectories[j.ordinal()].get(i).z + " ";
				}

				content += "\n";
				bw.write(content);
			}

			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeToArff(String filename)
	{

		try
		{
			boolean writeHeader = false;

			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
				writeHeader = true;
			}

			// append mode with 2nd arg as true
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			if (writeHeader)
			{
				bw.write("@RELATION arms\n");
				bw.write("@ATTRIBUTE  LEFT_ELBOW_x NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_ELBOW_y NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_ELBOW_z NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_HAND_x NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_HAND_y NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_HAND_z NUMERIC\n");

				bw.write("@ATTRIBUTE  RIGHT_ELBOW_x NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_ELBOW_y NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_ELBOW_z NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_HAND_x NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_HAND_y NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_HAND_z NUMERIC\n");

				bw.write("@ATTRIBUTE class {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}\n");
				bw.write("\n");
				bw.write("@DATA\n");
			}

			String content = "";
			for (int i = 0; i < trajectories[8].size(); i++)
			{

				content = "";

				for (Joint j : Joint.leftArm())
				{
					if (j.isDetected() && j != Joint.LEFT_SHOULDER)
						content += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
				}

				for (Joint j : Joint.rightArm())
				{
					if (j.isDetected() && j != Joint.RIGHT_SHOULDER)
						content += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
				}
				content += name;
				content += "\n";
				bw.write(content);

			}
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public String capture2string(int i)
	{
		String capture = "";

		for (Joint j : ChalearnRecognizer.meaningfulJoints)
		{
			if (j.isDetected())
				capture += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
		}

		return capture;
	}

	public String capture2stringORIG(int i)
	{
		String capture = "";

		for (Joint j : Joint.leftArm())
		{
			if (j.isDetected() && j != Joint.LEFT_SHOULDER)
				capture += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
		}

		for (Joint j : Joint.rightArm())
		{
			if (j.isDetected() && j != Joint.RIGHT_SHOULDER)
				capture += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
		}

		return capture;
	}

	public void writeToArffContext(String filename)
	{

		try
		{
			boolean writeHeader = false;

			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
				writeHeader = true;
			}

			// append mode with 2nd arg as true
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			int context = 1; // {1,2} : 1 veut dire context1: t-1:t:t+1; 2 veut dire context2: t-2:t-1:t:t+1:t+2;

			if (writeHeader)
			{
				bw.write("@RELATION arms\n");

				for (int i = 0; i <= context * 2; i++)
				{
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_z_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_z_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_WRIST_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_WRIST_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_WRIST_z_" + i + " NUMERIC\n");

					bw.write("@ATTRIBUTE  LEFT_ELBOW_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_ELBOW_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_ELBOW_z_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_z_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_WRIST_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_WRIST_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_WRIST_z_" + i + " NUMERIC\n");

				}
				bw.write("@ATTRIBUTE class {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}\n");
				bw.write("\n");
				bw.write("@DATA\n");
			}

			String content = "";

			if (context == 1)
			{
				// first line: t0:t0:t1
				{
					content = capture2string(0);
					content = content + content;
					content += capture2string(1);
					content += name;
					content += "\n";
					bw.write(content);
				}
				// end of print 1st line

				// line i: ti-1:ti:ti+1
				for (int i = 1; i < trajectories[8].size() - 1; i++)
				{
					content = "";
					content += capture2string(i - 1);// t-1
					content += capture2string(i);// t
					content += capture2string(i + 1);// t+1
					content += name;
					content += "\n";
					bw.write(content);
				}

				// last line: tN-2:tN-1:tN-1
				{
					content = "";
					int i = trajectories[8].size() - 2;
					content += capture2string(i);

					i = trajectories[8].size() - 1;
					String content2 = "";
					content2 += capture2string(i);
					content = content + content2 + content2;
					content += name;
					content += "\n";
					bw.write(content);
				}
			}

			if (context == 2)
			{
				// first line: t0:t0:t0:t1:t2
				{
					content = capture2string(0);
					content = content + content + content;
					content += capture2string(1);
					content += capture2string(2);
					content += name;
					content += "\n";
					bw.write(content);
				}
				// end of print 1st line

				// 2nd line: t0:t1:t1:t2:t3
				{
					content = "";
					content = capture2string(0);
					content += capture2string(1) + capture2string(1);
					content += capture2string(2);
					content += capture2string(3);
					content += name;
					content += "\n";
					bw.write(content);
				}
				// end of print 2nd line

				// line i: t-2:t-1:t:t+1:t+2
				for (int i = 2; i < trajectories[8].size() - 2; i++)
				{
					content = "";
					content += capture2string(i - 2);// t-2
					content += capture2string(i - 1);// t-1
					content += capture2string(i);// t
					content += capture2string(i + 1);// t+1
					content += capture2string(i + 1);// t+2
					content += name;
					content += "\n";
					bw.write(content);
				}

				// penultimate line: tN-4:tN-3:tN-2:tN-1:tN-1
				{
					content = "";
					int i = trajectories[8].size() - 2;
					content += capture2string(i - 2);
					content += capture2string(i - 1);
					content += capture2string(i) + capture2string(i) + capture2string(i + 1);
					content += name;
					content += "\n";
					bw.write(content);
				}

				// last line: tN-3:tN-2:tN-1:tN-1:tN-1
				{
					content = "";
					int i = trajectories[8].size() - 1;
					content += capture2string(i - 2);
					content += capture2string(i - 1);
					content += capture2string(i) + capture2string(i) + capture2string(i);
					content += name;
					content += "\n";
					bw.write(content);
				}
			}
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeToArffContextHMMStates(String filename, int[] stateLabels)
	{

		try
		{
			boolean writeHeader = false;

			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
				writeHeader = true;
			}

			// append mode with 2nd arg as true
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			int context = 2; // {2,4} : 2 veut dire context1: t-1:t:t+1; 4 veut dire context2: t-2:t-1:t:t+1:t+2;

			if (writeHeader)
			{
				bw.write("@RELATION arms\n");

				for (int i = 0; i <= context; i++)
				{

					bw.write("@ATTRIBUTE  RIGHT_ELBOW_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_z_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_z_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_WRIST_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_WRIST_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_WRIST_z_" + i + " NUMERIC\n");

					bw.write("@ATTRIBUTE  LEFT_ELBOW_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_ELBOW_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_ELBOW_z_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_z_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_WRIST_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_WRIST_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_WRIST_z_" + i + " NUMERIC\n");

				}
				// bw.write("@ATTRIBUTE class {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}\n");
				// 3 states per gesture but 20 is 1-state:
				bw.write("@ATTRIBUTE class {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60}\n");
				bw.write("\n");
				bw.write("@DATA\n");
			}

			String content = "";
			int classe = 0;

			if (context == 2)
			{
				// first line: t0:t0:t1
				{
					content = capture2string(0);
					content = content + content;
					content += capture2string(1);
					classe = stateLabels[0] + Integer.parseInt(name) * 3;
					content += Integer.toString(classe);
					content += "\n";
					bw.write(content);
				}
				// end of print 1st line

				// line i: ti-1:ti:ti+1
				for (int i = 1; i < trajectories[8].size() - 1; i++)
				{
					content = "";
					content += capture2string(i - 1);// t-1
					content += capture2string(i);// t
					content += capture2string(i + 1);// t+1
					classe = stateLabels[i] + Integer.parseInt(name) * 3;
					content += Integer.toString(classe);
					content += "\n";
					bw.write(content);
				}

				// last line: tN-2:tN-1:tN-1
				{
					content = "";
					int i = trajectories[8].size() - 2;
					content += capture2string(i);

					i = trajectories[8].size() - 1;
					String content2 = "";
					content2 += capture2string(i);
					content = content + content2 + content2;
					classe = stateLabels[i] + Integer.parseInt(name) * 3;
					content += Integer.toString(classe);
					content += "\n";
					bw.write(content);
				}
			}

			if (context == 4)
			{
				// first line: t0:t0:t0:t1:t2
				{
					content = capture2string(0);
					content = content + content + content;
					content += capture2string(1);
					content += capture2string(2);
					content += Integer.toString(stateLabels[0] + Integer.parseInt(name) * 3);
					content += "\n";
					bw.write(content);
				}
				// end of print 1st line

				// 2nd line: t0:t1:t1:t2:t3
				{
					content = "";
					content = capture2string(0);
					content += capture2string(1) + capture2string(1);
					content += capture2string(2);
					content += capture2string(3);
					content += Integer.toString(stateLabels[1] + Integer.parseInt(name) * 3);
					content += "\n";
					bw.write(content);
				}
				// end of print 2nd line

				// line i: t-2:t-1:t:t+1:t+2
				for (int i = 2; i < trajectories[8].size() - 2; i++)
				{
					content = "";
					content += capture2string(i - 2);// t-2
					content += capture2string(i - 1);// t-1
					content += capture2string(i);// t
					content += capture2string(i + 1);// t+1
					content += capture2string(i + 1);// t+2
					content += Integer.toString(stateLabels[i] + Integer.parseInt(name) * 3);
					content += "\n";
					bw.write(content);
				}

				// penultimate line: tN-4:tN-3:tN-2:tN-1:tN-1
				{
					content = "";
					int i = trajectories[8].size() - 2;
					content += capture2string(i - 2);
					content += capture2string(i - 1);
					content += capture2string(i) + capture2string(i) + capture2string(i + 1);
					content += Integer.toString(stateLabels[i] + Integer.parseInt(name) * 3);
					content += "\n";
					bw.write(content);
				}

				// last line: tN-3:tN-2:tN-1:tN-1:tN-1
				{
					content = "";
					int i = trajectories[8].size() - 1;
					content += capture2string(i - 2);
					content += capture2string(i - 1);
					content += capture2string(i) + capture2string(i) + capture2string(i);
					content += Integer.toString(stateLabels[i] + Integer.parseInt(name) * 3);
					content += "\n";
					bw.write(content);
				}
			}
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeToArffContextDerivativeHMMStates(String filename, int[] stateLabels, Matrix hands)
	{

		// sub zero values by unknown values in arff:
		// sed 's/,0.0,,?,/g'

		try
		{
			boolean writeHeader = false;
			boolean useDelta = false;
			boolean useStates = false;
			boolean useHands = true;

			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
				writeHeader = true;
			}

			// append mode with 2nd arg as true
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			int context = 1; // {1} : 1 veut dire context1: t-1:t:t+1;

			if (writeHeader)
			{
				bw.write("@RELATION ContextDerivativeHMMStates\n");

				for (int i = 0; i <= context + 1; i++)
				{

					bw.write("@ATTRIBUTE  RIGHT_ELBOW_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_z_" + i + " NUMERIC\n");
					if (useDelta)
					{
						bw.write("@ATTRIBUTE  RIGHT_ELBOW_dx_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  RIGHT_ELBOW_dy_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  RIGHT_ELBOW_dz_" + i + " NUMERIC\n");
					}
					bw.write("@ATTRIBUTE  RIGHT_HAND_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_z_" + i + " NUMERIC\n");

					if (useDelta)
					{
						bw.write("@ATTRIBUTE  RIGHT_HAND_dy_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  RIGHT_HAND_dz_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  RIGHT_HAND_dx_" + i + " NUMERIC\n");
					}

					bw.write("@ATTRIBUTE  RIGHT_WRIST_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_WRIST_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_WRIST_z_" + i + " NUMERIC\n");
					if (useDelta)
					{
						bw.write("@ATTRIBUTE  RIGHT_WRIST_dx_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  RIGHT_WRIST_dy_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  RIGHT_WRIST_dz_" + i + " NUMERIC\n");
					}

					bw.write("@ATTRIBUTE  LEFT_ELBOW_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_ELBOW_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_ELBOW_z_" + i + " NUMERIC\n");
					if (useDelta)
					{
						bw.write("@ATTRIBUTE  LEFT_ELBOW_dx_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  LEFT_ELBOW_dy_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  LEFT_ELBOW_dz_" + i + " NUMERIC\n");
					}
					bw.write("@ATTRIBUTE  LEFT_HAND_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_z_" + i + " NUMERIC\n");
					if (useDelta)
					{
						bw.write("@ATTRIBUTE  LEFT_HAND_dx_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  LEFT_HAND_dy_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  LEFT_HAND_dz_" + i + " NUMERIC\n");
					}

					bw.write("@ATTRIBUTE  LEFT_WRIST_x_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_WRIST_y_" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_WRIST_z_" + i + " NUMERIC\n");
					if (useDelta)
					{
						bw.write("@ATTRIBUTE  LEFT_WRIST_dx_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  LEFT_WRIST_dy_" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  LEFT_WRIST_dz_" + i + " NUMERIC\n");
					}
					if (useHands)
					{
						bw.write("@ATTRIBUTE  AREA_BLOB_LEFT" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  AREA_BLOB_RIGHT" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  BARY_X_LEFT" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  BARY_Y_LEFT" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  BARY_X_RIGHT" + i + " NUMERIC\n");
						bw.write("@ATTRIBUTE  BARY_Y_RIGHT" + i + " NUMERIC\n");
					}

				}
				if (useStates)
					// 3 states per gesture but 20 is 1-state:
					bw.write("@ATTRIBUTE class {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60}\n");
				else
					bw.write("@ATTRIBUTE class {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}\n");

				bw.write("\n");
				bw.write("@DATA\n");
			}

			String content = "";
			int classe = 0;
			Matrix M = null;

			if (useDelta)
				M = this.toMatrixWithContextAndDerivative(context, ChalearnRecognizer.meaningfulJoints);
			else
				M = this.toMatrixWithContext(context, hands, ChalearnRecognizer.meaningfulJoints);

			int n = M.rows();
			int m = M.cols();

			for (int i = 0; i < n; i++)
			{
				content = "";
				for (int j = 0; j < m; j++)
					content += Double.toString(M.get(i, j)) + ",";
				if (useStates)
					content += Integer.toString(stateLabels[i] + 3 * Integer.parseInt(this.name));
				else
					content += Integer.toString(Integer.parseInt(this.name));

				// System.out.print(Integer.toString(stateLabels[i]));
				content += "\n";
				bw.write(content);
			}
			// System.out.println(" ");

			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeToArffWithStateLabels(String filename, int[] stateLabels)
	{

		try
		{
			boolean writeHeader = false;

			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
				writeHeader = true;
			}

			// append mode with 2nd arg as true
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			if (writeHeader)
			{
				bw.write("@RELATION arms\n");
				bw.write("@ATTRIBUTE  LEFT_ELBOW_x NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_ELBOW_y NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_ELBOW_z NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_HAND_x NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_HAND_y NUMERIC\n");
				bw.write("@ATTRIBUTE  LEFT_HAND_z NUMERIC\n");

				bw.write("@ATTRIBUTE  RIGHT_ELBOW_x NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_ELBOW_y NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_ELBOW_z NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_HAND_x NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_HAND_y NUMERIC\n");
				bw.write("@ATTRIBUTE  RIGHT_HAND_z NUMERIC\n");

				// bw.write("@ATTRIBUTE class NUMERIC\n");
				bw.write("@ATTRIBUTE class {0,1,2}\n");
				bw.write("\n");
				bw.write("@DATA\n");
			}

			String content = "";
			for (int i = 0; i < trajectories[8].size(); i++)
			{

				for (Joint j : Joint.leftArm())
				{
					if (j.isDetected() && j != Joint.LEFT_SHOULDER)
						content += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
				}

				for (Joint j : Joint.rightArm())
				{
					if (j.isDetected() && j != Joint.RIGHT_SHOULDER)
						content += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
				}
				content += stateLabels[i];
				content += "\n";
				bw.write(content);
				content = "";
			}
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void writeToArffOneVectorPerGesture(String filename)
	{

		try
		{
			boolean writeHeader = false;

			File file = new File(filename);
			if (!file.exists())
			{
				file.createNewFile();
				writeHeader = true;
			}

			// append mode with 2nd arg as true
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);

			if (writeHeader)
			{
				bw.write("@RELATION arms\n");

				for (int i = 0; i < 32; i++)
				{
					bw.write("@ATTRIBUTE  LEFT_ELBOW_x" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_ELBOW_y" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_ELBOW_z" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_x" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_y" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  LEFT_HAND_z" + i + " NUMERIC\n");

					bw.write("@ATTRIBUTE  RIGHT_ELBOW_x" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_y" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_ELBOW_z" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_x" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_y" + i + " NUMERIC\n");
					bw.write("@ATTRIBUTE  RIGHT_HAND_z" + i + " NUMERIC\n");
				}
				// bw.write("@ATTRIBUTE class NUMERIC\n");
				bw.write("@ATTRIBUTE class {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}\n");
				bw.write("\n");
				bw.write("@DATA\n");
			}

			String content = "";
			for (int i = 0; i < trajectories[8].size(); i++)
			{

				for (Joint j : Joint.leftArm())
				{
					if (j.isDetected() && j != Joint.LEFT_SHOULDER)
						content += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
				}

				for (Joint j : Joint.rightArm())
				{
					if (j.isDetected() && j != Joint.RIGHT_SHOULDER)
						content += trajectories[j.ordinal()].get(i).x + "," + trajectories[j.ordinal()].get(i).y + "," + trajectories[j.ordinal()].get(i).z + ",";
				}

			}

			content += name;
			content += "\n";
			bw.write(content);
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static double shoulderAngle(Point leftShoulder, Point rightShoulder)
	{
		return Math.atan2(rightShoulder.z - leftShoulder.z, rightShoulder.x - leftShoulder.x);
	}

	private static String pathToName(String path)
	{
		String[] res = path.split("(/)|(\\.)");
		path = res[res.length - 2];
		// res = path.split("_");
		return path;
	}

	public enum TranslationMethod
	{
		MIN, CENTROID, SHOULDERS
	};

	public enum ResamplingMethod
	{
		DISTANCE, TIME, GLOBAL_DISTANCE
	};

	public enum RotationMethod
	{
		NONE, SHOULDERS
	};

	public enum ScalingMethod
	{
		FULL_BODY, ARMS
	};

	public enum ScoreMethod
	{
		FULL_BODY, ARMS
	};
}
