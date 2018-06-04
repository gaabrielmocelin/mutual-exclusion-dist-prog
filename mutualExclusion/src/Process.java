
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

class Process {

    private static String destineIp;
    private static int destinePort;
    private static int myPort;
    private static int waitTime;
    private static String token;
    private static DatagramSocket clientSocket;
    private static byte[] receiveData = new byte[1024];
    
    

    public Process() {
        
    }

    public static void main(String args[]) throws Exception {
        destineIp = "127.0.0.1";
        token = null;
        myPort = 9872;
   
        clientSocket = new DatagramSocket(myPort);
        
        if (token != null){
            tokenReceived(token);    
        }
        
        updateDestinePortOf(myPort);
        startListener();
    }
    
    
    //CONTROL RESULTS FILE METHODS
    private static void readFile() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("text.txt"));
        try {
            
            String currentLine, lastLine = "";
            
            while((currentLine = br.readLine()) != null){
                lastLine = currentLine;
            }
            
            if (lastLine != ""){
                System.out.println("last line: " + lastLine);
                executeOperation(lastLine);
            }
            
        }catch (Exception e){
            System.out.println("error reading the file");
        } finally {
            br.close();
        }
    }
    
    private static void executeOperation(String line){
        System.out.println("executing operation");
        
        saveOperation(line);
    }
    
    private static void saveOperation(String line){
        System.out.println("saving operation");
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("filename"))) {
            
            bw.write(line);
        } catch (IOException e) {
            System.out.println("error writing on file");
            e.printStackTrace();
        }
    }
    
    //CONTROL PROCESS FILE METHODS
    //THIS METHOD SHOULD BE ALWAYS RUNNING ON A SECONDARY THREAD
       private static void updateDestinePortOf(int port) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("process.txt"));
        try {
            
            String firstLine = null;
            boolean isFirstLine = true;
            
            String currentLine, nextLine;
            while((currentLine = br.readLine()) != null){
                
                if(isFirstLine){
                    firstLine = currentLine;
                    isFirstLine = false;
                }
                
                int possiblePort = Integer.parseInt(currentLine);
                if (possiblePort == port){
                    
                    nextLine = br.readLine();
                    if (nextLine != null){
                        destinePort = Integer.getInteger(currentLine);
                    }else{
                        destinePort = Integer.getInteger(firstLine);
                    } 
                    
                    break;
                }
            }
        }catch (Exception e){
            System.out.println("error reading the file");
        } finally {
            br.close();
        }
    }
           
    //CONTROL TOKEN METHODS
    private static boolean sendMessage(String message) throws Exception {
            byte[] sendData = message.getBytes();
            InetAddress destineAddress = InetAddress.getByName(destineIp);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destineAddress, destinePort);
            clientSocket.send(sendPacket);
            return true;
    }
    
    private static void sendToken() throws Exception {
        String msg = "1234";
        System.out.println("port: " + myPort + " sending token");
        //sendMessage(msg);
        
        Socket cliente = new Socket(destineIp, destinePort);
        ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
        saida.flush();
        saida.writeObject(msg);
        saida.close();
        cliente.close();
        token = null;
    }

    private static boolean hasToken() {
        return (token != null);
    }

    private static void startListener() throws Exception {
        (new Thread() {
            @Override
            public void run() {
                try {
                    // Instancia o ServerSocket ouvindo a porta 12345
                    ServerSocket servidor = new ServerSocket(myPort);
                    while(true) {
                        Socket cliente = servidor.accept();
                        ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
                        
                        String msg = (String) entrada.readObject();
                        System.out.println(msg + "received on port: " + myPort);
                        System.out.println("*************");
                        
                        tokenReceived(msg);
                        
                        entrada.close();
                        cliente.close();
                  }  
                }   
                catch(Exception e) {
                   System.out.println("Erro: " + e.getMessage());
                }
            }
        }).start();
    }
    
    private static void tokenReceived(String msg) throws Exception {
       token = msg;
       
       //readFile();
       TimeUnit.MINUTES.sleep(1);
       
       sendToken();
    }
}
