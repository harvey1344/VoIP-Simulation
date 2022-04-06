package com.Networks.Channel2;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket2;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

public class AudioReceiver implements Runnable
{
    static DatagramSocket2 receiver;


    private static short extractSeq(ByteBuffer voipPacket) {
        return voipPacket.getShort(2);
    }

    private static byte[] extractAudio(ByteBuffer voipPacket) {
        byte[] packet = voipPacket.array();
        return Arrays.copyOfRange(packet, 4, packet.length);
    }


    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }


    //Selection sort to sort interleaved packets back into order
    public Vector<byte[]> sort(Vector<byte[]> v) {
        int n = v.size();
        for (int i = 0; i < n - 1; i++) {
            // Find the minimum element in unsorted array
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if ((extractSeq(ByteBuffer.wrap(v.get(j)))) < (extractSeq(ByteBuffer.wrap(v.get(min))))) {
                    min = j;
                    // Swap the found minimum element with the first
                    // element
                    byte[] temp = v.get(min);
                    v.add(min, v.get(i));
                    v.add(i, temp);
                }
            }
        }
        return v;
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
            receiver = new DatagramSocket2(App2.PORT);
            receiver.setSoTimeout(32);
        } catch (SocketException ex) {
            System.out.println("Unable to open UDP socket on port"
                    + App2.PORT);
            ex.printStackTrace();
        }

        boolean running = true;

        int packetsReceived = 0;
        Vector<byte[]> storageVec = new Vector<>();
        Vector<byte[]> received = new Vector<>();

        while (running) {
            received.clear();
            while (received.size() != 9) {
                //Receive packet and add to a byte vector
                byte[] receivedPacket = new byte[516];
                DatagramPacket packet = new DatagramPacket(receivedPacket, 0, 516);
                {
                    try {
                        receiver.receive(packet);
                        packetsReceived++;
                        received.add(packet.getData());
                    } catch (SocketTimeoutException ignored) {
                    } catch (IOException ex) {
                        System.out.println("Unexpected error");
                        ex.printStackTrace();
                    }
                }
            }

            int smallest;
            int index = 0;
            int lastPacket=0;


            //Initial sorting of data
            //We found that then sorting it again with selection sort ensured better audio quality
            //It does seem a bit silly to have 2 sorting methods, but it sounds better with 2 so we decided to
            //keep both
            while (received.size() != 0) {
                index = 0;
                smallest = extractSeq(ByteBuffer.wrap(received.get(0)));
                for (int i = 0; i < received.size(); i++) {
                    if (smallest > extractSeq(ByteBuffer.wrap(received.get(i)))) {
                        smallest = extractSeq(ByteBuffer.wrap(received.get(i)));
                        index = i;
                    }
                }
                storageVec.add(received.remove(index));
            }

            Vector<byte[]> sortedVec = sort(storageVec);


            byte[] audio;
            byte[] currentAudio;
            byte[] lastAudio = new byte[512];
            short seq;

            int lostPacketCount = 0;

            //Continuously runs while sortedVec is not empty
            while (!(sortedVec.isEmpty())) {
                boolean playAudio = true;

                //Extract packet from sorted Vector
                ByteBuffer voipPacket = ByteBuffer.wrap(sortedVec.remove(0));

                //Extract sequence number from the packet
                seq = extractSeq(voipPacket);
                System.out.println(seq);

                //Extract audio from the packet
                currentAudio = extractAudio(voipPacket);
                //audio = new byte[512];


                //Packet loss handling. Decides what to play based on what Sequence number it is
                //if current seq number is not the next consecutive number from the last number, then check if it is
                //the next 1, 2 or 3. If so, play audio anyway as up to 3 packets missed does not seem too noticable
                if (seq == lastPacket + 1 || seq == lastPacket + 2 || seq == lastPacket + 3) {
                    //System.out.println(seq);
                    playAudio = true;
                    lastPacket = seq;
                } else if (seq < lastPacket) { //if the current seq number is less than the previous, play nothing
                    //as they are in wrong order.
                    System.out.println("Current Seq Number " + seq + "less than previous: " + lastPacket);
                    playAudio = false;
                } else {
                    playAudio = true;
                }
                lastPacket = seq;
                lastAudio = currentAudio;

                //Play audio
                if (playAudio) {
                    try {
                        assert player != null;
                        player.playBlock(currentAudio);
                        //lastAudio = audio;
                    } catch (IOException ex) {
                        System.out.println("Error playing block");
                    }
                }

            }

        }
        //close player and receiver
        player.close();
        receiver.close();
    }
}
