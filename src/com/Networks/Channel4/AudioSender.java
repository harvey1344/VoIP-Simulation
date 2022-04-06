package com.Networks.Channel4;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

public class AudioSender implements Runnable
{
    static DatagramSocket4 sender;
    public void start()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run()
    {
        // Set up recorder
        AudioRecorder recorder = null;
        try
        {
            recorder= new AudioRecorder();
        }
        catch (LineUnavailableException ex)
        {
            System.out.println("Line in unavailable");
            ex.printStackTrace();
        }

        // Set up IP to send to
        InetAddress clientIP= null;
        try
        {
            clientIP= InetAddress.getByName("192.168.43.14");
        }
        catch (UnknownHostException ex)
        {
            System.out.println("Cannot resolve hostname");
            ex.printStackTrace();
        }

        // Create new datagram socket on application port
        try
        {
            sender= new DatagramSocket4();
        }
        catch (SocketException ex)
        {
            System.out.println("Unable to open UDP socket on port"
                    + App4.PORT);
            ex.printStackTrace();
        }

        /*
        *
        * Main Loop for VoIP
        *
         */

        boolean running =true;
        short seqNum=0;
        Vector<byte[]> stored = new Vector<>();
        while (running) {
            seqNum++;
            ByteBuffer VoipPacket = ByteBuffer.allocate(520);
            byte[] audio;
            int hash;
            // Record audio
            try {
                assert (recorder != null);
                audio = recorder.getBlock();
                hash = Arrays.hashCode(audio);
                VoipPacket.putInt(hash);
                // add authentication number SECURITY
                VoipPacket.putShort(App4.AUTHENTICATION);
                // add sequence number VOIP
                VoipPacket.putShort(seqNum);
                // add actual audio APPLICATION
                VoipPacket.put(audio);

                } catch (IOException ex) {
                    System.out.println("Unexpected Error");
                    ex.printStackTrace();
                }

                byte[] payload= VoipPacket.array();

                // Create new packet and send
                DatagramPacket packet = new DatagramPacket(payload, payload.length, clientIP, App4.PORT);
                try {
                    sender.send(packet);
                } catch (IOException e) {
                    System.out.println("Packet not sent");
                }


        }
        recorder.close();
        sender.close();
    }

}
