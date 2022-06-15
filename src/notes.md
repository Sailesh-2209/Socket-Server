# Socket server

## Network Interfaces

1. Get all the Network Interfaces
```
NetworkInterface.getNetworkInterfaces() -> returns Enumeration<NetworkInterface>
```
2. Properties of each Network Interface
   1. index
      ```
      networkInterface.getIndex()
      ```
      * Ranges from 1 to 65
   
   2. Interface Address List
      ```
      networkInterface.getInterfaceAddresses()
      ```
      * Returns List<InterfaceAddress>
      * Seemingly useless
      * Seems to contain IPv4 and IPv6 addresses

---

## Test Programs

### Test 1

#### Server program for single client

```java
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException {
        displayIPAddress();
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server setup success");
        Socket socket = server.accept();
        System.out.println("Connection establishment success");
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        String deviceId = inputStream.readUTF();
        outputStream.writeUTF("Device is initialized with the role: " + deviceId);
        Scanner scanner = new Scanner(System.in);
        String message = "";
        while (!message.equalsIgnoreCase("START")) message = scanner.next();
        outputStream.writeUTF("START");
        while (!message.equalsIgnoreCase("STOP")) message = scanner.next();
        outputStream.writeUTF("STOP");
        File file = new File("C:\\Users\\saile\\Desktop\\video.mp4");
        if (!file.exists()) file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        byte[] bytes = new byte[4 * 1024];
        int count = 0;
        while ((count = inputStream.read(bytes)) != -1) {
            out.write(bytes, 0, count);
        }
        out.close();
        inputStream.close();
        outputStream.close();
        socket.close();
        server.close();
    }
    public static void displayIPAddress() {
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface networkInterface = e.nextElement();
                List<InterfaceAddress> interfaceAddressList = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddressList) {
                    InetAddress inetAddress = interfaceAddress.getAddress();
                    String name = networkInterface.getName();
                    String displayName = networkInterface.getDisplayName();

                    boolean isUp = networkInterface.isUp();
                    boolean ignore = displayName.startsWith("VMware")
                            || displayName.startsWith("VirtualBox")
                            || displayName.startsWith("Hyper-V")
                            || displayName.startsWith("Software");
                    boolean ok = inetAddress.getHostAddress().startsWith("192");

                    if (isUp && !ignore && ok) {
                        System.out.println(name + " " + displayName + " " + inetAddress.getHostAddress());
                        System.out.println();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
```

### Test 2

#### Testing all the network interfaces to get the right IP address

```java
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class Test2 {
    public static void main(String[] args) {
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface networkInterface = e.nextElement();
                List<InterfaceAddress> interfaceAddressList = networkInterface.getInterfaceAddresses();
                for (InterfaceAddress interfaceAddress : interfaceAddressList) {
                    InetAddress inetAddress = interfaceAddress.getAddress();
                    String name = networkInterface.getName();
                    String displayName = networkInterface.getDisplayName();

                    boolean isUp = networkInterface.isUp();
                    boolean ignore = displayName.startsWith("VMware")
                            || displayName.startsWith("VirtualBox")
                            || displayName.startsWith("Hyper-V")
                            || displayName.startsWith("Software");

                    if (isUp && !ignore) {
                        System.out.println(name + " " + displayName + " " + inetAddress.getHostAddress());
                        System.out.println();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
```
