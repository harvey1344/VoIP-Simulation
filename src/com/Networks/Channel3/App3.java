package com.Networks.Channel3;

/*
 * Packet Structure for Channel 3
 * 0-1 = Authentication number
 * 2-3 = Sequence Number
 * 4-516 = Audio
 */


public class App3
{
    static int PORT= 55555;
    static short AUTHENTICATION = 699;

    public static void main(String[] args)  {
        System.out.println("Network Coursework Channel 3");
        AudioSender sender= new AudioSender();
        AudioReceiver receiver= new AudioReceiver();
        sender.start();
        receiver.start();



    }
}
