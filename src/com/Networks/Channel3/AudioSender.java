package com.Networks.Channel3;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Vector;

public class AudioSender implements Runnable
{
    static DatagramSocket3 sender;

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        // Set up recorder.
        AudioRecorder recorder = null;
        try {
            recorder = new AudioRecorder();
        } catch (LineUnavailableException ex) {
            System.out.println("Line in unavailable");
            ex.printStackTrace();
        }

        // Set up IP to send to.
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("192.168.43.14");
        } catch (UnknownHostException ex) {
            System.out.println("Cannot resolve hostname");
            ex.printStackTrace();
        }

        // Create new datagram socket on application port.
        try {
            sender = new DatagramSocket3();
        } catch (SocketException ex) {
            System.out.println("Unable to open UDP socket on port" + App3.PORT);
            ex.printStackTrace();
        }

        short sequenceNum=0;
        byte[][] payloads = new byte[9][516];

        //Main loop.
        while (true)
        {
            //Gets 9 blocks of audio and adds a sequence number and auth code puting it into a 3D byte array.
            for (int i=0; i<9; i++) {
                sequenceNum++;
                // Record audio and store byte
                byte[] audio = new byte[512];
                try {
                    assert (recorder != null);
                    audio = recorder.getBlock();
                } catch (IOException e) {
                    System.out.println("Unexpected Error");
                    e.printStackTrace();
                }
                ByteBuffer buffer = ByteBuffer.allocate(516);
                buffer.putShort(App3.AUTHENTICATION);
                buffer.putShort(sequenceNum);
                buffer.put(audio);
                payloads[i] = buffer.array();
            }

            // Creates a vector of interleaved packets from the 3D byte array
            Vector<byte[]> interleaved = interleave(payloads);

            //sends the packets one at a time until the vector is empty.
            while (!interleaved.isEmpty()) {
                DatagramPacket packet = new DatagramPacket(interleaved.remove(0), 516, clientIP, App3.PORT);
                try {
                    sender.send(packet);
                } catch (IOException e) {
                    System.out.println("Packet not sent");
                }
            }
        }
    }

    public static Vector<byte[]> interleave(byte[][] bytes)
    {
        // adjusts the order of packets so that no consecutive packets are sent one after another.
        Vector<byte[]> interleaved = new Vector<>();
        for (int i=0; i<3; i++){
            for (int j=0; j<3;j++){
                int x = j*3+(3-1-i);
                interleaved.add(bytes[x]);
            }
        }
        return interleaved;
    }
}
