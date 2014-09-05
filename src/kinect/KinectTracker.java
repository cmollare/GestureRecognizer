package kinect;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.nio.ShortBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.openni.*;

import ui.View;
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
	private volatile Capture lastCapture;
	private Joint[] joints;

	public KinectTracker(Joint[] joints, String filename)
	{
		this.joints = joints;
		queue = new ConcurrentLinkedQueue<Capture>();
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
					lastCapture = null;
				}
			});

			skelCap = userGen.getSkeletonCapability();
			skelCap.setSkeletonProfile(SkeletonProfile.ALL);
		}
		catch (GeneralException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void start()
	{
		try
		{
			context.startGeneratingAll();
		}
		catch (StatusException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isDone()
	{
		return false; // TODO
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

		if (skelCap.isSkeletonTracking(userID))
		{
			Capture newCapture = new Capture();
			for (Joint j : joints)
			{
				SkeletonJoint joint = j.openni();
				try
				{
					Point p2;
					if (skelCap.isJointAvailable(joint) && skelCap.isJointActive(joint))
					{
						SkeletonJointPosition jointPos = skelCap.getSkeletonJointPosition(userID, joint);
						Point3D p = jointPos.getPosition();
						p2 = new Point(p.getX(), p.getY(), p.getZ());
					}
					else
						p2 = new Point(0, 0, 0);
					
					newCapture.put(p2, j);
				}
				catch (StatusException e)
				{
					System.err.println("Cannot read position for joint: " + joint.name() + " of user: " + userID);
					e.printStackTrace();
				}
			}
			lastCapture = newCapture;
			queue.add(lastCapture);
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

	public static Gesture extractONIGesture(Joint[] joints, String filename)
	{
		return recordGesture(joints, filename, 1000);
	}
	
	public static Gesture recordGesture(Joint[] joints, String filename, double duration)
	{
		Gesture g = new Gesture();
		
		long startTime = 0;
		long ld = (long) (duration * 1000);
		int lastFrameID = -1;
		KinectTracker kt = new KinectTracker(joints, filename);
		kt.start();
		boolean firstFrame = true;
		boolean run = true;
		
		while(run)
		{
			kt.update();
			long currentTime = kt.depthGen.getTimestamp();
			int currentFrameID = kt.depthGen.getFrameID();
			
			if (firstFrame)
			{
				startTime = currentTime;
				firstFrame = false;
			}
			
			if (lastFrameID < currentFrameID && currentTime - startTime < ld)
			{
				g.addCapture(kt.getCapture());
				lastFrameID = currentFrameID;
			}
			else
				run = false;
		}
		
		return g;
	}
	
	public static void main(String[] arg)
	{
		Gesture g = extractONIGesture(Joint.arms(), "res/p1.oni");
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
			for (Point p : lastCapture)
			{
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
				Point a = lastCapture.get(link[0]);
				Point b = lastCapture.get(link[1]);
				if (!a.isZero() && !b.isZero())
				{
					try
					{
						Point3D a_res = depthGen.convertRealWorldToProjective(new Point3D((float) a.x, (float) a.y, (float) a.z));
						Point3D b_res = depthGen.convertRealWorldToProjective(new Point3D((float) b.x, (float) b.y, (float) b.z));
						g.setColor(Color.BLUE);
						g.drawLine((int) a_res.getX(), (int) a_res.getY(), (int) b_res.getX(), (int) b_res.getY());
					}
					catch (StatusException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void update()
	{
		updateAllMaps();
	}
}
