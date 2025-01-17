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

// This library
import com.snowplowanalytics.snowplow.tracker.constants.Parameter;
import com.snowplowanalytics.snowplow.tracker.constants.Constants;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;

import java.util.Objects;

/**
 * Constructs a PageView event object.
 *
 * When tracked, generates a "pv" or "page_view" event.
 */
public class PageView extends AbstractEvent {

    private final String pageUrl;
    private final String pageTitle;
    private final String referrer;

    public static abstract class Builder<T extends Builder<T>> extends AbstractEvent.Builder<T> {

        private String pageUrl;
        private String pageTitle;
        private String referrer;

        /**
         * Required.
         *
         * @param pageUrl URL of the viewed page
         * @return itself
         */
        public T pageUrl(String pageUrl) {
            this.pageUrl = pageUrl;
            return self();
        }

        /**
         * Optional.
         *
         * @param pageTitle Title of the viewed page
         * @return itself
         */
        public T pageTitle(String pageTitle) {
            this.pageTitle = pageTitle;
            return self();
        }

        /**
         * Optional.
         *
         * @param referrer Referrer URL of the page
         * @return itself
         */
        public T referrer(String referrer) {
            this.referrer = referrer;
            return self();
        }

        public PageView build() {
            return new PageView(this);
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

    protected PageView(Builder<?> builder) {
        super(builder);

        // Precondition checks
        Objects.requireNonNull(builder.pageUrl);
        if (builder.pageUrl.isEmpty()) {
            throw new IllegalArgumentException("pageUrl cannot be empty");
        }

        this.pageUrl = builder.pageUrl;
        this.pageTitle = builder.pageTitle;
        this.referrer = builder.referrer;
    }

    /**
     * Returns a TrackerPayload which can be passed to an Emitter.
     *
     * @return the payload to be sent.
     */
    public TrackerPayload getPayload() {
        TrackerPayload payload = new TrackerPayload();
        payload.add(Parameter.EVENT, Constants.EVENT_PAGE_VIEW);
        payload.add(Parameter.PAGE_URL, this.pageUrl);
        payload.add(Parameter.PAGE_TITLE, this.pageTitle);
        payload.add(Parameter.PAGE_REFR, this.referrer);
        return putTrueTimestamp(payload);
    }
}
