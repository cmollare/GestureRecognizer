package recognition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import core.Gesture;
import core.GestureLabel;
import core.Joint;
import learning.HMM;
import learning.HMM.ViterbiResult;
import learning.Matrix;

public class HMMRecognizer
{
	private static HMM[] hmm;

	public static void learn(List<List<Matrix>> templates, List<List<int[]>> labels)
	{
		int count = templates.size();

		hmm = new HMM[count];
		for (int i = 0; i < count; i++)
		{
			List<Matrix> classGestures = templates.get(i);
			List<int[]> classLabels = labels.get(i);
			int labelCount = labelCount(classLabels);
			List<Matrix> gestureMatrices = gesturesToLabelMatrix(classGestures, classLabels, labelCount, i);
//			System.out.println("Gesture: " + i);
			hmm[i] = new HMM(gestureMatrices, labels.get(i));
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

			// Alignment ... print to file...

			int nb_g = 0;

			List<List<int[]>> newLLabels = new ArrayList<List<int[]>>();

			for (int i = 0; i < count; i++)
			{
				List<Matrix> allGestures = templates.get(i);
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

			for (int i = 0; i < count; i++)
			{
				List<Matrix> classGestures = templates.get(i);
				List<int[]> classLabels = newLLabels.get(i);

				int labelCount = labelCount(classLabels);
				List<Matrix> gestureMatrices = gesturesToLabelMatrix(classGestures, classLabels, labelCount, i);
				hmm[i] = new HMM(gestureMatrices, classLabels);

				// System.out.println("Gest i: " + i + " labelCount: " +
				// labelCount);
				// hmm[i].print();

			}
		}
	}

	public static ViterbiResult[] recognize(Matrix m)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];
		for (int i = 0; i < hmm.length; i++)
		{
			scores[i] = hmm[i].viterbi(m);
		}

		return scores;
	}

	public static ViterbiResult[] forward(Matrix m)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];

		for (int i = 0; i < hmm.length; i++)
		{
			scores[i] = hmm[i].forward(m);
		}

		// for (int i = 0; i < hmm.length; i++)
		// scores[i] = hmm[i].forward(g.toMatrix(meaningfulJoints));

		return scores;
	}

	public static ViterbiResult[] recognizeMLP(Matrix[] MLPpost)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];
		for (int i = 0; i < hmm.length; i++)
			scores[i] = hmm[i].viterbiMLP(MLPpost, i);

		return scores;
	}

	public static ViterbiResult[] forwardMLP(Matrix[] MLPpost)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];
		for (int i = 0; i < hmm.length; i++)
			scores[i] = hmm[i].forwardMLP(MLPpost, i);

		return scores;
	}

	public static ViterbiResult[] forwardMLPLog(Matrix[] MLPpost)
	{
		ViterbiResult scores[] = new ViterbiResult[hmm.length];
		for (int i = 0; i < hmm.length; i++)
			scores[i] = hmm[i].forwardMLPLog(MLPpost, i);

		return scores;
	}

	private static int labelCount(List<int[]> labels)
	{
		int labelCount = 0;
		for (int[] v : labels)
		{
			for (int a : v)
				labelCount = Math.max(labelCount, a + 1);
		}

		return labelCount;
	}

	private static List<Matrix> gesturesToLabelMatrix(List<Matrix> gestures, List<int[]> labels, int labelCount, int gestureLabel)
	{
		List<List<double[]>> l0 = new ArrayList<List<double[]>>();

		for (int i = 0; i < labelCount; i++)
			l0.add(new ArrayList<double[]>());

		int s = gestures.size();
		for (int i = 0; i < s; i++)
		{
			Matrix m = gestures.get(i);

			int n = m.rows();
			for (int j = 0; j < n; j++)
			{
				// System.out.println("i: " + i + " j: " + j);
				int label = labels.get(i)[j];
				l0.get(label).add(m.getRow(j));
			}
			// // String outname =
			// "ChaLearn2014/arff/TRAIN_1_to_470samples_DELTA.arff";
			// String outname =
			// "ChaLearn2014/arff/VALIDATION_471_to_700_samples_DELTA.arff";
			// // System.out.println(outname);
			// System.out.println("Gest: " + gestures.get(i).name);
			// m.write(outname, gestures.get(i).name);

		}

		List<Matrix> l = new ArrayList<Matrix>();
		for (List<double[]> l2 : l0)
			l.add(new Matrix(l2));

		return l;
	}
}
