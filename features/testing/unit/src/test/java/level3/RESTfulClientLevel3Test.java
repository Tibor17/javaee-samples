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
package level3;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.Integer.getInteger;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static level3.RelationType.SELF;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class RESTfulClientLevel3Test {
    private final static int PORT = getInteger("test.rest.server.port", 8089);

    private final RESTfulClientLevel3Service client = new RESTfulClientLevel3Service();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Rule
    public ErrorCollector $ = new ErrorCollector();

    @Before
    public void resetServer() {
        resetToDefault();
        wireMockRule.addMockServiceRequestListener((request, response) -> {
            System.out.println("=== REQUEST ===");
            System.out.println(request.getMethod().value() + " " + request.getUrl() + " HTTP/1.1");
            request.getHeaders().all().forEach(System.out::print);
            String body = request.getBodyAsString().trim();
            if (!body.isEmpty()) System.out.println(body);

            System.out.println("=== RESPONSE ===");
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
    public void shouldGetAppointments() throws URISyntaxException {
        serverMockWithGET(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                        "<appointments>" +
                            "<link rel=\"self\" uri=\"/rest/api/appointments/mjones/jsmith\"/>" +
                        "</appointments>");

        AppointmentsType appointments = client.findAppointments("mjones", "jsmith", 30, 10);
        $.checkThat(appointments, is(notNullValue()));
        $.checkThat(appointments.getLink(), is(notNullValue()));
        $.checkThat(appointments.getLink().getRel(), is(notNullValue()));
        $.checkThat(appointments.getLink().getRel(), is(SELF));
        $.checkThat(appointments.getLink().getRel(), is(notNullValue()));
        $.checkThat(appointments.getLink().getUri(), is(new URI("/rest/api/appointments/mjones/jsmith")));

        serverGetRequestVerification();
    }

    @Test
    public void shouldPostAppointments() throws URISyntaxException {
        serverMockWithPOST();

        AppointmentType resource = new AppointmentType();

        URI location = client.insertAppointment(resource);
        $.checkThat(location, is(notNullValue()));
        $.checkThat(location, is(new URI("/rest/api/appointments/320579")));

        serverPostRequestVerification();
    }

    private void serverMockWithGET(String bodyXml) {
        givenThat(get(urlPathEqualTo("/rest/api/appointments/mjones/jsmith"))
                .withQueryParam("offset", equalTo("30"))
                .withQueryParam("pagesize", equalTo("10"))
                .withHeader("Accept", containing(APPLICATION_XML))
                .withHeader("Accept-Charset", equalTo("UTF-8"))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Content-Type", APPLICATION_XML + "; charset=UTF-8")
                        .withBody(bodyXml)));
    }

    private void serverMockWithPOST() {
        givenThat(post(urlPathEqualTo("/rest/api/appointments"))
                .withHeader("Content-Type", equalTo(APPLICATION_XML))
                .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withHeader("Location", "/rest/api/appointments/320579")));
    }

    private static void serverGetRequestVerification() {
        verify(getRequestedFor(urlPathEqualTo("/rest/api/appointments/mjones/jsmith"))
                .withQueryParam("offset", equalTo("30"))
                .withQueryParam("pagesize", equalTo("10"))
                .withHeader("Accept", containing(APPLICATION_XML)));
    }

    private static void serverPostRequestVerification() {
        verify(postRequestedFor(urlPathEqualTo("/rest/api/appointments"))
                .withHeader("Content-Type", containing(APPLICATION_XML)));
    }
}
