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

package org.wso2.siddhi.extension.nlp.cat;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.extension.nlp.OpenNLPLoader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This is a Siddhi function for OpenNLP Classifier for an input stream in CEP.
 */
@SiddhiExtension(namespace = "nlp", function = "getCategory")
public class DocCategorizor extends FunctionExecutor {

	// Initializing the logger.
	private static final Logger log = Logger.getLogger(DocCategorizor.class);
	// Initializing the return type.
	private Attribute.Type returnType;

	private boolean isDebugEnabled;

	private DocumentCategorizerME classificationME = null;

	private OpenNLPLoader openNLPLoader = new OpenNLPLoader();

	/**
	 * @param types Attribute types
	 * @param siddhiContext Siddhi Context
	 */
	@Override
	public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {

		if (types.length == 2) {
			// Assigning the attribute type.
			Attribute.Type attributeType1 = types[0];
			Attribute.Type attributeType2 = types[1];

			isDebugEnabled = log.isDebugEnabled();
			if (isDebugEnabled) {
				log.debug("Attribute types are " + attributeType1 + " , " + attributeType2);
			}
			if (attributeType1 == Attribute.Type.STRING
			    && attributeType2 == Attribute.Type.STRING) {
				// Assigning the return type.
				returnType = Attribute.Type.STRING;
				if (isDebugEnabled) {
					log.debug("Return type is " + returnType);
				}
			} else {
				log.error("String values are acceptable types,categorizing texts and the model");

			}
		} else {
			log.error("Two arguments are expected ,categorizing texts and the model");

		}

	}

	/**
	 * @param obj Objects
	 * @return Object []
	 * @throws OperationNotSupportedException
	 */
	@Override
	protected Object process(Object obj) throws OperationNotSupportedException {
		// intializing the result.
		String result = null;
		try {
			if (obj != null) {
				Object[] paramObj = (Object[]) obj;
				String textTobeCategorized = String.valueOf(paramObj[0]);
				String categorizingModelRelPath = String.valueOf(paramObj[1]);
				if (classificationME == null) {
					File modelFile = openNLPLoader.retrieveModel(categorizingModelRelPath);
					DoccatModel categorizingModel = new DoccatModel(new FileInputStream(modelFile));
					classificationME = new DocumentCategorizerME(categorizingModel);
				}
				double[] classDistribution = classificationME.categorize(textTobeCategorized);
				result = classificationME.getBestCategory(classDistribution);
				if (isDebugEnabled) {
					log.debug("Input Values are " + textTobeCategorized + " , " +
					          categorizingModelRelPath);
					log.debug("Category for input value :" + textTobeCategorized + "is " + result);
				}
			} else {
				log.error("Object is null or object is not an array");
			}
		} catch (IOException e) {
			throw new OperationNotSupportedException(e);
		}catch (ConfigurationException e){
			throw new OperationNotSupportedException(e);
		}catch (RegistryException e){
			throw new OperationNotSupportedException(e);
		}
		return result;
	}

	/**
	 * Destroy method.
	 */
	@Override
	public void destroy() {
	}

	/**
	 * Return type of the custom function mentioned
	 *
	 * @return returnType
	 */
	@Override
	public Attribute.Type getReturnType() {
		return returnType;
	}
}
