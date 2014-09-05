package recognition;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.Gesture;
import core.GestureLabel;
import core.Joint;
import learning.HMM.ViterbiResult;
import learning.Matrix;

public class ChalearnMLP
{
//	private static final int classCount = 21;
//
//	public static void main(String[] args)
//	{
//
//		BufferedReader readerTest;
//		Instances dataTest;
//
//		int context = 1;
//		boolean useHands = true;
//
//		int testStart = 471;
//		int testEnd = 485;// 623
//
//		long startTime = System.currentTimeMillis();
//
//		try
//		{
//			//
//			if (useHands)
//				readerTest = new BufferedReader(new FileReader("ChaLearn2014/arff/VALIDATION_471_to_623samples_context1_21selected_6handFeat.arff"));
//			else
//				readerTest = new BufferedReader(new FileReader("ChaLearn2014/arff/VALIDATION_471_to_700samples_WRIST_context" + context + ".arff"));
//
//			// sert uniquement a charger le header pour avoir le nbAtt et les noms des att...
//
//			try
//			{
//				Classifier MLP = null;
//				if (useHands)
//				{
//					MLP = (Classifier) weka.core.SerializationHelper.read("ChaLearn2014/MLPs/MLP_train_1_470_WRIST_21selected_context1_6handFeat.model");
//				}
//				else
//				{
//					MLP = (Classifier) weka.core.SerializationHelper.read("ChaLearn2014/MLPs/MLP_train_1_470_WRIST_context" + context + ".model");
//					// MLP = (Classifier) weka.core.SerializationHelper.read("ChaLearn2014/MLPs/MLP_train_1_470_context" + context + ".model");
//				}
//
//				System.out.println("Testing...");
//
//				dataTest = new Instances(readerTest);
//				// setting class attribute
//				dataTest.setClassIndex(dataTest.numAttributes() - 1);
//				ArrayList<Attribute> attInfo = new ArrayList<Attribute>();
//
//				for (int i = 0; i < dataTest.numAttributes(); i++)
//					attInfo.add(i, dataTest.attribute(i));
//				// System.out.println("Att: " + attInfo.get(i).toString());
//
//				System.out.println("AttInfo size: " + attInfo.size());
//
//				//
//				// for (int i = 0; i < dataTest.numInstances(); i++) {
//				// double clsLabel = MLP.classifyInstance(dataTest.instance(i));
//				// double [] probs = MLP.distributionForInstance(dataTest.instance(i));
//				//
//				// System.out.println("i: " + i + " label: " + clsLabel + " probs: " + Arrays.toString(probs));
//				// // for(double p : probs){
//				// // System.out.println(" probs: " + p);
//				// // }
//				// // labeledTest.instance(i).setClassValue(clsLabel);
//				// }
//
//				System.out.println("Clearing predictions...");
//				clearPredictions();
//				//
//				for (int i = testStart; i <= testEnd; i++)
//				{
//
//					if (i == 487 || i == 520 || i == 525 || ((567 < i) && (i < 600)))
//						continue;
//
//					String name = "Sample" + String.format("%04d", i);
//					System.out.println("Decoding " + name);
//					String testPath = "ChaLearn2014/validation/" + name + "_skeleton.csv";
//					String handPath = "ChaLearn2014/validation/" + name + "_hands.csv";
//
//					String predictionPath = "ChaLearn2014/predictions/" + name + "_prediction.csv";
//					Gesture test = Gesture.load_chalearn(testPath, null);
//
//					Matrix hands = load_handFile(handPath);
//					List<GestureLabel> resLabels = labelize(test, hands, MLP, attInfo, context, useHands);
//					// for(int k=1; k<21; k++)
//					// GestureLabel.filter(resLabels, k);
//					GestureLabel.filter(resLabels, 20);
//					GestureLabel.smooth(resLabels, 6);
//
//					List<GestureLabel> valLabels = GestureLabel.fromFile("ChaLearn2014/validation/" + name + "_labels.csv");
//					// GestureLabel.printLinear(resLabels, valLabels);
//
//					GestureLabel.printLinear2File("ChaLearn2014/predictions/" + name + "_linearLabels_MLP.txt", resLabels, valLabels);
//
//					GestureLabel.write(resLabels, predictionPath);
//
//					System.out.println("Wrote " + predictionPath);
//				}
//
//				evaluate();
//
//				long endTime = System.currentTimeMillis();
//				long totalTime = endTime - startTime;
//				System.out.println("Total processing time (ms): " + totalTime);
//
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace();
//			}
//
//		}
//		catch (FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//
//	}
//
//	public static void clearPredictions()
//	{
//		try
//		{
//			Process p = Runtime.getRuntime().exec(new String[] { "sh", "-c", "rm ChaLearn2014/predictions/*" });
//			p.waitFor();
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	public static void evaluate()
//	{
//		try
//		{
//			Process p = Runtime.getRuntime().exec(new String[] { "sh", "-c", "cd ChaLearn2014/script/; python eval.py" });
//			p.waitFor();
//
//			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//			String line = "";
//			while ((line = reader.readLine()) != null)
//				System.out.println(line);
//
//			reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//			line = "";
//			while ((line = reader.readLine()) != null)
//				System.out.println(line);
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
//
//	public static List<GestureLabel> labelize(Gesture g, Matrix h, Classifier MLP, ArrayList<Attribute> attInfo, int context, boolean useHands)
//	{
//		// if(useMLP)
//		// int[] windows = new int[]{ 30};
//		// else
//		int[] windows = new int[] { 32 };
//
//		int testLength = g.size();
//		int nbStates = classCount;
//		Matrix scores = new Matrix(testLength, classCount, Double.NEGATIVE_INFINITY);
//
//		List<GestureLabel> labels = new ArrayList<GestureLabel>();
//		GestureLabel currentLabel = new GestureLabel(0, 0, classCount - 1);
//
//		// testLength = 250;
//		for (int i = 0; i < testLength; i++)
//		{
//			// System.out.println("FRAME : " + i);
//
//			if (i % 500 == 0)
//				System.out.println(i + " labelized frames...");
//
//			for (int k = 0; k < windows.length; k++)
//			{
//				int w = windows[k];
//				int i0 = i;
//				int i1 = i + w;
//
//				if (i0 >= 0 && i1 < testLength)
//				{
//					Gesture current = g.copy(i0, i1);
//					if (!current.isClean())
//						continue;
//
//					// System.out.println("i0: " + i0 + " i1:" + i1);
//
//					current = current.normalize();
//
//					// ViterbiResult[] vr = new ViterbiResult[classCount];
//					Matrix currentMatrix = null;
//					Matrix hands = null;
//
//					if (useHands)
//						hands = h.copy(i0, i1).resample(32);
//
//					if (useHands)
//						currentMatrix = current.toMatrixWithContext(1, hands, ChalearnRecognizer.meaningfulJoints);
//					else
//						currentMatrix = current.toMatrix(ChalearnRecognizer.meaningfulJoints);
//
//					int sizeInstances = current.size();
//					// currentMatrix.print();
//
//					Instances currentInst = new Instances("currentInst", attInfo, sizeInstances);
//					// int nbAtt = attInfo.size();// 12+1: 12 att values + 1
//					// class att
//
//					currentInst = initializeInstances(currentMatrix, sizeInstances, attInfo, context);
//
//					currentInst.setClassIndex(currentInst.numAttributes() - 1);
//
//					// System.out.println("SiZE: " + currentInst.size());
//
//					// System.out.println("Instances size: " +
//					// currentInst.numInstances());
//
//					// evaluate MLP and get posteriors
//					Matrix[] MLPpost = new Matrix[classCount];
//
//					for (int l = 0; l < classCount; l++)
//					{
//						MLPpost[l] = new Matrix(sizeInstances, nbStates);
//						// System.out.println("GESTE : " + l);
//						for (int n = 0; n < sizeInstances; n++)
//						{
//							Instance inst = currentInst.instance(n);
//
//							try
//							{
//
//								double clsLabel = MLP.classifyInstance(inst);
//								double[] probs = MLP.distributionForInstance(inst);
//								MLPpost[l].set(n, probs); // 1 ligne =
//								// [p_state1
//								// pstate2
//								// pstate3 ... ]
//								// pour la
//								// capture n
//
//								// int lab = (int) Math.round(clsLabel);
//								// System.out.println("Frame: " + n +
//								// " label: " +
//								// lab + " probPre: " + probs[lab]);
//
//							}
//							catch (Exception e)
//							{
//								e.printStackTrace();
//							}
//						}
//
//					}
//					// if (i==52)
//					// // MLPpost.print();
//					// if (useForward)
//					// vr = HMMRecognizer.forwardMLP(MLPpost);
//					// else
//					// vr = HMMRecognizer.recognizeMLP(MLPpost);
//					//
//					// // ViterbiResult[] vr = HMMRecognizer.forward(current);
//					// }
//					// else
//					// {
//					//
//					// if (useForward)
//					// vr = HMMRecognizer.forward(current);
//					// else
//					// vr = HMMRecognizer.recognize(current);
//					//
//					// }
//
//					for (int l = i0; l < i1; l++)
//					{
//						for (int j = 0; j < classCount; j++)
//						{
//							// System.out.println("l: " + l + " g:" + j + " i1:" + i1);
//
//							double score = MLPpost[j].get(l - i0, j);
//							scores.set(l, j, Math.max(score, scores.get(l, j)));
//							// scores.set(l, j, 0.001*score + 0.999*Math.max(score, scores.get(l, j)));
//						}
//					}
//
//				}
//			}
//		}
//
//		// scores.print();
//
//		for (int i = 0; i < testLength; i++)
//		{
//			int bestClass = 20;
//			double bestScore = Double.NEGATIVE_INFINITY;
//			for (int j = 0; j < 21; j++)
//			{
//				double s = scores.get(i, j);
//				if (s > bestScore)
//				{
//					bestScore = s;
//					bestClass = j;
//				}
//			}
//
//			if (currentLabel == null)
//				currentLabel = new GestureLabel(i, i, bestClass);
//			else if (currentLabel.id == bestClass)
//				currentLabel.end = i;
//			else
//			{
//				labels.add(currentLabel);
//				currentLabel = new GestureLabel(i, i, bestClass);
//			}
//
//			// if(i>130 && i<180){
//			// System.out.println("it: " + i + " best: " + bestClass + " " + scores.get(i, bestClass));
//			// }
//
//		}
//
//		labels.add(currentLabel);
//
//		return labels;
//	}
//
//	public static Instances initializeInstances(Matrix M, int sizeInstances, ArrayList<Attribute> attInfo, int context)
//	{
//
//		Instances currentInst = new Instances("currentInst", attInfo, sizeInstances);
//
//		int nbAtt = attInfo.size();
//
//		if (context == 0)
//		{
//			for (int l = 0; l < sizeInstances; l++)
//			{
//				Instance inst = new DenseInstance(nbAtt);
//				for (int m = 0; m < nbAtt - 1; m++)
//					inst.setValue(m, M.get(l, m));
//
//				inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//				currentInst.add(l, inst);
//				// System.out.println(inst.toString());
//			}
//		}
//
//		if (context == 1)
//		{
//
//			Instance inst = new DenseInstance(nbAtt);
//			int nbAttCapture = (nbAtt - 1) / (context + 2);
//			// System.out.println("nbAtt=" + nbAtt + " nbAttCapture:" + nbAttCapture);
//
//			// first line: t0:t0:t1
//			for (int m = 0; m < nbAttCapture; m++)
//				inst.setValue(m, M.get(0, m));
//			for (int m = nbAttCapture; m < 2 * nbAttCapture; m++)
//				inst.setValue(m, M.get(0, m - nbAttCapture));
//			for (int m = 2 * nbAttCapture; m < nbAtt - 1; m++)
//				inst.setValue(m, M.get(1, m - 2 * nbAttCapture));
//			inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//			currentInst.add(0, inst);
//
//			// line i: ti-1:ti:ti+1
//			for (int l = 1; l < sizeInstances - 1; l++)
//			{
//				inst = new DenseInstance(nbAtt);
//				for (int m = 0; m < nbAttCapture; m++)
//					inst.setValue(m, M.get(l - 1, m));
//				for (int m = nbAttCapture; m < 2 * nbAttCapture; m++)
//					inst.setValue(m, M.get(l, m - nbAttCapture));
//				for (int m = 2 * nbAttCapture; m < nbAtt - 1; m++)
//					inst.setValue(m, M.get(l + 1, m - 2 * nbAttCapture));
//				inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//				currentInst.add(l, inst);
//				// System.out.println(inst.toString());
//			}
//
//			// last line: tN-2:tN-1:tN-1
//			int l = sizeInstances - 1;
//			inst = new DenseInstance(nbAtt);
//			for (int m = 0; m < nbAttCapture; m++)
//				inst.setValue(m, M.get(l - 1, m));
//			for (int m = nbAttCapture; m < 2 * nbAttCapture; m++)
//				inst.setValue(m, M.get(l, m - nbAttCapture));
//			for (int m = 2 * nbAttCapture; m < nbAtt - 1; m++)
//				inst.setValue(m, M.get(l, m - 2 * nbAttCapture));
//			inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//			currentInst.add(l, inst);
//		}
//
//		if (context == 2)
//		{
//
//			int nbAttCapture = (nbAtt - 1) / (context + 3);
//			// System.out.println("nbAtt=" + nbAtt + " nbAttCapture:" + nbAttCapture);
//
//			// first line: t0:t0:t0:t1:t2
//			Instance inst = new DenseInstance(nbAtt);
//			for (int m = 0; m < nbAttCapture; m++)
//				inst.setValue(m, M.get(0, m));
//			for (int m = nbAttCapture; m < 2 * nbAttCapture; m++)
//				inst.setValue(m, M.get(0, m - nbAttCapture));
//			for (int m = 2 * nbAttCapture; m < 3 * nbAttCapture; m++)
//				inst.setValue(m, M.get(0, m - 2 * nbAttCapture));
//			for (int m = 3 * nbAttCapture; m < 4 * nbAttCapture; m++)
//				inst.setValue(m, M.get(1, m - 3 * nbAttCapture));
//			for (int m = 4 * nbAttCapture; m < nbAtt - 1; m++)
//				inst.setValue(m, M.get(2, m - 4 * nbAttCapture));
//			inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//			currentInst.add(0, inst);
//
//			// 2nd i: t0:t1:t1:t2:t3
//			inst = new DenseInstance(nbAtt);
//			for (int m = 0; m < nbAttCapture; m++)
//				inst.setValue(m, M.get(0, m));
//			for (int m = nbAttCapture; m < 2 * nbAttCapture; m++)
//				inst.setValue(m, M.get(1, m - nbAttCapture));
//			for (int m = 2 * nbAttCapture; m < 3 * nbAttCapture; m++)
//				inst.setValue(m, M.get(1, m - 2 * nbAttCapture));
//			for (int m = 3 * nbAttCapture; m < 4 * nbAttCapture; m++)
//				inst.setValue(m, M.get(2, m - 3 * nbAttCapture));
//			for (int m = 4 * nbAttCapture; m < nbAtt - 1; m++)
//				inst.setValue(m, M.get(3, m - 4 * nbAttCapture));
//			inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//			currentInst.add(1, inst);
//
//			// line i: t-2:t-1:t:t+1:t+2
//			for (int l = 2; l < sizeInstances - 2; l++)
//			{
//				inst = new DenseInstance(nbAtt);
//				for (int m = 0; m < nbAttCapture; m++)
//					inst.setValue(m, M.get(l - 2, m));
//				for (int m = nbAttCapture; m < 2 * nbAttCapture; m++)
//					inst.setValue(m, M.get(l - 1, m - nbAttCapture));
//				for (int m = 2 * nbAttCapture; m < 3 * nbAttCapture; m++)
//					inst.setValue(m, M.get(l, m - 2 * nbAttCapture));
//				for (int m = 3 * nbAttCapture; m < 4 * nbAttCapture; m++)
//					inst.setValue(m, M.get(l + 1, m - 3 * nbAttCapture));
//				for (int m = 4 * nbAttCapture; m < nbAtt - 1; m++)
//					inst.setValue(m, M.get(l + 2, m - 4 * nbAttCapture));
//				inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//				currentInst.add(l, inst);
//				// System.out.println(inst.toString());
//			}
//
//			// penultimate line: tN-4:tN-3:tN-2:tN-1:tN-1
//			int l = sizeInstances - 2;
//			inst = new DenseInstance(nbAtt);
//			for (int m = 0; m < nbAttCapture; m++)
//				inst.setValue(m, M.get(l - 2, m));
//			for (int m = nbAttCapture; m < 2 * nbAttCapture; m++)
//				inst.setValue(m, M.get(l - 1, m - nbAttCapture));
//			for (int m = 2 * nbAttCapture; m < 3 * nbAttCapture; m++)
//				inst.setValue(m, M.get(l, m - 2 * nbAttCapture));
//			for (int m = 3 * nbAttCapture; m < 4 * nbAttCapture; m++)
//				inst.setValue(m, M.get(l + 1, m - 3 * nbAttCapture));
//			for (int m = 4 * nbAttCapture; m < nbAtt - 1; m++)
//				inst.setValue(m, M.get(l + 1, m - 4 * nbAttCapture));
//			inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//			currentInst.add(l, inst);
//
//			// last line: tN-3:tN-2:tN-1:tN-1:tN-1
//			l = sizeInstances - 1;
//			inst = new DenseInstance(nbAtt);
//			for (int m = 0; m < nbAttCapture; m++)
//				inst.setValue(m, M.get(l - 2, m));
//			for (int m = nbAttCapture; m < 2 * nbAttCapture; m++)
//				inst.setValue(m, M.get(l - 1, m - nbAttCapture));
//			for (int m = 2 * nbAttCapture; m < 3 * nbAttCapture; m++)
//				inst.setValue(m, M.get(l, m - 2 * nbAttCapture));
//			for (int m = 3 * nbAttCapture; m < 4 * nbAttCapture; m++)
//				inst.setValue(m, M.get(l, m - 3 * nbAttCapture));
//			for (int m = 4 * nbAttCapture; m < nbAtt - 1; m++)
//				inst.setValue(m, M.get(l, m - 4 * nbAttCapture));
//			inst.setValue(nbAtt - 1, classCount - 1);// filler as default class
//			currentInst.add(l, inst);
//		}
//
//		return currentInst;
//	}
//
//	public static Matrix load_handFile(String filename)
//	{
//
//		Matrix M = null;
//
//		BufferedReader br = null;
//		BufferedReader br2 = null;
//		try
//		{
//			String line;
//			br = new BufferedReader(new FileReader(filename));
//			int ind_line = 0;
//
//			while ((line = br.readLine()) != null)
//				ind_line++;
//
//			M = new Matrix(ind_line, 72);
//
//			br2 = new BufferedReader(new FileReader(filename));
//			ind_line = 0;
//
//			while ((line = br2.readLine()) != null)
//			{
//				// System.out.println("New capture: " + line);
//				String[] sl = line.split(",");
//				for (int i = 0; i < sl.length; i++)
//				{
//					M.set(ind_line, i, Double.parseDouble(sl[i]));
//					// System.out.println("M(" + i + ", " + );
//				}
//				ind_line++;
//			}
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		finally
//		{
//			try
//			{
//				if (br != null)
//					br.close();
//
//				if (br2 != null)
//					br2.close();
//			}
//			catch (IOException ex)
//			{
//				ex.printStackTrace();
//			}
//		}
//		return M;
//
//	}

}
