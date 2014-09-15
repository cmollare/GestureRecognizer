package ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import core.GestureLabel;

public class FileLabelWriter implements LabelWriter
{
	private BufferedWriter bw;
	
	public FileLabelWriter(String path)
	{
		try
		{
			File f = new File(path);
//			if (f.exists())
//				f.delete();
			bw = new BufferedWriter(new FileWriter(f.getAbsoluteFile()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void write(GestureLabel label)
	{
		try
		{
			bw.write(label.toString() + "\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void write(String s)
	{
		try
		{
			bw.write(s + "\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void close()
	{
		try
		{
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
