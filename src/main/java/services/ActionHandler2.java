package services;

import io.reactivex.subjects.PublishSubject;
import models.TurnMessage;

public class ActionHandler2 {
    private static ActionHandler2 instance;

    public static ActionHandler2 getInstance() {
        if(instance == null) {
            instance = new ActionHandler2();
            return instance;
        }
        return instance;
    }
    public PublishSubject<TurnMessage> actionPlayer = PublishSubject.create();
    public PublishSubject<TurnMessage> actionOpponent = PublishSubject.create();
}