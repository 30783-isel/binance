package com.binance.api.x3la;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.Instances;

public class WekaImplAux {

	public static Instances createAttributes() {
		Attribute open = new Attribute("open");
		Attribute close = new Attribute("close");
		Attribute low = new Attribute("low");
		Attribute high = new Attribute("high");
		Attribute volume = new Attribute("volume");
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("no");
		labels.add("yes");
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

	public static void createInstanceForWeka( Instances dataset, double open, double close, double low, double high, double volume ) {
		double[] vals = new double[dataset.numAttributes()];
		vals[0] = open;
		vals[1] = close;
		vals[2] = low;
		vals[3] = high;
		vals[4] = volume;
	}
	
}
