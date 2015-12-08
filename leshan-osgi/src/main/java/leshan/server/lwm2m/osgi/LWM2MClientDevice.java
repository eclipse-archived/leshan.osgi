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
package leshan.server.lwm2m.osgi;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.core.request.DiscoverRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteAttributesRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.DiscoverResponse;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteAttributesResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.request.LwM2mRequestSender;
import org.osgi.service.device.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The LWM2MClientDevice implements the {@link LWM2MClient} and contains the
 * properties for registration at OSGi Service Registry.
 */
public class LWM2MClientDevice implements LWM2MClient {

    private static final Logger LOG = LoggerFactory.getLogger(LWM2MClientDevice.class);
    private static final long DEFAULT_RESPONSE_TIMEOUT = 2000L;

    private Client client;

    private final LwM2mRequestSender requestSender;

    /**
     * Constructor for new LWM2MClientDevice.
     *
     * @param client {@link Client} the client.
     * @param requestSender {@link LwM2mRequestSender}
     */
    public LWM2MClientDevice(final Client client, final LwM2mRequestSender requestSender) {
        this.client = client;
        this.requestSender = requestSender;
    }

    @Override
    public ReadResponse read(final ReadRequest readRequest) throws InterruptedException, UnsupportedEncodingException {
        LOG.trace("send ReadRequest to {}", client.getEndpoint());
        return requestSender.send(client, readRequest, DEFAULT_RESPONSE_TIMEOUT);
    }

    @Override
    public WriteResponse write(final WriteRequest writeRequest) {
        LOG.trace("send WriteRequest to {}", client.getEndpoint());
        return requestSender.send(client, writeRequest, DEFAULT_RESPONSE_TIMEOUT);
    }

    @Override
    public WriteAttributesResponse writeAttribute(final WriteAttributesRequest writeRequest) {
        LOG.trace("send WriteAttributesRequest to {}", client.getEndpoint());
        return requestSender.send(client, writeRequest, DEFAULT_RESPONSE_TIMEOUT);
    }

    @Override
    public ExecuteResponse execute(final ExecuteRequest executeReqest) {
        LOG.trace("send ExecuteRequest to {}", client.getEndpoint());
        return requestSender.send(client, executeReqest, DEFAULT_RESPONSE_TIMEOUT);
    }

    @Override
    public ObserveResponse observe(final ObserveRequest observeRequest) {
        LOG.trace("send ObserveRequest to {}", client.getEndpoint());
        return requestSender.send(client, observeRequest, DEFAULT_RESPONSE_TIMEOUT);
    }

    @Override
    public DiscoverResponse discover(final DiscoverRequest discoverRequest) {
        LOG.trace("send ObserveRequest to {}", client.getEndpoint());
        return requestSender.send(client, discoverRequest, DEFAULT_RESPONSE_TIMEOUT);
    }

    @Override
    public LinkObject[] getObjectLinks() {
        return client.getObjectLinks();
    }

    @Override
    public Client getClient() {
        return client;
    }

    /**
     * Returns the ServiceProperties for OSGi Service Registry for the given
     * Client.
     *
     * @param client
     * @return Dictionary
     */
    public Dictionary<String, Object> getServiceRegistrationProperties(final Client client) {
        final Dictionary<String, Object> registrationProperties = new Hashtable<>();
        final Long expirationTime = new Date().getTime() + client.getLifeTimeInSec() * 1000;
        registrationProperties.put(Property.REGISTRATION_ID, client.getRegistrationId());
        registrationProperties.put(Constants.DEVICE_CATEGORY, new String[] { Property.CATEGORY_LWM2M_CLIENT });
        registrationProperties.put(Property.REGISTRATION_EXPIRATION, Long.toString(expirationTime));
        registrationProperties.put(Property.LWM2M_OBJECTS, client.getObjectLinks());
        registrationProperties.put(org.osgi.framework.Constants.SERVICE_PID, client.getEndpoint());

        return registrationProperties;
    }

    /**
     * returns true if the client is still alive, means that the client sent his
     * update in the required time.
     *
     * @return
     */
    public boolean isAlive() {
        return getClient().isAlive();
    }

    /**
     * This Method update the embedded client object. The client cannot be
     * updated if the given clients endpointID not match to the existing.
     *
     * @param clientupdated the new Client
     */
    protected void updateClient(final Client clientupdated) {
        if (!clientupdated.getEndpoint().equals(this.client.getEndpoint())) {
            throw new IllegalArgumentException(
                    "The client can not be updated because the new endpointID do not match to the existing one.");
        }
        this.client = clientupdated;
    }
}
