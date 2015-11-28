package net.premereur.cards

import net.premereur.cards.Cards.{Deck, IndexedDeck}
import org.scalacheck.Gen

import scala.util.Random

/**
  * Created by gpremer on 11/26/15.
  */
class IndexedDeckSpec extends BaseCardSpec {

  describe("An IndexedDeck") {
    describe("when doing size checks") {
      it("should yield the number of cards inside") {
        forAll((Gen.chooseNum(0, 100), "size")) { size: Int =>
          val deck = IndexedDeck(0 until size)
          deck.size shouldBe size
        }
      }
      it("should find that a deck with at least one card is not empty") {
        forAll((Gen.chooseNum(1, 100), "size")) { size: Int =>
          val deck = IndexedDeck(0 until size)
          deck.isEmpty shouldBe false
        }
      }
      it("should find that a deck without cards is empty") {
        val deck = IndexedDeck()
        deck.isEmpty shouldBe true
      }
      it("should find that the 'empty' property is always the opposite of the 'nonEmpty property") {
        forAll((Gen.chooseNum(0, 100), "size")) { size: Int =>
          val deck = IndexedDeck(0 until size)
          deck.isEmpty should not be deck.isNotEmpty
        }
      }
    }
    describe("when removing the first card") {
      describe("when the deck is empty") {
        it("should return None") {
          IndexedDeck[Int]().removeFirst._1 shouldBe None
        }
      }
      describe("when removing within bounds") {
        it("should remove the known first card") {
          forAll("deck") { deck: Deck[Int] =>
            val (first, _) = deck.removeFirst
            first shouldBe Some(0)
          }
        }
        it("should yield a deck with one less card") {
          forAll("deck") { deck: Deck[Int] =>
            val (_, nextDeck) = deck.removeFirst
            nextDeck.size shouldBe deck.size - 1
          }
        }
        it("should yield a new deck with a first card that comes directly after the original first card") {
          forAll("deck") { deck: Deck[Int] =>
            whenever(deck.size >= 2) {
              val (first, deckMinOne) = deck.removeFirst
              val (second, _) = deckMinOne.removeFirst
              first.get + 1 shouldBe second.get
            }
          }
        }
      }
    }
    describe("when removing the last card") {
      describe("when the deck is empty") {
        it("should return None") {
          IndexedDeck[Int]().removeLast._1 shouldBe None
        }
      }
      describe("when removing within bounds") {
        it("should remove the known last card") {
          forAll("deck") { deck: Deck[Int] =>
            val (last, _) = deck.removeLast
            last shouldBe Some(deck.size - 1)
          }
        }
        it("should yield a deck with one less card") {
          forAll("deck") { deck: Deck[Int] =>
            val (_, nextDeck) = deck.removeLast
            nextDeck.size shouldBe deck.size - 1
          }
        }
        it("should yield a new deck with a last card that comes directly before the original last card") {
          forAll("deck") { deck: Deck[Int] =>
            whenever(deck.size >= 2) {
              val (last, deckMinOne) = deck.removeLast
              val (secondLast, _) = deckMinOne.removeLast
              last.get - 1 shouldBe secondLast.get
            }
          }
        }
      }
    }
    describe("when removing arbitrary cards") {
      describe("when removing within bounds") {
        it("should yield the known card at the position") {
          forAll((Gen.chooseNum(1, 100), "size")) { size: Int =>
            val deck = IndexedDeck(0 until size)
            val n = Random.nextInt(size)
            deck.removeNth(n)._1 shouldBe Some(n)
          }
        }
        it("should yield a deck with one less card") {
          forAll((Gen.chooseNum(1, 100), "size")) { size: Int =>
            val deck = IndexedDeck(0 until size)
            val n = Random.nextInt(size)
            val (_, nextDeck) = deck.removeNth(n)
            nextDeck.size shouldBe deck.size - 1
          }
        }
        it("should never remove the same card twice") {
          forAll((Gen.chooseNum(2, 10), "size")) { size: Int =>
            val deck = IndexedDeck(0 until size)
            val n1 = Random.nextInt(size)
            val (card1, nextDeck1) = deck.removeNth(n1)
            val n2 = Random.nextInt(size - 1)
            val (card2, _) = nextDeck1.removeNth(n2)
            card1 should not be card2
          }
        }
      }
      describe("when removing negatively out of bounds") {
        it("should keep the deck intact") {
          forAll("deck") { deck: Deck[Int] =>
            deck.removeNth(-1 - Random.nextInt(100))._2 shouldBe deck
          }
        }
        it("should yield no card") {
          forAll("deck") { deck: Deck[Int] =>
            deck.removeNth(-1 - Random.nextInt(100))._1 shouldBe None
          }
        }
      }
      describe("when removing positively out of bounds") {
        it("should keep the deck intact") {
          forAll("deck") { deck: Deck[Int] =>
            deck.removeNth(deck.size + Random.nextInt(100))._2 shouldBe deck
          }
        }
        it("should yield no card") {
          forAll("deck") { deck: Deck[Int] =>
            deck.removeNth(deck.size + Random.nextInt(100))._1 shouldBe None
          }
        }
      }
    }
    describe("when inserting cards in the beginning") {
      it("should create a deck that has as many extra cards as are inserted") {
        forAll("deck", "cards") { (deck: Deck[Int], cards: List[Int]) =>
          val nextDeck = cards.foldLeft(deck)((nextDeck, card) => nextDeck.insertFirst(card))
          nextDeck.size shouldBe deck.size + cards.size
        }
      }
      it("should have the new card at the front") {
        forAll("deck", "card") { (deck: Deck[Int], card: Int) =>
          deck.insertFirst(card).removeFirst._1 shouldBe Some(card)
        }
      }
    }
    describe("when inserting cards at the end") {
      it("should create a deck that has as many extra cards as are inserted") {
        forAll("deck", "cards") { (deck: Deck[Int], cards: List[Int]) =>
          val nextDeck = cards.foldLeft(deck)((nextDeck, card) => nextDeck.insertLast(card))
          nextDeck.size shouldBe deck.size + cards.size
        }
      }
      it("should have the new card at the back") {
        forAll("deck", "card") { (deck: Deck[Int], card: Int) =>
          deck.insertLast(card).removeLast._1 shouldBe Some(card)
        }
      }
    }
    describe("when inserting cards at arbitrary locations") {
      it("should create a deck that has as many extra cards as are inserted") {
        forAll("deck", "cards") { (deck: Deck[Int], cards: List[Int]) =>
          val nextDeck = cards.foldLeft(deck)((nextDeck, card) => nextDeck.insertNth(Random.nextInt(nextDeck.size), card))
          nextDeck.size shouldBe deck.size + cards.size
        }
      }
      it("should have the new card at the desired position") {
        forAll("deck", "card") { (deck: Deck[Int], card: Int) =>
          val position = Random.nextInt(deck.size)
          deck.insertNth(position, card).removeNth(position)._1 shouldBe Some(card)
        }
      }
    }
    describe("when inserting and then deleting a card at the same location") {
      it("should leave the deck unchanged") {
        forAll("deck", "card") { (deck: Deck[Int], card: Int) =>
          val position = Random.nextInt(deck.size)
          deck.insertNth(position, card).removeNth(position)._2 shouldBe deck
        }
      }
    }
  }
}
