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

package org.wso2.siddhi.extension.nlp.ner;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.transform.TransformProcessor;
import org.wso2.siddhi.extension.nlp.OpenNLPLoader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.BoolConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This is a Siddhi transformer for OpenNLP NER for an input stream in CEP
 */
@SiddhiExtension(namespace = "nlp", function = "getEntities")
public class NameEntityRecognizer extends TransformProcessor {

	private static final Logger log = Logger.getLogger(NameEntityRecognizer.class);

	private boolean debugEnabled = false;
	// The desired attribute name for tweet text
	private String text = "";
	// The desired attribute position in input stream for maximum number of
	// entities
	private int maximumEntities = 0;
	// The desired model
	private String model = "";
	// An array of Objects to manipulate output stream elements
	private Object[] data = null;
	//tokenizing model
	private Tokenizer tokenizer = null;
	//
	private NameFinderME nameFinderME = null;

	private InputStream modelInputStream = null;

	private boolean isManualTokenization = false;

	private OpenNLPLoader openNLPLoader = new OpenNLPLoader();

	/**
	 * This method called when processing an event
	 *
	 * @param inEvent inEvent
	 * @return InStream
	 */
	@Override
	protected InStream processEvent(InEvent inEvent) {
		if (debugEnabled) {
			log.debug("Processing a new Event for Name Entity Recognition, Event : " + inEvent);
		}
		doProcessing(inEvent);
		return new InEvent(inEvent.getStreamId(), System.currentTimeMillis(), data);
	}

	@Override
	protected InStream processEvent(InListEvent inListEvent) {
		InListEvent transformedListEvent = new InListEvent();
		for (Event event : inListEvent.getEvents()) {
			if (event instanceof InEvent) {
				transformedListEvent.addEvent((Event) processEvent((InEvent) event));
			}
		}
		return transformedListEvent;
	}

	/**
	 * This method used to return the current state, Used for persistence of data
	 *
	 * @return Object []
	 */
	@Override
	protected Object[] currentState() {
		return new Object[] { text, maximumEntities, model, tokenizer, nameFinderME,
		                      isManualTokenization };
	}

	/**
	 * This method is used to restore from the persisted state
	 *
	 * @param objects objects
	 */
	@Override
	protected void restoreState(Object[] objects) {
		if ((objects.length == 6) && (objects[0] instanceof String) &&
		    (objects[1] instanceof Integer) && (objects[2] instanceof String)) {
			this.text = (String) objects[0];
			this.maximumEntities = (Integer) objects[1];
			this.model = (String) objects[2];
			this.tokenizer = (Tokenizer) objects[3];
			this.nameFinderME = (NameFinderME) objects[4];
			this.isManualTokenization = (Boolean) objects[5];
			log.debug("successfully NLP Transformer is restored.");
		} else {
			log.error("Restoring NLP Transformer get failed");
		}
	}

