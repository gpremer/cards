package net.premereur.cards

import net.premereur.cards.Cards.{Deck, IndexedDeck}
import org.scalacheck.{Arbitrary, Gen}

/**
  * Created by gpremer on 11/26/15.
  */
trait Generators {

  def deckGen: Gen[Deck[Int]] = for {
    size <- Gen.chooseNum(1, 100)
  } yield IndexedDeck(0 until size)

  implicit val deckArb = Arbitrary(deckGen)
}

object Generators extends Generators