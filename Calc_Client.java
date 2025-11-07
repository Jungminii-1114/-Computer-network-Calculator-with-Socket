import java.io.*;
import java.net.*;

public class Calc_Client {
    public static void main(String[] args) throws IOException {
        String sentence;
        String sentenceFromServer = "";
        String serverIP = "127.0.0.1";
        int nPort = 3456;

        Socket clientSocket = null;
        BufferedReader inFromUser = null;
        BufferedReader inFromServer = null;
        PrintWriter outToServer = null;

        try {
            clientSocket = new Socket(serverIP, nPort);
            System.out.println("Connected to " + serverIP + ":" + nPort);

            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while(true) {
                System.out.println("Input to calculate (Type exit if you want quit)");
                sentence = inFromUser.readLine();
                outToServer.println(sentence);
                if(sentence.equalsIgnoreCase("exit")){
                    System.out.println("Exiting...");
                    break;
                }
                sentenceFromServer = inFromServer.readLine();

                System.out.println("FROM SERVER : " + sentenceFromServer);
            }
        } catch (UnknownHostException e) {
            System.out.println("Cannot Find host : " + serverIP);
        } catch (IOException e) {
            System.err.println("I/O Error : " + e.getMessage());
        } finally {
            System.out.println("Closing connection");
            if (inFromServer != null) inFromServer.close();
            if (inFromUser != null) inFromUser.close();
            if (outToServer != null) outToServer.close();
            if (clientSocket != null) clientSocket.close();
        }
    }
}