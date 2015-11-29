package net.premereur.cards.java;

/**
 * A typical Java implementation of a Deck of cards. Let's go for a traditional mutable interface. Also, unlike in a
 * functional version, any out-of-bounds access will trigger a RuntimeException. To keep things efficient, peek and swap
 * have been added to the interface.
 *
 * @param <Card> The type of cards in the deck
 */
interface Deck<Card> {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean isNotEmpty() {
        return size() != 0;
    }

    Card removeNth(int n);

    default Card removeFirst() {
        return removeNth(0);
    }

    default Card removeLast() {
        return removeNth(size() - 1);
    }

    void insertNth(int n, Card card);

    default void insertFirst(Card card) {
        insertNth(0, card);
    }

    default void insertLast(Card card) {
        insertNth(0, card);
    }

    Card peek(int n);

    void swap(int i, int j);
}
