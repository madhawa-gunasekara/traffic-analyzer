/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.fasttrack.project.roadlk.ModelTrainer;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.featuregen.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

/**
 *
 *
 */
public class ModelTrain {

	private static final Logger log = LogManager.getLogger(ModelTrain.class);

	/**
	 * Training location model to extract locations from twitter texts using
	 * OpenNLP Api
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		// create NameSampleDataStream
		// load trained data into memory
		// converts tagged strings from trained data into NameSample objects
		// populated in next step

		NameSampleDataStream nameSampleDataStream = null;

		try {
			nameSampleDataStream =
					new NameSampleDataStream(
							new PlainTextByLineStream(
									new java.io.FileReader(
											new File(
													"taggedLocation.txt"))));
		} catch (FileNotFoundException e2) {
			log.error("Modeled data set file didn't find. \t FileNotFoundException :" +
			          e2.getMessage());
		}

		// create "location" model
		TokenNameFinderModel locationModel = null;

		// train the model
		try {
			AdaptiveFeatureGenerator featureGenerator = new CachedFeatureGenerator(
					new AdaptiveFeatureGenerator[] {
							new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
							new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
							new OutcomePriorFeatureGenerator(),
							new PreviousMapFeatureGenerator(),
							new BigramNameFeatureGenerator(),
							new SentenceFeatureGenerator(true, false)
					});
			locationModel = NameFinderME.train("en", // language of the training data
			                                   "location", // type of model
			                                   // created above NameSample collection
			                                   nameSampleDataStream,
			                                   // null = default set of feature
			                                   // generators for
			                                   // NE detection
			                                   (AdaptiveFeatureGenerator) featureGenerator,
			                                   // empty, not adding additional resources
			                                   // to the model
			                                   Collections.<String, Object>emptyMap(),
			                                   // iterations
			                                   300,
			                                   // cutoff
			                                   3);
		} catch (IOException e1) {
			log.error("IOException occurred while training the model. /t IOException :" +
			          e1.getMessage());
		}

		// save the model to disk
		try {
			locationModel.serialize(new FileOutputStream(new File("en-location.bin")));
		} catch (FileNotFoundException e) {
			log.error(
					"File not found exception occurred while saving the model \t FileNotFoundException :" +
					e.getMessage());
		} catch (IOException e) {
			log.error("IOException occurred while saving the model. /t IOException :" +
			          e.getMessage());

		}

	}
}
