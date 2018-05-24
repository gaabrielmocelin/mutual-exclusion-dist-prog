
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;

class Process {

    private static ArrayList<String> messageList = new ArrayList<>();
    private static String nickname;
    private static String destineIp;
    private static int destinePort;
    private static String destineNickname;
    private static int waitTime;
    private static String token;
    private static int errorCount = 0;
    private static DatagramSocket clientSocket;
    private static byte[] receiveData = new byte[1024];

    public Process() {
        
    }

    public static void main(String args[]) throws Exception {
        token = "1234";
        destinePort = 9876;
        clientSocket = new DatagramSocket(destinePort);
        readFile();
        startListener();
        inputMessages();
        sendToken();
    }

    private static void inputMessages() throws Exception {
        (new Thread() {
            @Override
            public void run() {
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
                        System.in));

                while (true) {
                    System.out.println("Digite o nome do destinatário:");
                    try {
                        destineNickname = inFromUser.readLine();
                    } catch (IOException ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Digite o texto a ser enviado:");
                    String sentence;
                    try {
                        sentence = inFromUser.readLine();
                        if (messageList.size() < 10) {
                            messageList.add(sentence);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
    
    private static void readFile() throws IOException{
        BufferedReader br = new BufferedReader(new FileReader("text.txt"));
        try {
            destineIp = br.readLine();
            nickname = br.readLine();
            waitTime = Integer.parseInt(br.readLine());

            System.out.println(destineIp + " - " + nickname + " - " + waitTime);
        }catch (Exception e){
          
        } finally {
            br.close();
        }
    }
    
    private static boolean sendMessage(String message) throws Exception {
            byte[] sendData = message.getBytes();
            InetAddress destineAddress = InetAddress.getByName(destineIp);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destineAddress, destinePort);
            clientSocket.send(sendPacket);
            return true;
    }

    private static boolean sendNextMessage() throws Exception {
        if(messageList.isEmpty() || !hasToken()) {
            return false;
        }
        // 2345;naocopiado:Bob:Alice:Oi Mundo!
        String message = "2345;naocopiado:" + nickname + ":" + destineNickname + ":" + messageList.remove(0);
        
        if (sendMessage(message)){
            System.out.println("Enviando:" + message);
            return true;
        }else{
            return false;
        }
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
                        doSomething(modifiedSentence);
                        receiveData = null;
                        receiveData = new byte[1024];
                    } catch (Exception ex) {
                        Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
    
    private static void doSomething(String messageReceived) throws Exception {
        if(messageReceived.startsWith("1234")) {
            //token
            token = messageReceived.substring(0, 3);
            if(!sendNextMessage()) {
                sendToken();
            }
        } else if(messageReceived.startsWith("2345")) {
            //mensagem
            if (isMineMessage(messageReceived)){
                if (isBroadcastMessage(messageReceived)) {
                    sendToken();
                } else if (isMessageWithError(messageReceived)){
                    if(errorCount < 1){
                        messageReceived = updateErrorMessage(messageReceived);
                        sendMessage(messageReceived);
                        errorCount ++;
                    } else {
                        errorCount = 0;
                        sendToken();
                    }
                    
                } else {
                    errorCount = 0;
                    sendToken();
                }
            } else if (isBroadcastMessage(messageReceived)) {
                System.out.println("Repassando mensagem de broadcast: " + messageReceived);
                sendMessage(messageReceived);
            } else {
                if (isThisMessageForMe(messageReceived)){
                    printMessage(messageReceived);
                    String updatedMessage = updateMessage(messageReceived);
                    sendMessage(updatedMessage);
               } else {
                    System.out.println("Repassando mensagem: " + messageReceived);
                    sendMessage(messageReceived);
                    
                }
            }
        }
    }
    
    private static boolean isMessageWithError(String message){
       return message.contains("erro");
    }
    
    private static void printMessage(String message) {
        String[] msg = message.split(":");
        
        System.out.println("origem: " + msg[1] + " - " + msg[3]);
    }
    
    private static String updateErrorMessage(String message) {
        message.replace("erro", "naocopiado");
        return message;
    }
    
    private static String updateMessage(String message) {
        
        double random = Math.random() % 10;
        
        if (random < 3){
            message.replace("naocopiado", "erro");
            return message;
        }
        
        message.replace("naocopiado", "OK");
        return message;
    }
    
    private static boolean isMineMessage(String message) {
        String[] split = message.split(":");
        return split[1].equals(nickname);
    }
    
    private static boolean isThisMessageForMe(String message) {
        String[] split = message.split(":");
        return split[2].equals(nickname) ;
    }
    
    private static boolean isBroadcastMessage(String message) {
        String[] split = message.split(":");
        return split[2].equals("TODOS");
    }
}
