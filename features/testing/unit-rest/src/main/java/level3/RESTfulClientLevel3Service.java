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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.StatusType;
import java.net.URI;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static javax.ws.rs.client.Entity.xml;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@ApplicationScoped
public class RESTfulClientLevel3Service {
    @Resource
    URI connection;

    private Client client;

    @PostConstruct
    void connect() {
        // cannot be cached in RestEasy:3.0.4. Since of 3.0.8 it is fixed and client can be cached.
        client = ClientBuilder.newBuilder().build();
    }

    @PostConstruct
    void closeConnection() {
        client.close();
    }

    /**
     * @apiNote URI path escape characters https://en.wikipedia.org/wiki/Percent-encoding
     */
    public AppointmentsType findAppointments(String doctor, String patient, int offset, int pageSize) {
        Response response = client.target(connection)
                .path("/rest/api/appointments/{doctor}/{patient}")
                .resolveTemplate("doctor", doctor)
                .resolveTemplate("patient", patient)
                .resolveTemplate("offset", offset)
                .resolveTemplate("pageSize", pageSize)
                .queryParam("offset", offset)
                .queryParam("pagesize", pageSize)
                .request(APPLICATION_XML)
                .header("Accept-Charset", "UTF-8")
                .get();

        try {
            return response.getLength() == 0 ? null : response.readEntity(AppointmentsType.class);
        } finally {
            response.close();
        }
    }

    public URI insertAppointment(AppointmentType resource) {
        Response response = client.target(connection)
                .path("/rest/api/appointments")
                .request()
                // overrides previous headers: Content-Type, Content-Language, Content-Encoding
                .post(xml(resource));

        response.close();

        if (response.getStatus() >= HTTP_BAD_REQUEST) {
            StatusType stat = response.getStatusInfo();
            throw new IllegalStateException(stat.getStatusCode() + "/" + stat.getFamily().name()
                    + " " + stat.getReasonPhrase());
        }

        return response.getLocation();
    }
}
