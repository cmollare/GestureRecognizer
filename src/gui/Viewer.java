package gui;
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
	private boolean shouldRun = true;
	private JFrame frame;
	private View view;

	public Viewer(View view)
	{
		this.view = view;
		frame = new JFrame("OpenNI Viewer");
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		frame.add("Center", this);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);

		frame.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent arg0)
			{
			}

			@Override
			public void keyReleased(KeyEvent arg0)
			{
			}

			@Override
			public void keyPressed(KeyEvent arg0)
			{
				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					shouldRun = false;
				}
			}
		});
	}

	public void run()
	{
		while (shouldRun)
		{
			view.update();
			this.repaint();
		}
		frame.dispose();
	}
	
	public void update()
	{
		view.update();
		this.repaint();
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
