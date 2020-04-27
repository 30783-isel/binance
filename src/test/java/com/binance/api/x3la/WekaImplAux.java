package com.binance.api.x3la;

import java.util.ArrayList;
import java.util.List;

import com.binance.api.client.domain.market.Candlestick;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;

public class WekaImplAux {

	public static Instances createAttributes() {
		Attribute open = new Attribute("open");
		Attribute close = new Attribute("close");
		Attribute low = new Attribute("low");
		Attribute high = new Attribute("high");
		Attribute volume = new Attribute("volume");
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("up");
		labels.add("down");
		Attribute cls = new Attribute("class", labels);
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(open);
		attributes.add(close);
		attributes.add(low);
		attributes.add(high);
		attributes.add(volume);
		attributes.add(cls);
		Instances dataset = new Instances("Test-dataset", attributes, 0);

		return dataset;
	}

	public static void createInstanceForWeka(Instances dataset, double open, double close, double low, double high, double volume) {
		double[] vals = new double[dataset.numAttributes()];
		vals[0] = open;
		vals[1] = close;
		vals[2] = low;
		vals[3] = high;
		vals[4] = volume;
	}

	public static void prepareInstancesForWeka(List<Candlestick> response) throws Exception {

		Instances instances = WekaImplAux.createAttributes();
		
		List<double[]> listAttributes = prepareListOfAttributes( response, instances);
		
		List<String[]> allElements = TechnicalAnalysis.prepareListForTa4j(listAttributes);
		
		TechnicalAnalysis.executeTechnicalAnalysis(allElements);
		
		// add data to instance
		//-----------------------------------------------------------------------------------------------------instances.add(new DenseInstance(1.0, values));

		// instance row to predict
		int index = 10;

		mainWeka(instances);

	}

	private static List<double[]> prepareListOfAttributes(List<Candlestick> response, Instances instances) {
		
		List<double[]> listAttributes = new ArrayList<double[]>();
		
		
		
		double lastCloseValue = 0.0;

		for (Candlestick candlestick : response) {
			
			double[] values = new double[7];
			
			System.out.println(candlestick + "\n");
			String open = candlestick.getOpen();
			String high = candlestick.getHigh();
			String low = candlestick.getLow();
			String close = candlestick.getClose();
			String volume = candlestick.getVolume();
			String timestamp = Long.toString(candlestick.getOpenTime());
			
			double closeValue = Double.parseDouble(close);

			values[0] = Double.parseDouble((timestamp));
			values[1] = closeValue;
			values[2] = Double.parseDouble(volume);
			values[3] = Double.parseDouble(open);
			values[4] = Double.parseDouble(low);
			values[5] = Double.parseDouble(high);
			

			if (closeValue > lastCloseValue) {
				values[6] = instances.attribute(5).indexOfValue("up");
			} else {
				values[6] = instances.attribute(5).indexOfValue("down");
			}

			lastCloseValue = Double.parseDouble(close);
			
			listAttributes.add(values);
		}
		return listAttributes;
	}

	public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
		Evaluation evaluation = new Evaluation(trainingSet);

		model.buildClassifier(trainingSet);
		evaluation.evaluateModel(model, testingSet);

		return evaluation;
	}

	public static double calculateAccuracy(FastVector predictions) {
		double correct = 0;

		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
			if (np.predicted() == np.actual()) {
				correct++;
			}
		}

		return 100 * correct / predictions.size();
	}

	public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
		Instances[][] split = new Instances[2][numberOfFolds];

		for (int i = 0; i < numberOfFolds; i++) {
			split[0][i] = data.trainCV(numberOfFolds, i);
			split[1][i] = data.testCV(numberOfFolds, i);
		}

		return split;
	}

	public static void mainWeka(Instances data) throws Exception {

		data.setClassIndex(data.numAttributes() - 1);

		// Do 10-split cross validation
		Instances[][] split = crossValidationSplit(data, 10);

		// Separate split into training and testing arrays
		Instances[] trainingSplits = split[0];
		Instances[] testingSplits = split[1];

		// Use a set of classifiers
		Classifier[] models = { new J48(), // a decision tree
				new PART(), new DecisionTable(), // decision table majority classifier
				new DecisionStump() // one-level decision tree
		};

		// Run for each model
		for (int j = 0; j < models.length; j++) {

			// Collect every group of predictions for current model in a FastVector
			FastVector predictions = new FastVector();

			// For each training-testing split pair, train and test the classifier
			for (int i = 0; i < trainingSplits.length; i++) {
				Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);

				predictions.appendElements(validation.predictions());

				// Uncomment to see the summary for each training-testing pair.
				// System.out.println(models[j].toString());
			}

			// Calculate overall accuracy of current classifier on all splits
			double accuracy = calculateAccuracy(predictions);

			// Print current classifier's name and accuracy in a complicated,
			// but nice-looking way.
			System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": " + String.format("%.2f%%", accuracy) + "\n---------------------------------");
		}

	}

}
