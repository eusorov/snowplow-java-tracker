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

package com.snowplowanalytics;

import com.snowplowanalytics.snowplow.tracker.Snowplow;
import com.snowplowanalytics.snowplow.tracker.Subject;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.configuration.EmitterConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.TrackerConfiguration;
import com.snowplowanalytics.snowplow.tracker.events.*;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;

import java.util.Collections;
import static java.util.Collections.singletonList;
import java.util.List;


public class Main {

    public static String getUrlFromArgs(String[] args) {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("Collector URL is required");
        }
        return args[0];
    }

    public static void main(String[] args) throws InterruptedException {
        String collectorEndpoint = getUrlFromArgs(args);

        // the application id to attach to events
        String appId = "java-tracker-sample-console-app";
        // the namespace to attach to events
        String namespace = "demo";

        // The easiest way to build a tracker is with configuration classes
        TrackerConfiguration trackerConfig = new TrackerConfiguration(namespace, appId);
        NetworkConfiguration networkConfig = new NetworkConfiguration(collectorEndpoint);
        EmitterConfiguration emitterConfig = new EmitterConfiguration().batchSize(4); // send batches of 4 events. In production this number should be higher, depending on the size/event volume

        // We need a tracker to turn our events into something a Snowplow collector can understand
        final Tracker tracker = Snowplow.createTracker(trackerConfig, networkConfig, emitterConfig);

        System.out.println("Sending events to " + collectorEndpoint);
        System.out.println("Using tracker version " + tracker.getTrackerVersion());

        // This is an example of a custom context entity
        List<SelfDescribingJson> context = singletonList(
            new SelfDescribingJson(
                "iglu:com.snowplowanalytics.iglu/anything-c/jsonschema/1-0-0",
                Collections.singletonMap("foo", "bar")));

        // This is an example of a eventSubject for adding user data
        Subject eventSubject = new Subject();
        eventSubject.setUserId("example@snowplowanalytics.com");
        eventSubject.setLanguage("EN");

        // This is a sample page view event
        // the eventSubject has been included in this event
        PageView pageViewEvent = PageView.builder()
            .pageTitle("Snowplow Analytics")
            .pageUrl("https://www.snowplowanalytics.com")
            .referrer("https://www.google.com")
            .customContext(context)
            .subject(eventSubject)
            .build();
        
        // EcommerceTransactions will be deprecated soon: we advise using SelfDescribing events instead
        // EcommerceTransactionItems are tracked as part of an EcommerceTransaction event
        // They are processed into separate events during the `track()` call
        EcommerceTransactionItem item = EcommerceTransactionItem.builder()
            .itemId("order_id")
            .sku("sku")
            .price(1.0)
            .quantity(2)
            .name("name")
            .category("category")
            .currency("currency")
            .customContext(context)
            .build();

        // EcommerceTransaction event
        EcommerceTransaction ecommerceTransaction = EcommerceTransaction.builder()
            .orderId("order_id")
            .totalValue(1.0)
            .affiliation("affiliation")
            .taxValue(2.0)
            .shipping(3.0)
            .city("city")
            .state("state")
            .country("country")
            .currency("currency")
            .items(item) // EcommerceTransactionItem events are added to a parent EcommerceTransaction here
            .customContext(context)
            .build();


        // This is an example of a custom SelfDescribing event based on a schema
        SelfDescribing selfDescribing = SelfDescribing.builder()
            .eventData(new SelfDescribingJson(
                    "iglu:com.snowplowanalytics.iglu/anything-a/jsonschema/1-0-0",
                    Collections.singletonMap("foo", "bar")
            ))
            .customContext(context)
            .build();


        // This is an example of a ScreenView event which will be translated into a SelfDescribing event
        ScreenView screenView = ScreenView.builder()
            .name("name")
            .id("id")
            .customContext(context)
            .build();


        // This is an example of a Timing event which will be translated into a SelfDescribing event
        Timing timing = Timing.builder()
            .category("category")
            .label("label")
            .variable("variable")
            .timing(10)
            .customContext(context)
            .build();

        // This is an example of a Structured event
        Structured structured = Structured.builder()
                .category("category")
                .action("action")
                .label("label")
                .property("property")
                .value(12.34)
                .customContext(context)
                .build();

        tracker.track(pageViewEvent); // the .track method schedules the event for delivery to Snowplow
        tracker.track(ecommerceTransaction); // This will track two events
        tracker.track(selfDescribing);
        tracker.track(screenView);
        tracker.track(timing);
        tracker.track(structured);

        // Will close all threads and force send remaining events
        tracker.close();
        Thread.sleep(5000);

        System.out.println("Tracked 7 events");
    }

}
