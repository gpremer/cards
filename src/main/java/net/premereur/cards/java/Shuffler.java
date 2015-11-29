package net.premereur.cards.java;

/**
 * A shuffling strategy. Modifies the deck in-place.
 *
 * @param <Card> The type of cards in the deck
 */
public interface Shuffler<Card> {
    void shuffle(Deck<Card> deck);
}
