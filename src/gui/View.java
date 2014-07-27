package gui;
import java.awt.Dimension;
import java.awt.Graphics;


public interface View
{
	public Dimension getPreferredSize();
	public void paint(Graphics g);
	public void update();
}
