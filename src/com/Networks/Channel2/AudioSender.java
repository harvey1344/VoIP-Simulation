package com.Networks.Channel2;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Vector;

public class AudioSender implements Runnable {
    static DatagramSocket2 sender;

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
            System.out.println("Line Unavailable Exception");
            ex.printStackTrace();
        }

        // Set up IP to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("192.168.43.14");
        } catch (UnknownHostException ex) {
            System.out.println("Unknown Host Exception");
            ex.printStackTrace();
        }

        // Create new datagram socket on application port
        try {
            sender = new DatagramSocket2();
        } catch (SocketException ex) {
            System.out.println("Socket Error Exception on Port " + App2.PORT);
            ex.printStackTrace();
        }


        short sequenceNum = 0;
        byte[][] toSend = new byte[9][516];
        boolean running = true;

        while (running) {
            for (int i = 0; i < 9; i++) {
                sequenceNum++;
                // Record audio and store byte
                byte[] audio = new byte[512];
                try {
                    assert (recorder != null);
                    audio = recorder.getBlock();
                } catch (IOException e) {
                    System.out.println("Error");
                    e.printStackTrace();
                }
                ByteBuffer buffer = ByteBuffer.allocate(516);
                buffer.putShort(App2.AUTHENTICATION);
                buffer.putShort(sequenceNum);
                buffer.put(audio);
                toSend[i] = buffer.array();
            }



            //Interleave data
            Vector<byte[]> interleavedData = new Vector<>();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    int x = j * 3 + (3 - 1 - i);
                    interleavedData.add(toSend[x]);
                }

                //Send packet data
                while (!interleavedData.isEmpty()) {
                    DatagramPacket packet = new DatagramPacket((byte[]) interleavedData.remove(0), 516, clientIP, App2.PORT);
                    try {
                        sender.send(packet);
                    } catch (IOException e) {
                        System.out.println("Packet not sent");
                    }
                }
            }
        }
    }
}
