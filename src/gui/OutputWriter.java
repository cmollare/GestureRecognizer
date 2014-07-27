package gui;

import recognition.Word;
import core.*;

public interface OutputWriter
{
	public void write(GestureLabel label);
	public void write(Word w);
}
