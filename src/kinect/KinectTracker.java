package kinect;
import gui.View;
import gui.Viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openni.*;


import com.primesense.NITE.*;

import core.Capture;
import core.Gesture;
import core.Joint;
import core.Point;

public class KinectTracker implements View
{
	private int width, height;
	private Context context;
	private float[] histogram;
	private byte[] imgbytes;
	private ImageGenerator imageGen;
	private DepthGenerator depthGen;
	private UserGenerator userGen;
	private SkeletonCapability skelCap;
	private volatile int userID;
	private volatile BufferedImage image;
	private ConcurrentLinkedQueue<Capture> queue;
	private volatile int frameCount;
	private volatile Capture lastCapture;

	public KinectTracker()
	{
		this(null);
	}

	public KinectTracker(String filename)
	{
		queue = new ConcurrentLinkedQueue<Capture>();
		frameCount = 0;

		try
		{
			context = new Context();
			if (filename != null)
				context.openFileRecordingEx(filename);

			License licence = new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=");
			context.addLicense(licence);

			imageGen = ImageGenerator.create(context);
			depthGen = DepthGenerator.create(context);
			// depthGen.getAlternativeViewpointCapability().setViewpoint(imageGen);

			ImageMetaData imageMD = imageGen.getMetaData();
			histogram = new float[10000];
			width = imageMD.getFullXRes();
			height = imageMD.getFullYRes();
			imgbytes = new byte[width * height];
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

			userGen = UserGenerator.create(context);
			userGen.getNewUserEvent().addObserver(new IObserver<UserEventArgs>()
			{
				@Override
				public void update(IObservable<UserEventArgs> observable, UserEventArgs args)
				{
					//System.out.println("New user: " + args.getId());
					userID = args.getId();
					try
					{
						skelCap.startTracking(userID);
					}
					catch (StatusException e)
					{
						e.printStackTrace();
					}
				}
			});
			userGen.getLostUserEvent().addObserver(new IObserver<UserEventArgs>()
			{
				@Override
				public void update(IObservable<UserEventArgs> observable, UserEventArgs args)
				{
					//System.out.println("Lost user: " + args.getId());
					lastCapture = null;
				}
			});

			skelCap = userGen.getSkeletonCapability();
			skelCap.setSkeletonProfile(SkeletonProfile.ALL);
			context.startGeneratingAll();
			updateAllMaps();
		}
		catch (GeneralException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void updateAllMaps()
	{
		try
		{
			context.waitAndUpdateAll();
		}
		catch (StatusException e)
		{
			e.printStackTrace();
		}
		int timestamp = 0;
		if (skelCap.isSkeletonTracking(userID))
		{
			Point[] positions = new Point[Capture.jointCount];
			for (SkeletonJoint joint : SkeletonJoint.values())
			{
				try
				{
					if (skelCap.isJointAvailable(joint) && skelCap.isJointActive(joint))
					{
						SkeletonJointPosition jointPos = skelCap.getSkeletonJointPosition(userID, joint);
						Point3D p = jointPos.getPosition();
						positions[joint.ordinal()] = new Point(p.getX(), p.getY(), p.getZ());
						// System.out.println("Acquired position of joint: " +
						// joint.name());
					}
					else
					{
						positions[joint.ordinal()] = new Point(0, 0, 0);
					}
				}
				catch (StatusException e)
				{
					System.err.println("Cannot read position for joint: " + joint.name() + " of user: " + userID);
					e.printStackTrace();
				}
			}
			lastCapture = new Capture(timestamp, positions);
			queue.add(lastCapture);
			frameCount++;
		}
		else
		{
			// System.out.println("User not tracked");
		}
	}

	public Capture getCapture()
	{
		return queue.poll();
	}
	
	public boolean isEmpty()
	{
		return queue.isEmpty();
	}

	private void updateImage()
	{
		calcHist();
		ShortBuffer depth = depthGen.getMetaData().getData().createShortBuffer();
		depth.rewind();

		while (depth.remaining() > 0)
		{
			int pos = depth.position();
			short pixel = depth.get();
			imgbytes[pos] = (byte) histogram[pixel];
		}

		DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width * height);
		Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
		image.setData(raster);
	}

	private void calcHist()
	{
		DepthMetaData depthMD = depthGen.getMetaData();
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
	public Dimension getPreferredSize()
	{
		return new Dimension(width, height);
	}

	@Override
	public void paint(Graphics g)
	{
		updateImage();
		if (image != null)
			g.drawImage(image, 0, 0, null);

		if (lastCapture != null)
		{
			for (int i = 0; i < lastCapture.points.length; i++)
			{
				Point p = lastCapture.points[i];
				if (!p.isZero())
				{
					try
					{
						Point3D res = depthGen.convertRealWorldToProjective(new Point3D((float) p.x, (float) p.y, (float) p.z));
						g.setColor(Color.RED);
						g.fillOval((int) res.getX() - 3, (int) res.getY() - 3, 6, 6);
					}
					catch (StatusException e)
					{
						e.printStackTrace();
					}
				}
			}

			for (Joint[] link : Joint.links())
			{
				int a = link[1].ordinal();
				int b = link[0].ordinal();
				if (!lastCapture.points[a].isZero() && !lastCapture.points[b].isZero())
				{
					try
					{
						Point3D a_res = depthGen.convertRealWorldToProjective(new Point3D((float) lastCapture.points[a].x, (float) lastCapture.points[a].y, (float) lastCapture.points[a].z));
						Point3D b_res = depthGen.convertRealWorldToProjective(new Point3D((float) lastCapture.points[b].x, (float) lastCapture.points[b].y, (float) lastCapture.points[b].z));
						g.setColor(Color.BLUE);
						g.drawLine((int) a_res.getX(), (int) a_res.getY(), (int) b_res.getX(), (int) b_res.getY());
					}
					catch (StatusException e)
					{
						e.printStackTrace();
					}
				}
			}

			Point lShoulder = lastCapture.points[Joint.LEFT_SHOULDER.ordinal()];
			Point rShoulder = lastCapture.points[Joint.RIGHT_SHOULDER.ordinal()];
			double theta = Gesture.shoulderAngle(lShoulder, rShoulder);
			String thetaStr = Double.toString(theta).substring(0, 5);
			//g.drawString("Frame: " + frameCount, 12, 22);
			//System.out.println(thetaStr);
			//g.drawString("Theta: " + thetaStr, 12, 12);
		}
	}

	@Override
	public void update()
	{
		updateAllMaps();
	}

	public static void main(String[] args)
	{
		Viewer v = new Viewer(new KinectTracker("/home/thomas/corpora/soundpainting/corpus_pilote_07122013/p1.oni"));
		v.run();
	}
}
