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
package javaee.samples.utils.propertiesloader;

import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PropertiesLoaderTest {

    @Test
    public void shouldLoadEnglishAuditLog() {
        PropertiesLoader loader = new PropertiesLoader("AuditLog", Locale.ENGLISH);
        String msg = "[Example: English]Registered (me) with identifications (RFID) and products (Parking).";
        assertThat(loader.load("registrationaction.newcustomer", "me", "RFID", "Parking"), is(msg));
    }

    @Test
    public void shouldLoadDefaultAuditLog() {
        PropertiesLoader loader = new PropertiesLoader("AuditLog");
        String msg = "Registered (me) with identifications (RFID) and products (Parking).";
        assertThat(loader.load("registrationaction.newcustomer", "me", "RFID", "Parking"), is(msg));
    }

    @Test
    public void shouldLoadEnglishAuditLogFromClassBundle() {
        PropertiesLoader loader = new PropertiesLoader("AuditLog", getClass(), Locale.ENGLISH);
        String msg = "[Example: English]Registered (me) with identifications (RFID) and products (Parking).";
        assertThat(loader.load("registrationaction.newcustomer", "me", "RFID", "Parking"), is(msg));
    }

    @Test
    public void shouldLoadDefaultAuditLogFromClassBundle() {
        PropertiesLoader loader = new PropertiesLoader("AuditLog", getClass());
        String msg = "Registered (me) with identifications (RFID) and products (Parking).";
        assertThat(loader.load("registrationaction.newcustomer", "me", "RFID", "Parking"), is(msg));
    }

}
