package pt.tecnico.distledger.namingserver;

import pt.tecnico.distledger.namingserver.domain.ServiceEntry;
import pt.tecnico.distledger.namingserver.domain.ServerEntry;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class NamingServerState {
    
    HashMap<String, ServiceEntry> serviceEntries;
    private boolean toDebug;

    public NamingServerState(boolean toDebug) {
        this.serviceEntries = new HashMap<String, ServiceEntry>();
        this.toDebug = toDebug;

        // TODO: remover
        ServiceEntry serviceEntry1 = new ServiceEntry("createAccount");
        serviceEntry1.addServerEntry(new ServerEntry("localhost:50051", "A"));
        ServiceEntry serviceEntry2 = new ServiceEntry("transferTo");
        serviceEntry2.addServerEntry(new ServerEntry("localhost:50051", "A"));
        ServiceEntry serviceEntry3 = new ServiceEntry("deleteAccount");
        serviceEntry3.addServerEntry(new ServerEntry("localhost:50051", "A"));
        ServiceEntry serviceEntry4 = new ServiceEntry("balance");
        serviceEntry4.addServerEntry(new ServerEntry("localhost:50051", "A"));
        addServiceEntry(serviceEntry1);
        addServiceEntry(serviceEntry2);
        addServiceEntry(serviceEntry3);
        addServiceEntry(serviceEntry4);
    }

    private void debug(String debugMessage) {
        if (toDebug)
            System.err.println(debugMessage);
    }

    public void addServiceEntry(ServiceEntry serviceEntry) {
        this.serviceEntries.put(serviceEntry.getServiceName(), serviceEntry);
    }

    public ServiceEntry getServiceEntry(String serviceName) {
        return this.serviceEntries.get(serviceName);
    }

    public void removeServiceEntry(String serviceName) {
        this.serviceEntries.remove(serviceName);
    }

    public void addServerEntryToServiceEntry(String serviceName, ServerEntry serverEntry) {
        // if service exists
        ServiceEntry serviceEntry = this.serviceEntries.get(serviceName);
        // else create service //TODO

        serviceEntry.addServerEntry(serverEntry);
    }

    public List<String> lookup(String serviceName, String qualifier) {
        ServiceEntry serviceEntry = getServiceEntry(serviceName);
        if (serviceEntry == null) {
            debug("lookup: Service " + serviceName + " not found");
            return new ArrayList<String>();
        }

        List<String> serviceEntriesWithQualifier = serviceEntry.lookup(qualifier);
        debug("lookup: Service " + serviceName + " found in servers: " + serviceEntriesWithQualifier);

        return serviceEntriesWithQualifier;
    }

}
