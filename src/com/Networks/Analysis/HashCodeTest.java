package com.Networks.Analysis;

import javax.management.StringValueExp;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HashCodeTest
{
    public static void main(String[] args)
    {
        String str= String.valueOf(100);
        ByteBuffer buff = ByteBuffer.allocate(6);
        buff.putShort((short) 10);
        buff.putInt(100);
        byte[] a= str.getBytes();
        byte[] b = buff.array();
        byte[] c = new byte[4];
        System.arraycopy(b, 2, c, 0, 4);



        System.out.println(Arrays.hashCode(a));
        System.out.println(Arrays.hashCode(c));
        String as= new String(a);
        System.out.println(as);
        String bs= new String(c);
        System.out.println(bs);



    }
}
