import models.InitMassage;
import models.TurnMessage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) throws InterruptedException {
        try(Socket socket = new Socket("localhost", 3345);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());)
        {

            System.out.println("Client connected to socket ");
            System.out.println("Client create gameField ");
            UIGame uiGame = null;

            InitMassage initMassage = (InitMassage) ois.readObject();
            if(uiGame==null) {
                uiGame = new UIGame(initMassage.player, Integer.parseInt(initMassage.turn));
                uiGame.actionPlayerUI.subscribe(turnMessage -> {
                    oos.writeObject(turnMessage);
                    oos.flush();
                    System.out.println("Client sent message " + turnMessage.idStick + " " + turnMessage.resultTurn);
                });
            }
            while(!socket.isOutputShutdown()){
                TurnMessage opponentTurn = (TurnMessage) ois.readObject();
                uiGame.actionOpponentUI.onNext(opponentTurn);
                System.out.println("Client sent message to GUI");
            }
            System.out.println("Closing connections & channels on clentSide - DONE.");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
