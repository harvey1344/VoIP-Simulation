package com.Networks.Channel1Auth;

import CMPC3M06.AudioPlayer;


import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AudioReceiver implements Runnable
{
    static DatagramSocket receiver;

    private static short extractSeq(ByteBuffer voipPacket) {
    return voipPacket.getShort(2);
    }
    private static short getAuth(ByteBuffer voipPacket) {
    return voipPacket.getShort(0);
    }
    private static byte[] extractAudio(ByteBuffer voipPacket) {
        byte[] packet = voipPacket.array();
        return Arrays.copyOfRange(packet, 4, packet.length);
    }
    private static boolean approvePacket(ByteBuffer voipPacket)
    {
        short authenticate= getAuth(voipPacket);
        return authenticate== App.AUTHENTICATION;
    }



    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        // Set up player
        AudioPlayer player = null;
        try {
            player = new AudioPlayer();
        } catch (LineUnavailableException ex) {
            System.out.println("No line available");
            ex.printStackTrace();
        }

        try {
            receiver = new DatagramSocket(App.PORT);
            receiver.setSoTimeout(32);
        } catch (SocketException ex) {
            System.out.println("Unable to open UDP socket on port"
                    + App.PORT);
            ex.printStackTrace();
        }

        boolean running = true;
        short sequenceNumber;
        byte[] empty = new byte[512];

        while (running)
        {
            byte[] receivedPacket = new byte[512];
            DatagramPacket packet = new DatagramPacket(receivedPacket, 0, 512);

            try {
                receiver.receive(packet);
                ByteBuffer buffer= ByteBuffer.wrap(receivedPacket);
                sequenceNumber= extractSeq(buffer);

                if (approvePacket(buffer)) {
                    byte[] audio = extractAudio(buffer);
                    try {
                        assert player != null;
                        //System.out.println(sequenceNumber);
                        player.playBlock(audio);

                    } catch (IOException ex) {
                        System.out.println("Error playing block");
                    }
                }
                else
                {
                    System.out.println("rejected");
                }

            } catch (SocketTimeoutException e) {
                try {
                    assert player != null;
                    player.playBlock(empty);

                } catch (IOException ex) {
                    System.out.println("Error playing block");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }





        }
    }
}
