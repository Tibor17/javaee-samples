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
package client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.Integer.getInteger;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.*;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

public class RestfulClientTest {
    private final static int PORT = getInteger("test.rest.server.port", 8089);

    private final RestClientService client = new RestClientService();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Rule
    public ErrorCollector $ = new ErrorCollector();

    @Before
    public void resetServer() {
        resetToDefault();
        wireMockRule.addMockServiceRequestListener((request, response) -> {
            System.out.println("request:");
            System.out.println(request.getMethod().value() + " " + request.getUrl() + " HTTP/1.1");
            request.getHeaders().all().forEach(System.out::print);
            System.out.println(request.getBodyAsString());

            System.out.println("response:");
            System.out.println("HTTP/1.1 " + response.getStatus());
            response.getHeaders().all().forEach(System.out::print);
            System.out.println(response.getBodyAsString());

            System.out.println();
        });
    }

    @Before
    public void startClient() throws URISyntaxException {
        client.connection = new URI("http://localhost:" + PORT);
        client.connect();
    }

    @After
    public void stopClient() {
        client.closeConnection();
    }

    @Test
    public void shouldGetXML() {
        serverMockWithGET(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "\n<resources xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                        + "\n\t<x>4</x>"
                        + "\n\t<resource from=\"marry@smith.com\">"
                        + "\n\t\t<gender>W</gender>"
                        + "\n\t\t<content>Some content</content>"
                        + "\n\t</resource>"
                        + "\n\t<resource from=\"john@smith.com\">"
                        + "\n\t\t<gender>M</gender>"
                        + "\n\t\t<content>Another content</content>"
                        + "\n\t</resource>"
                        + "\n</resources>");

        ResourcesType resources = client.findIssueById(5);

        $.checkThat(resources.getX(), is(4));
        $.checkThat(resources.getResource(), hasSize(2));
        $.checkThat(resources.getResource().get(0).getFrom(), is("marry@smith.com"));
        $.checkThat(resources.getResource().get(0).getContent(), is("Some content"));
        $.checkThat(resources.getResource().get(0).getGender(), is("W"));
        $.checkThat(resources.getResource().get(1).getFrom(), is("john@smith.com"));
        $.checkThat(resources.getResource().get(1).getContent(), is("Another content"));
        $.checkThat(resources.getResource().get(1).getGender(), is("M"));

        serverGetRequestVerification();
    }

    @Test
    public void shouldPutXML() {
        serverMockWithPUT();

        ResourcesType resources = new ResourcesType()
                .setX(4);

        resources.getResource().add(
                new ResourceType()
                .setFrom("marry@smith.com")
                .setGender("W")
                .setContent("Some content"));

        resources.getResource().add(
                new ResourceType()
                .setFrom("john@smith.com")
                .setGender("M")
                .setContent("Any content"));

        client.overwriteIssue(5, resources);

        serverPutRequestVerification();
    }

    @Test
    public void shouldGetCollection() {
        serverMockWithGET(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                        + "\n<resources xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                        + "\n\t<resource from=\"marry@smith.com\">"
                        + "\n\t\t<gender>W</gender>"
                        + "\n\t\t<content>Some content</content>"
                        + "\n\t</resource>"
                        + "\n\t<resource from=\"john@smith.com\">"
                        + "\n\t\t<gender>M</gender>"
                        + "\n\t\t<content>Another content</content>"
                        + "\n\t</resource>"
                        + "\n</resources>");

        List<ResourceType> resources = client.findIssueByIdAsList(5);

        $.checkThat(resources, hasSize(2));
        $.checkThat(resources.get(0).getFrom(), is("marry@smith.com"));
        $.checkThat(resources.get(0).getContent(), is("Some content"));
        $.checkThat(resources.get(0).getGender(), is("W"));
        $.checkThat(resources.get(1).getFrom(), is("john@smith.com"));
        $.checkThat(resources.get(1).getContent(), is("Another content"));
        $.checkThat(resources.get(1).getGender(), is("M"));

        serverGetRequestVerification();
    }

    private void serverMockWithGET(String bodyXml) {
        givenThat(get(urlPathMatching("/rest/api/2.0-alpha1/issues/id/5"))
                .withHeader("Accept", containing(APPLICATION_XML))
                .withHeader("Accept-Charset", equalTo("UTF-8"))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", APPLICATION_XML + "; charset=UTF-8")
                        .withBody(bodyXml)));
    }

    private void serverMockWithPUT() {
        givenThat(put(urlPathMatching("/rest/api/2.0-alpha1/issues/id/5"))
                .withHeader("Content-Type", equalTo(APPLICATION_XML))
                .withRequestBody(matchingXPath("/resources[x = 4]"))
                .withRequestBody(matchingXPath("/resources[count(resource) = 2]"))
                .willReturn(aResponse().withStatus(HTTP_OK)));
    }

    private static void serverGetRequestVerification() {
        verify(getRequestedFor(urlMatching("/rest/api/2.0-alpha1/issues/id/[0-9]*"))
                .withHeader("Accept", containing(APPLICATION_XML)));
    }

    private static void serverPutRequestVerification() {
        verify(putRequestedFor(urlMatching("/rest/api/2.0-alpha1/issues/id/[0-9]*"))
                .withHeader("Content-Type", equalTo(APPLICATION_XML)));
    }
}
