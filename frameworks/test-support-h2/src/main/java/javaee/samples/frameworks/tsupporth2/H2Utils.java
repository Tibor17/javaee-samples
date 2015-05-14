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
package javaee.samples.frameworks.tsupporth2;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class H2Utils {

  private H2Utils() {
    throw new IllegalStateException("Not instantiated constructor.");
  }

  public static void shutdownH2(File dbPath) throws IOException, SQLException {
    shutdownH2(dbPath, "sa", "");
  }

  public static void shutdownH2(File dbPath, String user, String password) throws IOException, SQLException {
    shutdownH2(dbPath.getCanonicalPath(), user, password);
  }

  public static void shutdownH2(String dbPath) throws SQLException {
    shutdownH2("jdbc:h2:" + dbPath, "sa", "");
  }

  public static void shutdownH2(String url, String user, String password) throws SQLException {
    // Don't use SHUTDOWN IMMEDIATELY. It results in thrown exception saying that the connection is already closed.
    execute(url, user, password, "SHUTDOWN");
  }

  public static void dropAllObjects(File dbPath) throws IOException, SQLException {
    dropAllObjects(dbPath.getCanonicalPath());
  }

  public static void dropAllObjects(String dbPath) throws SQLException {
    dropAllObjects("jdbc:h2:" + dbPath, "sa", "");
  }

  public static void dropAllObjects(String url, String user, String password) throws SQLException {
    execute(url, user, password, "DROP ALL OBJECTS");
  }

  public static boolean execute(String url, String user, String password, String command) throws SQLException {
    org.h2.Driver.load();
    try (Connection conn = DriverManager.getConnection(url, user, password); Statement stat = conn.createStatement()) {
      return stat.execute(command);
    }
  }

}