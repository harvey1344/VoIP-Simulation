package com.Networks.Analysis;

/*
 * TextReceiver.java
 */

/**
 *
 * @author  abj
 */
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import java.lang.reflect.Array;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.Arrays;
import java.util.Vector;

public class TextReceiver implements Runnable{

    static DatagramSocket4 receiving_socket;




    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run (){

        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        //***************************************************

        //***************************************************
        //Open a socket to receive from on port PORT

        //DatagramSocket receiving_socket;
        try{
            receiving_socket = new DatagramSocket4(PORT);
            //receiving_socket.setSoTimeout(8);
        } catch (SocketException e){
            System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Main loop.

        boolean running = true;
        int packetCount=0;
        int badPackets=0;
        Vector<byte[]> storage= new Vector<>();

        for (int i=0;i<1000;i++){

                try {
                    //Receive a DatagramPacket (note that the string cant be more than 80 chars)
                    byte[] buffer = new byte[86];
                    DatagramPacket packet = new DatagramPacket(buffer, 0, 80);
                    receiving_socket.receive(packet);
                    packetCount += 1;
                    ByteBuffer buff = ByteBuffer.wrap(buffer);
                    int hash = buff.getInt(0);
                    short auth = buff.getShort(4);
                    byte[] string = Arrays.copyOfRange(buffer, 6, buffer.length);
                    //System.out.print(hash +"####"+Arrays.hashCode(string));
                    //System.out.println();
                    //System.out.println(auth);

                    if (auth==5698 && hash!= Arrays.hashCode(string)) {
                        badPackets+=1;
                    }

                    //Get a string from the byte buffer
                    String str = new String(string);
                    //Display it
                    System.out.println(str);

                    //The user can type EXIT to quit
                    if (str.substring(0, 4).equals("EXIT")) {
                        running = false;
                    }
                } catch (SocketTimeoutException ex) {
                    break;
                } catch (IOException e) {
                    System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                    e.printStackTrace();
                }


            //System.out.println(storage.size());





                //}



        }
        //Close the socket
        receiving_socket.close();
        //***************************************************

        // print diagnostics
        int sentPackets=1000;

        double packetlost = 100- ((double)packetCount/ (double) sentPackets *100);
        System.out.println("Sent packets= " + sentPackets);
        System.out.println("Received Packets =" + packetCount);
        System.out.println("PacketLoss = " + packetlost + "%");
        System.out.println("Bad packets = " + badPackets);
    }
}
