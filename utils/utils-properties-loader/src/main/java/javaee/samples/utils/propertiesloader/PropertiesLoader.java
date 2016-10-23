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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

public final class PropertiesLoader {
    private static final Locale DEFAULT_FALLBACK = Locale.ENGLISH;

    private final ResourceBundle loader;
    private final Locale fallback;

    public PropertiesLoader(String bundleName) {
        ResourceBundle resourceBundle;
        try {
            resourceBundle = unlocalizedResourceLoader(bundleName);
        } catch (IllegalStateException | IOException e) {
            resourceBundle = resourceLoader(bundleName, DEFAULT_FALLBACK);
        }
        loader = resourceBundle;
        fallback = DEFAULT_FALLBACK;
    }

    public PropertiesLoader(String bundleName, Class<?> bundleClass) {
        this(bundleName, bundleClass, null);
    }

    public PropertiesLoader(String bundleName, Class<?> bundleClass, Locale expectedLocale) {
        ResourceBundle resourceBundle;
        try {
            resourceBundle = unlocalizedResourceLoader(bundleName, bundleClass, expectedLocale);
        } catch (IllegalStateException | IOException e) {
            resourceBundle = resourceLoader(bundleName, DEFAULT_FALLBACK);
        }
        loader = resourceBundle;
        fallback = DEFAULT_FALLBACK;
    }

    public PropertiesLoader(String bundleName, Locale expectedLocale) {
        this(bundleName, expectedLocale, DEFAULT_FALLBACK);
    }

    public PropertiesLoader(String bundleName, Locale expectedLocale, Locale fallback) {
        this.fallback = fallback;
        loader = resourceLoader(bundleName, expectedLocale);
    }

    public String load(String key) {
        return loader.getString(key);
    }

    public String load(String key, Object... arguments) {
        return MessageFormat.format(loader.getString(key), arguments);
    }

    public Locale getFallbackLocale() {
        return fallback;
    }

    private final class PropertiesLoaderControl extends ResourceBundle.Control {
        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return PropertiesLoader.this.fallback;
        }
    }

    private ResourceBundle unlocalizedResourceLoader(String bundleName) throws IllegalStateException, IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        if (cl == null) {
            throw new IllegalStateException();
        }
        InputStream properties = cl.getResourceAsStream(bundleName + ".properties");
        if (properties == null) {
            throw new IllegalStateException();
        }
        PropertyResourceBundle bundle = new PropertyResourceBundle(properties);
        properties.close();
        return bundle;
    }

    private ResourceBundle unlocalizedResourceLoader(String bundleName, Class<?> bundleClass, Locale expectedLocale)
            throws IllegalStateException, IOException {
        String locale = expectedLocale == null ? "" : "_" + expectedLocale.toString();
        InputStream properties = bundleClass.getResourceAsStream("/" + bundleName + locale + ".properties");
        if (properties == null) {
            throw new IllegalStateException();
        }
        PropertyResourceBundle bundle = new PropertyResourceBundle(properties);
        properties.close();
        return bundle;
    }

    private ResourceBundle resourceLoader(String bundleName, Locale expectedLocale) {
        return getBundle(bundleName, expectedLocale, new PropertiesLoaderControl());
    }

}
