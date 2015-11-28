package net.premereur.cards

import net.premereur.cards.Cards.{Dealing, Deck, IndexedDeck}
import net.premereur.cards.Demo.TopDealing
import org.scalacheck.{Arbitrary, Gen}

/**
  * Created by gpremer on 11/27/15.
  */
class TopDealingSpec extends BaseCardSpec with DealerBehaviours {
  describe("A TopDealing strategy") {
    val dealing = new TopDealing[Int] {}.dealer
    it("should deal the last card") {
      forAll("deck") { deck: Deck[Int] =>
        whenever(deck.isNotEmpty) {
          dealing.deal(deck)._1 shouldBe Some(deck.size - 1)
        }
      }
    }
    it should behave like anyDealer(dealing)
  }
}
