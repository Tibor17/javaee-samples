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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import java.net.URI;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.util.Locale.ENGLISH;
import static javax.ws.rs.client.Entity.xml;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

@ApplicationScoped
public class RestClientService {
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

    public ResourcesType findIssueById(int issueId) {
        return client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/id/{id}")
                .resolveTemplate("id", issueId)
                .request(APPLICATION_XML)
                .header("Accept-Charset", "UTF-8")
                .get(ResourcesType.class);
    }

    public List<ResourceType> findIssueByIdAsList(int issueId) {
        return client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/id/{id}")
                .resolveTemplate("id", issueId)
                .request(APPLICATION_XML_TYPE)
                .header("Accept-Charset", "UTF-8")
                .get(new GenericType<List<ResourceType>>() {});
    }

    public void overwriteIssue(int issueId, ResourcesType resource) {
        Response response = client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/id/{id}")
                .resolveTemplate("id", issueId)
                .request()
                //.acceptEncoding("gzip")
                .header("Accept-Charset", "UTF-8")
                .acceptLanguage(ENGLISH)
                .put(xml(resource));

        if (response.getStatus() >= HTTP_BAD_REQUEST) {
            StatusType stat = response.getStatusInfo();
            throw new IllegalStateException(stat.getStatusCode() + "/" + stat.getFamily().name()
                    + " " + stat.getReasonPhrase());
        }
    }
}
