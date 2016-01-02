package fr.xebia.xke.fp2

import fr.xebia.xke._
import fr.xebia.xke.fp3.EXO_3_9
import org.scalacheck.Gen
import org.scalacheck.Gen.{alphaLowerChar, oneOf}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSpec, Matchers, PropSpec}

class ListeFoncteurSpec extends FunSpec with Matchers {

  val _1_2_3_as_string = List("1", "2", "3")

  val _1_2_3_as_int = List(1, 2, 3)
  
  import Foncteur.listFoncteur._

  describe("Functor instance for list") {
    it("should map a function over a list") {
      val result_of_map = map(_1_2_3_as_string)(Integer.parseInt)

      result_of_map shouldBe _1_2_3_as_int
    }

    it("should pair the input and output of a function over a list with fproduct") {
      val result_of_fproduct = fproduct(_1_2_3_as_string)(Integer.parseInt)

      result_of_fproduct shouldBe List(
        ("1", 1),
        ("2", 2),
        ("3", 3)
      )
    }

    it("should be able to apply a list of functions over an element with mapply") {
      val result_of_mapply = mapply("heLLo")(List(
        (s: String) => s.toUpperCase,
        (s: String) => s.toLowerCase
      ))

      result_of_mapply shouldBe List(
        "HELLO",
        "hello"
      )
    }

    it("can be composed with another functor", EXO_3_9) {
      val plus_one: (Int => Int) = _ + 1

      val list_of_options = List(Some(1), None)
      val listOptionFoncteur = Foncteur.listFoncteur.compose(Foncteur.optionFoncteur)
      val firstResult = listOptionFoncteur.map(list_of_options)(plus_one)
      firstResult shouldBe List(Some(2), None)


      val option_of_list = Some(List(1, 2, 3))
      val optionListFoncteur = Foncteur.optionFoncteur.compose(Foncteur.listFoncteur)
      val secondResult = optionListFoncteur.map(option_of_list)(plus_one)
      secondResult shouldBe Some(List(2, 3, 4))


      val list_of_list = List(List(1, 2, 3), Nil)
      val listListFoncteur = Foncteur.listFoncteur.compose(Foncteur.listFoncteur)
      val thirdResult = listListFoncteur.map(list_of_list)(plus_one)
      thirdResult shouldBe List(List(2, 3, 4), Nil)
    }
  }
}

class ListeFoncteurLawSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {

  import fr.xebia.xke.fp2.ListeFoncteurLawSpec._
  import Foncteur.listFoncteur._

  property("identity law of List Functor instance") {

    def id[T]: (T => T) = c => c

    forAll(listsOf(alphaLowerChar)) { list =>
      map(list)(id[Char]) should equal(list)
    }

  }

  property("associativity law of List Functor instance") {

    forAll(listsOf(alphaLowerChar)) { list =>
      val f: (Char) => Int = (c: Char) => c.toInt
      val g: (Int) => String = (i: Int) => i.toString
      val h: (Char) => String = g compose f

      val h_over_list = map(list)(h)
      val f_over_list = map(list)(f)
      val g_over_f_over_list = map(f_over_list)(g)

      h_over_list should equal(g_over_f_over_list)
    }
  }
}

object ListeFoncteurLawSpec {

  def listsOf[A](as: Gen[A]): Gen[List[A]] = oneOf(Gen.const(Nil), consOf(as))

  def consOf[A](as: Gen[A]): Gen[Cons[A]] =
    for {
      a <- as
      tail <- listsOf(as)
    } yield Cons(a, tail)
}
