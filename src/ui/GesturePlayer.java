package ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.nio.ShortBuffer;

import org.openni.*;


public class GesturePlayer implements View
{
	private Context ctx;
	private float[] histogram;
	private byte[] imgbytes;
	private DataBufferByte dataBuffer;
	private BufferedImage bimg;
	private DepthGenerator depthGen;
	private int width;
	private int height;

	public GesturePlayer()
	{
		try
		{
			ctx = new Context();
			ctx.openFileRecordingEx("/home/thomas/corpora/soundpainting/corpus_pilote_07122013/p1.oni");
			depthGen = DepthGenerator.create(ctx);
			DepthMetaData depthMD = depthGen.getMetaData();

			histogram = new float[10000];
			width = depthMD.getFullXRes();
			height = depthMD.getFullYRes();

			imgbytes = new byte[width * height];

			dataBuffer = new DataBufferByte(imgbytes, width * height);
			Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
			bimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			bimg.setData(raster);
		}
		catch (GeneralException e)
		{
			e.printStackTrace();
		}
	}

	private void calcHist(DepthMetaData depthMD)
	{
		// reset
		for (int i = 0; i < histogram.length; ++i)
			histogram[i] = 0;

		ShortBuffer depth = depthMD.getData().createShortBuffer();
		depth.rewind();

		int points = 0;
		while (depth.remaining() > 0)
		{
			short depthVal = depth.get();
			if (depthVal != 0)
			{
				histogram[depthVal]++;
				points++;
			}
		}

		for (int i = 1; i < histogram.length; i++)
		{
			histogram[i] += histogram[i - 1];
		}

		if (points > 0)
		{
			for (int i = 1; i < histogram.length; i++)
			{
				histogram[i] = (int) (256 * (1.0f - (histogram[i] / (float) points)));
			}
		}
	}

	@Override
	public void update()
	{
		try
		{
			DepthMetaData depthMD = depthGen.getMetaData();

			ctx.waitAnyUpdateAll();

			calcHist(depthMD);
			ShortBuffer depth = depthMD.getData().createShortBuffer();
			depth.rewind();

			while (depth.remaining() > 0)
			{
				int pos = depth.position();
				short pixel = depth.get();
				imgbytes[pos] = (byte) histogram[pixel];
			}
		}
		catch (GeneralException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(width, height);
	}

	@Override
	public void paint(Graphics g)
	{
		DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width * height);
		Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
		bimg.setData(raster);

		g.drawImage(bimg, 0, 0, null);
	}
	
	public static void main(String[] argv)
	{
		Viewer v = new Viewer(new GesturePlayer());
		v.run();
	}
}
