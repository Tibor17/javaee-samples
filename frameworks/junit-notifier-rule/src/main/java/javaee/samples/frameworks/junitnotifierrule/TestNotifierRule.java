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
package javaee.samples.frameworks.junitnotifierrule;

import org.junit.AssumptionViolatedException;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;

import java.math.BigDecimal;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.BigInteger.ONE;
import static java.math.RoundingMode.HALF_UP;

public class TestNotifierRule extends Stopwatch {
    private static final Logger LOG = Logger.getGlobal();
    private static final int MICROSECONDS = 9;

    private static final BigDecimal MICRO_SECOND = new BigDecimal(ONE, 6);
    private static final BigDecimal MILLI_SECOND = new BigDecimal(ONE, 3);
    private static final BigDecimal SECOND = BigDecimal.ONE;
    private static final BigDecimal MINUTE = BigDecimal.valueOf(60);

    @Override
    protected final void failed(long nanos, Throwable e, Description description) {
        completed(nanos, description, "failed");
    }

    @Override
    protected final void skipped(long nanos, AssumptionViolatedException e, Description description) {
        completed(nanos, description, "skipped");
    }

    @Override
    protected final void succeeded(long nanos, Description description) {
        completed(nanos, description, "succeeded");
    }

    private static void completed(long nanos, Description description, String detail) {
        String method = description.getMethodName();
        Class<?> clazz = description.getTestClass();
        String thread = Thread.currentThread().toString();
        BigDecimal seconds = BigDecimal.valueOf(nanos, MICROSECONDS);
        LOG.info(format("### Test [%s#%s] %s in %s after [%s].", clazz.getSimpleName(), method, detail, thread,
                convertDuration(seconds)));
    }

    private static String convertDuration(BigDecimal duration) {
        return convertDuration("", duration);
    }

    private static String convertDuration(String previous, BigDecimal duration) {
        if (duration.compareTo(MINUTE) >= 0) {
            BigDecimal[] quotientReminder = duration.divideAndRemainder(MINUTE);
            if (previous.isEmpty()) {
                previous = quotientReminder[0].stripTrailingZeros().toPlainString();
                previous += " min ";
                return convertDuration(previous, quotientReminder[1]);
            } else if (quotientReminder[0].compareTo(ZERO) != 0) {
                BigDecimal quotient = round(quotientReminder[0].stripTrailingZeros());
                return quotient.equals(ZERO) ? previous.trim() : previous + quotient.toPlainString() + " min";
            }
        } else if (duration.compareTo(SECOND) >= 0) {
            BigDecimal[] quotientReminder = duration.divideAndRemainder(SECOND);
            if (previous.isEmpty()) {
                previous = quotientReminder[0].stripTrailingZeros().toPlainString();
                previous += " sec ";
                return convertDuration(previous, quotientReminder[1]);
            } else if (quotientReminder[0].compareTo(ZERO) != 0) {
                BigDecimal quotient = round(quotientReminder[0].stripTrailingZeros());
                return quotient.equals(ZERO) ? previous.trim() : previous + quotient.toPlainString() + " sec";
            }
        } else if (duration.compareTo(MILLI_SECOND) >= 0) {
            BigDecimal[] quotientReminder = duration.divideAndRemainder(MILLI_SECOND);
            if (previous.isEmpty()) {
                previous = quotientReminder[0].stripTrailingZeros().toPlainString();
                previous += " millis ";
                return convertDuration(previous, quotientReminder[1]);
            } else if (quotientReminder[0].compareTo(ZERO) != 0) {
                BigDecimal quotient = round(quotientReminder[0].stripTrailingZeros());
                return quotient.equals(ZERO) ? previous.trim() : previous + quotient.toPlainString() + " millis";
            }
        } else {
            BigDecimal[] quotientReminder = duration.divideAndRemainder(MICRO_SECOND);
            if (previous.isEmpty()) {
                previous = quotientReminder[0].stripTrailingZeros().toPlainString();
                previous += " micros ";
                previous += quotientReminder[1].intValue();
                return previous + " nanos";
            } else if (quotientReminder[0].compareTo(ZERO) != 0) {
                BigDecimal quotient = round(quotientReminder[0].stripTrailingZeros());
                return quotient.equals(ZERO) ? previous.trim() : previous + quotient.toPlainString() + " micros";
            }
        }
        return previous;
    }

    private static BigDecimal round(BigDecimal number) {
        return number.setScale(1, HALF_UP);
    }
}
