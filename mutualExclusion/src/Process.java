
import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

class Process {

    private static int destinePort;
    private static int myPort;
    private static String token;
    private static Timer timeOut = new Timer();

    public Process() {

    }

    public static void main(String args[]) throws Exception {
        token = "1234";
        myPort = 9872;
//        token = null;
//        myPort = 9871;
//        token = null;
//        myPort = 9873;

        updateDestinePortOf(myPort);
        startListener();
        startReadFile();
        if (token != null) {
            tokenReceived(token);
        }
    }
    
    private static void resetTimeOut() {
        timeOut.cancel();
        timeOut.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Run election ");
            }
        }, 20000);
    }
    
    private static void restartToken() throws Exception {
        token = "1234";
        TimeUnit.SECONDS.sleep(1);
        updateDestinePortOf(myPort);
        if (token != null) {
            tokenReceived(token);
        }
    }
    
    private static void startReadFile() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    readFile();
                } catch (IOException ex) {
                    System.out.println("Erro: " + ex.getMessage());
                }
            }
        }, 1000);
    }

    //CONTROL RESULTS FILE METHODS
    private static void readFile() throws IOException {
        if (!hasToken()) {
            return;
        }

        BufferedReader br = new BufferedReader(new FileReader("text.txt"));
        try {

            String currentLine, lastLine = "";

            while ((currentLine = br.readLine()) != null) {
                lastLine = currentLine;
            }

            if (lastLine != "") {
                System.out.println("last line: " + lastLine);
                executeOperation(lastLine);
            }

        } catch (Exception e) {
            System.out.println("error reading the file xxxx");
        } finally {
            br.close();
        }
    }

    private static void executeOperation(String line) {
        System.out.println("executing operation");

        saveOperation(line);
    }

    private static void saveOperation(String line) {
        System.out.println("saving operation");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("filename"))) {

            bw.write(line);
        } catch (IOException e) {
            System.out.println("error writing on file");
            e.printStackTrace();
        }
    }

    //CONTROL PROCESS FILE METHODS
    private static void updateDestinePortOf(int port) throws IOException {
//        if(!hasToken()) {
//            return;
//        }
        BufferedReader br = new BufferedReader(new FileReader("process.txt"));
        try {

            String firstLine = null;
            boolean isFirstLine = true;

            String currentLine, nextLine;
            while ((currentLine = br.readLine()) != null) {

                if (isFirstLine) {
                    firstLine = currentLine;
                    isFirstLine = false;
                }

                int possiblePort = Integer.parseInt(currentLine);
                if (possiblePort == port) {

                    nextLine = br.readLine();
                    if (nextLine != null) {
                        destinePort = Integer.parseInt(nextLine);
                    } else {
                        destinePort = Integer.parseInt(firstLine);
                    }

                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("error reading the file");
        } finally {
            br.close();
        }
    }

    //CONTROL TOKEN METHODS
    private static void sendToken() throws Exception {
        String msg = "1234";
        System.out.println("port: " + myPort + " sending token" + destinePort);
        //sendMessage(msg);
        try (Socket cliente = new Socket("localhost", destinePort)) {
            ObjectOutputStream saida = new ObjectOutputStream(cliente.getOutputStream());
            saida.flush();
            saida.writeObject(msg);
            saida.close();
        } catch(Exception e) {
            updateDestinePortOf(destinePort);
            if (destinePort != myPort) {
                sendToken();
            }
        }
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
                    while (true) {
                        System.out.println("*************");
                        Socket cliente = servidor.accept();
                        System.out.println("*************");
                        ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

                        String msg = (String) entrada.readObject();
                        System.out.println(msg + "received on port: " + myPort);
                        System.out.println("*************");

                        tokenReceived(msg);

                        entrada.close();
                        cliente.close();
                    }
                } catch (Exception e) {
                    System.out.println("Erro: " + e.getMessage());
                }
            }
        }).start();
    }

    private static void tokenReceived(String msg) throws Exception {
        resetTimeOut();
        token = msg;

        TimeUnit.SECONDS.sleep(5);
        sendToken();
    }
}
