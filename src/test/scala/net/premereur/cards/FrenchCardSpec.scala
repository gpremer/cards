package net.premereur.cards

import net.premereur.cards.Demo.FrenchCards

/**
  * Created by gpremer on 11/27/15.
  */
class FrenchCardSpec extends BaseCardSpec {
  describe ("French cards") {
    it("should have 4 distinct suits") {
      FrenchCards.suits.toSet.size shouldBe 4
    }
    it("should have 13 distinct values") {
      FrenchCards.values.toSet.size shouldBe 13
    }
    it("should have 52 different cards") {
      FrenchCards.allCards.toSet.size shouldBe 52
    }
  }
}
