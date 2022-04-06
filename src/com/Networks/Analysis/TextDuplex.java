package com.Networks.Analysis;

/*
 * TextDuplex.java
 */

/**
 *
 * @author  abj
 */
public class TextDuplex {

    public static void main (String[] args){

        TextReceiver receiver = new TextReceiver();
        TextSender sender = new TextSender();

        receiver.start();
        sender.start();

    }

}