	/**
	 * This method called when initialising the extension
	 *
	 * @param parameters          Expression
	 * @param expressionExecutors List<ExpressionExecutor>
	 * @param inStreamDefinition  input StreamDefinition
	 * @param outStreamDefinition output StreamDefinition
	 * @param elementId           String
	 * @param siddhiContext       SiddhiContext
	 */
	@Override
	protected void init(Expression[] parameters, List<ExpressionExecutor> expressionExecutors,
	                    StreamDefinition inStreamDefinition, StreamDefinition outStreamDefinition,
	                    String elementId, SiddhiContext siddhiContext) {
		// assigning log variable
		debugEnabled = log.isDebugEnabled();
		if (parameters.length != 4) {
			throw new QueryCreationException(
					"Parameters count is not matching, There should be four parameters");
		} else {
			this.text = ((Variable) parameters[0]).getAttributeName();
			this.maximumEntities = ((IntConstant) parameters[1]).getValue();
			this.isManualTokenization = ((BoolConstant) parameters[2]).getValue();
			this.model = ((StringConstant) parameters[3]).getValue();
			if(maximumEntities == 0){
				log.error("Please enter positive non zero number for maximum Entities");
			}
			//initializing data array
			data = new Object[maximumEntities];
			this.outStreamDefinition = new StreamDefinition().name("nameEntityStream");
			for (int i = 1; i <= maximumEntities; i++) {
				this.outStreamDefinition.attribute("nameElement" + i, Attribute.Type.STRING);

			}
			try {
				// load tokenizer model
				modelInputStream = getClass().getClassLoader().getResourceAsStream("en-token.bin");
				tokenizer = new TokenizerME(new TokenizerModel(modelInputStream));
				log.debug("tokenizer model is loaded successfully");
			} catch (FileNotFoundException e) {
				log.error("File not found error occurred. \"en-token.bin\" is missing" +
				          e.getMessage());
			} catch (InvalidFormatException e) {
				log.error("\"en-token.bin\" is corrupt" + e.getMessage());
			} catch (IOException e) {
				log.error("IO Exception occurred when NLP is running", e);
			} finally {
				if (modelInputStream != null) {
					try {
						modelInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void destroy() {

	}

	/**
	 * doProcessing Method mainly used to recognize NE from text according to the model
	 *
	 * @param event dataevent
	 */
	private void doProcessing(InEvent event) {
		text = event.getData(0).toString();
		log.debug(event);
		Arrays.fill(data, "N/A");
		// Loading the Model
		if (nameFinderME == null) {
			try {
				File modelFile = openNLPLoader.retrieveModel(model);
				modelInputStream = new FileInputStream(modelFile);
				TokenNameFinderModel modelLocation = new TokenNameFinderModel(modelInputStream);
				nameFinderME = new NameFinderME(modelLocation);
			} catch (IOException e) {
				log.error("File is not found");
			} catch (RegistryException e) {
				log.error("Error occurred during connecting to the registry");
			} catch (ConfigurationException e) {
				log.error(e.getMessage());
			} finally {
				if (modelInputStream != null) {
					try {
						modelInputStream.close();
					} catch (IOException e) {
						log.error("Error occurred Stream couldn't closed");
					}
				}
			}
		}
		// convert sentence into tokens
		String[] tokens = tokenizer.tokenize(text);
		// find locations
		Span[] nameEntitySpans = nameFinderME.find(tokens);
		// find probabilities for locations
		double[] spanProbs = nameFinderME.probs(nameEntitySpans);

		int index = 0;
		for (int id = 0; id < nameEntitySpans.length; id++) {
			// Extract only for number of maximum entities
			if (index < maximumEntities) {
				// to combine location tokens with
				if (nameEntitySpans[id].getStart() + 1 < tokens.length) {
					// to manual tokenize
					int i = nameEntitySpans[id].getStart() + 1;
					if (isManualTokenization) {
						Pattern[][] patterns = openNLPLoader.getPatternsRegex();
						int[][] patternsLength = openNLPLoader.getPatternsLength();
						for (int num = 0; num < patterns.length; num++) {
							if (patterns[num][0].matcher(tokens[i - 1]).find() &&
							    tokens[i - 1].length() <= patternsLength[num][0] &&
							    patterns[num][1].matcher(tokens[i]).find() &&
							    tokens[i].length() <= patternsLength[num][1]) {
								tokens[i - 1] = tokens[i - 1] + " " + tokens[i];
								tokens[i] = "";
								break;
							}
						}
					}
					data[index] = tokens[nameEntitySpans[id].getStart()];
					if (debugEnabled) {
						log.debug("Covered Entity Text is: " +
						          tokens[nameEntitySpans[id].getStart()]);
						log.debug("Probability is: " + spanProbs[id]);
					}
					index++;
				} else {
					data[index] = tokens[nameEntitySpans[id].getStart()];
					if (debugEnabled) {
						log.debug("Covered Entity Text is: " +
						          tokens[nameEntitySpans[id].getStart()]);
						log.debug("Probability is: " + spanProbs[id]);
					}

					index++;
				}
			}
		}
	}
}
