/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.fasttrack.project.roadlk.rest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * DatasetGenerator class generates a dataset from the Twitter feed of @road_lk
 * Twitter account.
 */

public class DatasetGenerator {

	private static final Logger LOGGER = Logger.getLogger(DatasetGenerator.class);

	public static void main(String[] args) {
		DatasetGenerator datasetGenerator = new DatasetGenerator();

		String consumerKey = "OLxKYv2XxfAwUCW4nui2xy6V9";
		String consumerSecret = "jpJ1z84ix20vV97EcT3Q5KVrrn4PMEr8SXhJCBqGl8i7OPG4g0";
		String accessToken = "2857575000-CdhvMvTsuErPlgu8U454cbCrsZhJDO1sULxym4c";
		String accessTokenSecret = "HZTf71KfO0jI2pKOwE3FS9JOooD6SaBhpMurOl0IBZmDR";
		int pageLimit = 30;

		try {
			datasetGenerator.generateDataset(consumerKey, consumerSecret, accessToken,
			                                 accessTokenSecret, pageLimit);
		} catch (Exception e) {
			LOGGER.error("Exception occurred." + e.getMessage());
		}
	}

	/**
	 * @param consumerKey
	 *            Twitter Consumer Key (API Key)
	 * @param consumerSecret
	 *            Twitter Consumer Secret (API Secret)
	 * @param accessToken
	 *            Twitter Access Token
	 * @param accessTokenSecret
	 *            Twitter Access Token Secret
	 * @param pageLimit
	 *            Maximum pages to be retrieved
	 * @throws IOException
	 * @throws TwitterException
	 */
	@SuppressWarnings("resource")
	public void generateDataset(String consumerKey, String consumerSecret, String accessToken,
	                            String accessTokenSecret, int pageLimit) throws IOException,
	                                                                    TwitterException {
		// Twitter object of Twitter4J library
		Twitter twitter = TwitterFactory.getSingleton();

		// Twitter API authentication
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));

		PrintWriter printWriter = null;

		try {
			printWriter = new PrintWriter(new BufferedWriter(new FileWriter("dataset.txt", true)));
		} catch (IOException e) {
			throw new IOException(e);
		}

		LOGGER.debug("Twitter feed extraction started.");

		for (int i = 1; i < pageLimit; i = i + 1) {

			Paging paging = new Paging(i, 100);
			ResponseList<Status> statuses = null;

			try {
				statuses = twitter.getUserTimeline("road_lk", paging);
			} catch (TwitterException e) {
				//LOGGER.error("TwitterException occurred." + e.getMessage());
				throw new TwitterException(e);
			}

			for (Status status : statuses) {
				printWriter.println(status.getCreatedAt() + ": " + status.getText());
			}
		}
		printWriter.close();
		LOGGER.debug("Twitter feed extraction completed.");
	}
}