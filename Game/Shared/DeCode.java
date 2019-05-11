/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Game.Shared;

import Game.Client.ClientGame;

import java.io.*;
import java.nio.ByteBuffer;
/**
 *
 * @author alawren3
 */
public class DeCode {

    public int opcode;
    public String ip;
    public Card card;
    public Suit wildSuit;
    public boolean skip;
    public ClientGame game;
    private byte[] cardData;

    public DeCode(byte[] unParsedData){
        ByteBuffer bb = ByteBuffer.wrap(unParsedData);
        opcode = bb.getInt();
        if(opcode == 1){
            if(unParsedData.length - 4 > 0) {
                byte[] unparsedString = new byte[unParsedData.length - 4];
                bb.get(unparsedString);
                ip = new String(unparsedString);
            }else{
                ip = null;
            }
        }else if(opcode == 5 ||opcode == 6 ||opcode == 7 ||opcode == 11){
            cardData = new byte[unParsedData.length-4];
            if(cardData.length>0){
                bb.get(cardData);
                try{
                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(cardData));
                    card = (Card)in.readObject();
                }
                catch (IOException exp){
                    exp.printStackTrace();
                }
                catch (ClassNotFoundException exp){
                    exp.printStackTrace();
                }
            }else{
                card = null;
            }


        }else if(opcode == 13){//wild suit
            byte suitNum = bb.get();
            wildSuit = Suit.fromByte(suitNum);
           /* cardData = new byte[unParsedData.length-5];
            if(cardData.length>0){
                bb.get(cardData);
                try{
                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(cardData));
                    card = (Card)in.readObject();
                }
                catch (IOException exp){
                    exp.printStackTrace();
                }
                catch (ClassNotFoundException exp){
                    exp.printStackTrace();
                }
            }else{
                card = null;
            }*/
        }else if(opcode == 8){
            byte byteSkip = bb.get();
            skip = byteSkip == 1;
        }
    }
}
