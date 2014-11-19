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

package org.wso2.siddhi.extension.nlp;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryClientUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * ModelLoader class mainly used to connect to the registry and get the particular file
 */
public class OpenNLPLoader {

	private static final Logger log = Logger.getLogger(OpenNLPLoader.class);
	private String[] configurations = new String[4];
	private ArrayList<Pattern[]> patternRegexList = new ArrayList<Pattern[]>();
	private ArrayList<int[]> patternLengthList = new ArrayList<int[]>();

	/**
	 * @return PatternLengthList
	 */
	public int[][] getPatternsLength() {

		int[][] patternsLength = new int[patternLengthList.size()][2];
		for (int i = 0; i < patternLengthList.size(); i++) {
			patternsLength[i] = patternLengthList.get(i);
		}
		return patternsLength;
	}

	/**
	 * @return PatternRegexList
	 */
	public Pattern[][] getPatternsRegex() {
		Pattern[][] patternsRegex = new Pattern[patternRegexList.size()][2];
		for (int i = 0; i < patternRegexList.size(); i++) {
			patternsRegex[i] = (Pattern[]) patternRegexList.get(i);
		}
		return patternsRegex;
	}

	/**
	 * This function reads configurations of the file and pass the values
	 *
	 * @throws ConfigurationException
	 */
	private void initProperties() throws ConfigurationException {

		File configurationFile = new File("repository/conf/openNLP.xml");
		XMLConfiguration configuration = new XMLConfiguration();
		configuration.setFile(configurationFile);

		try {
			configuration.load();
			configurations[0] = configuration.getString("registryURL");
			configurations[1] = configuration.getString("UserName");
			configurations[2] = configuration.getString("PassWord");
			configurations[3] = configuration.getString("key_storeFilePath");

			Pattern[] patternRegex = new Pattern[2];
			int[] patternLength = new int[2];
			for (int id = 0; id < configuration.getMaxIndex("Patterns.Pattern"); id++) {
				patternRegex[0] = Pattern.compile(
						configuration.getString("Patterns.Pattern(" + id + ").Token1.Regex"));
				patternRegex[1] = Pattern.compile(
						configuration.getString("Patterns.Pattern(" + id + ").Token2.Regex"));
				patternRegexList.add(patternRegex.clone());
				patternLength[0] =
						configuration.getInt("Patterns.Pattern(" + id + ").Token1.Length");
				patternLength[1] =
						configuration.getInt("Patterns.Pattern(" + id + ").Token2.Length");
				patternLengthList.add(patternLength.clone());
			}
		} catch (ConfigurationException e) {
			throw new ConfigurationException(
					"Error occurred when loading opennlp configuration file.", e);
		}

	}

	/**
	 * This functions returns the particular file from the registry
	 *
	 * @param filePath relative path entered in the extension
	 * @return Return file from the registry
	 * @throws java.io.IOException
	 * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
	 * @throws org.apache.commons.configuration.ConfigurationException
	 */
	public File retrieveModel(String filePath)
			throws IOException, RegistryException, ConfigurationException {

		initProperties();
		try {
			File tempFile = File.createTempFile("temp-file-name", ".tmp");

			RemoteRegistry remoteRegistry;

			System.setProperty("javax.net.ssl.trustStore", configurations[3]);
			System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
			System.setProperty("javax.net.ssl.trustStoreType", "JKS");

			//checking for username
			if (configurations[1] == null) {
				remoteRegistry = new RemoteRegistry(new URL(configurations[0]));
			} else {
				remoteRegistry = new RemoteRegistry(new URL(configurations[0]), configurations[1],
				                                    configurations[2]);
			}

			if (remoteRegistry.resourceExists(filePath)) {
				RegistryClientUtils.exportFromRegistry(tempFile, filePath, remoteRegistry);
			} else {
				log.error(filePath + " file don't exists in the registry");
			}
			return tempFile;
		} catch (IOException e) {
			throw new IOException(
					"IOException Error occurred when fetching the file from registry ",
					e);
		} catch (RegistryException e) {
			throw new RegistryException(
					"RegistryException Error occurred when fetching the file from registry");
		}
	}
}

