package core;

import java.io.Serializable;

import org.OpenNI.SkeletonJoint;

public enum Joint implements Serializable
{	
	HEAD,
	NECK,
	TORSO,
	WAIST,
	LEFT_COLLAR,
	LEFT_SHOULDER,
	LEFT_ELBOW,
	LEFT_WRIST,
	LEFT_HAND,
	LEFT_FINGERTIP,
	RIGHT_COLLAR,
	RIGHT_SHOULDER,
	RIGHT_ELBOW,
	RIGHT_WRIST,
	RIGHT_HAND,
	RIGHT_FINGERTIP,
	LEFT_HIP,
	LEFT_KNEE,
	LEFT_ANKLE,
	LEFT_FOOT,
	RIGHT_HIP,
	RIGHT_KNEE,
	RIGHT_ANKLE,
	RIGHT_FOOT;
	
	public SkeletonJoint openni()
	{
		switch(this)
		{
		case HEAD:
			return SkeletonJoint.HEAD;
		case LEFT_ANKLE:
			return SkeletonJoint.LEFT_ANKLE;
		case LEFT_COLLAR:
			return SkeletonJoint.LEFT_COLLAR;
		case LEFT_ELBOW:
			return SkeletonJoint.LEFT_ELBOW;
		case LEFT_FINGERTIP:
			return SkeletonJoint.LEFT_FINGER_TIP;
		case LEFT_FOOT:
			return SkeletonJoint.LEFT_FOOT;
		case LEFT_HAND:
			return SkeletonJoint.LEFT_HAND;
		case LEFT_HIP:
			return SkeletonJoint.LEFT_HIP;
		case LEFT_KNEE:
			return SkeletonJoint.LEFT_KNEE;
		case LEFT_SHOULDER:
			return SkeletonJoint.LEFT_SHOULDER;
		case LEFT_WRIST:
			return SkeletonJoint.LEFT_WRIST;
		case NECK:
			return SkeletonJoint.NECK;
		case RIGHT_ANKLE:
			return SkeletonJoint.RIGHT_ANKLE;
		case RIGHT_COLLAR:
			return SkeletonJoint.RIGHT_COLLAR;
		case RIGHT_ELBOW:
			return SkeletonJoint.RIGHT_ELBOW;
		case RIGHT_FINGERTIP:
			return SkeletonJoint.RIGHT_FINGER_TIP;
		case RIGHT_FOOT:
			return SkeletonJoint.RIGHT_FOOT;
		case RIGHT_HAND:
			return SkeletonJoint.RIGHT_HAND;
		case RIGHT_HIP:
			return SkeletonJoint.RIGHT_HIP;
		case RIGHT_KNEE:
			return SkeletonJoint.RIGHT_KNEE;
		case RIGHT_SHOULDER:
			return SkeletonJoint.RIGHT_SHOULDER;
		case RIGHT_WRIST:
			return SkeletonJoint.RIGHT_WRIST;
		case TORSO:
			return SkeletonJoint.TORSO;
		case WAIST:
			return SkeletonJoint.WAIST;
		default:
			break;
		}
		
		return null;
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
	
	private static Joint[] leftArm = new Joint[]{LEFT_SHOULDER, LEFT_ELBOW, LEFT_HAND};
	private static Joint[] rightArm = new Joint[]{RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_HAND};
	private static Joint[] arms = new Joint[]{LEFT_SHOULDER, LEFT_ELBOW, LEFT_HAND, RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_HAND};
	private static Joint[] niteJoints = new Joint[]{HEAD, NECK, TORSO, LEFT_SHOULDER, LEFT_ELBOW, LEFT_HAND, RIGHT_SHOULDER, RIGHT_ELBOW, RIGHT_HAND, LEFT_HIP, LEFT_KNEE, LEFT_FOOT, RIGHT_HIP, RIGHT_KNEE, RIGHT_FOOT};
	
	public static Joint[] niteJoints()
	{
		return niteJoints;
	}
	
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
}
