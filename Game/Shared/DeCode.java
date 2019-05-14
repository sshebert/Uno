/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game.Shared;

import Game.Client.ClientGame;
import Game.Client.Main;

import java.io.*;
import java.nio.ByteBuffer;
/**
 *
 * @author alawren3
 */
public class DeCode {

    public int opcode;
    public String ip;
    public Player player;
    public ClientGame game;

    public DeCode(byte[] unParsedData){//send player object
        ByteBuffer bb = ByteBuffer.wrap(new AESEncrytion(Main.getKey()).decryptToBytes(unParsedData));
        opcode = bb.getInt();
        if(opcode == 0){
            byte[] playerData = new byte[bb.remaining()];
            if(playerData.length>0){
                bb.get(playerData);
                try{
                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(playerData));
                     player = (Player) in.readObject();
                }
                catch (IOException exp){
                    exp.printStackTrace();
                }
                catch (ClassNotFoundException exp){
                    exp.printStackTrace();
                }
            }
        }else if(opcode == 1) {//send multicast IP
            if (unParsedData.length - 4 > 0) {
                byte[] unparsedString = new byte[unParsedData.length - 4];
                bb.get(unparsedString);
                ip = new String(unparsedString);
            } else {
                ip = null;
            }
        }else if(opcode == 2){//send game
            byte[] gameData = new byte[unParsedData.length-4];
            if(gameData.length>0){
                bb.get(gameData);
                try{
                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(gameData));
                    game = (ClientGame) in.readObject();
                }
                catch (IOException exp){
                    exp.printStackTrace();
                }
                catch (ClassNotFoundException exp){
                    exp.printStackTrace();
                }
            }
        }else if(opcode == 3){//ack

        }
        else {//failed to decrypt packet - section by Benjamin Groman
        	opcode = -1;
        	ip = null;
        	player = null; 
        	game = null;
        }
    }
}