package ui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

public class Viewer extends Component
{
	private JFrame frame;
	private View view;

	public Viewer(final View view)
	{
		this.view = view;
		frame = new JFrame("Viewer");
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				view.stop();
			}
		});

		frame.add("Center", this);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setFocusableWindowState(false);

//		frame.addKeyListener(new KeyListener()
//		{
//			@Override
//			public void keyTyped(KeyEvent arg0)
//			{
//			}
//
//			@Override
//			public void keyReleased(KeyEvent arg0)
//			{
//			}
//
//			@Override
//			public void keyPressed(KeyEvent arg0)
//			{
//				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
//				{
//					
//				}
//			}
//		});
	}
	
	public void update()
	{
		this.repaint();
	}
	
	public void stop()
	{
		frame.dispose();
	}
	
	public Dimension getPreferredSize()
	{
		return view.getPreferredSize();
	}

	public void paint(Graphics g)
	{
		view.paint(g);
	}
}
