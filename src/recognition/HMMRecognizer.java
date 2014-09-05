package recognition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import core.Gesture;
import core.Joint;
import learning.HMM;
import learning.HMM.ViterbiResult;
import learning.Matrix;

public class HMMRecognizer extends Recognizer
{
	private HMM[] hmm;

	@Override
	public void learn(HashMap<String, List<Gesture>> gestures)
	{
		labels = (String[]) gestures.keySet().toArray();
		
		List<List<Matrix>> gestureList = new ArrayList<List<Matrix>>();
		for (String s : labels)
		{
			List<Matrix> matrices = new ArrayList<Matrix>();
			gestureList.add(matrices);
			
			for (Gesture g : gestures.get(s))
				matrices.add(g.toMatrix());
		}
		
		learn(gestureList);
	}

	@Override
	public HashMap<String, Double> recognize(Gesture g)
	{
		Matrix gm = g.toMatrix(Joint.arms());
		double[] scores = recognize(gm);
		HashMap<String, Double> scoreMap = new HashMap<String, Double>();
		int i = 0;
		for (String l : labels)
		{
			scoreMap.put(l, scores[i]);
			i++;
		}
		return null;
	}

	public void learn(List<List<Matrix>> set)
	{
		int classCount = set.size();

		hmm = new HMM[classCount];
		for (int i = 0; i < classCount; i++)
		{
			List<Matrix> classGestures = set.get(i);
			List<int[]> classLabels = new ArrayList<int[]>();
			for (Matrix m : classGestures)
				classLabels.add(HMM.generateStateLabels(m.rows(), false, 1, 1, 1));
			
			hmm[i] = new HMM(classGestures, classLabels);
		}

		boolean train_again = false;
		int it = 0;
		double score_acc = 0.0;
		double score_prev = 0.0;
		double ecart = 10;

		while (train_again)
		{
			it++;
			System.out.println("Training it: " + it);

			int nb_g = 0;

			List<List<int[]>> newLLabels = new ArrayList<List<int[]>>();

			for (int i = 0; i < classCount; i++)
			{
				List<Matrix> allGestures = set.get(i);
				Iterator<Matrix> iterg = allGestures.iterator();

				List<int[]> setLabels = new ArrayList<int[]>();

				while (iterg.hasNext())
				{
					Matrix m = iterg.next();

					// if(gLabel == 20){
					// setLabels.add(HMM.generateStateLabels(false, 1));
					// continue;
					// }

					// ViterbiResult vr =
					// hmm[gLabel].viterbi(g.toMatrix(Joint.RIGHT_ELBOW,
					// Joint.RIGHT_HAND, Joint.LEFT_ELBOW, Joint.LEFT_HAND ));

					// System.out.print("g: " + gLabel);

					// else if (g.isLeftHanded())
					// m = g.toMatrix(meaningfulJointsLeft);
					// else
					// m = g.toMatrix(meaningfulJointsRight);

					ViterbiResult vr = hmm[i].viterbiAlign(m, i);

					boolean ok = false;

					if (i == 20)
						ok = true;
					else
						for (int r = 0; r < vr.path.length; r++)
							ok = ok || vr.path[r] != 0;
					// ok = ok || vr.path[r-1]!=vr.path[r];

					// System.out.print("g: " + gLabel);
					// System.out.print(" score: " + vr.score + " " + ok + " ");
					// System.out.println(Arrays.toString(vr.path));
					if (ok)
					{

						// setLabels.add(HMM.generateStateLabels(false, 1,1,1));
						setLabels.add(vr.path);

						score_acc += vr.score;
						nb_g++;
						// p++;
						// g.writeToArffWithStateLabels("ChaLearn2014/arff/stateLabels/3states/TRAIN_"
						// + g.name + "_1_to_100.arff", vr.path);
					}
					else
					{
						System.out.println("REMOVED: " + i + " vit: " + Arrays.toString(vr.path));
						iterg.remove();
					}
				}

				newLLabels.add(setLabels);

			}

			score_acc /= nb_g;
			if (it > 1)
			{
				ecart = 100 * (Math.abs(score_acc) - Math.abs(score_prev)) / Math.abs(score_prev);
				ecart = Math.abs(ecart);
			}
			System.out.println("score_acc=" + score_acc + " score_prev=" + score_prev + " ECART = " + ecart);

			score_prev = score_acc;

			if (ecart < 0.001)
				train_again = false;

			for (int i = 0; i < classCount; i++)
			{			
				List<Matrix> classGestures = set.get(i);
				List<int[]> classLabels = newLLabels.get(i);
				
				hmm[i] = new HMM(classGestures, classLabels);
			}
		}
	}

	public double[] recognize(Matrix m)
	{
		double scores[] = new double[hmm.length];
		for (int i = 0; i < hmm.length; i++)
		{
			scores[i] = hmm[i].viterbi(m).score;
		}

		return scores;
	}

	public ViterbiResult[] forward(Matrix m)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];

		for (int i = 0; i < hmm.length; i++)
		{
			scores[i] = hmm[i].forward(m);
		}

//		for (int i = 0; i < hmm.length; i++)
//			scores[i] = hmm[i].forward(g.toMatrix(meaningfulJoints));

		return scores;
	}

	public ViterbiResult[] recognizeMLP(Matrix[] MLPpost)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];
		for (int i = 0; i < hmm.length; i++)
			scores[i] = hmm[i].viterbiMLP(MLPpost, i);

		return scores;
	}

	public ViterbiResult[] forwardMLP(Matrix[] MLPpost)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];
		for (int i = 0; i < hmm.length; i++)
			scores[i] = hmm[i].forwardMLP(MLPpost, i);

		return scores;
	}

	public ViterbiResult[] forwardMLPLog(Matrix[] MLPpost)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];
		for (int i = 0; i < hmm.length; i++)
			scores[i] = hmm[i].forwardMLPLog(MLPpost, i);

		return scores;
	}
}
