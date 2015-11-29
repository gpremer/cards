package net.premereur.cards.java;

/**
 * A dealing strategy. Modifies the deck in-place.
 *
 * @param <Card> The type of cards in the deck
 */
public interface Dealer<Card> {
    Card deal(Deck<Card> deck);
}
