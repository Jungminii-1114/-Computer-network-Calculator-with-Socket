import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientHandler_Final implements Runnable {
    private Socket connectionSocket;

    public ClientHandler_Final(Socket socket) {
        this.connectionSocket = socket;
    }
    @Override
    public void run() {
        BufferedReader inFromClient = null;
        PrintWriter outToClient = null;

        try{
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);

            String clientSentence;

            while((clientSentence = inFromClient.readLine()) != null){
                System.out.println("FROM CLIENT (" + connectionSocket.getInetAddress().getHostName() + ") : " + clientSentence);

                if(clientSentence.equalsIgnoreCase("exit")){
                    System.out.println("Client (" + connectionSocket.getInetAddress().getHostName() + ") has been exited");
                    break;
                }
                String response;

                try{
                    StringTokenizer st = new StringTokenizer(clientSentence);

                    if(st.countTokens() != 3){
                        throw new IllegalArgumentException("Invalid client sentence");
                    }

                    int a = Integer.parseInt(st.nextToken());
                    String operation = st.nextToken().toUpperCase();
                    int b = Integer.parseInt(st.nextToken());
                    int answer = 0;

                    switch(operation){
                        case "+" : case "ADD":
                            answer = a+b;
                            break;
                        case "-" : case "SUB":
                            answer = a-b;
                            break;
                        case "*" : case "MUL":
                            answer = a*b;
                            break;
                        case "/" : case "DIV":
                            if(b == 0){
                                throw new ArithmeticException("Division by zero");
                            }
                            answer = a/b;
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid operation");
                    }
                    response = "OK|" + answer;
                }catch(NoSuchElementException | NumberFormatException e){
                    response = "ERR|INVALID_FORMAT|Invalid number format or missing arguments.";
                }catch(IllegalArgumentException e){
                    if(e.getMessage().equals("INVALID_ARGS")){
                        response = "ERR|INVALID_ARGS|too many arguments.";
                    }else{
                        response = "ERR|UNKNOWN_OP|Invalid operation";
                    }
                }catch(ArithmeticException e){
                    response = "ERR|DIV_BY_ZERO|divided by zero";
                }

                outToClient.println(response);
                System.out.println("TO CLIENT : " + response) ;
            }
        }catch(IOException e){
            System.out.println("CLIENT (" + connectionSocket.getInetAddress() + ") : " + e.getMessage());
        }finally{
            System.out.println("CLIENT (" + connectionSocket.getInetAddress() + ") connection resource close");
            try{
                if(inFromClient != null) inFromClient.close();
                if(outToClient != null) outToClient.close();
                if(connectionSocket != null) connectionSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
public class Calc_Pool_serer_v2 {
    public static void main(String[] args) {
        ServerSocket welcomeSocket = null;
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try{
            int nPort = 3456;
            welcomeSocket = new ServerSocket(nPort);
            System.out.println("Calculator server Starts... (port");
            while(true){
                try{
                    Socket connectionSocket = welcomeSocket.accept();
                    System.out.println("Client connected " + connectionSocket.getInetAddress().getHostName());

                    Runnable clientTask = new ClientHandler_Final(connectionSocket);

                    pool.execute(clientTask);
                }catch(IOException e){
                    System.out.println("Error occurred while client accept" + e.getMessage());
                }
            }
        }catch(IOException e){
            System.out.println("Server Socket Error : " + e.getMessage());
        }finally{
            pool.shutdown();
            try{
                if(welcomeSocket != null)welcomeSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
