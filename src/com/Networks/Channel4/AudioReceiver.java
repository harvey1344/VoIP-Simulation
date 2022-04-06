package com.Networks.Channel4;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

public class AudioReceiver implements Runnable {
    static DatagramSocket4 receiver;

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    private static byte[] getAudio(ByteBuffer voipPacket) {
        byte[] packet = voipPacket.array();
        return Arrays.copyOfRange(packet, 8, packet.length);

    }


    private static short getSeq(ByteBuffer voipPacket) {
        return voipPacket.getShort(6);
    }

    private static int getHash(ByteBuffer voipPacket) {
        return voipPacket.getInt(0);
    }

    private static short getAuth(ByteBuffer voipPacket) {
        return voipPacket.getShort(4);
    }

    // returns true if packets auth header matches the app static auth numbe
    private static boolean authenticatePacket(ByteBuffer voipPacket) {
        short auth = getAuth(voipPacket);
        return auth == App4.AUTHENTICATION;

    }
    // returns true if data is unchanged
    private static boolean isGood(ByteBuffer voipPacket) {
        int hash = getHash(voipPacket);
        byte[] audio = getAudio(voipPacket);
        return hash == Arrays.hashCode(audio);

    }


    public void run()
    {
        // Set up player
        AudioPlayer player = null;
        try {
            player = new AudioPlayer();
        } catch (LineUnavailableException ex) {
            System.out.println("No line available");
            ex.printStackTrace();
        }

        byte[] audio;
        short seq;
        // Set up receiving socket

        try {
            receiver = new DatagramSocket4(App4.PORT);
            receiver.setSoTimeout(32);
        } catch (SocketException ex) {
            System.out.println("Unable to open UDP socket on port"
                    + App4.PORT);
            ex.printStackTrace();
        }

        // Main loop
        boolean running = true;
        int packetsReceived = 0;
        int intercepted = 0;
        Vector<byte[]> storage = new Vector<>();

        //int prevSeq=0;
        //TODO store in a bufer before playing
        while (running) {
            byte[] prev= new byte[512];
            byte[] receivedPacket = new byte[520];
            DatagramPacket packet = new DatagramPacket(receivedPacket, 0, 520);
            {
                try {
                    receiver.receive(packet);
                    ByteBuffer voipPacket = ByteBuffer.wrap(receivedPacket);
                    seq = getSeq(voipPacket);
                    audio = getAudio(voipPacket);
                    // receiver based compensation
                    // if packet okay store to play in place of corrupted
                    if (authenticatePacket(voipPacket) && isGood(voipPacket))
                    {
                        prev=audio;
                        try {
                            assert player != null;
                            player.playBlock(audio);
                        } catch (IOException ex) {
                            System.out.println("Error playing block");
                        }
                    }

                    } catch (SocketTimeoutException e)
                    {
                        try {
                            assert player != null;
                            player.playBlock(prev);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }

                    } catch (IOException ex) {
                        System.out.println("Unexpected error");

                        ex.printStackTrace();
                    }



                //TODO decrypt packet
            }






        }
        player.close();
        receiver.close();
        }






}




