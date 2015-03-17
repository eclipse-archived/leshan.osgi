/*******************************************************************************
 * Copyright (c) 2015, Bosch Software Innovations GmbH
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 *
 * Contributors:
 *     Bosch Software Innovations GmbH - OSGi support
 *******************************************************************************/
package leshan.server.lwm2m.osgi.messaging;

import java.util.Dictionary;
import java.util.Hashtable;

import leshan.server.lwm2m.osgi.Property;

import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.observation.Observation;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes observed resources as events using OSGi Event Admin service. The
 * Lwm2mEventPublisher implements the {@link ObservationListener} to receive the
 * new Value from the observed resources.
 */
public class Lwm2mEventPublisher implements ObservationListener {

    private static final Logger LOG = LoggerFactory.getLogger(Lwm2mEventPublisher.class);
    private final BundleContext context;

    /**
     * Constructor for new Lwm2mEventPublisher. The Lwm2mEventPublisher
     * implements the {@link ObservationListener} to receive the new Value from
     * the observed resources.
     *
     * @param bundleContext
     */
    public Lwm2mEventPublisher(final BundleContext bundleContext) {
        context = bundleContext;
    }

    @Override
    public void newValue(final Observation observation, final LwM2mNode node) {

        final Dictionary<String, Object> properties = new Hashtable<String, Object>();

        if (node == null || observation == null) {
            throw new IllegalArgumentException("Missing required propertie");
        } else {

            properties.put(Property.LWM2MNODE, node);
            properties.put(Property.LWM2MPATH, observation.getPath());
            properties.put(Property.CLIENT, observation.getClient());

            sendEvent(observation.getClient(), observation.getPath(), properties);
            LOG.trace("Received new value for observation from: {}", observation.getClient().getEndpoint());
        }
    }

    @Override
    public void cancelled(final Observation observation) {
        // cancel
    }

    private void sendEvent(final Client client, final LwM2mPath path, final Dictionary<String, Object> properties) {

        final ServiceReference<?> ref = context.getServiceReference(EventAdmin.class.getName());
        if (ref != null) {

            final StringBuilder topic = new StringBuilder();
            topic.append(client.getEndpoint());
            topic.append('/').append(path.getObjectId());
            if (path.getObjectInstanceId() != null) {
                topic.append('/').append(path.getObjectInstanceId());
                if (path.getResourceId() != null) {
                    topic.append('/').append(path.getResourceId());
                }
            }
            final Event notifyEvent = new Event(topic.toString(), properties);

            // postEvent sends events asynchronously -> Method does not block
            final EventAdmin eventAdmin = (EventAdmin) context.getService(ref);
            eventAdmin.postEvent(notifyEvent);
            LOG.trace("Sending event to topic {}", topic.toString());
        }
    }
}
