package recognition;
import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class Phrase
{
	private Word fader;
	private Word destination;
	private Word order;

	public Phrase()
	{
	}

	public Phrase(Word destination, Word fader, Word order)
	{
		this.destination = destination;
		this.fader = fader;
		this.order = order;
	}

	public void add(Word w)
	{
		switch (w.type())
		{
		case DESTINATION:
			destination = w;
			break;
		case FADER:
			fader = w;
			break;
		case ORDER:
			order = w;
			break;
		}
	}

	public boolean isValid()
	{
		return destination != null && order != null;
	}

	public void play()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
				    AudioInputStream stream = AudioSystem.getAudioInputStream(new File("res/sound.wav"));
				    AudioFormat format = stream.getFormat();
				    DataLine.Info info = new DataLine.Info(Clip.class, format);
				    Clip clip = (Clip) AudioSystem.getLine(info);
				    clip.open(stream);
				    clip.start();
				}
				catch (Exception e)
				{
					System.err.println(e.getMessage());
				}
			}
		}).start();
	}
	
	public static void main(String[] args)
	{
		Phrase p = new Phrase();
		p.play();
	}
}
