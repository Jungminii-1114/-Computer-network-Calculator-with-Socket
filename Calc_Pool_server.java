import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientHandler implements Runnable {
    private Socket connectionSocket;
    public ClientHandler(Socket socket) {
        this.connectionSocket = socket;
    }
    @Override
    public void run() {
        BufferedReader inFromClient = null;
        PrintWriter outToClient = null;
        String response="";

        try{
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);

            String clientSentence;
            while((clientSentence=inFromClient.readLine())!=null){
                System.out.println("FROM CLIENT " + connectionSocket.getInetAddress() + " : " + clientSentence);
                if(clientSentence.equals("END")){
                    System.out.println("CLIENT" + connectionSocket.getInetAddress() + " Terminated connection.");
                    break;
                }
                response = ""; // 응답 초기화

                try{
                    StringTokenizer stringTokenizer = new StringTokenizer(clientSentence);
                    int a = Integer.parseInt(stringTokenizer.nextToken());
                    String operation = stringTokenizer.nextToken();
                    int b = Integer.parseInt(stringTokenizer.nextToken());
                    int answer = 0;

                    if(operation.equals("ADD") || operation.equals("+") || operation.equals("add")){
                        answer = a + b;
                    }else if(operation.equals("SUB") || operation.equals("-") || operation.equals("sub")){
                        answer = a - b;
                    }else if(operation.equals("MUL") || operation.equals("*") || operation.equals("mul")){
                        answer = a * b;
                    }else if(operation.equals("DIV") || operation.equals("/") || operation.equals("div")){
                        if(b == 0)response = "Error : cannot divide by zero";
                        else answer = a / b;
                    } else{
                        response = "Error : invalid operation";
                    }
                    if(!response.startsWith("Error")){
                        response = String.valueOf(answer);
                    }
                }catch(NoSuchElementException | NumberFormatException e){
                    response = "Error : Invalid Input Format. (e.g. 10 + 20)";
                }catch(ArithmeticException e){
                    response = "Error : Cannot divide by zero";
                }
                outToClient.println(response);
                System.out.println("TO CLIENT " + connectionSocket.getInetAddress() + " : " + response);
            }
        }catch(IOException e) {
            System.out.println("Client(" + connectionSocket.getInetAddress() + ") 통신 중 오류 : " + e.getMessage());
        }finally{
            System.out.println("Client(" + connectionSocket.getInetAddress() + ") 연결 리소스 정리");
            try{
                if(inFromClient!=null) inFromClient.close();
                if(outToClient!=null) outToClient.close();
                if(connectionSocket!=null) connectionSocket.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
public class Calc_Pool_server {
    public static void main(String[] args) {
        ServerSocket welcomeSocket = null;
        ExecutorService ThreadPool = Executors.newFixedThreadPool(10);
        try{
            int nPort = 3456;
            welcomeSocket = new ServerSocket(nPort);
            System.out.println("Calculator Server Starts... (port = " + nPort + ")");

            while(true){
                try{
                    Socket connectionSocket = welcomeSocket.accept();
                    System.out.println("Client Connected " + connectionSocket.getInetAddress());

                    Runnable clientTask = new ClientHandler(connectionSocket);
                    ThreadPool.execute(clientTask);
                }catch(IOException e) {
                    System.out.println("Error while Client Accept : " + e.getMessage());
                }
            }
        }catch(IOException e) {
            System.out.println("Server Socket Error : " + e.getMessage());
        }finally{
            ThreadPool.shutdown();
            try{
                if(welcomeSocket!=null) welcomeSocket.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
