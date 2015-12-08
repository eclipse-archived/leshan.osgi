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

/**
 * The LWM2MClient Interface includes the methods of communication lwm2m.
 */
public interface LWM2MClient {

    /**
     * Send a ReadRequest to the client.
     *
     * @param readRequest
     * @return the ValueResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    ReadResponse read(ReadRequest readRequest) throws InterruptedException, UnsupportedEncodingException;

    /**
     * Send a ExecuteRequest to the client.
     *
     * @param executeReqest
     * @return the ClientResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    ExecuteResponse execute(ExecuteRequest executeReqest);

    /**
     * Send a WriteRequest to the client.
     *
     * @param writeRequest
     * @return the ClientResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    WriteResponse write(WriteRequest writeRequest);

    /**
     * Send a WriteAttributesRequest to the client.
     *
     * @param writeRequest
     * @return the ClientResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    WriteAttributesResponse writeAttribute(WriteAttributesRequest writeRequest);

    /**
     * Send a ObserveRequest to the client.
     *
     * @param observeRequest
     * @return the ValueResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    ObserveResponse observe(ObserveRequest observeRequest);

    /**
     * Send a DiscoverRequest to the client.
     *
     * @param discoverRequest
     * @return the DiscoverResponse or <code>null</code> if timeout occurred.
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     */
    DiscoverResponse discover(DiscoverRequest discoverRequest);

    /**
     * return a {@link LinkObject}[] Array from the client.
     *
     * @return LinkObject[]
     */
    LinkObject[] getObjectLinks();

    /**
     * Returns the Client.
     *
     * @return {@link Client}
     */
    Client getClient();

}
