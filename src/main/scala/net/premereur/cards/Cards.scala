package net.premereur.cards

import net.premereur.cards.Cards._

import scala.util.Random

/**
  * Created by gpremer on 11/26/15.
  */
object Cards {

  /**
    * The Deck trait describes the minimal operations (insert, remove and size check) on a deck. How these are used to
    * deal and shuffle cards is described in other traits.
    * The date structure is purely functional: there is no mutable state. This means that "modifying" operations return
    * a new instance. The implementation is of course expected make sure that those operations avoid making copies of
    * the data as much as possible.
    *
    * @tparam Card The underlying type of cards in the deck. Has almost no requirements except that there have to be
    *              instances of the type.
    */
  trait Deck[Card] {
    def size: Int

    def isEmpty = size == 0

    def isNotEmpty = size != 0

    def removeNth(n: Int): (Option[Card], Deck[Card])

    def removeFirst: (Option[Card], Deck[Card]) = removeNth(0)

    def removeLast: (Option[Card], Deck[Card]) = removeNth(size - 1)

    def insertNth(n: Int, card: Card): Deck[Card]

    def insertFirst(card: Card): Deck[Card] = insertNth(0, card)

    def insertLast(card: Card): Deck[Card] = insertNth(size, card)
  }

  /**
    * This is the default implementation of a Deck. It uses an Indexed as a backing store so that operations have
    * either O(1) or O(n) time complexity. It is possible to override the default implementations of the *First and
    * *Last methods for a bit more efficiency, but that is left as an exercise for the reader.
    *
    * @param cards the cards that are the backing store of the deck.
    * @tparam Card The underlying type of cards in the deck. Has almost no requirements except that there have to be
    *              instances of the type.
    */
  final case class IndexedDeck[Card](cards: IndexedSeq[Card]) extends Deck[Card] {
    override def size: Int = cards.size

    override def removeNth(n: Int): (Option[Card], Deck[Card]) =
      if (cards.isDefinedAt(n))
        (Some(cards(n)), new IndexedDeck(cards.patch(n, IndexedSeq[Card](), 1)))
      else
        (None, this) // unfortunately, patch rebases the start index to zero if it's negative

    override def insertNth(n: Int, card: Card): Deck[Card] =
      new IndexedDeck((cards.take(n) :+ card) ++ cards.takeRight(size - n))
  }

  /**
    * Contains some handy constructors for our default Deck implementation.
    */
  object IndexedDeck {
    def apply[Card](): Deck[Card] = IndexedDeck(IndexedSeq[Card]())

    def apply[Card](cards: Iterable[Card]): Deck[Card] = IndexedDeck(cards.toIndexedSeq)
  }

  // Now we define two strategies for operating on Decks. We use the Cake pattern, this related to dependency
  // injection and the strategy pattern. However, it does not use constructor injection as in dependency injection (or
  // that's at least how I prefer to use dependency injection) it also focuses on compile time construction unlike the
  // strategy pattern (but given that def's are used, it can still be done).

  /**
    * The contract for strategies dealing a single card.
    *
    * @tparam Card the type of cards in the Deck
    */
  trait Dealing[Card] {
    def dealer: Dealer

    trait Dealer {
      def deal(deck: Deck[Card]): (Option[Card], Deck[Card])
    }

  }

  /**
    * The contract for strategies shuffling a complete deck. Since shuffling often entails a measure of randomness, a
    * random generator is required. The random generator is made implicit to lessen the syntactic overhead when using a
    * dealing strategy.
    *
    * @tparam Card the type of cards in the type
    */
  trait Shuffling[Card] {
    def shuffler: Shuffler

    trait Shuffler {
      def shuffle(deck: Deck[Card])(implicit random: Random): Deck[Card]
    }

  }

  /**
    * A default random generator for those that do not want to define their own.
    */
  implicit val random = new Random()
}

/**
  * A demonstration of how the traits defined above can be used and combined to build more complex behaviour.
  */
object Demo extends App {

  /**
    * First we define a Card type to work with. Almost anything can be a Card, but here we're using the classical French
    * cards.
    */
  object FrenchCards {

    sealed trait Suit

    case object Harts extends Suit

    case object Diamonds extends Suit

    case object Clubs extends Suit

    case object Spades extends Suit

    sealed trait Value

    case object Ace extends Value

    case object Two extends Value

    case object Three extends Value

    case object Four extends Value

    case object Five extends Value

    case object Six extends Value

    case object Seven extends Value

    case object Eight extends Value

    case object Nine extends Value

    case object Ten extends Value

    case object Jack extends Value

    case object Queen extends Value

    case object King extends Value

    case class Card(suit: Suit, value: Value)

