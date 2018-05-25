
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;

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
        token = "1234";
        myPort = 9875;
        destinePort = 9876;
        destineIp = "127.0.0.1";
        clientSocket = new DatagramSocket(myPort);
        
        if (token != null){
            tokenReceived(token);    
        }
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
       private static void readProcessFile() throws IOException{
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
                
                int port = Integer.parseInt(currentLine);
                if (port == myPort){
                    
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
       
    private static void saveProcess(String line){
        System.out.println("saving operation");
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("process.txt"))) {
            bw.write(String.valueOf(myPort));
        } catch (IOException e) {
            System.out.println("error writing on file");
            e.printStackTrace();
        }
    }
    
    private static void removeNextProcess(String line){
        System.out.println("should remove the destine port because it does not exist anymore");
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
        System.out.println("Enviando token");
        sendMessage(msg);
        token = null;
    }

    private static boolean hasToken() {
        return (token != null);
    }

    private static void startListener() throws Exception {
        (new Thread() {
            @Override
            public void run() {
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        clientSocket.receive(receivePacket);
                    } catch (IOException ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String modifiedSentence = new String(receivePacket.getData());
                    System.out.println("DEBUGGGG Texto recebido do servidor:" + modifiedSentence);
                    System.out.println("WAITING");
                    try {
                        Thread.sleep(waitTime * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        tokenReceived(modifiedSentence);
                        receiveData = null;
                        receiveData = new byte[1024];
                    } catch (Exception ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
    
    private static void tokenReceived(String msg) throws Exception {
       token = msg;
       
       readFile();
       
       sendToken();
    }
}
