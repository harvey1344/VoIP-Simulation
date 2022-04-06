package com.Networks.Analysis;

/*
 * TextSender.java
 */

/**
 *
 * @author  abj
 */
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

public class TextSender implements Runnable{

    static DatagramSocket4 sending_socket;

    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run (){

        //***************************************************
        //Port to send to
        int PORT = 55555;
        //IP ADDRESS to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost");  //CHANGE localhost to IP or NAME of client machine
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Open a socket to send from
        //We dont need to know its port number as we never send anything to it.
        //We need the try and catch block to make sure no errors occur.

        //DatagramSocket sending_socket;
        try{
            sending_socket = new DatagramSocket4();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Get a handle to the Standard Input (console) so we can read user input

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //***************************************************

        //***************************************************
        //Main loop.

        boolean running = true;
        int send=1;
        short auth=5698;
        int hash;
        String str = null;
        Vector<byte[]> stored = new Vector<>();


        for (int i=0;i<1000;i++){
            try {

                byte[] packet = new byte[0];


                    //Read in a string from the standard input
                    str = String.valueOf(i);
                    send++;

                    ByteBuffer buffer = ByteBuffer.allocate(86);
                    ByteBuffer buffer1 = ByteBuffer.allocate(80);
                    byte[] buff = str.getBytes();
                    buffer1.put(buff);
                    byte[] string = buffer1.array();


                    hash = Arrays.hashCode(string);
                    buffer.putInt(hash);
                    buffer.putShort(auth);
                    buffer.put(string);
                    //Convert it to an array of bytes
                    packet = buffer.array();

                    // stored 9 packets before sending




                    DatagramPacket p = new DatagramPacket(packet,86, clientIP, PORT);

                    //Send it
                    sending_socket.send(p);

                    //The user can type EXIT to quit
                    if (str.equals("EXIT")) {
                        running = false;
                    }


                //Make a DatagramPacket from it, with client address and port number

                //The user can type EXIT to quit
                if (str.equals("EXIT")) {
                    running = false;
                }

            } catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        sending_socket.close();
        //***************************************************
    }
}