package com.Networks.Channel2;

/*
 * Packet Structure for Channel 2
 * 0-1 = Authentication number
 * 2-3 = Sequence Number
 * 4-516 = Audio
 */


public class App2
{
    static int PORT= 55555;
    static short AUTHENTICATION = 699;

    public static void main(String[] args)  {
        System.out.println("Network Coursework Channel 2");
        AudioSender sender= new AudioSender();
        AudioReceiver receiver= new AudioReceiver();
        sender.start();
        receiver.start();

    }
}
