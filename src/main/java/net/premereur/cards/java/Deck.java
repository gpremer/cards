package net.premereur.cards.java;

import java.util.Optional;

/**
 * Created by gpremer on 11/28/15.
 */
interface Deck<Card> {

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean isNotEmpty() {
        return size() != 0;
    }

    RemoveResult<Card, Card> removeNth(int n);

    default RemoveResult<Card, Card> removeFirst() {
        return removeNth(0);
    }

    default RemoveResult<Card, Card> removeLast() {
        return removeNth(size() - 1);
    }

    Deck<Card> insertNth(int n, Card card);

    default Deck<Card> insertFirst(Card card) {
        return insertNth(0, card);
    }

    default Deck<Card> insertLast(Card card) {
        return insertNth(0, card);
    }

    Optional<Card> peek(int n);
}
