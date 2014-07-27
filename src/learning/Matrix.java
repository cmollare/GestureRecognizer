package learning;

import java.awt.image.SampleModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import core.Point;
import core.Trajectory;

public class Matrix
{
	private double[][] t;

	public Matrix(int n, int m)
	{
		t = new double[n][m];
	}
	
	public Matrix(int n, int m, double initValue)
	{
		t = new double[n][m];
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
				t[i][j] = initValue;
		}
	}

	public Matrix(double[][] mat)
	{
		t = mat;
	}
	

	public Matrix(double[] vec)
	{
		t = new double[1][];
		t[0] = vec;
	}

	public Matrix(List<double[]> l)
	{
		int n = l.size();
		int m = l.get(0).length;
		t = new double[n][m];
		
		for (int i = 0; i < n; i++)
			t[i] = l.get(i);
	}
	
	public static Matrix join(List<Matrix> matrices)
	{
		int n = 0;
		int m = matrices.get(0).cols();
		Matrix r = new Matrix(n, m);
		int s = matrices.size();
		
		for (Matrix mat : matrices)
			n += mat.rows();
				
		int ti = 0;
		for (int i = 0; i < s; i++)
		{
			Matrix mat = matrices.get(i);
			for (int j = 0; j < mat.rows(); j++)
			{
				r.set(ti, mat.getRow(j));
				ti++;
			}
		}
		
		return r; 
	}
	
	public static Matrix joinRows(Matrix ... matrices)
	{
		int n = matrices[0].rows();
		int m = 0;
		int s = matrices.length;
		
		for (Matrix mat : matrices)
			m += mat.cols();
				
		Matrix r = new Matrix(n, m);

		for (int i = 0; i < n; i++)
		{
			int tc = 0;
			for (int k = 0; k < s; k++)
			{
				Matrix a = matrices[k];
				int cc = a.cols();
				for (int j = 0; j < cc; j++)
				{
					r.set(i, tc, a.get(i, j));
					tc++;
				}
			}
		}
		
		return r; 
	}

	public double get(int i, int j)
	{
		return t[i][j];
	}

	public void set(int i, int j, double val)
	{
		t[i][j] = val;
	}
	
	public void set(int i, double[] row)
	{
		t[i] = row;
	}

	public int rows()
	{
		return t.length;
	}

	public int cols()
	{
		return t == null ? 0 : t.length == 0 ? 0 : t[0].length;
	}

	public Matrix getRowMatrix(int i)
	{
		return new Matrix(this.t[i].clone());
	}
	
	public double[] getRow(int i)
	{
		return this.t[i].clone();
	}
	
	public Matrix clone()
	{
		int n = this.rows();
		int m = this.cols();
		Matrix r =  new Matrix(n, m);
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
				r.set(i, j, this.get(i, j));
		}
		
		return r;
	}
	
	public Matrix transpose()
	{
		int n = this.rows();
		int m = this.cols();
		Matrix r = new Matrix(m, n);
		
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
				r.set(j, i, this.get(i, j));
		}
		
		return r;
	}
	
	public double det() // LU decomposition
	{
		int n = this.rows();
		Matrix u = this.clone();
		
		for (int i = 0; i < n; i++)
		{
			for (int j = i + 1; j < n; j++)
			{
				double alpha = u.get(j, i) / u.get(i, i);
				for (int k = 0; k < n; k++)
					u.set(j, k, u.get(j, k) - u.get(i, k) * alpha);
			}
		}
		
		double r = 1;
		for (int i = 0; i < n; i++)
			r *= u.get(i, i);
		
		return r;
	}
	
	public Matrix inv() // Gauss-Jordan Elimination
	{
		int n = this.rows();
		int n2 = n * 2;
		
		Matrix r = new Matrix(n, n2);
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < n2; j++)
			{
				if (j < n)
					r.set(i, j, this.get(i, j));
				else if (j - n == i)
					r.set(i, j, 1);
			}
		}
		
		for (int i = 0; i < n; i++)
		{
			double d = r.get(i, i);
			for (int j = 0; j < n2; j++)
				r.set(i, j, r.get(i, j) / d);
			
			for (int k = 0; k < n; k++)
			{
				if (k == i)
					continue;
				
				double alpha = r.get(k, i);
				for (int j = 0; j < n2; j++)
					r.set(k, j, r.get(k, j) - alpha * r.get(i, j));
			}
		}
		
		Matrix r2 = new Matrix(n, n);
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < n; j++)
				r2.set(i, j, r.get(i, j + n));
		}
		
		return r2;
	}
	
	public Matrix mean()
	{
		int n = this.rows();
		int m = this.cols();

		Matrix r = new Matrix(1, m);

		for (int i = 0; i < m; i++)
		{
			double val = 0;
			
			for (int j = 0; j < n; j++)
				val += this.get(j, i);
			
			r.set(0, i, val / n);
		}

		return r;
	}

	public Matrix covariance()
	{
		return this.covariance(this.mean());
	}
	
	public Matrix covariance(Matrix mean)
	{
		int n = this.rows();
		int m = this.cols();

		Matrix cov = new Matrix(m, m);

		for (int i = 0; i < m; i++)
		{
			for (int j = 0; j < m; j++)
			{
				double var = 0;
				
				for (int k = 0; k < n; k++)
					var += (this.get(k, i) - mean.get(0, i)) * (this.get(k, j) - mean.get(0, j));
				
				cov.set(i, j, var / (n-1));
			}
		}
		
		return cov;
	}
	
	public Matrix multiply(Matrix b)
	{
		return Matrix.multiply(this, b);
	}
	
	public Matrix add(Matrix b)
	{
		return Matrix.add(this, b);
	}
	
	public Matrix substract(Matrix b)
	{
		return Matrix.substract(this, b);
	}
	
	public Matrix removeColumns(int ... indexes)
	{
		int n = this.rows();
		int m = this.cols();
		
		Matrix r = new Matrix(n, m - indexes.length);
		int rj = 0;
		int k = 0;
		for (int j = 0; j < m; j++)
		{
			if (k < indexes.length && indexes[k] == j)
			{
				k++;
				continue;
			}
			for (int i = 0; i < n; i++)
				r.set(i, rj, this.get(i, j));
			rj++;
		}
		
		return r;
	}
	
	public Matrix selectColumns(int ... indexes)
	{
		int n = this.rows();
		int m = this.cols();
		
		Matrix r = new Matrix(n, indexes.length);
		for (int j = 0; j < indexes.length; j++)
		{
			int rj = indexes[j];
			for (int i = 0; i < n; i++)
				r.set(i, j, this.get(i, rj));
		}
		
		return r;
	}
	
	public void print()
	{
		int n = this.rows();
		int m = this.cols();
		
		System.out.println();
		for (int i = 0; i < n; i++)
		{
			System.out.print(i + " [ ");
			for (int j = 0; j < m; j++)
			{
				Double.toString(this.get(i, j));
				System.out.print(String.format("%.4f", this.get(i, j)) + " ");
			}
			System.out.println("]");
		}
		// System.out.println();
	}
	
	public void printDimensions()
	{
		System.out.println(this.rows() + " x " + this.cols());
	}

	public void write(String filename, String gestureName)
	{
		int n = this.rows();
		int m = this.cols();
		try
		{

			File file = new File(filename);
			if (!file.exists())
				file.createNewFile();

			// append mode with 2nd arg as true
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			String content = "";
			
			for (int i = 0; i < n; i++)
			{
				for (int j = 0; j < m; j++)
					content += Double.toString(this.get(i, j)) + ",";
				content += gestureName;
				content += "\n";
			}
			// System.out.println();
			bw.write(content);
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static Matrix multiply(Matrix a, Matrix b)
	{
		if (a.cols() != b.rows())
		{
			System.err.println("Matrix.multiply(): Matrix dimensions are not compatible ( A.cols() " + a.cols() + " != b.rows() " + b.rows() + " )");
			return null;
		}

		int n = a.rows();
		int m = b.cols();
		int p = a.cols();

		Matrix r = new Matrix(n, m);

		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
			{
				double val = 0;
				
				for (int k = 0; k < p; k++)
					val += a.get(i, k) * b.get(k, j);
				
				r.set(i, j, val);
			}
		}

		return r;
	}

	public static Matrix add(Matrix a, Matrix b)
	{
		if (a.rows() != b.rows() || a.cols() != b.cols())
		{
			System.err.println("Matrix dimensions are different");
			return null;
		}

		int n = a.rows();
		int m = a.cols();

		Matrix r = new Matrix(n, m);

		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
				r.set(i, j, a.get(i, j) + b.get(i, j));
		}

		return r;
	}

	public static Matrix substract(Matrix a, Matrix b)
	{
		if (a.rows() != b.rows() || a.cols() != b.cols())
		{
			System.err.println("Matrix dimensions are different");
			return null;
		}

		int n = a.rows();
		int m = a.cols();

		Matrix r = new Matrix(n, m);

		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < m; j++)
				r.set(i, j, a.get(i, j) - b.get(i, j));
		}

		return r;
	}


	public Matrix copy(int start, int end) 
	{
		int length = end - start + 1;
		
		int m = this.cols();
		
		Matrix r = new Matrix(length, m);

		for (int i = 0; i < length; i++)
		{
			for (int j = 0; j < m; j++)
				r.set(i, j, this.get(start + i, j));
		}

		return r;
	}

	public Matrix resample(int n) 
	{	
		int s = this.rows();
		int m = this.cols();
		double step = ((double) (s - 1)) / ((double)(n - 1));
		Matrix newMat = new Matrix(n, m);
		newMat.set(0, this.getRow(0));
		double current = step;
		int s2 = 1;
		while (s2 < n - 1)
		{
			int i = (int) Math.floor(current);
			double[] a = this.getRow(i);
			double[] b = this.getRow(i + 1);
			double r = current - Math.floor(current);
			double[] c = new double[m];
			for (int j = 0; j < m; j++)
				c[j] = a[j] + r * (b[j] - a[j]);
			
			newMat.set(s2, c);
			s2++;
			current += step;
		}

		newMat.set(n - 1, this.getRow(s - 1));
		
		return newMat;
	}
	
	public static void main(String[] args)
	{
		double[][] t = new double[][] {{2, 3, 4, 5}, {6, 4, 3, 2}, {7, 1, 2, 5}};
		Matrix m = new Matrix(t);
		m.selectColumns(1).print();
	}
}
