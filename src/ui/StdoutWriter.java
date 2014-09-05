package ui;

import core.GestureLabel;

public class StdoutWriter implements OutputWriter
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
}
