**:warning: This project is not maintained anymore. :warning:**

# leshan.osgi
Leshan OSGi provides an adapter layer between the core leshan library and an OSGi framework.
The adapter layer includes
* An implementation of leshan's `org.eclipse.leshan.server.client.ClientRegistry` using the OSGi service registry as its backing store.
The client is registered as a service object with specified properties.
* An OSGi based implementation of the `org.eclipse.leshan.server.client.ClientRegistryListener` which sends an event via OSGi's Event Admin Service if one of the `ClientRegistryListener` methods is called.
* An implementation of the `org.eclipse.leshan.server.observation.ObservationListener` to be notified of new values for observed resources. This implementation publishes observed resources as events using OSGi Event Admin Service.
To receive the published events implement the `org.osgi.service.event.EventHandler` interface and register the handler on a topic like <br> `event.topics = endpointID/objectID/*`

# Dependencies
Leshan OSGi (obviously) depends on leshan's core libraries and the OSGi framework's Core and Compendium classes.

#Usage
A few notes how leshan.osgi can be used:
* Use the californium-osgi `org.eclipse.californium.osgi.ManagedServer` to run a CoAP server as an OSGi Service.
* Implement a OSGi Config Admin `ManagedService` providing a management wrapper around leshan's `org.eclipse.leshan.server.LwM2mServer` interface.
* Use the `OsgiBasedClientRegistry` for the `ClientRegistry` interface in this implementation.
