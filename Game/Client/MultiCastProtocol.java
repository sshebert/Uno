/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game.Client;


import Game.Shared.Constants;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author alawren3
 */
public class MultiCastProtocol {
    
    private static Constants C = new Constants();
    private MulticastSocket socket;
    private boolean inGroup;
    private static String SERVER_IP_GROUP;
    private static final Lock lock = new ReentrantLock();
    private InetAddress lastReceivedAddress;

    MultiCastProtocol(String groupAddress){
        try {
            SERVER_IP_GROUP = groupAddress;
            socket = new MulticastSocket(C.MultiPORT);
            socket.joinGroup(InetAddress.getByName(groupAddress));
            socket.setSoTimeout(C.MultiTimeout);
            inGroup=true;
        }catch(IOException e){
            e.printStackTrace();
        }
    }
   

    public boolean joinGroup(String address){
        try {
            InetAddress group = InetAddress.getByName(address);
            if (inGroup) {
                socket.joinGroup(group);
                return true;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean leaveGroup(String address) {
        try {
            InetAddress group = InetAddress.getByName(address);
            if (inGroup) {
                socket.leaveGroup(group);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public byte[] receive(int dataSize,int timeOut){

        try {
            socket.setSoTimeout(timeOut);
            DatagramPacket packet = new DatagramPacket(new byte[dataSize], dataSize);
            socket.receive(packet);
            lastReceivedAddress = packet.getAddress();
            byte[] data = new byte[packet.getLength()];
            ByteBuffer bb = ByteBuffer.wrap(packet.getData());
            bb.get(data);
            return data;
        }catch(SocketTimeoutException e){
            return null;
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
    }
    
    public InetAddress getLastReceivedAddress(){
        return lastReceivedAddress;
    }
    /*
    I dont see us using this ever so its gonna be left blank for not and possible
    commented out later
     */
    public void send(byte[] data){
        lock.lock();
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(SERVER_IP_GROUP), C.MultiPORT);
            socket.send(packet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            lock.unlock();
        }


    }

}
