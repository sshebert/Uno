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

/**
 *
 * @author alawren3
 */
public class MultiCastProtocol {

    private MulticastSocket socket;
    private boolean inGroup;
    private static final String SERVER_IP_GROUP = "356.25.0.0";

    MultiCastProtocol(String groupAddress){
        try {
            socket = new MulticastSocket(Constants.MultiPORT);
            socket.joinGroup(InetAddress.getByName(groupAddress));
            inGroup=true;
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    MultiCastProtocol(){
        try {
            socket = new MulticastSocket(Constants.MultiPORT);
            inGroup=false;
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

    public byte[] receive(int dataSize){
        try {
            DatagramPacket packet = new DatagramPacket(new byte[dataSize], dataSize);
            socket.receive(packet);
            byte[] data = new byte[packet.getLength()];
            ByteBuffer bb = ByteBuffer.wrap(packet.getData());
            bb.get(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
    I dont see us using this ever so its gonna be left blank for not and possible
    commented out later
     */
    public void send(byte[] data){
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(SERVER_IP_GROUP), Constants.MultiPORT);
            socket.send(packet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
