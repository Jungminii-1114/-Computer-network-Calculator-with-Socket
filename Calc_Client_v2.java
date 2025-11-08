import java.io.*;
import java.net.*;

public class Calc_Client_v2 {
    private static String serverIP = "127.0.0.1";
    private static int nPort = 3456;
    private static final String CONFIG_FILE = "server_info.dat";

    private static void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists() && !configFile.isDirectory()) {
            try(BufferedReader br = new BufferedReader(new FileReader(configFile))){
                String lineIP = br.readLine();
                String linePort = br.readLine();

                if(lineIP != null && linePort != null){
                    serverIP = lineIP.trim();
                    nPort = Integer.parseInt(linePort.trim());
                    System.out.println(CONFIG_FILE + "Setting readed from file. (Server : " + serverIP + ", Port : " + nPort + ")");
                }else{
                    System.out.println(CONFIG_FILE + "File format Error");
                }
            }catch(IOException | NumberFormatException e){
                System.out.println(CONFIG_FILE + "File read Error. Use default Value (127.0.0.1)" + e.getMessage());
            }
        }else{
            System.out.println(CONFIG_FILE + "File cannot found. Use default Value (127.0.0.1)");
        }
    }

    public static void main(String[] args) throws IOException {
        loadConfig();

        Socket clientSocket = null;
        PrintWriter outToServer = null;
        BufferedReader inFromClient = null;
        BufferedReader inFromServer = null;

        try{
            clientSocket = new Socket(serverIP, nPort);
            System.out.println("Server Connected : (Target : " + serverIP + " : " + nPort + ")");

            inFromClient = new BufferedReader(new InputStreamReader(System.in));
            outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while(true){
                System.out.println("Enter to Calculate (Type \"exit\" if you want to terminate)");
                String sentenceFromUser = inFromClient.readLine();

                if(sentenceFromUser == null){
                    System.out.println("Input didn't accepted. Terminate the program.");
                    break;
                }

                // 입력된 내용을 서버로 전달
                outToServer.println(sentenceFromUser);

                if(sentenceFromUser.equalsIgnoreCase("exit")){
                    System.out.println("Program Terminated");
                    break;
                }

                String sentenceFromServer = inFromServer.readLine();
                if(sentenceFromServer == null){
                    System.out.println("Connection loss from server");
                    break;
                }

                System.out.println("RAW FROM SERVER : " + sentenceFromServer);

                // Protocol Parsing...
                String[] parts = sentenceFromServer.split("\\|");
                if(parts.length > 0 && parts[0].equals("OK")){
                    if(parts.length > 2){
                        System.out.println("Answer : " + parts[1]);
                    }
                } else if (parts.length > 0 && parts[0].equals("ERR")) {
                    if(parts.length >=3){
                        System.out.println("Error Message : " + parts[2]);
                    }else if(parts.length >= 2){
                        System.out.println("Error Message : " + parts[1]);
                    }
                }else{
                    System.out.println("Error : Unknown response accepted from Server.");
                }
            }
        }catch(UnknownHostException e){
            System.out.println("Cannot find Host : " + serverIP);
        }catch(ConnectException e){
            System.out.println("Failed to connect to Server : " + serverIP);
            System.out.println("Check whether server is running and check " + CONFIG_FILE + "'s IP/PORT number.");
        }catch(IOException e){
            System.out.println("I/O Error : " + e.getMessage());
        }finally{
            System.out.println("Terminate Connection");
            if(inFromClient != null) inFromClient.close();
            if(outToServer != null) outToServer.close();
            if(inFromServer != null) inFromServer.close();
            if(clientSocket != null) clientSocket.close();
        }
    }
}
