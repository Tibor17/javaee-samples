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
package unit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.logging.Logger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.xml.XmlPath.with;
import static com.jayway.restassured.http.ContentType.XML;
import static java.lang.Integer.getInteger;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.xml.HasXPath.hasXPath;

/**
 * You can reach the server at
 * {@linkplain http://localhost:8089/my/resource/5?jql=project%20=%20BAM%20AND%20issuetype%20=%20Bug}
 * Server Stubbing API:
 * @apiNote http://wiremock.org/stubbing.html
 */
public class HttpMockTest {
    private static final Logger LOG = Logger.getGlobal();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(getInteger("test.rest.server.port", 8089));

    @Before
    public void resetServer() {
        resetToDefault();
    }

    @Test
    public void exampleWithFrameworks_RestAssured_And_WireMock() {
        serverMockWithWireMock(
                "<responses>"
                        + "<response>Some content</response>" +
                "</responses>");

        // do something
        // just mocking
        given().accept(XML)
                .body("<messages><message>1234</message></messages>")
                        //.request().param("param-key", "param-val")
                .queryParam("jql", "project = MPROJECT AND issuetype = Bug")
                .get("http://localhost:" + wireMockRule.port() + "/rest/api/2.0-alpha1/issues/id/{id}", 5)
                .then()
                .assertThat()
                .contentType(XML).and().statusCode(HTTP_OK)
                .and().body("responses.response", is("Some content"))
                .and().body(hasXPath("/responses/response", is("Some content")));

        serverVerificationWithWireMock();
    }

    /**
     * @apiNote http://static.javadoc.io/com.jayway.restassured/xml-path/2.8.0/com/jayway/restassured/path/xml/XmlPath.html
     */
    @Test
    public void xPathAssertionsExample() {
        serverMockWithWireMock(
                "<responses>"
                        + "<response from=\"marry@smith.com\">"
                            + "<gender>W</gender>"
                            + "<content>Some content</content>"
                        + "</response>"
                        + "<response from=\"john@smith.com\">"
                            + "<gender>M</gender>"
                            + "<content>Another content</content>"
                        + "</response>" +
                "</responses>");

        String response = given().accept(XML)
                .body("<messages><message>1234</message></messages>")
                .queryParam("jql", "project = MPROJECT AND issuetype = Bug")
                .get("http://localhost:" + wireMockRule.port() + "/rest/api/2.0-alpha1/issues/id/{id}", 5)
                .asString();
        LOG.info(with(response).prettyPrint());
        assertThat(with(response).get("responses.response[1].content"), is("Another content"));
        assertThat(with(response).param("email", "marry@smith.com").get("**.findAll {it.@from != email}.content"), hasItems("Another content"));

        // do something
        // just mocking
        given().accept(XML)
                .body("<messages><message>1234</message></messages>")
                .queryParam("jql", "project = MPROJECT AND issuetype = Bug")
                .get("http://localhost:" + wireMockRule.port() + "/rest/api/2.0-alpha1/issues/id/{id}", 5)
                .then()
                .assertThat()
                .contentType(XML).and().statusCode(HTTP_OK)
                .and().body("responses.response.text()", containsString("Some"))
                .and().body("responses.response.findAll {it.@from == 'marry@smith.com'}.size()", is(1))
                .and().body("responses.response.find {it.@from == 'marry@smith.com'}.content", is("Some content"))
                .and().body("**.find {it.@from == 'marry@smith.com'}.content", is("Some content"))
                .and().body("**.findAll {it.@from != 'marry@smith.com'}.content", hasItem("Another content"));

        serverVerificationWithWireMock();
    }

    private static void serverVerificationWithWireMock() {
        verify(getRequestedFor(urlMatching("/rest/api/2.0-alpha1/issues/id/[a-z0-9]+(\\?.*)?"))
                .withQueryParam("jql", equalTo("project = MPROJECT AND issuetype = Bug"))
                .withRequestBody(matching(".*<message>1234</message>.*"))
                .withHeader("Content-Length", matching("44"))
                .withHeader("Accept", matching(".*;?\\s*text/xml\\s*;?.*")));
    }

    private void serverMockWithWireMock(String bodyXml) {
        // See the URL escape characters http://www.w3schools.com/tags/ref_urlencode.asp
        givenThat(get(urlPathMatching("/rest/api/2.0-alpha1/issues/id/5"))
                .withHeader("Accept", containing("text/xml"))
                .withQueryParam("jql", equalTo("project%20%3D%20MPROJECT%20AND%20issuetype%20%3D%20Bug"))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", "text/xml; UTF-8")
                        .withBody(bodyXml)));
    }
}
