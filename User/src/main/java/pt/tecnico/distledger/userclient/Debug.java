package pt.tecnico.distledger.userclient;

public class Debug {
    public static boolean toDebug = false;

    public static void setDebugFlag(boolean flag) {

        toDebug = flag;
    }

    public static void write(String debugMessage){
        if (toDebug)
            System.err.println(debugMessage);
    }
}