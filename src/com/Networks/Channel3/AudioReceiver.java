package com.Networks.Channel3;

import CMPC3M06.AudioPlayer;
import uk.ac.uea.cmp.voip.DatagramSocket3;
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
    static DatagramSocket3 receiver;
    int nextPacket=1;
    Packet lastPacket=new Packet();

    private class Packet implements Comparable<Packet>{
        short seq;
        short auth;
        byte[] audio;

        public Packet(ByteBuffer buffer){
            auth = buffer.getShort(0);
            seq = buffer.getShort(2);
            byte[] packet = buffer.array();
            audio = Arrays.copyOfRange(packet, 4, packet.length);
        }

        public Packet(){
            audio = new byte[512];
            auth = App3.AUTHENTICATION;
        }

        public short getSeq() {
            return seq;
        }

        public short getAuth() {
            return auth;
        }

        public byte[] getAudio() {
            return audio;
        }

        @Override
        public int compareTo(Packet o) {
            return this.seq-o.getSeq();
        }

        public boolean approvePacket(){
            if(this.auth==App3.AUTHENTICATION){
                return true;
            }else {
                return false;
            }
        }
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

        //setting up the packet receiver
        try {
            receiver = new DatagramSocket3(App3.PORT);
            receiver.setSoTimeout(32);
        } catch (SocketException ex) {
            System.out.println("Unable to open UDP socket on port" + App3.PORT);
            ex.printStackTrace();
        }

        Vector<Packet> storageVec = new Vector<>();
        Vector<Packet> toPlay = new Vector<>();
        int lpl =0;

        //main loop
        while (true) {
            storageVec.clear();

            //receives nine packets and adds them to a vector after checking they are from the expected sender.
            while (storageVec.size()!=9) {
                byte[] receivedPacket = new byte[516];
                DatagramPacket datagramPacket = new DatagramPacket(receivedPacket, 0, 516);
                {
                    try {
                        receiver.receive(datagramPacket);
                        Packet packet = new Packet(ByteBuffer.wrap(datagramPacket.getData()));
                        if (packet.approvePacket()) {
                            storageVec.add(packet);
                        }
                    } catch (SocketTimeoutException ignored) {
                    } catch (IOException ex) {
                        System.out.println("Unexpected error");
                        ex.printStackTrace();
                    }
                }
            }

            // sorts the vector to account for packets arriving out of order and interleavig
            storageVec.sort(Packet::compareTo);


            /*
            Adding packets that are ready to be played to a vector. Discards packets with a lower sequence number than
            thr last packet that was added. Missing packets are also accounted for, if one packet is missing the last to
            be added would be added twice to fill the space. If two are missing in a row the last packet and the next packet
            would both be played twice.If more are missing up to two blank audio sequences would be played before the audio
            is simply spliced together.
             */
            int packetsMissing=0;
            boolean largePLoss = false;
            while (!storageVec.isEmpty()){
                Packet next = storageVec.get(0);
                if(next.seq==nextPacket) {
                    packetsMissing=0;
                    toPlay.add(next);
                    storageVec.remove(0);
                    nextPacket++;
                    if (largePLoss){
                        toPlay.add(toPlay.size()-1,next);
                        largePLoss = false;
                        lpl++;
                        System.out.println(lpl);
                    }
                }else if(next.seq<nextPacket) {
                    storageVec.remove(0);
                }else {
                    if (packetsMissing<1){
                        if(toPlay.size()>1){
                            toPlay.add(toPlay.get(toPlay.size()-1));
                        }else {
                            toPlay.add(lastPacket);
                        }
                    }
                    else if(packetsMissing<2){
                        toPlay.add(new Packet());

                    }else if(packetsMissing<3){
                        largePLoss = true;
                        toPlay.add(new Packet());
                    }
                    packetsMissing++;
                    nextPacket++;
                }
            }



            // The array of packets that have been ordered are played one by one.
            while (!(toPlay.isEmpty()))
            {
                Packet packet = toPlay.remove(0);
                try {
                    player.playBlock(packet.audio);
                    lastPacket=packet;
                } catch (IOException ex) {
                    System.out.println("Error playing block");
                }
            }
        }
    }
}


