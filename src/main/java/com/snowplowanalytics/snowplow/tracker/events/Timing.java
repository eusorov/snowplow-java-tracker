/*
 * Copyright (c) 2014-2022 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.snowplow.tracker.events;

// Java
import java.util.LinkedHashMap;
import java.util.Objects;

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

/**
 * Constructs a Timing event object.
 *
 * When tracked, generates a SelfDescribing event (event type "ue").
 */
public class Timing extends AbstractEvent {

    private final String category;
    private final String variable;
    private final Integer timing;
    private final String label;

    public static abstract class Builder<T extends Builder<T>> extends AbstractEvent.Builder<T> {

        private String category;
        private String variable;
        private Integer timing;
        private String label;

        /**
         * Required.
         *
         * @param category The category of the timed event
         * @return itself
         */
        public T category(String category) {
            this.category = category;
            return self();
        }

        /**
         * Required.
         *
         * @param variable Identify the timing being recorded
         * @return itself
         */
        public T variable(String variable) {
            this.variable = variable;
            return self();
        }

        /**
         * Required.
         *
         * @param timing The number of milliseconds in elapsed time to report
         * @return itself
         */
        public T timing(Integer timing) {
            this.timing = timing;
            return self();
        }

        /**
         * Optional.
         *
         * @param label Optional description of this timing
         * @return itself
         */
        public T label(String label) {
            this.label = label;
            return self();
        }

        public Timing build() {
            return new Timing(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        @Override
        protected Builder2 self() {
            return this;
        }
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    protected Timing(Builder<?> builder) {
        super(builder);

        // Precondition checks
        Objects.requireNonNull(builder.category);
        Objects.requireNonNull(builder.timing);
        Objects.requireNonNull(builder.variable);
        if (builder.category.isEmpty()) {
            throw new IllegalArgumentException("category cannot be empty");
        }
        if (builder.variable.isEmpty()) {
            throw new IllegalArgumentException("variable cannot be empty");
        }

        this.category = builder.category;
        this.variable = builder.variable;
        this.label = builder.label;
        this.timing = builder.timing;
    }

    /**
     * Return the payload wrapped into a SelfDescribingJson. When a Timing event is tracked,
     * the Tracker creates and tracks a SelfDescribing event from this SelfDescribingJson.
     *
     * @return the payload as a SelfDescribingJson.
     */
    public SelfDescribingJson getPayload() {
        LinkedHashMap<String,Object> payload = new LinkedHashMap<>();
        payload.put(Parameter.UT_CATEGORY, this.category);
        payload.put(Parameter.UT_LABEL, this.label);
        payload.put(Parameter.UT_TIMING, this.timing);
        payload.put(Parameter.UT_VARIABLE, this.variable);
        return new SelfDescribingJson(Constants.SCHEMA_USER_TIMINGS, payload);
    }
}
