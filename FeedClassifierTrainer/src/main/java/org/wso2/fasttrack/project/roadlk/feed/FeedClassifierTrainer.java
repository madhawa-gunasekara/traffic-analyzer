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
package org.wso2.fasttrack.project.roadlk.feed;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import org.apache.log4j.Logger;

import java.io.*;

public class FeedClassifierTrainer {

	private static final Logger log = Logger.getLogger(FeedClassifierTrainer.class);

	private static DoccatModel model = null;

	public static void main(String[] args) {
		log.debug("Model training started");
		train();
		//testing purpose
		String content = "thank you";
		try {
			new FeedClassifierTrainer().test(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Training the models
	 */
	public static void train() {
		String onlpModelPath = "en-doccat.bin";
		String trainingDataFilePath = "data.txt";

		InputStream dataInputStream = null;
		OutputStream onlpModelOutput = null;
		try {
			// Read training data file
			dataInputStream = new FileInputStream(trainingDataFilePath);
			// Read each training instance
			ObjectStream<String> lineStream = new PlainTextByLineStream(dataInputStream, "UTF-8");
			ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
			// Calculate the training model
			model = DocumentCategorizerME.train("en", sampleStream, 2, 300);
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			if (dataInputStream != null) {
				try {
					dataInputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
		/*
		 * Now we are writing the calculated model to a file in order to use the
		 * trained classifier in production
		 */
		try {
			if (model != null) {
				model.serialize(new FileOutputStream(onlpModelPath));
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	/*
	 * Now we call the saved model and test it
	 * Give it a new text document and the expected category
	 */
	public void test(String text) throws IOException {
		String classificationModelFilePath = "en-doccat.bin";
		DocumentCategorizerME classificationME =
				new DocumentCategorizerME(
						new DoccatModel(
								new FileInputStream(
										classificationModelFilePath)));
		String documentContent = text;
		double[] classDistribution = classificationME.categorize(documentContent);
		String predictedCategory = classificationME.getBestCategory(classDistribution);
		System.out.println("Model prediction : " + predictedCategory);

	}
}
