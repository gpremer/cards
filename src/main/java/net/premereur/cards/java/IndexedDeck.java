package net.premereur.cards.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by gpremer on 11/28/15.
 */
public class IndexedDeck<Card> implements Deck<Card> {
    private ArrayList<Card> cards;

    public IndexedDeck() {
        this.cards = new ArrayList<>();
    }

    public IndexedDeck(final Collection<Card> cards) {
        this.cards = new ArrayList<>(cards);
    }

    @Override
    public int size() {
        return cards.size();
    }

    @Override
    public RemoveResult<Card, Card> removeNth(final int n) {
        if (n < 0 || n >= size()) {// Let's not use exceptions
            return new RemoveResult<>(Optional.empty(), this);
        } else {
            final ArrayList<Card> nextCards = new ArrayList<>(cards);
            final Card removed = nextCards.remove(n);
            return new RemoveResult<>(Optional.of(removed), new IndexedDeck<>(nextCards));
        }
    }

    @Override
    public Deck<Card> insertNth(final int n, final Card card) {
        final ArrayList<Card> nextCards = new ArrayList<>(this.cards);
        if (n < 0 || n > size()) {
            nextCards.add(card);
        } else {
            nextCards.add(n, card);
        }
        return new IndexedDeck<>(nextCards);
    }

    @Override
    public Optional<Card> peek(final int n) {
        if (n < 0 || n >= size()) {
            return Optional.empty();
        } else {
            return Optional.of(cards.get(n));
        }
    }
}
