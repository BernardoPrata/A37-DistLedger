package pt.tecnico.distledger.namingserver.domain;

public class ServerEntry {

    // has host:port
    private String address;

    private String qualifier;

    public ServerEntry(String address, String qualifier) {
        this.address = address;
        this.qualifier = qualifier;
    }

    public String getAddress() {
        return address;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "ServerEntry{" +
                "address='" + address + '\'' +
                ", qualifier='" + qualifier + '\'' +
                '}';
    }
}
