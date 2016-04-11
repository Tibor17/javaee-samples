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
package audit.query.search.api;

import javax.enterprise.inject.Vetoed;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Vetoed
public final class AuditQuery implements Paging, Serializable {
    private static final long serialVersionUID = 1;

    private int startRowNum, pageSize;
    private Calendar from, to;

    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("startRowNum", Integer.TYPE),
            new ObjectStreamField("pageSize", Integer.TYPE),
            new ObjectStreamField("from", String.class),
            new ObjectStreamField("to", String.class),
    };

    @Override
    public int getStartRowNum() {
        return startRowNum;
    }

    @Override
    public AuditQuery setStartRowNum(@Min(0) int startRowNum) {
        this.startRowNum = startRowNum;
        return this;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public AuditQuery setPageSize(@Min(1) int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Calendar getFrom() {
        return from;
    }

    public Calendar getTo() {
        return to;
    }

    public boolean hasFromTo() {
        return from != null && to != null;
    }

    public AuditQuery setFromTo(@NotNull Calendar from, @NotNull Calendar to) {
        this.from = requireNonNull(from);
        this.to = requireNonNull(to);
        return this;
    }
}
