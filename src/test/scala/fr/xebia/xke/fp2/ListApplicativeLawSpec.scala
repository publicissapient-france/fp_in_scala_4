package fr.xebia.xke.fp2

import fr.xebia.xke._
import fr.xebia.xke.fp3.EXO_3_9
import org.scalacheck.Gen
import org.scalacheck.Gen.{alphaLowerChar, choose, oneOf}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSpec, Matchers, PropSpec}

class ListApplicativeSpec extends FunSpec with Matchers {

  val _1_2_3_as_string = List("1", "2", "3")

  val _1_2_3_as_int = List(1, 2, 3)

  import Applicative.listApplicative._

  describe("An applicative instance for list") {
    it("should map a function over a list") {
      val result_of_map = map(_1_2_3_as_string)(Integer.parseInt)

      result_of_map shouldBe _1_2_3_as_int
    }

    it("should apply a list of functions on a list of elements") {
      val result_of_ap = ap(_1_2_3_as_int)(List(
        (i: Int) => i + 1,
        (i: Int) => i - 1
      ))

      result_of_ap shouldBe List(2, 3, 4, 0, 1, 2)
    }

    it("should apply a list of functions over pairs over a pair of list of elements") {
      val result_of_ap: List[Int] = ap2(_1_2_3_as_int, _1_2_3_as_int)(List(
        _ + _,
        _ * _
      ))

      result_of_ap shouldBe List(
        2, // SUM
        3,
        4,

        3, // SUM
        4,
        5,

        4, // SUM
        5,
        6,

        1, // PRODUCT
        2,
        3,

        2, // PRODUCT
        4,
        6,

        3, // PRODUCT
        6,
        9)
    }

    it("can be composed with another applicative", EXO_3_9) {
      val plus: ((Int, Int) => Int) = _ + _

      val list_of_options_1 = List(Some(1), None)
      val list_of_options_2 = List(None, Some(2))
      val listOptionApplicative = Applicative.listApplicative.compose(Applicative.optionApplicative)
      val firstResult = listOptionApplicative.apply2(list_of_options_1, list_of_options_2)(plus)
      firstResult shouldBe List(None, Some(3), None, None)


      val option_of_list1 = Some(List(1, 2, 3))
      val option_of_list2 = None
      val optionListApplicative = Applicative.optionApplicative.compose(Applicative.listApplicative)
      val secondResult = optionListApplicative.apply2(option_of_list1, option_of_list2)(plus)
      secondResult shouldBe None

      val list_of_list_1 = List(List(1), Nil)
      val list_of_list_2 = List(Nil, List(2, 3))
      val listListApplicative = Applicative.listApplicative.compose(Applicative.listApplicative)
      val thirdResult: List[List[Int]] = listListApplicative.apply2(list_of_list_1, list_of_list_2)(plus)
      thirdResult shouldBe List(
        Nil,
        List(3, 4),
        Nil,
        Nil
      )
    }
  }
}

object ListApplicativeSpec {

  def listsOf[A](as: Gen[A]): Gen[List[A]] = oneOf(Gen.const(Nil), consOf(as))

  def consOf[A](as: Gen[A]): Gen[Cons[A]] =
    for {
      a <- as
      tail <- listsOf(as)
    } yield Cons(a, tail)
}


class ListApplicativeLawSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {

  import fr.xebia.xke.fp2.ListApplicativeLawSpec._

  import Applicative.listApplicative._

  property("identity law of List Applicative instance") {

    def id[T]: (T => T) = c => c

    forAll(listsOf(alphaLowerChar)) { list =>
      ap(list)(point(id[Char])) should equal(list)
    }

  }

  property("homomorphism of point") {

    val f: (Char) => Int = c => c.toInt

    forAll(alphaLowerChar) { aChar =>

      ap(point(aChar))(point(f)) should equal(point(f(aChar)))
    }

  }

  property("interchange") {
    val f: (Int) => Int = i => i + 1
    val g: (Int) => Int = i => i - 1

    val functions: List[(Int) => Int] = List(f, g)

    forAll(choose(Int.MinValue, Int.MaxValue)) { i =>
      val functions_on_i = ap(point(i))(functions)
      val i_on_functions = ap(functions)(point((f: Int => Int) => f(i)))

      functions_on_i should equal(i_on_functions)
    }

  }
}

object ListApplicativeLawSpec {

  def listsOf[A](as: Gen[A]): Gen[List[A]] = oneOf(Gen.const(Nil), consOf(as))

  def consOf[A](as: Gen[A]): Gen[Cons[A]] =
    for {
      a <- as
      tail <- listsOf(as)
    } yield Cons(a, tail)
}
