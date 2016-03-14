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
package audit.domain.unittests;

import audit.domain.UuidConverter;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UuidConverterTest {
    @Test
    public void shouldConvertToNull() {
        UuidConverter c = new UuidConverter();

        assertThat(c)
                .extracting(t -> t.convertToEntityAttribute(""))
                .containsNull();

        assertThat(c)
                .extracting(t -> t.convertToEntityAttribute(null))
                .containsNull();
    }

    @Test
    public void shouldConvertUUID() {
        UUID uuid = UUID.randomUUID();
        UuidConverter converter = new UuidConverter();

        String encoded = converter.convertToDatabaseColumn(uuid);
        assertThat(encoded)
                .hasSize(36);

        assertThat(encoded)
                .isEqualTo(uuid.toString());

        assertThat(converter.convertToEntityAttribute(encoded))
                .isEqualTo(uuid);
    }
}
