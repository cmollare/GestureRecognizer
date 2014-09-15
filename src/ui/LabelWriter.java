package ui;

import core.*;

public interface LabelWriter
{
	public void write(GestureLabel label);
	public void write(String s);
	public void close();
}