    val suits = List(Harts, Diamonds, Spades, Clubs)

    val values = List(Ace, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten, Jack, Queen, King)

    val allCards = for {
      suit <- suits
      value <- values
    } yield Card(suit, value)
  }

  type FrenchCard = FrenchCards.Card

  /**
    * Simple dealing strategy that always takes the bottom card in the deck.
    *
    * @tparam Card the type of cards in the Deck
    */
  trait BottomDealing[Card] extends Dealing[Card] {
    def dealer = new BottomDealer

    class BottomDealer extends Dealer {
      def deal(deck: Deck[Card]): (Option[Card], Deck[Card]) = deck.removeFirst
    }

  }

  /**
    * Simple strategy that always takes the top card in the deck.
    *
    * @tparam Card the type of cards in the Deck
    */
  trait TopDealing[Card] extends Dealing[Card] {
    def dealer = new TopDealer

    class TopDealer extends Dealer {
      def deal(deck: Deck[Card]): (Option[Card], Deck[Card]) = deck.removeLast
    }

  }

  /**
    * A trivial shuffling strategy that performs no shuffling at all. Useful for visually verifying that the
    * implementations of other methods behave as they should.
    *
    * Implementation note: I have foregone the creation of a named class and directly implemented the required interface
    *
    * @tparam Card the type of cards in the Deck
    */
  trait NoShuffling[Card] extends Shuffling[Card] {
    def shuffler = new Shuffler {
      def shuffle(deck: Deck[Card])(implicit random: Random): Deck[Card] = deck
    }
  }

  /**
    * A shuffling strategy that thoroughly shuffles the cards.
    *
    * @tparam Card the type of cards in the Deck
    */
  trait DeepShuffling[Card] extends Shuffling[Card] {
    def shuffler = new Shuffler {
      def shuffle(deck: Deck[Card])(implicit random: Random): Deck[Card] =
        (0 until deck.size).foldLeft(IndexedDeck[Card]()) { case (newDeck, n) =>
          val position = random.nextInt(1 + n)
          newDeck.insertNth(position, deck.removeNth(n)._1.get)
        }
    }
  }

  /**
    * A shuffling strategy that mimics how a typical human card player shuffles a deck. I.e. repeatedly take a number
    * of cards from the bottom or the middle of the deck and put them on top.
    *
    * @tparam Card the type of cards in the type
    */
  trait HumanLikeShuffling[Card] extends Shuffling[Card] {
    // the number of times to take cards from the deck. Other parameters can be extracted too, but I have left them in
    // as hard-coded values. The configurability is demonstrated by this one parameter.
    def maxShuffles: Int

    def shuffler = new HumanLikeShuffler(maxShuffles)

    class HumanLikeShuffler(maxShuffles: Int) extends Shuffler {
      def shuffle(deck: Deck[Card])(implicit random: Random): Deck[Card] = {
        def moveFromTopToBottom(deck: Deck[Card]) = {
          val height = deck.size
          if (height >= 2) {
            val first = Math.min(height - 2, (height / 3 + height / 3 * random.nextGaussian()).toInt)
            val number = random.nextInt(height - first - 1)
            (0 until number).foldLeft(deck) { (newDeck, _) =>
              val (card, newestDeck) = newDeck.removeNth(first)
              card.map(newestDeck.insertLast).getOrElse(newDeck)
            }
          } else {
            deck // cannot shuffle if there are not at least two cards
          }
        }
        (0 until random.nextInt(maxShuffles)).foldLeft(deck) { (nextDeck, _) =>
          moveFromTopToBottom(nextDeck)
        }
      }
    }

  }


  // Finally we come to some higher-level abstractions that make use of the the strategies we have defined earlier.

  // Let's start of with some type aliases to simplify the function signatures
  type Hand[Card] = List[Card]
  type AllHands[Card] = List[Hand[Card]]

  /**
    * We define a specification for games that deal all cards in a deck and make hands out of them. By construction,
    * the remaining deck is empty, so does not need to be returned.
    *
    * @tparam Card the type of cards in the Deck
    */
  trait CompleteDealGame[Card] {
    self: Shuffling[Card] with Dealing[Card] =>

    def dealAll(deck: Deck[Card]): AllHands[Card]
  }

  /**
    * Reusable functionality for the common case where a game wants to deal a number of cards using the same base deal
    * strategy.
    *
    * @tparam Card the type of cards in the Deck
    */
  trait ConsecutiveDealing[Card] {
    self: Dealing[Card] =>

    def dealN(n: Int, deck: Deck[Card]): (List[Card], Deck[Card]) =
      (1 to n).foldLeft((List[Card](), deck)) { case ((hand, curDeck), _) =>
        val (card, nextDeck) = dealer.deal(curDeck)
        (card.map(_ :: hand).getOrElse(hand), nextDeck) // it could happen that there are not enough cards
      }
  }

