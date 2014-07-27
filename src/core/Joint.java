package core;

public enum Joint
{	
	HEAD, //4.Head
	NECK, //3.ShoulderCenter
	TORSO,//2.Spine
	WAIST, //1.HipCenter
	LEFT_COLLAR,
	LEFT_SHOULDER,// 5.ShoulderLeft
	LEFT_ELBOW,//6.ElbowLeft
	LEFT_WRIST,//7.WristLeft
	LEFT_HAND,//8.HandLeft
	LEFT_FINGERTIP,
	RIGHT_COLLAR,
	RIGHT_SHOULDER,//9.ShoulderRight
	RIGHT_ELBOW,//10.ElbowRight
	RIGHT_WRIST,//11.WristRight
	RIGHT_HAND,//12.HandRight,
	RIGHT_FINGERTIP,
	LEFT_HIP,//13.HipLeft
	LEFT_KNEE,//14.KneeLeft
	LEFT_ANKLE,//15.AnkleLeft
	LEFT_FOOT,// 16.FootLeft
	RIGHT_HIP,// 17.HipRight
	RIGHT_KNEE,// 18.KneeRight
	RIGHT_ANKLE,// 19.AnkleRight
	RIGHT_FOOT;// 20.FootRight
	
	
	public double weight()
	{
		switch(this)
		{
		case HEAD:
		case LEFT_ELBOW:
		case LEFT_HAND:
		case LEFT_SHOULDER:
		case NECK:
		case RIGHT_ELBOW:
		case RIGHT_HAND:
		case RIGHT_SHOULDER:
		case TORSO:
			return 1;
		case LEFT_HIP:
		case LEFT_KNEE:
		case RIGHT_HIP:
		case LEFT_FOOT:
		case RIGHT_FOOT:
		case RIGHT_KNEE:
		case WAIST:
		case LEFT_COLLAR:
		case RIGHT_COLLAR:
		case RIGHT_ANKLE:
		case LEFT_ANKLE:
		case RIGHT_FINGERTIP:
		case LEFT_FINGERTIP:
		case LEFT_WRIST:
		case RIGHT_WRIST:
		default:
			return 0;
		}
	}
	
	public boolean isDetected()
	{
		return isDetected(defaultConfiguration);
	}
	
	public boolean isDetected(JointConfiguration c)
	{
		switch(c)
		{
		case NITE:
			switch(this)
			{
			case HEAD:
			case LEFT_ELBOW:
			case LEFT_FOOT:
			case LEFT_HAND:
			case LEFT_HIP:
			case LEFT_KNEE:
			case LEFT_SHOULDER:
			case NECK:
			case RIGHT_ELBOW:
			case RIGHT_FOOT:
			case RIGHT_HAND:
			case RIGHT_HIP:
			case RIGHT_KNEE:
			case RIGHT_SHOULDER:
			case TORSO:
				return true;
			case WAIST:
			case LEFT_COLLAR:
			case RIGHT_COLLAR:
			case RIGHT_ANKLE:
			case LEFT_ANKLE:
			case RIGHT_FINGERTIP:
			case LEFT_FINGERTIP:
			case LEFT_WRIST:
			case RIGHT_WRIST:
			default:
				return false;
			}
		case CHALEARN:
		default:
			switch(this)
			{
			case HEAD:
			case LEFT_ELBOW:
			case LEFT_FOOT:
			case LEFT_HAND:
			case LEFT_HIP:
			case LEFT_KNEE:
			case LEFT_SHOULDER:
			case LEFT_ANKLE:
			case NECK:
			case RIGHT_ELBOW:
			case RIGHT_FOOT:
			case RIGHT_HAND:
			case RIGHT_HIP:
			case RIGHT_KNEE:
			case RIGHT_SHOULDER:
			case RIGHT_ANKLE:
			case TORSO:
			case LEFT_WRIST:
			case RIGHT_WRIST:
			case WAIST:
				return true;
			case LEFT_COLLAR:
			case RIGHT_COLLAR:
			case RIGHT_FINGERTIP:
			case LEFT_FINGERTIP:
			default:
				return false;
			}
		}
	}
	
	private static Joint[][] links = new Joint[][]{
		{HEAD, NECK},
		{NECK, TORSO},
		{NECK, LEFT_SHOULDER},
		{NECK, RIGHT_SHOULDER},
		{LEFT_SHOULDER, LEFT_ELBOW},
		{LEFT_ELBOW, LEFT_HAND},
		{RIGHT_SHOULDER, RIGHT_ELBOW},
		{RIGHT_ELBOW, RIGHT_HAND},
		{TORSO, LEFT_HIP},
		{LEFT_HIP, LEFT_KNEE},
		{LEFT_KNEE, LEFT_FOOT},
		{TORSO, RIGHT_HIP},
		{RIGHT_HIP, RIGHT_KNEE},
		{RIGHT_KNEE, RIGHT_FOOT}
};
	
	private static Joint[] leftArm = new Joint[]{LEFT_SHOULDER, LEFT_ELBOW, LEFT_HAND, LEFT_WRIST};
	private static Joint[] rightArm = new Joint[]{RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_HAND, RIGHT_WRIST};
	private static Joint[] arms = new Joint[]{LEFT_SHOULDER, LEFT_ELBOW, LEFT_HAND, LEFT_WRIST, RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_HAND, RIGHT_WRIST};

	public static Joint[] leftArm()
	{
		return leftArm;
	}
	
	public static Joint[] rightArm()
	{
		return rightArm;
	}
	
	public static Joint[] arms()
	{
		return arms;
	}
	
	public static Joint[][] links()
	{
		return links;
	}
	
	public static final int effectiveJointCount = 9; 
	public static final JointConfiguration defaultConfiguration = JointConfiguration.CHALEARN;
	
	public enum JointConfiguration
	{
		NITE, CHALEARN;
	}
}
