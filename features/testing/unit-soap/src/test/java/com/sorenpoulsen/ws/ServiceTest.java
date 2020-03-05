/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.sorenpoulsen.ws;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tibor17.soap.TestPortType;
import org.tibor17.soap.TestRequest;
import org.tibor17.soap.TestResponse;
import org.tibor17.soap.TestService;

import javax.xml.ws.BindingProvider;

/**
 * Test the JAX-WS service client by calling the SOAPUI mock service.
 */
public class ServiceTest {
	private String mockEndpoint;

	@Before
	public void readMockPort() {
		String mockport = System.getProperty("soap.mock.port");
		if (mockport == null || mockport.isEmpty()) {
			mockport = "8088";
		}
		mockEndpoint = "http://localhost:" + mockport + "/testservice";
		System.out.println(mockEndpoint);
	}

	/**
	 * Sending the message "hello" must return a "triggered" response.
	 */
	@Test
	public void testTriggeredResponse() {
		TestService testService = new TestService();
		TestPortType testPort = testService.getTestPort();
		BindingProvider binding = (BindingProvider) testPort;
		binding.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mockEndpoint);
		TestRequest testRequest = new TestRequest();
		testRequest.setMessage("hello");
		TestResponse sendMessage = testPort.sendMessage(testRequest);
		String response = sendMessage.getResponse();
		Assert.assertEquals("triggered", response);
	}

	/**
	 * Sending any other message must return the "Default" response
	 */
	@Test
	public void testDefaultResponse() {
		TestService testService = new TestService();
		TestPortType testPort = testService.getTestPort();
		BindingProvider binding = (BindingProvider) testPort;
		binding.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, mockEndpoint);
		TestRequest testRequest = new TestRequest();
		testRequest.setMessage("xyz");
		TestResponse sendMessage = testPort.sendMessage(testRequest);
		String response = sendMessage.getResponse();
		Assert.assertEquals("Default", response);
	}
}
