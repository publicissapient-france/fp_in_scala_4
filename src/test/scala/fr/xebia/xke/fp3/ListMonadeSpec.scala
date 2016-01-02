package fr.xebia.xke.fp3

import fr.xebia.xke._
import org.scalacheck.Gen
import org.scalacheck.Gen.oneOf
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSpec, Matchers, PropSpec}

class ListMonadeSpec extends FunSpec with Matchers {

  val _1_2_3_as_string = List("1", "2", "3")

  val _1_2_3_as_int = List(1, 2, 3)

  import Monade.listMonade._

  describe("A monad instance for list") {
    it("should apply a function over a list and concat results with flatMap", EXO_3_1) {
      val result_of_flatMap: List[Char] = flatMap(List("123", "456"))(str => List(str.toCharArray))

      result_of_flatMap shouldBe List('1', '2', '3', '4', '5', '6')
    }

    it("should not flatMap a function over a Nil", EXO_3_1) {
      val result_of_flatMap: List[Char] = flatMap(Nil)((str: String) => List(str.toCharArray))

      result_of_flatMap shouldBe Nil
    }

    it("should flatMap a function with is always empty over a list", EXO_3_1) {
      val result_of_flatMap: List[Char] = flatMap(List("123", "456"))(str => Nil)

      result_of_flatMap shouldBe Nil
    }

    it("should map a function over a list", EXO_3_2) {
      val result_of_map = map(_1_2_3_as_string)(Integer.parseInt)

      result_of_map shouldBe _1_2_3_as_int
    }

    it("should apply a list of functions on a list of elements", EXO_3_2) {
      val result_of_ap = ap(_1_2_3_as_int)(List(
        (i: Int) => i + 1,
        (i: Int) => i - 1
      ))

      result_of_ap shouldBe List(2, 0, 3, 1, 4, 2)
    }

    it("should flatten a list of list of int", EXO_3_3) {
      val result_of_ap = flatten(List(_1_2_3_as_int, _1_2_3_as_int))

      result_of_ap shouldBe List(1, 2, 3, 1, 2, 3)
    }

  }
}

object ListMonadeSpec {

  def listsOf[A](as: Gen[A]): Gen[List[A]] = oneOf(Gen.const(Nil), consOf(as))

  def consOf[A](as: Gen[A]): Gen[Cons[A]] =
    for {
      a <- as
      tail <- listsOf(as)
    } yield Cons(a, tail)
}


class ListMonadeLawSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {

  import fr.xebia.xke.fp3.ListMonadeLawSpec._
  import Monade.listMonade._

  property("right identity law of List Monad instance", EXO_3_4) {

    forAll(listsOf(Gen.identifier)) { fa =>

      fa shouldBe flatMap(fa)(a => point(a))
    }

  }

  property("left identity law of List Monad instance", EXO_3_4) {

    forAll(Gen.identifier) { a =>
      val f: (String) => List[Char] = (s: String) => List(s.toCharArray)

      f(a) shouldBe flatMap(point(a))(f)
    }

  }

  property("associative flatMap", EXO_3_4) {

    val f: (String) => List[Char] = (s: String) => List(s.toCharArray)
    val g: (Char) => List[Int] = (c: Char) => List(c.toInt)

    forAll(listsOf(Gen.identifier)) { fa =>

      val result_of_flatMap_of_flatMap = flatMap(flatMap(fa)(f))(g)
      val result_of_nested_flatMap = flatMap(fa)((a: String) => flatMap(f(a))(g))

      result_of_flatMap_of_flatMap shouldBe result_of_nested_flatMap

    }
  }

}

object ListMonadeLawSpec {

  def listsOf[A](as: Gen[A]): Gen[List[A]] = oneOf(Gen.const(Nil), consOf(as))

  def consOf[A](as: Gen[A]): Gen[Cons[A]] =
    for {
      a <- as
      tail <- listsOf(as)
    } yield Cons(a, tail)
}
