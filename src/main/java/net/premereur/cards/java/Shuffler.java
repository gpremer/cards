package net.premereur.cards.java;

/**
 * Created by gpremer on 11/28/15.
 */
public interface Shuffler<Card> {
    Deck<Card> shuffle(Deck<Card> deck);
}
