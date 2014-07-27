package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GestureLabel
{
	public int start;
	public int end;
	public int id;
	public String name;
	
	public GestureLabel(int s, int e, int i)
	{
		this(s, e, i, "Unknown");
	}
	
	public GestureLabel(int s, int e, int i, String n)
	{
		start = s;
		end = e;
		id = i;
		name = n;
	}
	
	public GestureLabel(String s)
	{
		String[] str = s.split(",");
		start = Integer.parseInt(str[1]) - 1;
		end = Integer.parseInt(str[2]) - 1;
		id = Integer.parseInt(str[0]) - 1;
	}
	
	public static GestureLabel intersect(GestureLabel a, GestureLabel b)
	{
		if (a.id != b.id || a.end < b.start || a.start > b.end)
			return null;
		return new GestureLabel(Math.max(a.start, b.start), Math.min(a.end, b.end), a.id);
	}
	
	public static List<GestureLabel> merge(List<GestureLabel> a, List<GestureLabel> b)
	{
		List<GestureLabel> r = new ArrayList<GestureLabel>();		
		int as = a.size();
		int bs = b.size();
		int ai = 0;
		int bi = 0;
		while(ai < as && bi < bs)
		{
			GestureLabel al = a.get(ai);
			GestureLabel bl = b.get(bi);
			
			GestureLabel inter = GestureLabel.intersect(al, bl);
			if (inter != null)
			{
				r.add(inter);
			}
			
			if (al.end > bl.end)
				bi++;
			else
				ai++;
		}
		
		return r;
	}

	public void print()
	{
		System.out.println("[ " + start + " - " + end + " ] : " + (id + 1));
	}
	
	public static void print(List<GestureLabel> labels)
	{
		for (GestureLabel l : labels)
			l.print();
	}
	
	
	public static void printLinear(List<GestureLabel> ... labels)
	{
		int maxLength = 0;
		for (List<GestureLabel> l : labels)
			maxLength = Math.max(maxLength, l.get(l.size() - 1).end);
		
		for (int i = 0; i < maxLength; i++)
		{
			for (List<GestureLabel> l : labels)
			{
				printFrame(l, i);
				System.out.print("\t");
			}
			System.out.println();
		}
	}

	public static void printLinear2File(String filename, List<GestureLabel> ... labels)
	{
		
		File file = new File(filename);
		try {
			if (!file.exists())
				file.createNewFile();
			
//			System.out.println("Writing linear labels to " + filename);
			
			// append mode with 2nd arg as true
			FileWriter fw;
			try {
				fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);

				int maxLength = 0;
				for (List<GestureLabel> l : labels)
					maxLength = Math.max(maxLength, l.get(l.size() - 1).end);

				for (int i = 0; i < maxLength; i++)
				{

					for (List<GestureLabel> labs : labels)
					{

						for (GestureLabel l : labs)
						{
							if (i >= l.start && i <= l.end)
							{
								bw.write(Integer.toString(l.id + 1));
								break;
							}
							else if (i < l.start)
							{
								bw.write("_");
								break;
							}
						}
						
						bw.write("\t");
					}
					bw.write("\n");
				}
				
				bw.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void printFrame(List<GestureLabel> labels, int frame)
	{
		for (GestureLabel l : labels)
		{
			if (frame >= l.start && frame <= l.end)
			{
				System.out.print(l.id + 1);
				break;
			}
			else if (frame < l.start)
			{
				System.out.print("_");
				break;
			}
		}
	}
	
	public static List<GestureLabel> fromFile(String filename)
	{
		List<GestureLabel> labels = new ArrayList<GestureLabel>();
		
		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(filename));

			while ((line = br.readLine()) != null)
			{
				GestureLabel label = new GestureLabel(line);
				labels.add(label);
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
		return labels;
	}
	
	public static List<GestureLabel> fromStream(InputStream is, List<GestureLabel> labels)
	{		
		BufferedReader br = null;
		try
		{
			String line;
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			while ((line = br.readLine()) != null)
			{
				GestureLabel label = new GestureLabel(line);
				labels.add(label);
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
		return labels;
	}
	
	public static void write(List<GestureLabel> labels, String filename)
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

			for (GestureLabel label : labels)
				bw.write((label.id  + 1) + "," + (label.start + 1) + "," + (label.end + 1) + "\n");

			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void filter(List<GestureLabel> labels, int ignoreLabel)
	{
		Iterator<GestureLabel> iter = labels.iterator();
		while(iter.hasNext())
		{
			GestureLabel n = iter.next();
			if (n.id == ignoreLabel)
				iter.remove();
		}
	}
	
	public static void smooth(List<GestureLabel> labels, int level)
	{
		int minLength = level;
		Iterator<GestureLabel> iter = labels.iterator();
		while(iter.hasNext())
		{
			GestureLabel n = iter.next();
			if (n.end - n.start < minLength)
				iter.remove();
		}
	}
	
	public static void diffusion(List<GestureLabel> labels, int level)
	{
		int s = labels.size();
		for (int i = 0; i < s; i++)
		{
			GestureLabel l = labels.get(i);
			l.start = Math.max(0, l.start - level);
			if (i != s - 1)
				l.end = l.end + level;
		}
		
		GestureLabel prev = labels.get(0);
		int prevl = prev.end - prev.start + 1;
		
		List<GestureLabel> newLabels = new ArrayList<GestureLabel>();
		newLabels.add(prev);
		for (int i = 1; i < s; i++)
		{
			GestureLabel n = labels.get(i);
			int nl = n.end - n.start + 1;
			if (prev.end >= n.start)
			{
				prev.end = n.end;
				if (nl > prevl)
					prev.id = n.id;
			}
			else
			{
				prev = n;
				prevl = nl;
				newLabels.add(n);
			}
		}
		
		labels.clear();
		labels.addAll(newLabels);
	}
	
	public static void main(String[] args)
	{
//		int id = 580;
//		List<GestureLabel> gt = GestureLabel.fromFile("ChaLearn2014/validation/Sample0" + id + "_labels.csv");
//		List<GestureLabel> res = GestureLabel.fromFile("ChaLearn2014/predictions/Sample0" + id + "_prediction.csv");
////		List<GestureLabel> res2 = GestureLabel.fromFile("ChaLearn2014/oldpredictions/Sample0" + id + "_wrist_0.25.csv");
////		List<GestureLabel> res2 = GestureLabel.fromFile("ChaLearn2014/oldpredictions/1.csv");
////		List<GestureLabel> res3 = GestureLabel.fromFile("ChaLearn2014/oldpredictions/2.csv");
//		GestureLabel.printLinear(gt, res);
		
		List<GestureLabel> a = new ArrayList<GestureLabel>();
		List<GestureLabel> b = new ArrayList<GestureLabel>();
		
		a.add(new GestureLabel(0, 10, 0));
		a.add(new GestureLabel(14, 18, 0));
		
		b.add(new GestureLabel(0, 10, 0));
		b.add(new GestureLabel(11, 12, 0));
		b.add(new GestureLabel(14, 14, 0));
		b.add(new GestureLabel(16, 22, 0));
		
		
		List<GestureLabel> r = GestureLabel.merge(a, b);
		GestureLabel.print(r);
	}
}
