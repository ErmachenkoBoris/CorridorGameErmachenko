import io.reactivex.subjects.PublishSubject;
import models.Stick;
import models.StickStore;
import models.TurnMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class UIGame {

    private Map jPanel1;

    public PublishSubject<TurnMessage> actionPlayerUI = PublishSubject.create();
    public PublishSubject<TurnMessage> actionOpponentUI = PublishSubject.create();


    // private javax.swing.JPanel jPanel1;

    public UIGame(String player, int turn) {
         final int height = 500;
         final int width = 500;
         final int cells = 10;

        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("The sticks. Player name: " + player);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jPanel1 = new Map(cells, width, height, player, turn);
        frame.getContentPane().add(jPanel1);

        frame.setPreferredSize(new Dimension(width, height));

        frame.pack();
        frame.setVisible(true);

        jPanel1.actionPlayerMAP.subscribe(turnMessage -> {
            actionPlayerUI.onNext(turnMessage);
        });
        actionOpponentUI.subscribe(turnMessage -> {
            System.out.println("In subscribe actionOpponentUI");
            jPanel1.actionOpponentMAP.onNext(turnMessage);
        });
    }


}

class Map extends JPanel {

    public PublishSubject<TurnMessage> actionPlayerMAP = PublishSubject.create();
    public PublishSubject<TurnMessage> actionOpponentMAP = PublishSubject.create();

    private int linesCount = 0;

    public StickStore stickStore;
    private Stick startStick;
    private ArrayList<Stick> sticks = new ArrayList<>();

    private int width = 0;
    private int height = 0;
    private Color playerColor;
    private Color opponentColor;
    private Color green = new Color(0, 220, 0);
    private Color red = new Color(220, 109, 84);
    private Color blue = new Color(50, 25, 220);
    public int turn = 0;
    private int opponentScore = 0;
    private int playerScore = 0;

    public void repaintMap() {
        this.revalidate();
         this.repaint();
    }

