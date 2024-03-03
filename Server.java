import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 5000;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket listener = new ServerSocket(PORT)) {
            System.out.println("Server is waiting for connections...");

            while (true) {
                Socket client = listener.accept();
                System.out.println("Connected to client");
                ClientHandler clientThread = new ClientHandler(client, clients);
                clients.add(clientThread);

                pool.execute(clientThread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ArrayList<ClientHandler> clients;
    public ClientHandler(Socket socket, ArrayList<ClientHandler> clients) throws IOException {
        this.socket = socket;
        this.clients = clients;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    public void run() {
        try {
            while (true) {
                String messageFromClient = in.readLine();
                if (messageFromClient == null) {
                    break;
                }
                broadcastMessage(messageFromClient);
            }
        } catch (IOException e) {
            System.err.println("Error in client handler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void broadcastMessage(String messageToSend) {
        for (ClientHandler aClient : clients) {
            aClient.out.println(messageToSend);
        }
    }
}

