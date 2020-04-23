package services;

import io.reactivex.subjects.PublishSubject;
import models.TurnMessage;

public class ActionHandler {
    private static ActionHandler instance;

    public static ActionHandler getInstance() {
        if(instance == null) {
            instance = new ActionHandler();
            return instance;
        }
        return instance;
    }
    public PublishSubject<TurnMessage> actionPlayer = PublishSubject.create();
    public PublishSubject<TurnMessage> actionOpponent = PublishSubject.create();
}
