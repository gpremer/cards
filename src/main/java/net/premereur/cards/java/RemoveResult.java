package net.premereur.cards.java;

import java.util.Optional;

/**
 * Created by gpremer on 11/28/15.
 */
public class RemoveResult<T, Card> {
    private final Optional<T> card;
    private final Deck<Card> deck;

    public RemoveResult(final Optional<T> card, final Deck<Card> deck) {
        this.card = card;
        this.deck = deck;
    }

    public Optional<T> getCard() {
        return card;
    }

    public Deck<Card> getDeck() {
        return deck;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final RemoveResult<?, ?> that = (RemoveResult<?, ?>) o;

        return card.equals(that.card) && deck.equals(that.deck);

    }

    @Override
    public int hashCode() {
        int result = card.hashCode();
        result = 31 * result + deck.hashCode();
        return result;
    }
}