    public Map(final int linesCount, int width, int height, String player, int turn) {
        this.linesCount = linesCount;
        this.stickStore = new StickStore();
        this.height = height;
        this.width = width;
        this.turn = turn;
        switch (player) {
            case  ("GREEN"):
                this.playerColor = this.green;
                this.opponentColor = this.red;
                break;
            case ("RED"):
                this.playerColor = this.red;
                this.opponentColor = this.green;
                break;
            default:
                this.playerColor = this.blue;
                this.opponentColor = this.blue;
                break;
        }
        this.init();

        JLabel lab1 = new JLabel("Opponent: " + this.opponentScore+ "Your score: " + this.playerScore );
        setLayout(new FlowLayout());
        add(lab1);

        actionOpponentMAP.subscribe(turnMessage -> {
            System.out.println("In subscribe actionOpponentMAP");
            stickStore.getSticks().get(Integer.parseInt(turnMessage.idStick)).color = opponentColor;
            System.out.println("TURN " + turnMessage.resultTurn);
            if(Integer.parseInt(turnMessage.resultTurn) > 0) {
                this.opponentScore++;
                lab1.setText("Opponent: " + this.opponentScore+ "Your score: " + this.playerScore);
                calculateResult(stickStore.getSticks().get(Integer.parseInt(turnMessage.idStick)), opponentColor);
                System.out.println("OPPONENT SCORE SCORE " + this.opponentScore);
                this.turn = 0;
            } else {
                this.turn = 1;
            }
            System.out.println("OPPONENT " + this.turn +" and stick " + turnMessage.idStick);
            System.out.println(" getTurn() "+ getTurn());
            repaintMap();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(getTurn() == 1) {
                    System.out.println("Click yeah");
                    ArrayList<Stick> sticks = stickStore.getSticks();
                    for (int i = 0; i < sticks.size(); i++) {
                        Stick stick = sticks.get(i);
                        if (stick.contains(e.getPoint()) && stick.color!= playerColor && stick.color!= opponentColor) {
                            int result = calculateResult(stick, playerColor);
                            setTurn(result);
                            if(result>0) {
                                playerScore++;
                                lab1.setText("Opponent: " + opponentScore+ "Your score: " + playerScore);
                                System.out.println("PLAYER SCORE " + playerScore);
                            }
                            TurnMessage newTurnMessage = new TurnMessage(stick.getId(), result);
                            actionPlayerMAP.onNext(newTurnMessage);
                            stick.color = playerColor;
                            repaintMap();
                            break;
                        }
                    }
                } else {
                    System.out.println("IT'S YOUR OPPONENT TURN ! "+ getTurn());
                }
            }
        });

    }

    private void setTurn(int i) {
        this.turn = i;
    }

    public int getTurn() {
        return this.turn;
    }

    public void init() {
        int cellWidth = this.height / linesCount;
        int cellHeight = this.width / linesCount;

        int stickWidth = 6;
        int margin = 50;
        for (int i = 0; i < linesCount; i++) {
            for(int j = 0; j < linesCount; j++) {
                int index;
                if(j!=linesCount-1) {
                    index = this.stickStore.addStick(cellWidth * j, i * cellHeight + margin, cellHeight, stickWidth);
                    if (i == 0) {
                        this.stickStore.getSticks().get(index).borderU = true;
                    }
                    if (i == linesCount - 1) {
                        this.stickStore.getSticks().get(index).borderD = true;
                    }
                    this.stickStore.getSticks().get(index).horizontal = true;
                }
            }
            for(int j = 0; j < linesCount; j++) {
                int index;
                if(i!=linesCount-1) {
                    index = this.stickStore.addStick(cellWidth * j, i * cellHeight + margin, stickWidth, cellHeight);
                    if (j == 0) {
                        this.stickStore.getSticks().get(index).borderL = true;
                    }
                    if (j == linesCount - 1) {
                        this.stickStore.getSticks().get(index).borderR = true;
                    }
                    if (index > -1) this.stickStore.getSticks().get(index).vertical = true;
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        setBackground(Color.white);
        g.setColor(Color.BLACK);
        this.stickStore.draw(g);
    }

    public int calculateResult(Stick startStick, Color pColor) {
        this.sticks = this.stickStore.getSticks();
        this.startStick = startStick;
        boolean r = this.rightSearch(startStick.getId(),pColor);
        boolean l = this.leftSearch(startStick.getId(), pColor);
        boolean u = this.upSearch(startStick.getId(), pColor);
        boolean d = this.downSearch(startStick.getId(), pColor);
        System.out.println("this.right " + r);
        System.out.println("this.left " + l);
        System.out.println("this.up " + u);
        System.out.println("this.down " + d);
        if(r || l || u || d ) {
            return 1;
        } else
        {
            return 0;
        }
    }

    public boolean rightSearch(int id, Color pColor) {
        if(sticks.get(id).borderR || sticks.get(id).horizontal) {
            System.out.println("RRR Border!");
            return false;
        }
        if(
                        (this.sticks.get(id+1).color == opponentColor || this.sticks.get(id+1).color == playerColor) &&
                        (this.sticks.get(id+linesCount).color == opponentColor || this.sticks.get(id+linesCount).color == playerColor) &&
                        (this.sticks.get(id-linesCount+1).color == opponentColor || this.sticks.get(id-linesCount+1).color == playerColor)
        ) {

            int index = this.stickStore.addStick(this.sticks.get(id).x, this.sticks.get(id).y, this.width / linesCount, this.width / linesCount);
            this.stickStore.getSticks().get(index).color = pColor;
            this.stickStore.getSticks().get(index).cell = true;
            System.out.println("RETURN TRUE!");

            return true;
        }

        return false;
    }
    public boolean leftSearch(int id, Color pColor) {
        if(sticks.get(id).borderL || sticks.get(id).horizontal) {
            System.out.println("Border LLL!");
            return false;
        }
        if(
                (this.sticks.get(id-1).color == opponentColor || this.sticks.get(id-1).color == playerColor) &&
                        (this.sticks.get(id-linesCount).color == opponentColor || this.sticks.get(id-linesCount).color == playerColor) &&
                        (this.sticks.get(id+linesCount-1).color == opponentColor || this.sticks.get(id+linesCount-1).color == playerColor)
        ) {
            int index = this.stickStore.addStick(this.sticks.get(id-1).x, this.sticks.get(id-1).y, this.width / linesCount, this.width / linesCount);
            this.stickStore.getSticks().get(index).color = pColor;
            this.stickStore.getSticks().get(index).cell = true;

            System.out.println("RETURN TRUE!");
            return true;
        }

        return false;
    }
    public boolean upSearch(int id, Color pColor) {
        if(sticks.get(id).borderU || sticks.get(id).vertical ) {
            System.out.println("UUUBorder!");
            return false;
        }
        if(
                (this.sticks.get(id-linesCount*2+1).color == opponentColor || this.sticks.get(id-linesCount*2+1).color == playerColor) &&
                        (this.sticks.get(id-linesCount).color == opponentColor || this.sticks.get(id-linesCount).color == playerColor) &&
                        (this.sticks.get(id-linesCount+1).color == opponentColor || this.sticks.get(id-linesCount+1).color == playerColor)
        ) {
            int index = this.stickStore.addStick(this.sticks.get(id-linesCount).x, this.sticks.get(id-linesCount).y, this.width / linesCount, this.width / linesCount);
            this.stickStore.getSticks().get(index).color = pColor;
            this.stickStore.getSticks().get(index).cell = true;

            System.out.println("RETURN TRUE!");
            return true;
        }
        return false;
    }
    public boolean downSearch(int id, Color pColor) {
        if(sticks.get(id).borderD || sticks.get(id).vertical) {
            System.out.println("DDDD Border! "+sticks.get(id).borderD );
            System.out.println("DDDD Border! "+ sticks.get(id).vertical);
            return false;
        }
        if(
                (this.sticks.get(id+linesCount*2-1).color == opponentColor || this.sticks.get(id+linesCount*2-1).color == playerColor) &&
                        (this.sticks.get(id+linesCount).color == opponentColor || this.sticks.get(id+linesCount).color == playerColor) &&
                        (this.sticks.get(id+linesCount-1).color == opponentColor || this.sticks.get(id+linesCount-1).color == playerColor)
        ) {
            int index = this.stickStore.addStick(this.sticks.get(id+linesCount-1).x, this.sticks.get(id+linesCount-1).y, this.width / linesCount, this.width / linesCount);
            this.stickStore.getSticks().get(index).color = pColor;
            this.stickStore.getSticks().get(index).cell = true;

            System.out.println("RETURN TRUE!");
            return true;
        }
        return false;
    }

//    public boolean right(int id) {
//        if((id+1) % linesCount == 0) {
//            return false;
//        }
//        if(this.sticks.get(id + 1).color != this.playerColor){
//            return false;
//        };
//        if(this.sticks.get(id + 1).getId() == this.startStick.getId()){
//            return true;
//        };
//        id++;
//        boolean r = this.right(id);
//        boolean u = this.up(id);
//        boolean d = this.down(id);
//        return (r || u || d );
//
//    }
//
//    public boolean left(int id) {
//        if((id-1) % linesCount == 0) {
//            return false;
//        }
//        if(this.sticks.get(id - 1).color != this.playerColor){
//            return false;
//        };
//        if(this.sticks.get(id - 1).getId() == this.startStick.getId()){
//            return true;
//        };
//        id--;
//        boolean l = this.left(id);
//        boolean u = this.up(id);
//        boolean d = this.down(id);
//        return (l || u || d );
//    }
//
//    public boolean up(int id) {
//        if(id < linesCount ) {
//            return false;
//        }
//        if(this.sticks.get(id - linesCount).color != this.playerColor){
//            return false;
//        };
//        if(this.sticks.get(id - linesCount).getId() == this.startStick.getId()){
//            return true;
//        };
//        id = id - linesCount;
//        boolean r = this.right(id);
//        boolean l = this.left(id);
//        boolean u = this.up(id);
//        return (l || r || u );
//    }
//
//    public boolean down (int id) {
//        if(id >= linesCount*(linesCount-1) ) {
//            return false;
//        }
//        if(this.sticks.get(id + linesCount).color != this.playerColor){
//            return false;
//        };
//        if(this.sticks.get(id + linesCount).getId() == this.startStick.getId()){
//            return true;
//        };
//        id = id - linesCount;
//        boolean r = this.right(id);
//        boolean l = this.left(id);
//        boolean d = this.down(id);
//        return (l || r || d );
//    }



}
