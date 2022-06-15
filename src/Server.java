import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;

public class Server {
    static final String DIVIDER = "--------------------------------------------------";

    private ServerSocket server;
    private Socket socket1, socket2;
    DataInputStream inputStream1, inputStream2;
    DataOutputStream outputStream1, outputStream2;
    private String deviceId1, deviceId2;
    File outputDirectory;

    public Server(int port, File outputDirectory) {
        this.outputDirectory = outputDirectory;
        try {
            // setup server
            System.out.println("Step 2: Setup Server\n");
            server = new ServerSocket(port);
            System.out.println("Server started successfully");
            System.out.println("Server is running at: " + server.getLocalSocketAddress());
            System.out.println(DIVIDER);

            // accept connection from Client 1
            System.out.println("Step 3: Wait for both recording devices to connect\n");
            System.out.println("Waiting for Device 1 to connect.");
            socket1 = server.accept();
            inputStream1 = new DataInputStream(socket1.getInputStream());
            outputStream1 = new DataOutputStream(socket1.getOutputStream());
            System.out.println("Device 1 has connected successfully.");
            deviceId1 = inputStream1.readUTF();
            outputStream1.writeUTF("Device is initialized with the role: " + deviceId1);
            System.out.println("Device ID: " + deviceId1);
            System.out.println("Local Socket Address: " + socket1.getLocalSocketAddress());
            System.out.println("Remote Socket Address: " + socket1.getRemoteSocketAddress());
            System.out.println();

            // accept connection from Client 2
            System.out.println("Waiting for Client 2 to connect.");
            socket2 = server.accept();
            inputStream2 = new DataInputStream(socket2.getInputStream());
            outputStream2 = new DataOutputStream(socket2.getOutputStream());
            System.out.println("Client 2 has connected successfully.");
            deviceId2 = inputStream2.readUTF();
            if (deviceId2.equals(deviceId1)) {
                if (deviceId2.equals("SIDE_VIEW")) {
                    deviceId2 = "BACK_VIEW";
                } else if (deviceId2.equals("BACK_VIEW")) {
                    deviceId2 = "SIDE_VIEW";
                }
            }
            outputStream2.writeUTF("Device is initialized with the role: " + deviceId2);
            System.out.println("Device ID: " + deviceId2);
            System.out.println("Local Socket Address: " + socket2.getLocalSocketAddress());
            System.out.println("Remote Socket Address: " + socket2.getRemoteSocketAddress());
            System.out.println(DIVIDER);

            // wait for signal from user to start recording
            System.out.println("Step 4: Start the recording\n");
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter START to start recording");
            String input = "";
            while (!input.equalsIgnoreCase("START")) input = scanner.next();
            System.out.println("Sending STOP signal to both recording devices");

            // send start signal to start recording synchronously
            new Thread(() -> startRecording(outputStream1, deviceId1)).start();
            new Thread(() -> startRecording(outputStream2, deviceId2)).start();

            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                System.out.println("Main thread has been interrupted");
                e.printStackTrace();
            }

            System.out.println("\nEnter STOP to stop the recording: ");
            input = "";
            while (!input.equalsIgnoreCase("STOP")) input = scanner.next();
            System.out.println("Sending STOP signal to both recording devices");

            new Thread(() -> stopRecording(outputStream1, deviceId1)).start();
            new Thread(() -> stopRecording(outputStream2, deviceId2)).start();

            // receive files from both clients simultaneously
            new Thread(() -> receiveFile(inputStream1, deviceId1, socket1)).start();
            new Thread(() -> receiveFile(inputStream2, deviceId2, socket2)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecording(DataOutputStream outputStream, String deviceId) {
        try {
            outputStream.writeUTF("START");
            System.out.println("START signal sent to device with device ID: " + deviceId);
        } catch (IOException e) {
            System.out.println("Unable to send START signal to device with ID: " + deviceId);
            e.printStackTrace();
        }
    }

    private void stopRecording(DataOutputStream outputStream, String deviceId) {
        try {
            outputStream.writeUTF("STOP");
            System.out.println("STOP signal sent to device with device ID: " + deviceId);
        } catch (IOException e) {
            System.out.println("Unable to send STOP signal to device with ID: " + deviceId);
        }
    }

    private void receiveFile(DataInputStream inputStream, String deviceId, Socket socket) {
        File vidPath = new File(outputDirectory, deviceId + ".mp4");
        FileOutputStream fileOutputStream = null;
        int bufferSize = 16 * 1024;
        int count = 0;
        byte[] bytes = new byte[bufferSize];

        try {
            if (!vidPath.exists()) vidPath.createNewFile();
        } catch (IOException e) {
            System.out.println("Unable to create file at " + vidPath);
            e.printStackTrace();
        }

        try {
            fileOutputStream = new FileOutputStream(vidPath);
        } catch (FileNotFoundException e) {
            System.out.println("File " + vidPath + " not found");
            e.printStackTrace();
        }

        try {
            while ((count = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, count);
            }
            System.out.println("\nReceived file from device with ID: " + deviceId);
            System.out.println("File saved at " + vidPath + "\n");
        } catch (IOException e) {
            System.out.println("Error in reading bytes");
            e.printStackTrace();
        }

        try {
            fileOutputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

