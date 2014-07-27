package recognition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import learning.HMM;
import learning.HMM.ViterbiResult;
import learning.Matrix;
import core.Gesture;
import core.GestureLabel;
import core.Joint;

public class ChalearnRecognizer extends Thread
{
	private static final int classCount = 21;
	private static boolean useMLP = false;
	private static boolean useForward = true;
	private static boolean useHands = true;
	private static boolean useRotation = false;
	public static final boolean[] use2Arms = new boolean[] { false, false, false, false, true, true, true, false, true, false, false, false, true, false, false, false, false, false, false, false, false };
	public static int context = 1;
	private static int nbStates;
	private static MultilayerPerceptron[] MLP;
	private static ArrayList<Attribute> attInfo;
	public static Joint[] meaningfulJoints = new Joint[] { Joint.RIGHT_ELBOW, Joint.RIGHT_HAND, Joint.RIGHT_WRIST, Joint.LEFT_ELBOW, Joint.LEFT_HAND, Joint.LEFT_WRIST };
	private static Joint[] meaningfulJointsLeft = new Joint[] { Joint.LEFT_ELBOW, Joint.LEFT_HAND, Joint.LEFT_WRIST };
	private static Joint[] meaningfulJointsRight = new Joint[] { Joint.RIGHT_ELBOW, Joint.RIGHT_HAND, Joint.RIGHT_WRIST };
	private static int[] meaningfulHandFeatures = new int[] {0, 1, 2, 3, 4, 22, 24, 26, 36, 37, 38, 39, 40, 58, 60, 62};
	private static int maxThreadCount = 8;
	
	private static String trainPath;
	private static String testPath;
	private static String outPath;
	private static int trainStart;
	private static int trainEnd;
	private static int testStart;
	private static int testEnd;
	
