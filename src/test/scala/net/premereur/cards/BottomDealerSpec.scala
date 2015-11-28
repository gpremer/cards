package net.premereur.cards

import net.premereur.cards.Cards.{Deck, IndexedDeck}
import net.premereur.cards.Demo.BottomDealing

/**
  * Created by gpremer on 11/26/15.
  */
class BottomDealerSpec extends BaseCardSpec with DealerBehaviours {

  describe("A BottomDealing strategy") {
    val dealer = new BottomDealing[Int] {}.dealer
    it("should deal the first card") {
      forAll("deck") { deck: Deck[Int] =>
        whenever(deck.isNotEmpty) {
          dealer.deal(deck)._1 shouldBe Some(0)
        }
      }
    }
    it should behave like anyDealer(dealer)
  }
}
