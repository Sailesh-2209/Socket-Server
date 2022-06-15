import java.io.File;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class Main {
    static final String APP_NAME = "SYNCHRONIZED VIDEO RECORDER";
    static final String DIVIDER = "--------------------------------------------------";
    static final String START_DELIM = "***********************************************************";
    static final int PORT = 5000;
    static File file;

    public static void main(String[] args)  {
        try {
            greet();
            new Server(PORT, file);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void greet() throws UnknownHostException {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
        String rootDir = System.getProperty("user.home");
        String vidDir = "Videos";
        String appDir = "SportsTrack";
        String currDir = formatter.format(date);
        Path path = Paths.get(rootDir, vidDir, appDir, currDir);
        file = new File(path.toString());
        String ipv4Address = getHostAddress();
        if (!file.exists()) {
            file.mkdirs();
        }
        System.out.println(START_DELIM);
        System.out.println(APP_NAME);
        System.out.println();
        System.out.println("Start time: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        System.out.println("Make a note of the following information");
        System.out.println(DIVIDER);
        System.out.println("Step 1: Host Device Configuration\n");
        System.out.println("The recorded videos will be saved at: " + file);
        System.out.println("Host IP address: " + ipv4Address);
        System.out.println("Port: " + PORT);
        System.out.println(DIVIDER);
    }

    public static String getHostAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                List<InterfaceAddress> interfaceAddressList = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddressList) {
                    InetAddress inetAddress = interfaceAddress.getAddress();
                    String displayName = networkInterface.getDisplayName();

                    boolean isUp = networkInterface.isUp();
                    boolean ignore = displayName.startsWith("VMware")
                            || displayName.startsWith("VirtualBox")
                            || displayName.startsWith("Hyper-V")
                            || displayName.startsWith("Software");
                    boolean ok = inetAddress.getHostAddress().startsWith("192");

                    if (isUp && !ignore && ok) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "localhost";
        }
    }
}
