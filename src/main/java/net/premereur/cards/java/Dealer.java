package net.premereur.cards.java;

/**
 * Created by gpremer on 11/28/15.
 */
public interface Dealer<Card> {
    RemoveResult<Card, Card> deal(Deck<Card> deck);
}