	public static void main(String[] args)
	{
//		if (args.length < 7)
//		{
//			System.err.println("Missing arguments");
//			System.exit(1);
//		}
		
//		trainPath = args[0];
//		testPath = args[1];
//		outPath = args[2];
//		trainStart = Integer.parseInt(args[3]);
//		trainEnd = Integer.parseInt(args[4]);
//		testStart = Integer.parseInt(args[5]);
//		testEnd = Integer.parseInt(args[6]);
		
		trainPath = "ChaLearn2014/train/";
		testPath = "ChaLearn2014/eval/";
		outPath = "ChaLearn2014/predictions/";
		trainStart = 1;
		trainEnd = 470;
		testStart = 701;
		testEnd = 940;
		
		System.out.println("Starting");

		long startTime = System.currentTimeMillis();
		int nbStates = loadTemplates(trainStart, trainEnd);

		long trainingTime = System.currentTimeMillis();
		long totalTime = trainingTime - startTime;

		System.out.println("HMM Training processing time (s): " + totalTime / 1000.0);

		MLP = new MultilayerPerceptron[classCount];

		// liste des attributs, necessaire pour definir Instances
		attInfo = new ArrayList<Attribute>();

		if (useMLP)
		{
			//
			// read an MLP model and get the attributes
			//

			boolean firstMLP = true;// used to get the attributes only once

			int nbMLPs = 1; // 1 MLP for all gestures' HMM states

			for (int l = 0; l < nbMLPs; l++)
			{
				MLP[l] = new MultilayerPerceptron();

				BufferedReader readerTrain;
				Instances dataTrain;

				try
				{
					try
					{
						String MLPname = "ChaLearn2014/MLPs/MLP_1_to_470samples_WRIST_DELTA_HMMstates_context1_21selected.model";
						MLP[l] = (MultilayerPerceptron) weka.core.SerializationHelper.read(MLPname);
						System.out.println("Using MLP: " + MLPname);
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
					}

					readerTrain = new BufferedReader(new FileReader("ChaLearn2014/arff/TRAIN_1_to_470samples_WRIST_DELTA_HMMstates_context1_21selected.arff"));

					try
					{
						dataTrain = new Instances(readerTrain);
						dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
						System.out.println("MLP Train size: " + dataTrain.size());
						if (firstMLP)
						{
							for (int i = 0; i < dataTrain.numAttributes(); i++)
							{
								attInfo.add(i, dataTrain.attribute(i));
								// System.out.println("Att: " +
								// attInfo.get(i).toString());
							}
							firstMLP = false;
						}
						try
						{
							readerTrain.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}

					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}

//		System.out.println("Clearing predictions...");
//		clearPredictions();

		int threadCount = Math.min(maxThreadCount, testEnd - testStart + 1);
		Thread[] threads = new Thread[8];
		int c = (int) (testEnd - testStart + 1) / threadCount;
		for (int i = 0; i < threadCount; i++)
		{
			int start = testStart + i * c;
			int end = i == threadCount - 1 ? testEnd : testStart + (i + 1) * c - 1;
			threads[i] = new ChalearnRecognizer(start, end);
			threads[i].start();
		}

		System.out.println("Labelizing using " + threadCount + " threads");

		for (int i = 0; i < threadCount; i++)
		{
			try
			{
				threads[i].join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

//		evaluate();

		long endTime = System.currentTimeMillis();
		totalTime = endTime - startTime;
		System.out.println("Total processing time (s): " + totalTime / 1000.0);
	}

	private int start;
	private int end;

	public ChalearnRecognizer(int start, int end)
	{
		this.start = start;
		this.end = end;
	}

	public void run()
	{
		for (int j = start; j <= end; j++)
		{
			if (j == 487 || j == 520 || j == 525 || ((567 < j) && (j < 600)))
				continue;

			String name = "Sample" + String.format("%04d", j);
			String path = testPath + name + "_skeleton.csv";
			String handPath = testPath + name + "_hands.csv";

			// String testPath = "ChaLearn2014/train/" + name + "_skeleton.csv";
			// String handPath = "ChaLearn2014/train/" + name + "_hands.csv";

			String predictionPath = outPath + name + "_prediction.csv";
			Gesture test = Gesture.load_chalearn(path, null);
			
			Matrix features = null;
			if (useHands)
				features = load_handFile(handPath).selectColumns(meaningfulHandFeatures);
	
			if (useRotation)
			{
				Matrix rotations = loadRotationValues(path);
				features = features == null ? rotations : Matrix.joinRows(features, rotations);
			}
			
			List<GestureLabel> resLabels = labelize(test, features);

			GestureLabel.filter(resLabels, 20);
			GestureLabel.smooth(resLabels, 6);

			// GestureLabel.diffusion(resLabels, 1);

//			List<GestureLabel> valLabels = GestureLabel.fromFile(testPath + name + "_labels.csv");
			// GestureLabel.printLinear(resLabels, valLabels);
			// GestureLabel.printLinear2File("ChaLearn2014/predictions/" + name
			// + "_linearLabels.txt", resLabels, valLabels)

			GestureLabel.write(resLabels, predictionPath);
			System.out.println("Wrote " + predictionPath);

		}
	}

	public static void clearPredictions()
	{
		try
		{
			Process p = Runtime.getRuntime().exec(new String[] { "sh", "-c", "rm " + outPath + "*" });
			p.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void evaluate()
	{
		try
		{
			Process p = Runtime.getRuntime().exec(new String[] { "sh", "-c", "cd ChaLearn2014/script/; python eval.py" });
			p.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null)
				System.out.println(line);

			reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			line = "";
			while ((line = reader.readLine()) != null)
				System.out.println(line);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static List<GestureLabel> labelize(Gesture g, Matrix features)
	{
		int[] windows = new int[] { 5, 6, 7, 8, 9, 10, 20, 30, 40 };
		int testLength = g.size();
		Matrix scores = new Matrix(testLength, 21, Double.NEGATIVE_INFINITY);

		for (int i = 0; i < testLength; i++)
		{
			// System.out.println("FRAME : " + i);

			for (int k = 0; k < windows.length; k++)
			{
				int w = windows[k];
				int i0 = i;
				int i1 = i + w;

				if (i0 >= 0 && i1 < testLength)
				{
					Gesture current = g.copy(i0, i1);
					if (!current.isClean())
						continue;

					current = current.normalize();
					Matrix gm = current.toMatrix(meaningfulJoints);

					if (features != null)
					{
						Matrix currentFeatures = features.copy(i0, i1).resample(Gesture.sampleCount);
						gm = Matrix.joinRows(gm, currentFeatures);
					}

					ViterbiResult[] vr = new ViterbiResult[classCount];

					if (useMLP)
					{
						// Create a single empty instance 'inst' with nb of
						// attribute values
						// and add it to the set of instances 'currentInst'
						Matrix currentMatrix = new Matrix(current.size(), meaningfulJoints.length * 3 * (context + 2));
						int sizeInstances = 0;
						currentMatrix = current.toMatrixWithContextAndDerivative(context, meaningfulJoints);

						sizeInstances = current.size();
						Instances currentInst = new Instances("currentInst", attInfo, sizeInstances);
						int nbAtt = attInfo.size();// 12+1: 12 att values + 1
													// class att

						for (int l = 0; l < sizeInstances; l++)
						{
							Instance inst = new DenseInstance(nbAtt);
							for (int m = 0; m < nbAtt - 1; m++)
							{
								inst.setValue(m, currentMatrix.get(l, m));
							}
							inst.setValue(nbAtt - 1, classCount - 1);
							currentInst.add(l, inst);
						}
						currentInst.setClassIndex(currentInst.numAttributes() - 1);

						// System.out.println("SiZE: " + currentInst.size());

						// System.out.println("Instances size: " +
						// currentInst.numInstances());

						// evaluate MLP and get posteriors
						Matrix[] MLPpost = new Matrix[classCount];

						int nbMLPs = 1;

						for (int l = 0; l < nbMLPs; l++)
						{
							MLPpost[l] = new Matrix(sizeInstances, nbStates);
							// System.out.println("GESTE : " + l);
							for (int n = 0; n < sizeInstances; n++)
							{
								Instance inst = currentInst.instance(n);

								// System.out.println("Inst: " +
								// inst.toString());

								try
								{

									double clsLabel = MLP[l].classifyInstance(inst);
									double[] probs = MLP[l].distributionForInstance(inst);
									MLPpost[l].set(n, probs);

									int lab = (int) Math.round(clsLabel);
									// System.out.println("Frame: " + n +
									// " label: " +
									// clsLabel + " probPre: " + probs[lab]);

								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}

						if (useForward)
							vr = HMMRecognizer.forwardMLPLog(MLPpost);
						else
						{
							System.out.println("Viterbi is not implemented with state-based MLP!! Use forward.");
							return null;
						}
					}
					else
					{
						if (useForward)
							vr = HMMRecognizer.forward(gm);
						else
							vr = HMMRecognizer.recognize(gm);
					}

					for (int l = i0; l <= i1; l++)
					{
						for (int j = 0; j < classCount; j++)
						{
							double score = vr[j].score;
							scores.set(l, j, Math.max(score, scores.get(l, j)));
						}
					}
				}
			}
		}

		List<GestureLabel> labels = new ArrayList<GestureLabel>();
		GestureLabel currentLabel = new GestureLabel(0, 0, classCount - 1);

		// scores.print();

		for (int i = 0; i < testLength; i++)
		{
			int bestClass = 20;
			double bestScore = Double.NEGATIVE_INFINITY;
			for (int j = 0; j < 21; j++)
			{
				double s = scores.get(i, j);
				if (s > bestScore)
				{
					bestScore = s;
					bestClass = j;
				}
			}

			if (currentLabel == null)
				currentLabel = new GestureLabel(i, i, bestClass);
			else if (currentLabel.id == bestClass)
				currentLabel.end = i;
			else
			{
				labels.add(currentLabel);
				currentLabel = new GestureLabel(i, i, bestClass);
			}

			// if(i>130 && i<180){
			// System.out.println("it: " + i + " best: " + bestClass + " " +
			// scores.get(i, bestClass));
			// }

		}

		labels.add(currentLabel);

		return labels;
	}

	public static int loadTemplates(int start, int end)
	{
		List<int[]> stateLabelsTemplate = new ArrayList<int[]>();

//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 2, 1, 2)); // gesture 1
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 2, 1, 2)); // gesture 2
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 2, 1, 2)); // gesture 3
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 2, 1, 2)); // gesture 4
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 3)); // gesture 5
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 6
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 3, 1, 3)); // gesture 7
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 8
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 2, 1, 2)); // gesture 9
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 10
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 11
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 12
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 13
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 14
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 15
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 3, 2, 3)); // gesture 16
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 2, 1, 2)); // gesture 17
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 18
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 19
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1)); // gesture 20
//		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1)); // gesture 21

		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 1
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 2
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 3
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 4
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 5
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 6
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 7
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 8
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 9
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 10
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 11
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 12
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 13
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 14
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 15
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 16
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 17
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 18
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 19
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1, 1, 1)); // gesture 20
		stateLabelsTemplate.add(HMM.generateStateLabels(false, 1)); // gesture 21

		List<List<Matrix>> gestureList = new ArrayList<List<Matrix>>();
		List<List<int[]>> stateLabels = new ArrayList<List<int[]>>();

		for (int i = 0; i < classCount; i++)
			gestureList.add(new ArrayList<Matrix>());

		int nbAddedFramesFiller = 0;
		for (int i = start; i <= end; i++)
		{
			if (i == 141 || i == 158 || i == 329 || i == 401 || i == 434 || i == 448 || ((157 < i) && (i < 200)) || ((271 < i) && (i < 300)) || ((365 < i) && (i < 400)) || i > 463)
				continue;

			
			if(i == 487 || i == 520 || i == 525 || ((567 < i) && (i < 600)))
				continue;
			
			String path = trainPath + "Sample" + String.format("%04d", i);
//			String path = "ChaLearn2014/validation/Sample" + String.format("%04d", i);

			System.out.println("Learning from " + path);

			List<GestureLabel> labels = GestureLabel.fromFile(path + "_labels.csv");
			Gesture g = Gesture.load_chalearn(path + "_skeleton.csv", null);
			
//			if (nbAddedFramesFiller < 15000)
//				if (nbAddedFramesFiller < 6975)
//					if (nbAddedFramesFiller < 11000)
//			{
			nbAddedFramesFiller += addFillerLabels(labels, g);
			//System.out.println("nbAddedFramesFiller: " + nbAddedFramesFiller);
			// }

			Matrix currentHandAll = null;
			Matrix rotations = null;

			if (useHands)
				currentHandAll = load_handFile(path + "_hands.csv").selectColumns(meaningfulHandFeatures);
			if (useRotation)
				rotations = loadRotationValues(path + "_skeleton.csv");

			for (GestureLabel label : labels)
			{
				Gesture current = g.copy(label.start, label.end);

				if (!current.isClean())
					continue;

				current = current.normalize();
				current.name = Integer.toString(label.id);
				Matrix m = current.toMatrix(meaningfulJoints);
				
				if (useHands)
				{
					Matrix currentHand = currentHandAll.copy(label.start, label.end);
					currentHand = currentHand.resample(Gesture.sampleCount);
					m = Matrix.joinRows(m, currentHand);
				}

				if (useRotation)
				{
					Matrix currentRot = rotations.copy(label.start, label.end);
					currentRot = rotations.resample(Gesture.sampleCount);
					m = Matrix.joinRows(m, currentRot);
				}

				gestureList.get(label.id).add(m);
			}

		}

		int nbStates = 0;

		for (int j = 0; j < classCount; j++)
		{
			List<int[]> setLabels = new ArrayList<int[]>();
			int s = gestureList.get(j).size();
			for (int k = 0; k < s; k++)
				setLabels.add(stateLabelsTemplate.get(j));

			stateLabels.add(setLabels);
			nbStates = setLabels.get(0).length;
		}

		HMMRecognizer.learn(gestureList, stateLabels);

		return nbStates;
	}

	public static Matrix loadRotationValues(String filename)
	{
		Matrix m = null;
		BufferedReader br = null;
		Joint[] joints = ChalearnRecognizer.meaningfulJoints;
		
		try
		{
			String line;
			br = new BufferedReader(new FileReader(filename));
			List<double[]> frames = new ArrayList<double[]>();
			while ((line = br.readLine()) != null)
			{
				String[] lstr = line.split(",");
				double[] v = new double[joints.length * 4];
				for (int i = 0; i < joints.length; i++)
				{
					v[i * 4 + 0] = Double.parseDouble(lstr[joints[i].ordinal() * 9 + 3]);
					v[i * 4 + 1] = Double.parseDouble(lstr[joints[i].ordinal() * 9 + 4]);
					v[i * 4 + 2] = Double.parseDouble(lstr[joints[i].ordinal() * 9 + 5]);
					v[i * 4 + 3] = Double.parseDouble(lstr[joints[i].ordinal() * 9 + 6]);
				}

				frames.add(v);
			}

			m = new Matrix(frames.size(), joints.length * 4);
			for (int i = 0; i < frames.size(); i++)
				m.set(i, frames.get(i));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return m;
	}

	public static Matrix load_handFile(String filename)
	{

		Matrix M = null;

		BufferedReader br = null;
		BufferedReader br2 = null;
		try
		{
			String line;
			br = new BufferedReader(new FileReader(filename));
			int ind_line = 0;

			while ((line = br.readLine()) != null)
				ind_line++;

			M = new Matrix(ind_line, 72);

			br2 = new BufferedReader(new FileReader(filename));
			ind_line = 0;

			while ((line = br2.readLine()) != null)
			{
				// System.out.println("New capture: " + line);
				String[] sl = line.split(",");
				for (int i = 0; i < sl.length; i++)
				{
					M.set(ind_line, i, Double.parseDouble(sl[i]));
					// System.out.println("M(" + i + ", " + );
				}
				ind_line++;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)
					br.close();

				if (br2 != null)
					br2.close();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return M;

	}

	public static int addFillerLabels(List<GestureLabel> labels, Gesture g)
	{
		int filler_id = classCount - 1;
		int filler_start = 0;
		int filler_stop = 0;
		int filler_min_duration = 10;
		int nb_labels = 0;
		int labels_size = labels.size();
		int nbAddedFramesFiller = 0;

		for (int i = 0; i < labels_size; i++)
		{
			GestureLabel label = labels.get(i);

			// System.out.println("Label: " + label.id + " from: " + label.start
			// + " to: " + label.end);
			if (nb_labels > 0)
			{
				filler_stop = label.start - 1;
				if (filler_start + filler_min_duration < filler_stop)
				{
					GestureLabel label_filler = new GestureLabel(filler_start, filler_stop, filler_id);

					// // test if true 21 or if it is a non-labeled gesture
					// System.out.println("Start:" + filler_start + " Stop:" +
					// filler_stop);
					//
					Gesture current = g.copy(filler_start, filler_stop);
					if (current.isClean())
					{
						current = current.normalize();
						// System.out.println("GESTE : 21 ? " + " -- is21: " +
						// current.is21() + " isClean? " + current.isClean());

						if (current.is21())
						{
							labels.add(label_filler);
							// System.out.println("Label filler: " +
							// label_filler.id +
							// " from: " + label_filler.start + " to: " +
							// label_filler.end);
							nbAddedFramesFiller += filler_stop - filler_start + 1;
						}
					}

				}
			}
			filler_start = label.end + 1;

			nb_labels++;
		}

		return nbAddedFramesFiller;
	}
}
