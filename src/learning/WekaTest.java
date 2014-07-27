package learning;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
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
import java.util.ArrayList;
import java.util.Arrays;

public class WekaTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		BufferedReader readerTrain;
		BufferedReader readerTest;
		Instances dataTrain;
		Instances dataTest;

		try {
			readerTrain = new BufferedReader(new FileReader("ChaLearn2014/arff/TRAIN_1_to_470samples_WRIST_context1.arff"));
			readerTest = new BufferedReader(new FileReader("ChaLearn2014/arff/VALIDATION_471_to_700samples_WRIST_context1.arff"));
			
			try {
				dataTrain = new Instances(readerTrain);
				dataTest = new Instances(readerTest);
				// setting class attribute
				dataTrain.setClassIndex(dataTrain.numAttributes() - 1);
				dataTest.setClassIndex(dataTest.numAttributes() - 1);
				
				System.out.println("Train size: " + dataTrain.size());
				
				ArrayList<Attribute> attInfo = new ArrayList<Attribute>();
				for(int i=0; i<dataTrain.numAttributes(); i++){
					attInfo.add(i, dataTrain.attribute(i));
					System.out.println("Att: " + attInfo.get(i).toString());
				}
				
				System.out.println("AttInfo size: " + attInfo.size());
//				int sizeDummy = 32;
//				Instances dummy = new Instances("dummy", attInfo, sizeDummy);
//				for(int i=0; i<sizeDummy; i++)
//					dummy.add(i, dataTrain.get(i));
//				System.out.println("Dummy size: " + dummy.numInstances());
				
//				for(int i=0; i<dummy.numInstances(); i++)
//					System.out.println(dummy.get(i).toString());
				
//				dummy.setClassIndex(dummy.numAttributes() - 1);
				
				try {
					readerTrain.close();
					readerTest.close();


					MultilayerPerceptron MLP = new MultilayerPerceptron();

					
					try {
						
						String[] options = weka.core.Utils.splitOptions("-L 0.3 -M 0.2 -N 500 -V 0 -S 0 -E 20 -I -H a");
						MLP.setOptions(options);

						System.out.println("Training...");
						MLP.buildClassifier(dataTrain);

						// memory leak in saving models with this WEKA version ---> bad performance when using the saved model!!!
//						System.out.println("Saving model: ChaLearn2014/MLPs/MLP_train_1_470_WRIST_context1.model");
//						weka.core.SerializationHelper.write("ChaLearn2014/MLPs/MLP_train_1_470_WRIST_context1.model", MLP);
						
						System.out.println("Testing...");
						
//						Evaluation eval = new Evaluation(dataTrain);
//						eval.evaluateModel(MLP, dataTest);
//						System.out.println(eval.toSummaryString("\nResults\n======\n", false)); 
						 
						
//						// label instances
//						// create copy
//						 Instances labeledTest = new Instances(dataTest);
						 
						 for (int i = 0; i < dataTest.numInstances(); i++) {
						   double clsLabel = MLP.classifyInstance(dataTest.instance(i));
						   double [] probs = MLP.distributionForInstance(dataTest.instance(i));
						   
						   System.out.println("i: " + i + " label: " + clsLabel + " probs: " + Arrays.toString(probs));
//						   for(double p : probs){
//							   System.out.println(" probs: " + p);
//						   }
//						   labeledTest.instance(i).setClassValue(clsLabel);
						 }
//						 // save labeled data
//						 BufferedWriter writer = new BufferedWriter(
//						                           new FileWriter("../tmp/labeled.arff"));
//						 writer.write(labeledTest.toString());
//						 writer.newLine();
//						 writer.flush();
//						 writer.close();
						 
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					

				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
		
		
		
		
		// read a cvs file 
//		String filename = "ChaLearn2014/train/Sample0200_skeleton.csv";
//	    System.out.println("\nReading file " + filename + "...");
//	    try {
//			
//		    CSVLoader loader = new CSVLoader();
//		    loader.setSource(new File(filename));
//		
//		    Instances data = loader.getDataSet();
//		    
//		    Attribute classe = new Attribute("geste", "0");
//		    data.setClass(classe);
//		    
//		    System.out.println("\nHeader of dataset:\n");
//		    System.out.println(new Instances(data, 0));
//
//	
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	
	
	}


}
