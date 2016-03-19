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
package app;

import java.io.InputStream;
import java.net.*;

import static java.net.Proxy.Type.HTTP;

public final class ProxyMain {
    public void main(String... args) throws Exception {
        SocketAddress proxy = new InetSocketAddress(InetAddress.getByName("isaserver.vsb"), 3128);
        URLConnection connection = new URL("http://www.google.com").openConnection(new Proxy(HTTP, proxy));
        InputStream is = connection.getInputStream();
        System.out.println(is.available());

        // or Another alternative with system properties which are global in entire application:
        // -Dhttp.proxyHost=isaserver.vsb
        // -Dhttp.proxyPort=3128
        // -Dhttp.nonProxyHosts="localhost|127.0.0.1|10.*.*.*|mailserver"
        // -Dhttp.proxyUser=<your proxy LDAP username>
        // -Dhttp.proxyPassword=<your proxy password>
    }
}
