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
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.StatusType;
import java.net.URI;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.client.Entity.xml;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
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
        Response response = client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/{id}")
                .resolveTemplate("id", issueId)
                .request(APPLICATION_XML)
                .header("Accept-Charset", "UTF-8")
                .get();
        try {
            // cache and restore the same entity object
            // in case of multiple calls of readEntity() or getEntity()
            response.bufferEntity();

            return response.getLength() == 0 ? null : response.readEntity(ResourcesType.class);
        } finally {
            response.close();
        }
    }

    public List<ResourceType> findIssueByIdAsList(int issueId) {
        Response response = client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/{id}")
                .resolveTemplate("id", issueId)
                .request(APPLICATION_XML_TYPE)
                //.acceptEncoding("gzip")
                //.acceptLanguage(ENGLISH)
                .header("Accept-Charset", "UTF-8")
                .get();

        try {
            return response.readEntity(new GenericType<List<ResourceType>>() {});
        } finally {
            response.close();
        }
    }

    public void updateIssueWithoutCharset(int issueId, ResourcesType resource) {
        Response response = client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/{id}")
                .resolveTemplate("id", issueId)
                .request()
                // overrides previous headers: Content-Type, Content-Language, Content-Encoding
                .put(xml(resource));

        response.close();

        if (response.getStatus() >= HTTP_BAD_REQUEST) {
            StatusType stat = response.getStatusInfo();
            throw new IllegalStateException(stat.getStatusCode() + "/" + stat.getFamily().name()
                    + " " + stat.getReasonPhrase());
        }
    }

    public void updateIssue(int issueId, ResourcesType resource) {
        Response response = client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/{id}")
                .resolveTemplate("id", issueId)
                .request()
                        // overrides previous headers: Content-Type, Content-Language, Content-Encoding
                .put(entity(resource, MediaType.valueOf(APPLICATION_XML + "; charset=UTF-8")));

        response.close();

        if (response.getStatus() >= HTTP_BAD_REQUEST) {
            StatusType stat = response.getStatusInfo();
            throw new IllegalStateException(stat.getStatusCode() + "/" + stat.getFamily().name()
                    + " " + stat.getReasonPhrase());
        }
    }

    public void updateIssueAsJson(int issueId, ResourcesType resource) {
        Response response = client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/{id}")
                .resolveTemplate("id", issueId)
                .request()
                // overrides previous headers: Content-Type, Content-Language, Content-Encoding
                .put(entity(resource, MediaType.valueOf(APPLICATION_JSON)));

        response.close();

        if (response.getStatus() >= HTTP_BAD_REQUEST) {
            StatusType stat = response.getStatusInfo();
            throw new IllegalStateException(stat.getStatusCode() + "/" + stat.getFamily().name()
                    + " " + stat.getReasonPhrase());
        }
    }

    public boolean updateIssueIfLast(int issueId, ResourcesType resource, String md5) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("If-Match", md5);

        Response response = client.target(connection)
                .path("/rest/api/2.0-alpha1/issues/{id}")
                .resolveTemplate("id", issueId)
                .request()
                .headers(headers)
                // overrides previous headers: Content-Type, Content-Language, Content-Encoding
                .put(entity(resource, MediaType.valueOf(APPLICATION_XML + "; charset=UTF-8")));

        response.close();

        if (response.getStatus() != HTTP_OK && response.getStatus() != HTTP_NOT_MODIFIED) {
            StatusType stat = response.getStatusInfo();
            throw new IllegalStateException(stat.getStatusCode() + "/" + stat.getFamily().name()
                    + " " + stat.getReasonPhrase());
        }

        return response.getStatus() == HTTP_OK;
    }
}
