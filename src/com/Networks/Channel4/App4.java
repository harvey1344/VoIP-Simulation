package com.Networks.Channel4;

/*
 * Packet Structure
 * 0-3 HashFunction
 * 4-5 Auth Code
 * 6-7 Sequence Number
 * 8-520 Audio
 */

public class App4
{
    static int PORT= 55555;
    static short AUTHENTICATION = 699;


    public static void main(String[] args)  {
        System.out.println("Network Coursework Channel 4");
        AudioSender sender= new AudioSender();
        AudioReceiver receiver= new AudioReceiver();
        sender.start();
        receiver.start();



    }
}
