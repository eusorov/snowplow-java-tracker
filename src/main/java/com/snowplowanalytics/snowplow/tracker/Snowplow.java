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
package com.snowplowanalytics.snowplow.tracker;

import com.snowplowanalytics.snowplow.tracker.configuration.EmitterConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.SubjectConfiguration;
import com.snowplowanalytics.snowplow.tracker.configuration.TrackerConfiguration;
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Snowplow {

    private static final Map<String, Tracker> trackers = new HashMap<>();
    private static Tracker defaultTracker;

    /**
     * @return the stored tracker namespaces
     */
    public static Set<String> getInstancedTrackerNamespaces() {
        return trackers.keySet();
    }

    /**
     * @return the default Tracker, or null if no default is set
     */
    public static Tracker getDefaultTracker() {
        return defaultTracker;
    }

    /**
     * Set a specific Tracker instance as the default tracker. The Tracker will be added to the Snowplow class if its
     * namespace is not already stored.
     *
     * The first Tracker created using createTracker() or registered with registerTracker()
     * will automatically be the default tracker; this method is intended for use
     * with multiple trackers.
     *
     * @param tracker the tracker to use as default
     */
    public static void setDefaultTracker(Tracker tracker) {
        if (!trackers.containsKey(tracker.getNamespace())) {
            Snowplow.registerTracker(tracker);
        }

        defaultTracker = tracker;
    }

    /**
     * Set a registered Tracker as the default tracker, using its namespace.
     *
     * @param namespace the namespace of the tracker to set as default
     * @return true if the tracker was found and set
     */
    public static boolean setDefaultTracker(String namespace) {
        if (trackers.containsKey(namespace)) {
            defaultTracker = trackers.get(namespace);
            return true;
        }

        return false;
    }

    /**
     * Create a Snowplow tracker using Configuration objects.
     *
     * @param trackerConfig a TrackerConfiguration
     * @param networkConfig a NetworkConfiguration (will be used to create an OkHttpClientAdapter)
     * @param emitterConfig an EmitterConfiguration (will be used to create a BatchEmitter)
     * @param subjectConfig a SubjectConfiguration
     * @return the created Tracker
     */
    public static Tracker createTracker(TrackerConfiguration trackerConfig,
                                        NetworkConfiguration networkConfig,
                                        EmitterConfiguration emitterConfig,
                                        SubjectConfiguration subjectConfig) {
        Subject subject = null;
        if (subjectConfig != null) {
            subject = new Subject(subjectConfig);
        }

        BatchEmitter emitter = new BatchEmitter(networkConfig, emitterConfig);
        Tracker tracker =  new Tracker(trackerConfig, emitter, subject);
        registerTracker(tracker);
        return tracker;
    }

    /**
     * Create a Snowplow tracker with default configuration by providing three parameters.
     *
     * @param namespace unique identifier for the Tracker instance
     * @param appId application ID
     * @param collectorUrl collector endpoint
     * @return the created Tracker
     */
    public static Tracker createTracker(String namespace, String appId, String collectorUrl) {
        TrackerConfiguration trackerConfig = new TrackerConfiguration(namespace, appId);
        NetworkConfiguration networkConfig = new NetworkConfiguration(collectorUrl);

        return createTracker(trackerConfig, networkConfig, new EmitterConfiguration(), null);
    }

    /**
     * Create a Snowplow tracker using Configuration objects.
     *
     * @param trackerConfig a TrackerConfiguration
     * @param networkConfig a NetworkConfiguration
     * @param emitterConfig an EmitterConfiguration
     * @return the created Tracker
     */
    public static Tracker createTracker(TrackerConfiguration trackerConfig,
                                        NetworkConfiguration networkConfig,
                                        EmitterConfiguration emitterConfig) {
        return createTracker(trackerConfig, networkConfig, emitterConfig, null);
    }

    /**
     * Create a Snowplow tracker using Configuration objects.
     *
     * @param trackerConfig a TrackerConfiguration
     * @param networkConfig a NetworkConfiguration
     * @return the created Tracker
     */
    public static Tracker createTracker(TrackerConfiguration trackerConfig,
                                        NetworkConfiguration networkConfig) {
        return createTracker(trackerConfig, networkConfig, new EmitterConfiguration(), null);
    }

    /**
     * Create a Snowplow tracker using Configuration objects.
     *
     * @param trackerConfig a TrackerConfiguration
     * @param networkConfig a NetworkConfiguration
     * @param subjectConfig a SubjectConfiguration
     * @return the created Tracker
     */
    public static Tracker createTracker(TrackerConfiguration trackerConfig,
                                        NetworkConfiguration networkConfig,
                                        SubjectConfiguration subjectConfig) {
        return createTracker(trackerConfig, networkConfig, new EmitterConfiguration(), subjectConfig);
    }

    /**
     * Register a Tracker instance that was created manually, not via the Snowplow.createTracker() method.
     *
     * @param tracker a Tracker instance
     */
    public static void registerTracker(Tracker tracker) {
        String namespace = tracker.getNamespace();
        if (trackers.containsKey(namespace)) {
            throw new IllegalArgumentException("Tracker with this namespace already exists.");
        }

        trackers.put(namespace, tracker);

        if (defaultTracker == null) {
            defaultTracker = tracker;
        }
    }

    /**
     * Get a Tracker by its namespace
     *
     * @param namespace the namespace of the tracker to retrieve
     * @return the retrieved tracker
     */
    public static Tracker getTracker(String namespace) {
        return trackers.get(namespace);
    }

    /**
     * Unregister a Tracker, using its namespace.
     *
     * @param namespace the namespace of the tracker to remove
     * @return true if the tracker was found and removed
     */
    public static boolean removeTracker(String namespace) {
        Tracker removedTracker = trackers.remove(namespace);
        if ((defaultTracker != null) && defaultTracker.getNamespace().equals(namespace)) {
            defaultTracker = null;
        }
        return removedTracker != null;
    }

    /**
     * Unregister a Tracker.
     *
     * @param tracker the tracker to remove
     * @return true if the tracker was found and removed
     */
    public static boolean removeTracker(Tracker tracker) {
        return removeTracker(tracker.getNamespace());
    }

    /**
     * Clear (unregister) all trackers.
     */
    public static void reset() {
        trackers.clear();
        defaultTracker = null;
    }
}
