# leshan.osgi
Leshan OSGi provides an adapter layer between the core leshan library and an OSGi framework.
The adapter layer includes
* an implementation of leshan's `org.eclipse.leshan.server.client.ClientRegistry` using the OSGi service registry as
its backing store
* an adapter for forwarding notifications from LWM2M clients to the OSGi EventAdmin service
* an OSGi Config Admin `ManagedService` implementation providing a management wrapper around leshan's `LwM2mServer`class

# Dependencies
Leshan OSGi (obviously) depends on leshan's core libraries and the OSGi framework's Core and Compendium classes.
