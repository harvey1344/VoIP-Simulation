package com.Networks.Channel1Auth;

import CMPC3M06.AudioRecorder;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class AudioSender implements Runnable
{
    static DatagramSocket sender;
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }


    public void run() {
        // Set up recorder
        AudioRecorder recorder = null;
        try {
            recorder = new AudioRecorder();
        } catch (LineUnavailableException ex) {
            System.out.println("Line in unavailable");
            ex.printStackTrace();
        }

        // Set up IP to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("192.168.43.14");
        } catch (UnknownHostException ex) {
            System.out.println("Cannot resolve hostname");
            ex.printStackTrace();
        }

        // Create new datagram socket on application port
        try {
            sender = new DatagramSocket();
        } catch (SocketException ex) {
            System.out.println("Unable to open UDP socket on port"
                    + App.PORT);
            ex.printStackTrace();
        }

        boolean running =true;
        short sequenceNum=0;

        while (running)
        {
            sequenceNum++;
            // Record audio and store byte
            byte[] audio= new byte[512];
            try
            {
                assert (recorder != null);
                audio = recorder.getBlock();
            } catch (IOException e) {
                System.out.println("Unexpected Error");
                e.printStackTrace();
            }
            ByteBuffer buffer= ByteBuffer.allocate(516);
            buffer.putShort(App.AUTHENTICATION);
            buffer.putShort(sequenceNum);
            buffer.put(audio);
            byte[] payload= buffer.array();
            // Create packet and send
            DatagramPacket packet = new DatagramPacket(payload, payload.length, clientIP, App.PORT);
            try {
                sender.send(packet);
            } catch (IOException e) {
                System.out.println("Packet not sent");
            }

        }
    }
}
