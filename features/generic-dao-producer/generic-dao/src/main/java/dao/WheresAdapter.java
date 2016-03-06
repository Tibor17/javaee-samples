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

import com.querydsl.core.types.dsl.DslExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public abstract class WheresAdapter implements Wheres {
  private final List<Class<?>> aliasTypes = new ArrayList<>();
  private final Map<String, Object> embeddedCriteria;

  public WheresAdapter(Map<String, Object> embeddedCriteria) {
    this.embeddedCriteria = embeddedCriteria;
  }

  public abstract JPAQuery where(JPAQuery query, Map<Alias, PathBuilder<?>> builders, Map<Entry, Object> params);
  protected abstract boolean isKeyFilterable(String key);
  protected abstract String filterableAlias(String key);
  protected abstract String filterableProperty(String key);

  @Override
  public void where(JPAQuery query, Map<Alias, PathBuilder<?>> builders) {
    Map<Entry, Object> customCriteria = new HashMap<>();
    for (Iterator<Map.Entry<String, Object>> all = embeddedCriteria.entrySet().iterator(); all.hasNext(); ) {
      Map.Entry<String, Object> embedded = all.next();
      if (isKeyFilterable(embedded.getKey())) {
        Alias alias = new Alias(filterableAlias(embedded.getKey()));
        if (builders.containsKey(alias)) {
          customCriteria.put(new Entry(alias, filterableProperty(embedded.getKey())), embedded.getValue());
          all.remove();
        }
      }
    }
    aliasTypes.clear();

    aliasTypes.addAll(builders.values()
            .stream()
            .map(DslExpression::getType)
            .collect(toList()));

    where(query, builders, customCriteria);
  }

  public List<Class<?>> getAliasTypes() {
    return aliasTypes;
  }

  public Class<?>[] aliasTypes() {
    return aliasTypes.toArray(new Class<?>[aliasTypes.size()]);
  }
}
