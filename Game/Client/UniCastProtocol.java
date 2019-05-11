/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game.Client;

import Game.Shared.Constants;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 *
 * @author alawren3
 */
public class UniCastProtocol {

    private static DatagramSocket socket;
    private static DatagramPacket receiver;
    private static DatagramPacket sender;
    private InetAddress lastReceivedAddress;

    UniCastProtocol(int timeOutInMill) {
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeOutInMill);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] data, InetAddress sendTo) {

        try {
            sender = new DatagramPacket(data, data.length, sendTo, Constants.PORT);
            socket.send(sender);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*notes
    using InterruptedIOException to signify that you didnt get the packet you wanted
    in the amount of time you expected.
     */
    public byte[] recieve(int attempts, int dataSize) throws InterruptedIOException {
        int count = 0;

        try {
            receiver = new DatagramPacket(new byte[dataSize], dataSize);
            socket.receive(receiver);
            byte[] data = new byte[receiver.getLength()];
            ByteBuffer bb = ByteBuffer.wrap(receiver.getData());
            bb.get(data);
            lastReceivedAddress = receiver.getAddress();
            return data;
        } catch (SocketTimeoutException e) {
            count++;
            if (count >= attempts) {
                throw new InterruptedIOException();
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public String getAddress() {
        return receiver.getAddress().getHostName();
    }

    public InetAddress getLastReceivedAddress() {
        return lastReceivedAddress;
    }

}
