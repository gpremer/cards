package net.premereur.cards.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Created by gpremer on 11/28/15.
 */
public class GameDemo {
    interface CompleteDealGame<Card> {
        List<List<Card>> dealAll(Deck<Card> deck);
    }

    enum Suit {
        Harts, Diamonds, Spades, Clubs
    }

    enum Value {
        Ace, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King
    }

    static class FrenchCard {
        private Suit suit;
        private Value value;

        private FrenchCard(final Suit suit, final Value value) {
            this.suit = suit;
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final FrenchCard that = (FrenchCard) o;

            return suit == that.suit && value == that.value;

        }

        @Override
        public int hashCode() {
            int result = suit.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "[" + suit + ", " + value + ']';
        }
    }

    public static final List<FrenchCard> allCards =
            stream(Suit.values()).flatMap(suit -> stream(Value.values()).map(value -> new FrenchCard(suit, value)))
                    .collect(Collectors.toList());

    static class TopDealer<Card> implements Dealer<Card> {
        @Override
        public RemoveResult<Card, Card> deal(final Deck<Card> deck) {
            return deck.removeLast();
        }
    }


    static class DeepShuffler<Card> implements Shuffler<Card> {
        @Override
        public Deck<Card> shuffle(final Deck<Card> deck) {
            final ArrayList<Card> nextCards = new ArrayList<>();
            Deck<Card> nextDeck = deck;
            while (nextDeck.isNotEmpty()) {
                final RemoveResult<Card, Card> removeResult = nextDeck.removeNth(random(nextDeck.size()));
                nextCards.add(removeResult.getCard().get()); // OK by construction
                nextDeck = removeResult.getDeck();
            }
            return new IndexedDeck<>(nextCards);
        }

        private int random(int limit) {
            return (int) (Math.random() * limit);
        }
    }


    static <Card> RemoveResult<List<Card>, Card> dealN(Dealer<Card> dealer, Deck<Card> deck, int numCards) {
        final List<Card> nextCards = new ArrayList<>();
        Deck<Card> sourceDeck = deck;
        while (sourceDeck.isNotEmpty() && nextCards.size() < numCards) {
            final RemoveResult<Card, Card> deal = dealer.deal(sourceDeck);
            nextCards.add(deal.getCard().get()); // OK by construction
            sourceDeck = deal.getDeck();
        }
        return new RemoveResult<>(Optional.of(nextCards), sourceDeck);
    }

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
            Deck<FrenchCard> sourceDeck = shuffler.shuffle(deck);
            for (final int numCards : numCardsPerRound) {
                for (int hand = 0; hand < 4; ++hand) {
                    final RemoveResult<List<FrenchCard>, FrenchCard> result = dealN(dealer, sourceDeck, numCards);
                    sourceDeck = result.getDeck();
                    hands.get(hand).addAll(result.getCard().get()); // OK by construction
                }
            }
            return hands;
        }
    }

    static class Trickster implements Dealer<FrenchCard>, Shuffler<FrenchCard> {

        private final FrenchCard specialCard = allCards.get(0); // the easiest was to pick a card
        private final int limit = 4;
        private int deals = 0;
        private int specialPosition = 0;
        private boolean specialWasDealt = false;
        private boolean specialWasShuffeld = false;

        @Override
        public RemoveResult<FrenchCard, FrenchCard> deal(final Deck<FrenchCard> deck) {
            final int removePosition;
            deals += 1;
            // give it an (almost) equal chance that the special card is dealt in every turn until the limit
            if (!specialWasDealt && Math.random() < 1. * deals / limit) {
                removePosition = specialPosition;
                specialWasDealt = true;
            } else {
                removePosition = (int) (Math.random() * deck.size());
                if (removePosition < specialPosition) {
                    specialPosition -= 1;
                } else if (removePosition == specialPosition) {
                    specialWasDealt = true;
                }
            }
            if (deals == 52) {
                init();
            }
            return deck.removeNth(removePosition);
        }

        private void init() {
            deals = 0;
            specialPosition = 0;
            specialWasDealt = false;
            specialWasShuffeld = false;
        }

        @Override
        public Deck<FrenchCard> shuffle(final Deck<FrenchCard> deck) {
            final ArrayList<FrenchCard> nextCards = new ArrayList<>();
            Deck<FrenchCard> nextDeck = deck;
            while (nextDeck.isNotEmpty()) {
                final RemoveResult<FrenchCard, FrenchCard> removeResult = nextDeck.removeNth(random(nextDeck.size()));
                final FrenchCard card = removeResult.getCard().get();// OK by construction

                // Has to happen eventually unless the special card was not in the deck to begin with: the trickster tricked!
                if (card == specialCard) { // equivalence is OK because there is only one instance to begin with
                    specialWasShuffeld = true;
                } else {
                    if (!specialWasShuffeld) {
                        specialPosition += 1;
                    }
                }
                nextCards.add(card);
                nextDeck = removeResult.getDeck();
            }
            return new IndexedDeck<>(nextCards);
        }

        private int random(int limit) {
            return (int) (Math.random() * limit);
        }
    }

    static class PickGame implements CompleteDealGame<FrenchCard> {
        final Dealer<FrenchCard> dealer;
        final Shuffler<FrenchCard> shuffler;

        PickGame(final Dealer<FrenchCard> dealer, final Shuffler<FrenchCard> shuffler) {
            this.dealer = dealer;
            this.shuffler = shuffler;
        }

        @Override
        public List<List<FrenchCard>> dealAll(final Deck<FrenchCard> deck) {
            final ArrayList<List<FrenchCard>> hands = new ArrayList<>();
            final RemoveResult<List<FrenchCard>, FrenchCard> result = dealN(dealer, shuffler.shuffle(deck), allCards.size());
            hands.add(result.getCard().get());
            return hands;
        }
    }

    public static <Card> void showHands(List<List<Card>> hands) {
        for (final List<Card> hand : hands) {
            for (final Card card : hand) {
                System.out.print(card.toString() + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        final Shuffler<FrenchCard> deepShuffler = new DeepShuffler<>();
        final CompleteDealGame<FrenchCard> wiezen = new Wiezen(deepShuffler);
        final IndexedDeck<FrenchCard> initialDeck = new IndexedDeck<>(allCards);

        final CompleteDealGame<FrenchCard> bonafide = new PickGame(new TopDealer<>(), deepShuffler);
        final Trickster trickster = new Trickster();
        final CompleteDealGame<FrenchCard> tricky = new PickGame(trickster, trickster);


        System.out.println("wiezen");
        for (int i = 0; i < 3; ++i) {
            showHands(wiezen.dealAll(initialDeck));
        }
        System.out.println("bonafide");
        for (int i = 0; i < 3; ++i) {
            showHands(bonafide.dealAll(initialDeck));
        }
        System.out.println("tricky");
        for (int i = 0; i < 3; ++i) {
            showHands(tricky.dealAll(initialDeck));
        }
    }
}

