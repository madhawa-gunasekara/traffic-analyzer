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

package org.wso2.fasttrack.project.roadlk.publisher;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * TestPublisher class posts tweets to @TrafficAnalyse Twitter account. This is
 * used for testing purposes.
 */

public class TestPublisher {

	public static void main(String[] args) {
		TestPublisher testPublisher = new TestPublisher();

		String consumerKey = "OLxKYv2XxfAwUCW4nui2xy6V9";
		String consumerSecret = "jpJ1z84ix20vV97EcT3Q5KVrrn4PMEr8SXhJCBqGl8i7OPG4g0";
		String accessToken = "2857575000-CdhvMvTsuErPlgu8U454cbCrsZhJDO1sULxym4c";
		String accessTokenSecret = "HZTf71KfO0jI2pKOwE3FS9JOooD6SaBhpMurOl0IBZmDR";
		long timeInterval = 5000;

		try {
			testPublisher.publishToTwitter(consumerKey, consumerSecret, accessToken,
			                               accessTokenSecret, timeInterval);
		} catch (Exception e) {
			LOGGER.error("Exception occurred." + e.getMessage());
		}
	}

	private static final Logger LOGGER = Logger.getLogger(TestPublisher.class);

	/**
	 * @param consumerKey
	 *            Twitter Consumer Key (API Key)
	 * @param consumerSecret
	 *            Twitter Consumer Secret (API Secret)
	 * @param accessToken
	 *            Twitter Access Token
	 * @param accessTokenSecret
	 *            Twitter Access Token Secret
	 * @param timeInterval
	 *            Time interval between tweets
	 * @throws InterruptedException
	 * @throws TwitterException
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void publishToTwitter(String consumerKey, String consumerSecret, String accessToken,
	                             String accessTokenSecret, long timeInterval)
	                                                                         throws InterruptedException,
	                                                                         TwitterException,
	                                                                         IOException {

		// Generated dataset file from the Twitter feed of @road_lk
		String file = "dataset.txt";

		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException();
		}

		// One tweet read from dataset file
		String originalTweet;

		// Twitter object of Twitter4J library
		Twitter twitter = TwitterFactory.getSingleton();

		// Twitter API authentication
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));

		try {
			while ((originalTweet = bufferedReader.readLine()) != null) {
				try {
					Thread.sleep(timeInterval);
				} catch (InterruptedException e) {
					throw new InterruptedException();
				}

				// Edited tweet to be posted in @TrafficAnalyse Twitter account
				String editedTweet =
				                     "@TrafficAnalyse " +
				                             originalTweet.replaceAll("@\\w+", "")
				                                          .replaceAll(":", "")
				                                          .replaceAll("\\s+", " ")
				                                          .replaceAll("RT ", "");

				// Trim tweet length to maximum allowed tweet length
				if (editedTweet.length() > 140) {
					editedTweet = editedTweet.substring(0, 140);
				}

				Status status = null;
				try {
					status = twitter.updateStatus(editedTweet);
				} catch (TwitterException e) {
					throw new TwitterException(e);
				}
				LOGGER.debug("Successfully updated the status to [" + status.getText() + "].");
			}
		} catch (IOException e) {
			throw new IOException(e);
		}
		try {
			bufferedReader.close();
		} catch (IOException e) {
			throw new IOException(e);
		}
	}
}