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
	public String name;
	
	public GestureLabel(int s, int e, String n)
	{
		start = s;
		end = e;
		name = n;
	}
	
	public GestureLabel(String s)
	{
		String[] str = s.split(",");
		name = str[0];
		start = Integer.parseInt(str[1]) - 1;
		end = Integer.parseInt(str[2]) - 1;
	}
	
	public static GestureLabel intersect(GestureLabel a, GestureLabel b)
	{
		if (!a.name.equals(b.name) || a.end < b.start || a.start > b.end)
			return null;
		return new GestureLabel(Math.max(a.start, b.start), Math.min(a.end, b.end), a.name);
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
		System.out.println("[ " + start + " - " + end + " ] : " + name);
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
								bw.write(l.name);
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
				System.out.print(l.name);
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
				bw.write((label.name) + "," + (label.start + 1) + "," + (label.end + 1) + "\n");

			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void filter(List<GestureLabel> labels, String ignoreLabel)
	{
		Iterator<GestureLabel> iter = labels.iterator();
		while(iter.hasNext())
		{
			GestureLabel n = iter.next();
			if (n.name == ignoreLabel)
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
					prev.name = n.name;
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
}
