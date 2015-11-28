package net.premereur.cards

import net.premereur.cards.Cards.{IndexedDeck, Deck, Dealing}

/**
  * Created by gpremer on 11/28/15.
  */
trait DealerBehaviours {
  self: BaseCardSpec =>

  def anyDealer(dealer: Dealing[Int]#Dealer) = {
    it("should deal all cards") {
      forAll("deck") { startDeck: Deck[Int] =>
        val (dealtCards, _) = (0 until startDeck.size).foldLeft((Set[Int](), startDeck)) { case ((hand, deck), _) =>
          val (card, remaining) = dealer.deal(deck)
          (hand + card.get, remaining)
        }
        dealtCards should have size startDeck.size
      }
    }
    it("should yield a deck with one fewer card") {
      forAll("deck") { startDeck: Deck[Int] =>
        val (_, deck) = dealer.deal(startDeck)
        deck should have size startDeck.size - 1
      }
    }
    it("should draw no card from an empty deck") {
      dealer.deal(IndexedDeck())._1 shouldBe None
    }
  }
}
