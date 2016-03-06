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
package dao;

import java.util.Objects;
import static java.util.Objects.hash;

public final class Entry {
  private final Alias alias;
  private final String property;

  public Entry(Alias alias, String property) {
    this.alias = alias;
    this.property = property;
  }

  public Alias getAlias() {
    return alias;
  }

  public String getProperty() {
    return property;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Entry entry = (Entry) o;
    return Objects.equals(alias, entry.alias) &&
           Objects.equals(property, entry.property);
  }

  @Override
  public int hashCode() {
    return hash(alias, property);
  }

  @Override
  public String toString() {
    return "Entry{" +
           "alias=" + alias.asString() +
           ", property=" + property +
           '}';
  }
}