  /**
    * A dealing strategy that is explicitly defined on French cards and deals all cards in the deck after they have been
    * shuffled.
    */
  trait CompleteConsecutiveSingleDealGame
    extends CompleteDealGame[FrenchCard] with ConsecutiveDealing[FrenchCard] {
    self: Shuffling[FrenchCard] with Dealing[FrenchCard] =>

    def dealAll(deck: Deck[FrenchCard]): AllHands[FrenchCard] =
      (1 to FrenchCards.suits.size).foldLeft((List[Hand[FrenchCard]](), shuffler.shuffle(deck))) {
        case ((hands, curDeck), _) =>
          val (hand, nextDeck) = dealN(FrenchCards.values.size, curDeck)
          (hand :: hands, nextDeck)
      }._1
  }

  /**
    * A game that captures the common pattern of dealing in predetermined batches of cards.
    *
    * @tparam Card the type of cards in the Deck
    */
  trait PieceWiseDealGame[Card] extends CompleteDealGame[Card] with ConsecutiveDealing[Card] {
    self: Shuffling[Card] with Dealing[Card] =>

    def numDeals: List[Int]

    def dealAll(deck: Deck[Card]): AllHands[Card] =
      numDeals.foldLeft((List[Hand[Card]](), deck)) { case ((game, curDeck), dealNum) =>
        val (hand, nextDeck) = dealN(dealNum, curDeck)
        (hand :: game, nextDeck)
      }._1
  }

  /**
    * A recipe for handing out cards for the Belgian game of Wiezen (on extended version of Whist).
    * We leave the choice of shuffling strategy open.
    */
  trait Wiezen extends PieceWiseDealGame[FrenchCard] with TopDealing[FrenchCard] {
    self: Shuffling[FrenchCard] =>
    val numDeals = List(4, 4, 4, 4, 5, 5, 5, 5, 4, 4, 4, 4)
    val maxShuffles = 40

    override def dealAll(deck: Deck[FrenchCard]): AllHands[FrenchCard] =
      super.dealAll(shuffler.shuffle(deck)) // We now have 12 list of cards: one for every deal group
        .grouped(4) // combine per deal round: We now have 3 lists that contains lists of cards for all players
        .toList // grouped returns an iterator, but transpose needs a list
        .transpose // we need lists per player not per round. Transpose fixes this
        .map(_.flatten) // we need to combine the cards in the 3 rounds to one hand
  }

  /**
    * A recipe for a trick game. It is kind of pointless but it does serve to illustrate how a dealer and shuffler can
    * collude (this was one of the questions). The trick ;-) is that both interfaces are implemented by one class. An
    * alternative is using distinct classes and passing a reference to a shared resource during construction.
    */
  trait TrickGame[Card] extends CompleteDealGame[Card] with Dealing[Card] with Shuffling[Card] {
    def dealer = trickster

    def shuffler = trickster

    def specialCard: Card // A card that is considered special

    private lazy val trickster = new Dealer with Shuffler {
      // We have to use a var. This goes to prove that functional programming is not for tricksters!
      var specialCardPosition: Int = 0

      // This will look like random dealing but will make sure that the special card is drawn first
      override def deal(deck: Deck[Card]): (Option[Card], Deck[Card]) = ???

      // This will shuffle randomly with a deck that contains the special card in an arbitrary position, but will
      // remember the final position of the special card
      override def shuffle(deck: Deck[Card])(implicit random: Random): Deck[Card] = ???
    }
  }

  implicit val random = new Random(1) // override the default version in the Deck library

  // A few sample games that show how to compose strategies
  val noShuffleFullDealGame = new CompleteConsecutiveSingleDealGame()
    with NoShuffling[FrenchCard] with BottomDealing[FrenchCard]
  val deepShuffleFullDealGame = new CompleteConsecutiveSingleDealGame()
    with DeepShuffling[FrenchCard] with TopDealing[FrenchCard]
  val humanWiesGame = new Wiezen with HumanLikeShuffling[FrenchCard]

  private val sortedFullFrenchDeck = IndexedDeck(FrenchCards.allCards)

  // A nicer way of showing the dealt cards
  private def mkString[Card](hands: AllHands[Card]) = hands.map(_.mkString("|")).mkString("\n")

  // Here we make use of the fact that all games implement the CompleteDealGame: we call the dealAll method
  List(noShuffleFullDealGame, deepShuffleFullDealGame, humanWiesGame).foreach { game =>
    println(mkString(game.dealAll(sortedFullFrenchDeck)))
    println
  }
}
