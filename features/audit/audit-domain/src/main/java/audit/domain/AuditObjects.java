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
package audit.domain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import static java.util.Arrays.asList;

public final class AuditObjects implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * @serial size of array
     */
    private int size;

    private transient Audit[] audits = new Audit[0];

    public AuditObjects(Audit... audits) {
        this.audits = audits;
        size = audits.length;
    }

    public List<Audit> toList() {
        return asList(audits);
    }

    /**
     * @serialData serializing (size by default), Audit objects
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        for (Audit audit : audits) {
            stream.writeObject(audit);
        }
    }

    /**
     * @serialData deserializing (size by default), Audit objects
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        audits = new Audit[size];
        for (int i = 0; i < size; i++) {
            audits[i] = (Audit) stream.readObject();
        }
    }
}
