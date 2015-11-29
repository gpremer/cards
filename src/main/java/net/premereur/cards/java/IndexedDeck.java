package net.premereur.cards.java;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The default implementation of Deck. It is backed by an ArrayList. I can think of more efficient implementations, but
 * barring any efficiency requirements, this will do.
 *
 * @param <Card>
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
    public Card removeNth(final int n) {
        return cards.remove(n); // Just throw if need be
    }

    @Override
    public void insertNth(final int n, final Card card) {
        cards.add(n, card);
    }

    @Override
    public Card peek(final int n) {
        return cards.get(n); // Throw if need be
    }

    @Override
    public void swap(final int i, final int j) {
        if (i != j) {
            final Card tmp = cards.get(i); // Throw ...
            cards.set(i, cards.get(j));
            cards.set(j, tmp);
        }
    }
}
