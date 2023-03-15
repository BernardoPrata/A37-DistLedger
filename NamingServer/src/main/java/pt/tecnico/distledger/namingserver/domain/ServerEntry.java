package pt.tecnico.distledger.namingserver.domain;

public class ServerEntry {

    // has host:port
    private String serverAddress;

    private String serverQualifier;

    public ServerEntry(String serverAddress, String serverQualifier) {
        this.serverAddress = serverAddress;
        this.serverQualifier = serverQualifier;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getServerQualifier() {
        return serverQualifier;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setServerQualifier(String serverQualifier) {
        this.serverQualifier = serverQualifier;
    }

}
