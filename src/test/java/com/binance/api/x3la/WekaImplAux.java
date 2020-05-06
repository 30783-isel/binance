package com.binance.api.x3la;

import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.AccelerationDecelerationIndicator;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.CMOIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.HMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

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
	
	public static void prepareInstancesForWeka(List<Candlestick> response) throws Exception {

		Instances instances = WekaImplAux.createAttributes();

		List<double[]> listAttributes = prepareListOfAttributes(response, instances);

		BarSeries series = TechnicalAnalysis2.loadSeries(listAttributes);

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

		AccelerationDecelerationIndicator accelerationDeceleration = new AccelerationDecelerationIndicator(series);
		ATRIndicator atr = new ATRIndicator(series, 5);
		CCIIndicator cci = new CCIIndicator(series, 5);
		CMOIndicator cmo = new CMOIndicator(closePrice, 5);
		EMAIndicator ema = new EMAIndicator(closePrice, 5);
		HMAIndicator hma = new HMAIndicator(closePrice, 5);
		MACDIndicator macd = new MACDIndicator(closePrice);
		ROCIndicator roc = new ROCIndicator(closePrice, 5);
		RSIIndicator rsi = new RSIIndicator(closePrice, 5);
		SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
		SMAIndicator longSma = new SMAIndicator(closePrice, 30);
		
		int i = 0;
		int upDown = 0;
		int countUp = 0;
		int countDown = 0;
		
		double lastCloseValue = 0.0;
		for(double[] dbl : listAttributes) {
		
			double[] values = new double[17];
			values[0] = dbl[1];
			values[1] = dbl[2];
			values[2] = dbl[3];
			values[3] = dbl[4];
			values[4] = dbl[5];
			
			values[5] = shortSma.getValue(i).doubleValue();
			values[6] = longSma.getValue(i).doubleValue();
			values[7] = macd.getValue(i).doubleValue();
			values[8] = rsi.getValue(i).doubleValue();
			values[9] = roc.getValue(i).doubleValue();
			values[10] = cci.getValue(i).doubleValue();
			values[11] = atr.getValue(i).doubleValue();
			values[12] = ema.getValue(i).doubleValue();
			values[13] = hma.getValue(i).doubleValue();
			values[14] = accelerationDeceleration.getValue(i).doubleValue();
			values[15] = cmo.getValue(i).doubleValue();
			
			
			if ((values[3] > lastCloseValue) && rsi.getValue(i).doubleValue() > 60) {
				values[16] = instances.attribute(instances.numAttributes() - 1).indexOfValue("up");
				countUp++;
			} else {
				values[16] = instances.attribute(instances.numAttributes() - 1).indexOfValue("down");
				countDown++;
			}
			
			lastCloseValue = values[3];
			
			
			instances.add(new DenseInstance(1.0, values));
			i++;
		}

		System.out.println(String.format("UP - %d", countUp));
		System.out.println(String.format("DOWN - %d", countDown));
		
		mainWeka(instances);

	}

	public static Instances createAttributes() {
		Attribute open = new Attribute("open");
		Attribute close = new Attribute("close");
		Attribute low = new Attribute("low");
		Attribute high = new Attribute("high");
		Attribute volume = new Attribute("volume");
		Attribute shortSma = new Attribute("shortSma");
		Attribute longSma = new Attribute("longSma");
		Attribute macd = new Attribute("macd");
		Attribute rsi = new Attribute("rsi");
		Attribute roc = new Attribute("roc");
		Attribute cci = new Attribute("cci");
		Attribute atr = new Attribute("atr");
		Attribute ema = new Attribute("ema");
		Attribute hma = new Attribute("hma");
		Attribute accelerationDeceleration = new Attribute("accelerationDeceleration");
		Attribute cmo = new Attribute("cmo");
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
		attributes.add(shortSma);
		attributes.add(longSma);
		attributes.add(macd);
		attributes.add(rsi);
		attributes.add(roc);
		attributes.add(cci);
		attributes.add(atr);
		attributes.add(ema);
		attributes.add(hma);
		attributes.add(accelerationDeceleration);
		attributes.add(cmo);
		attributes.add(cls);
		Instances dataset = new Instances("Test-dataset", attributes, 0);

		return dataset;
	}


	

	private static List<double[]> prepareListOfAttributes(List<Candlestick> response, Instances instances) {

		List<double[]> listAttributes = new ArrayList<double[]>();

		for (Candlestick candlestick : response) {

			double[] values = new double[7];

			System.out.println(candlestick + "\n");

			String timestamp = Long.toString(candlestick.getOpenTime());
			String open = candlestick.getOpen();
			String high = candlestick.getHigh();
			String low = candlestick.getLow();
			String close = candlestick.getClose();
			String volume = candlestick.getVolume();

			double closeValue = Double.parseDouble(close);

			values[0] = Double.parseDouble((timestamp));
			values[1] = Double.parseDouble(open);
			values[2] = Double.parseDouble(high);
			values[3] = Double.parseDouble(low);
			values[4] = closeValue;
			values[5] = Double.parseDouble(volume);

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
