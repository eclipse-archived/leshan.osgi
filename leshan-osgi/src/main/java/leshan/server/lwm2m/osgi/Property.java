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

/**
 * Property Class for the leshan-osgi Module.
 */
public final class Property {

    public static final String REGISTRATION_ID = "LWM2M_REGISTRATIONID";
    public static final String REGISTRATION_EXPIRATION = "LWM2M_REGISTRATION_EXPIRATION";
    public static final String LWM2M_OBJECTS = "LWM2M_OBJECTS";
    public static final String CATEGORY_LWM2M_CLIENT = "LWM2MClient";

    public static final String LWM2MPATH = "lwm2mpath";
    public static final String LWM2MNODE = "node";
    public static final String CLIENT = "client";

    public static final String REGISTERED_EVENT = "CLIENT_REGISTERED";
    public static final String UPDATED_EVENT = "CLIENT_UPDATED";
    public static final String UNREGISTERED_EVENT = "CLIENT_UNREGISTERED";

    private Property() {

    }
}
