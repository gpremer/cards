package net.premereur.cards.java;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Some sample applications of the strategies set up previously.
 */
public class GameDemo {
    /**
     * A kind of Game that deals all available cards to a number of players.
     *
     * @param <Card> The type of cards to play with
     */
    interface CompleteDealGame<Card> {
        List<List<Card>> dealAll(Deck<Card> deck);
    }

    /**
     * The classical French cards.
     */
    static class FrenchCard {
        @SuppressWarnings("unused")
        enum Suit {
            Harts, Diamonds, Spades, Clubs
        }

        @SuppressWarnings("unused")
        enum Value {
            Ace, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King
        }

        private Suit suit;
        private Value value;

        private FrenchCard(final Suit suit, final Value value) {
            this.suit = suit;
            this.value = value;
        }

        // No need for equals and hashcode as we construct only one instance of Card (and block the constructor).

        @Override
        public String toString() {
            return "[" + suit + ", " + value + ']';
        }
    }

    /**
     * All the cards as a convenient list.
     */
    public static final List<FrenchCard> allCards =
            stream(FrenchCard.Suit.values()).flatMap(suit -> stream(FrenchCard.Value.values()).map(value -> new FrenchCard(suit, value)))
                    .collect(Collectors.toList());

    /**
     * A dealer strategy implementation that always deals the card on the top of the deck.
     *
     * @param <Card> The type of card in the deck
     */
    static class TopDealer<Card> implements Dealer<Card> {
        @Override
        public Card deal(final Deck<Card> deck) {
            return deck.removeLast();
        }
    }

    /**
     * A shuffler strategy that shuffles the deck so that any card is equally likely to end up at a given place.
     *
     * @param <Card> The type of card in the deck
     */
    static class DeepShuffler<Card> implements Shuffler<Card> {
        @Override
        public void shuffle(final Deck<Card> deck) {
            for (int i = 0; i < deck.size() - 1; ++i) {
                deck.swap(i, random(i, deck.size()));
            }

        }

        private int random(final int lower, final int limit) {
            return (int) (lower + Math.random() * (limit - lower));
        }
    }

    /**
     * A helper function that deals numDeals times and collects the result in a list.
     */
    static <Card> List<Card> dealN(Dealer<Card> dealer, Deck<Card> deck, int numDeals) {
        final List<Card> nextCards = new ArrayList<>();
        for (int i = 0; i < numDeals; ++i) {
            nextCards.add(dealer.deal(deck));
        }
        return nextCards;
    }

    /**
     * An implementation of dealing in Wiezen.
     */
    static class Wiezen implements CompleteDealGame<FrenchCard> {
        private final Dealer<FrenchCard> dealer = new TopDealer<>();
        private final Shuffler<FrenchCard> shuffler;

        public Wiezen(final Shuffler<FrenchCard> shuffler) {
            this.shuffler = shuffler;
        }

        @Override
        public List<List<FrenchCard>> dealAll(final Deck<FrenchCard> deck) {
            final int numCardsPerRound[] = {4, 5, 4};
            final List<List<FrenchCard>> hands = new ArrayList<>();
            for (int hand = 0; hand < 4; ++hand) {
                hands.add(new ArrayList<>());
            }
            shuffler.shuffle(deck);
            for (final int numCards : numCardsPerRound) {
                for (int handNum = 0; handNum < 4; ++handNum) {
                    List<FrenchCard> hand = dealN(dealer, deck, numCards);
                    hands.get(handNum).addAll(hand);
                }
            }
            return hands;
        }
    }

    /**
     * A demonstration of how one class can implement multiple strategy interfaces to do devious things. In this case
     * the deck is thoroughly shuffled and cards are picked at random, but yet in some way, the ace of harts is always
     * within the first four cards dealt (with an equal likelihood for all 4 first deals).
     */
    static class Trickster implements Dealer<FrenchCard>, Shuffler<FrenchCard> {
        private final FrenchCard specialCard = allCards.get(0); // the easiest was to pick a card
        private final int limit = 4;
        private int numDeals = 0;
        private int specialPosition = 0;
        private boolean specialWasDealt = false;

        @Override
        public FrenchCard deal(final Deck<FrenchCard> deck) {
            final int dealPosition;
            numDeals += 1;
            // give it an (almost) equal chance that the special card is dealt in every turn until the limit
            if (!specialWasDealt && Math.random() < 1. * numDeals / limit) {
                dealPosition = specialPosition;
                specialWasDealt = true;
            } else {
                dealPosition = (int) (Math.random() * deck.size());
                if (dealPosition < specialPosition) {
                    specialPosition -= 1;
                } else if (dealPosition == specialPosition) {
                    specialWasDealt = true;
                }
            }
            return deck.removeNth(dealPosition);
        }

        @Override
        public void shuffle(final Deck<FrenchCard> deck) {
            // Set up thr trickery
            numDeals = 0;
            specialWasDealt = false;
            // There is a 1 in (52*52) chance that the last card is the special one and that it remains there.
            specialPosition = 51;
            for (int i = 0; i < deck.size() - 1; ++i) {
                deck.swap(i, random(i, deck.size()));
                if (deck.peek(i) == specialCard) {
                    specialPosition = i;
                }
            }
        }

        private int random(final int lower, final int limit) {
            return (int) (lower + Math.random() * (limit - lower));
        }
    }

    /**
     * A simple game that simply deals all cards in one hands. One would expect the result to be random...
     */
    static class PickGame implements CompleteDealGame<FrenchCard> {
        final Dealer<FrenchCard> dealer;
        final Shuffler<FrenchCard> shuffler;

        PickGame(final Dealer<FrenchCard> dealer, final Shuffler<FrenchCard> shuffler) {
            this.dealer = dealer;
            this.shuffler = shuffler;
        }

        @Override
        public List<List<FrenchCard>> dealAll(final Deck<FrenchCard> deck) {
            shuffler.shuffle(deck);
            final ArrayList<List<FrenchCard>> hands = new ArrayList<>();
            hands.add(dealN(dealer, deck, allCards.size()));
            return hands;
        }
    }

    /**
     * Helper to show the hands in a CompleteDealGame. (Guava would have been helpful).
     */
    private static void showHands(final CompleteDealGame<FrenchCard> game) {
        final List<List<FrenchCard>> hands = game.dealAll(new IndexedDeck<>(allCards));
        for (final List<FrenchCard> hand : hands) {
            for (final FrenchCard card : hand) {
                System.out.print(card.toString() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        final Shuffler<FrenchCard> deepShuffler = new DeepShuffler<>();
        final CompleteDealGame<FrenchCard> wiezen = new Wiezen(deepShuffler);

        final CompleteDealGame<FrenchCard> bonaFide = new PickGame(new TopDealer<>(), deepShuffler);
        final Trickster trickster = new Trickster();
        final CompleteDealGame<FrenchCard> tricky = new PickGame(trickster, trickster);

        System.out.println("====== wiezen ======");
        for (int i = 0; i < 3; ++i) {
            showHands(wiezen);
        }
        System.out.println("====== bona fide ======");
        for (int i = 0; i < 3; ++i) {
            showHands(bonaFide);
        }
        System.out.println("====== tricky ======");
        for (int i = 0; i < 5; ++i) {
            showHands(tricky);
        }
    }
}

