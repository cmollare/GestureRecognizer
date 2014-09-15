package ui;

import core.GestureLabel;

public class StdoutLabelWriter implements LabelWriter
{
	@Override
	public void write(GestureLabel label)
	{
		System.out.println(label);
	}

	@Override
	public void write(String s)
	{
		System.out.println(s);
	}

	@Override
	public void close()
	{
		// nothing to do
	}
}
