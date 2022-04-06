package com.Networks.Encryption;

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Main {

    public static void main(String[] args) {
        System.out.println("Network Coursework Channel 1 Encrypted");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Input 1 for XOR Encryption or 2 for RSA/AES encryption or 3 for RSA/AES no decrypt ");
        String encryptionOption = null;
        try {
            encryptionOption = in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (!encryptionOption.equals("1") && !encryptionOption.equals("2") && !encryptionOption.equals("3"))
        {
            System.out.println("Didn't input a valid number try again");
            System.out.println("Input 1 for XOR Encryption or 2 for RSA/AES encryption or 3 for RSA/AES no decrypt ");
            try {
                encryptionOption = in.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (encryptionOption.equals("1"))
        {
            String address = null;
            try {
                System.out.println("Input IP address of other PC: ");
                address = in.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            senderAudio audioS = new senderAudio(address, 55555, 323, 323);
            audioS.start();
            receiverAudio audioR = new receiverAudio(55555, 323 , 323);
            audioR.start();
            System.out.println("Connected");
            System.out.println("Type exit to quit");
            String exit = null;
            try {
                exit = in.readLine();
                if (exit.equals("exit")) {
                    System.out.println("Closing Application...");
                    audioS.stop();
                    audioR.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (encryptionOption.equals("2")) {
            RSAEncryption encryption = new RSAEncryption();
            KeyPair keyPair = RSAEncryption.generateKeyPair(1024);
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            SecretKey secretKey = AESEncryption.generateSecretKey(256);
            IvParameterSpec IV = AESEncryption.generateIV();
            System.out.println("Input IP address of other PC: ");
            String result = null;
            try {
                result = in.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            PublicKeySender sendPublicKey = new PublicKeySender(55556, result, publicKey);
            sendPublicKey.start();
            PublicKeyReceiver recievePublicKey = new PublicKeyReceiver(55556);
            recievePublicKey.start();
            PublicKey receivedPublicKey = null;
            try {
                receivedPublicKey = recievePublicKey.getPublicKey();
                while(receivedPublicKey == null)
                {
                    receivedPublicKey = recievePublicKey.getPublicKey();
                    System.out.println(receivedPublicKey);
                }
                receivedPublicKey = recievePublicKey.getPublicKey();
                recievePublicKey.stop();
                sendPublicKey.stop();
                String output = Base64.getEncoder().encodeToString(receivedPublicKey.getEncoded());
                String currentPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                System.out.println("Output: " + currentPublicKey);
                System.out.println("Input: " + output);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RSATextSender rsaTextSender = new RSATextSender(55558, result, receivedPublicKey, secretKey, IV);
            rsaTextSender.start();
            RSATextReceiver rsaTextReceiver = new RSATextReceiver(55558, privateKey);
            rsaTextReceiver.start();
            SecretKey receivedKey = null;
            IvParameterSpec receivedIV= null;
            try {
                rsaTextReceiver.join();
                receivedKey = rsaTextReceiver.getSecretKey();
                receivedIV = rsaTextReceiver.getIV();
                rsaTextSender.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Made Handshake");
            System.out.println(receivedIV);
            System.out.println(receivedKey);
            AESAudioSender aesAudioSender = new AESAudioSender(55555, result, secretKey, IV);
            aesAudioSender.start();
            AESAudioReceiver aesAudioReceiver = new AESAudioReceiver(55555, receivedKey, receivedIV, true);
            aesAudioReceiver.start();
            System.out.println("Connected");
            System.out.println("Type exit to quit");
            String exit = null;
            try {
                exit = in.readLine();
                if (exit.equals("exit")) {
                    aesAudioSender.stop();
                    aesAudioReceiver.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(encryptionOption.equals("3")) {
            RSAEncryption encryption = new RSAEncryption();
            KeyPair keyPair = RSAEncryption.generateKeyPair(1024);
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            SecretKey secretKey = AESEncryption.generateSecretKey(256);
            IvParameterSpec IV = AESEncryption.generateIV();
            System.out.println("Input IP address of other PC: ");
            String result = null;
            try {
                result = in.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            PublicKeySender sendPublicKey = new PublicKeySender(55557, result, publicKey);
            sendPublicKey.start();
            PublicKeyReceiver recievePublicKey = new PublicKeyReceiver(55557);
            recievePublicKey.start();
            PublicKey receivedPublicKey = null;
            try {
                receivedPublicKey = recievePublicKey.getPublicKey();
                while(receivedPublicKey == null)
                {
                    receivedPublicKey = recievePublicKey.getPublicKey();
                    System.out.println(receivedPublicKey);
                }
                receivedPublicKey = recievePublicKey.getPublicKey();
                recievePublicKey.stop();
                sendPublicKey.stop();
                String output = Base64.getEncoder().encodeToString(receivedPublicKey.getEncoded());
                String currentPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                System.out.println("Output: " + currentPublicKey);
                System.out.println("Input: " + output);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RSATextSender rsaTextSender = new RSATextSender(55558, result, receivedPublicKey, secretKey, IV);
            rsaTextSender.start();
            RSATextReceiver rsaTextReceiver = new RSATextReceiver(55558, privateKey);
            rsaTextReceiver.start();
            SecretKey receivedKey = null;
            IvParameterSpec receivedIV= null;
            try {
                rsaTextReceiver.join();
                receivedKey = rsaTextReceiver.getSecretKey();
                receivedIV = rsaTextReceiver.getIV();
                rsaTextSender.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Made Handshake");
            System.out.println(receivedIV);
            System.out.println(receivedKey);
            AESAudioSender aesAudioSender = new AESAudioSender(55555, result, secretKey, IV);
            aesAudioSender.start();
            AESAudioReceiver aesAudioReceiver = new AESAudioReceiver(55555, receivedKey, receivedIV, false);
            aesAudioReceiver.start();
            System.out.println("Connected");
            System.out.println("Type exit to quit");
            String exit = null;
            try {
                exit = in.readLine();
                if (exit.equals("exit")) {
                    aesAudioSender.stop();
                    aesAudioReceiver.stop();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class XOREncryption extends Thread {
    int key;

    public XOREncryption(int key)
    {
        this.key = key;
    }

    public byte[] crypt(byte[] input)
    {
        ByteBuffer inputBuffer = ByteBuffer.wrap(input);
        ByteBuffer outputBuffer = ByteBuffer.allocate(input.length);
        for(int j = 0; j < input.length/4; j++)
        {
            int currentByte = inputBuffer.getInt();
            currentByte = currentByte ^ key;
            outputBuffer.putInt(currentByte);
        }
        return outputBuffer.array();
    }
}

class PublicKeySender extends Thread {
    static DatagramSocket socket;

    int port;
    String ip;
    PublicKey publicKey;

    public PublicKeySender(int port, String ip, PublicKey publicKey)
    {
        this.port = port;
        this.ip = ip;
        this.publicKey = publicKey;
    }

    @Override
    public void run()
    {
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName(ip);
        } catch (Exception e) {
            System.out.println("Could not get local IP");
            e.printStackTrace();
            System.exit(0);
        }
        try {
            socket = new DatagramSocket();
        } catch (Exception e) {
            System.out.println("Could not make UDP Socket");
            e.printStackTrace();
            System.exit(0);
        }
        String stringPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        //System.out.println("Running Main Packet Loop!");
        boolean running = true;

        while (running){
            try{
                //System.out.println("Sending " + stringPublicKey);
                //Convert it to an array of bytes
                byte[] buffer = stringPublicKey.getBytes();
                //byte[] encryptedBuffer = RSAEncryption.encrypt(buffer, publicKey);
                //Make a DatagramPacket from it, with client address and port number
                //System.out.println(buffer.length);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, port);

                //Send it
                socket.send(packet);

            } catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        socket.close();
    }
}

class PublicKeyReceiver extends Thread {
    int port;
    static DatagramSocket socket;
    PublicKey publicKey;

    public PublicKeyReceiver(int port)
    {
        this.port = port;
    }

    @Override
    public void run()
    {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[216];
        boolean running = true;
        //System.out.println("Reciever Running!");
        while(running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, 0, 216);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] decodedKeyBuffer = Base64.getDecoder().decode(buffer);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKeyBuffer);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey receivedPublicKey = keyFactory.generatePublic(keySpec);
                //System.out.println("Running");
                //byte[] decryptedBuffer = RSAEncryption.decrypt(buffer, privateKey);
                if (receivedPublicKey != null) {
                    //System.out.println("Ended Reciever");
                    this.publicKey = receivedPublicKey;
                    break;
                }
                String str = new String(buffer);
                System.out.println(str);
                if (str.contains("EXIT")) {
                    running = false;
                }
            } catch (Exception e) {
                System.out.println("Error receiving packet!");
                e.printStackTrace();
            }
        }
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }
}

class RSAEncryption {
    public static KeyPair generateKeyPair(int keyLength) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keyLength);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte[] input_bytes, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(input_bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] input_bytes, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(input_bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

class AESEncryption {
    public static SecretKey generateSecretKey(int size) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(size);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static IvParameterSpec generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static byte[] encrypt(byte[] input, SecretKey key, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return cipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] input, SecretKey key, IvParameterSpec iv) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

class RSATextSender extends Thread {
    static DatagramSocket socket;

    int port;
    String ip;
    PublicKey publicKey;
    SecretKey secretKey;
    IvParameterSpec IV;

    public RSATextSender(int port, String ip, PublicKey publicKey, SecretKey secretKey, IvParameterSpec IV)
    {
        this.port = port;
        this.ip = ip;
        this.publicKey = publicKey;
        this.secretKey = secretKey;
        this.IV = IV;
    }

    @Override
    public void run()
    {
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName(ip);
        } catch (Exception e) {
            System.out.println("Could not get local IP");
            e.printStackTrace();
            System.exit(0);
        }
        try {
            socket = new DatagramSocket();
        } catch (Exception e) {
            System.out.println("Could not make UDP Socket");
            e.printStackTrace();
            System.exit(0);
        }
        //System.out.println("Running Main Packet Loop!");
        for (int i = 0; i < 100; i++)
        {
            try{

                String stringSecretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                byte[] encryptedBuffer = RSAEncryption.encrypt(stringSecretKey.getBytes(), publicKey);
                //Make a DatagramPacket from it, with client address and port number
                DatagramPacket KeyPacket = new DatagramPacket(encryptedBuffer, encryptedBuffer.length, clientIP, port);
                byte[] IVBytes = IV.getIV();
                byte[] encryptedIVBuffer = RSAEncryption.encrypt(IVBytes, publicKey);
                DatagramPacket IVPacket = new DatagramPacket(encryptedIVBuffer, encryptedIVBuffer.length, clientIP, port);
                //Send it
                socket.send(KeyPacket);
                //System.out.println("Sent Key Packet!");
                socket.send(IVPacket);
                //System.out.println("Sent IV Packet!");
            } catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        socket.close();
    }
}

class RSATextReceiver extends Thread {
    int port;
    static DatagramSocket socket;
    PrivateKey privateKey;
    SecretKey secretKey;
    IvParameterSpec IV;

    public RSATextReceiver(int port, PrivateKey privateKey)
    {
        this.port = port;
        this.privateKey = privateKey;
    }

    @Override
    public void run()
    {
        try {
            this.socket = new DatagramSocket(port);
            //socket.setSoTimeout(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[128];
        boolean running = true;

        for (int i = 0; i < 100; i++) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, 0, 128);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                byte[] decryptedKeyBuffer = RSAEncryption.decrypt(buffer, privateKey);
                byte[] decodedKeyBuffer = Base64.getDecoder().decode(decryptedKeyBuffer);
                SecretKey secretKey = new SecretKeySpec(decodedKeyBuffer, 0, decodedKeyBuffer.length, "AES");
                //System.out.println("Made Secret Key!");
                DatagramPacket packet2 = new DatagramPacket(buffer, 0, 128);
                try {
                    socket.receive(packet2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                byte[] decryptedIVBuffer = RSAEncryption.decrypt(buffer, privateKey);
                IvParameterSpec IV = new IvParameterSpec(decryptedIVBuffer);
                //System.out.println("Made IV!");
                if (secretKey != null && IV != null) {
                    //System.out.println("Ending Reciever");
                    this.secretKey = secretKey;
                    this.IV = IV;
                    break;
                }
            } catch (Exception e) {
                System.out.println("Error receiving packet!");
                e.printStackTrace();
            }

        }

    }

    public SecretKey getSecretKey() {
        return this.secretKey;
    }

    public IvParameterSpec getIV() {
        return this.IV;
    }
}

class AESAudioSender extends Thread {
    int port;
    InetAddress clientIP;
    static DatagramSocket socket;
    SecretKey secretKey;
    IvParameterSpec IV;
    long auth = 323;
    long seqNum = 0;

    public AESAudioSender(int port, String address, SecretKey secretKey, IvParameterSpec IV)
    {
        this.port = port;
        this.secretKey = secretKey;
        this.IV = IV;
        this.clientIP = null;
        try {
            this.clientIP = InetAddress.getByName(address);
        } catch (Exception e) {
            System.out.println("Could not get local IP");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void run() {
        try {
            try {
                socket = new DatagramSocket();
            } catch (Exception e) {
                System.out.println("Could not make UDP Socket");
                e.printStackTrace();
                System.exit(0);
            }
            boolean running = true;
            AudioRecorder recorder = new AudioRecorder();
            //System.out.println("Recording and Sending Audio!");
            while (running)
            {
                seqNum++;
                //String testString = new String("Test");
                //byte[] tempBlock = testString.getBytes();
                byte[] tempBlock = recorder.getBlock();
                ByteBuffer blockBuffer = ByteBuffer.allocate(528);
                blockBuffer.putLong(auth);
                blockBuffer.putLong(seqNum);
                blockBuffer.put(tempBlock);
                byte[] blockBufferDone = blockBuffer.array();
                //System.out.println("Sent: " + tempBlock);
                //System.out.println(tempBlock.length);
                //byte[] finishedEncryptedBlock = new byte[512];
                byte[] finishedEncryptedBlock = AESEncryption.encrypt(blockBufferDone, secretKey, IV);
                //System.out.println(finishedEncryptedBlock.length);
                //System.out.println(finishedEncryptedBlock.length);
                //System.out.println("Finished Encryption Block: " + finishedEncryptedBlock.length);
                DatagramPacket dp = new DatagramPacket(finishedEncryptedBlock, finishedEncryptedBlock.length, clientIP, port);
                socket.send(dp);
                //System.out.println("Sent Audio!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class AESAudioReceiver extends Thread {
    int port;
    static DatagramSocket socket;
    SecretKey secretKey;
    IvParameterSpec IV;
    boolean decrypt;

    public AESAudioReceiver(int port, SecretKey secretKey, IvParameterSpec IV, boolean decrypt)
    {
        this.port = port;
        this.secretKey = secretKey;
        this.IV = IV;
        this.decrypt = decrypt;
    }

    @Override
    public void run() {
        try {
            AudioPlayer player = new AudioPlayer();
            byte[] tempBlock = new byte[544];
            try {
                socket = new DatagramSocket(port);
                socket.setSoTimeout(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                boolean running = true;
                while (running)
                {
                    DatagramPacket packet = new DatagramPacket(tempBlock, 0, 544);
                    try {
                        socket.receive(packet);
                        byte[] originalAudio = new byte[512];
                        if(decrypt) {
                            originalAudio = AESEncryption.decrypt(tempBlock, secretKey, IV);
                            //System.out.println(originalAudio.length);
                            ByteBuffer audioBuffer = ByteBuffer.wrap(originalAudio);
                            long rauth= audioBuffer.getLong(0);
                            long seqNum = audioBuffer.getLong(8);
                            System.out.println(seqNum);
                            //System.out.println(rauth);
                            byte[] audio = Arrays.copyOfRange(originalAudio, 16, originalAudio.length);
                            //System.out.println(audio.length);
                            if (rauth==323){
                                player.playBlock(audio);}
                            else
                            {
                                System.out.println("bad");
                            }
                        } else {
                            player.playBlock(tempBlock);
                        }
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class senderAudio extends Thread {
    int port;
    InetAddress clientIP;
    static DatagramSocket socket;
    int key;
    int authKey;
    public senderAudio(String address, int port, int key, int authKey)
    {
        this.port = port;
        this.key = key;
        this.authKey = authKey;
        this.clientIP = null;
        try {
            this.clientIP = InetAddress.getByName(address);
        } catch (Exception e) {
            System.out.println("Could not get local IP");
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void run() {
        try {
            try {
                socket = new DatagramSocket();
            } catch (Exception e) {
                System.out.println("Could not make UDP Socket");
                e.printStackTrace();
                System.exit(0);
            }
            boolean running = true;
            AudioRecorder recorder = new AudioRecorder();
            //System.out.println("Recording and Sending Audio!");
            int seqNum = 0;
            while (running)
            {
                seqNum++;
                //String testString = new String("Test");
                //byte[] tempBlock = testString.getBytes();
                byte[] tempBlock = recorder.getBlock();
                ByteBuffer blockBuffer = ByteBuffer.allocate(520);
                blockBuffer.putInt(authKey);
                blockBuffer.putInt(seqNum);
                blockBuffer.put(tempBlock);
                XOREncryption xorEncryption = new XOREncryption(key);
                byte[] finishedEncryptedBlock = xorEncryption.crypt(blockBuffer.array());
                //System.out.println("Finished Encryption Block: " + finishedEncryptedBlock.length);
                DatagramPacket dp = new DatagramPacket(finishedEncryptedBlock, finishedEncryptedBlock.length, clientIP, port);
                socket.send(dp);
                //System.out.println("Sent Audio!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class receiverAudio extends Thread {
    int port;
    static DatagramSocket socket;
    int key;
    int authKey;

    public receiverAudio(int port, int key, int authKey)
    {
        this.port = port;
        this.key = key;
        this.authKey = authKey;
    }

    @Override
    public void run() {
        try {
            AudioPlayer player = new AudioPlayer();
            byte[] tempBlock = new byte[520];
            try {
                socket = new DatagramSocket(port);
                socket.setSoTimeout(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                boolean running = true;
                while (running)
                {
                    DatagramPacket packet = new DatagramPacket(tempBlock, 0, 520);
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //String str = new String(tempBlock);
                    //System.out.println(str);
                    XOREncryption xorEncryption = new XOREncryption(key);
                    byte[] finishedDecryptionBlock = xorEncryption.crypt(tempBlock);
                    ByteBuffer audioBuffer = ByteBuffer.wrap(finishedDecryptionBlock);
                    int rauth = audioBuffer.getInt(0);
                    int seqNum = audioBuffer.getInt(4);
                    System.out.println(seqNum);
                    //System.out.println(rauth);
                    if (rauth==authKey) {
                        byte[] audio = Arrays.copyOfRange(finishedDecryptionBlock, 8, finishedDecryptionBlock.length);
                        player.playBlock(audio);
                    } else {
                        System.out.println("Received Bad Packet!");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
