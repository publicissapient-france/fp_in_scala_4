package fr.xebia.xke.fp3

import fr.xebia.xke.List
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{PropSpec, Matchers, FunSpec}

class RetrySpec extends FunSpec with Matchers {

  //TODO EXO5
  describe("A retry") {

    it("should be a success when the given work is successful on the first attempt", EXO_3_5) {
      Retry(retries = 1)(() => 5) shouldBe Success(5, 0)
    }

    it("should be a success when the given work is successful within the given tries", EXO_3_5) {
      var alreadyPassed = false
      Retry(retries = 2)(() => {
        if (alreadyPassed) {
          5
        }
        else {
          alreadyPassed = true
          throw new IllegalStateException()
        }
      }) shouldBe Success(5, 1)
    }

    it("should be a failure when the given work fails after the given tries", EXO_3_5) {
      var count = 0

      Retry(retries = 5)(() => {
        count += 1
        throw new IllegalStateException()
      }) shouldBe a[Failure]

      count shouldBe 5
    }
  }

  //TODO EXO6
  describe("A monad instance for retry") {
    import Monade.retryMonade._

    it("should map a function over a successful retry", EXO_3_6) {
      val result_of_map = map(Success("1"))(Integer.parseInt)

      result_of_map shouldBe Success(1, tries = 0)
    }

    it("should not map a function over a failed retry", EXO_3_6) {
      val result_of_map = map(Failure(new IllegalArgumentException))(Integer.parseInt)

      result_of_map shouldBe a[Failure]
    }

    it("should flatMap a successful function successful value", EXO_3_6) {
      val result_of_flatmap = flatMap(Success("1"))(i => Retry(retries = 1)(() => Integer.parseInt(i)))

      result_of_flatmap shouldBe Success(1, tries = 0)
    }

    it("should flatMap a failed function successful value", EXO_3_6) {
      val result_of_flatmap = flatMap(Success("a"))(i => Retry(retries = 1)(() => Integer.parseInt(i)))

      result_of_flatmap shouldBe a[Failure]
    }

    it("should not flatMap a successful function failed value", EXO_3_6) {
      val result_of_flatmap = flatMap(Failure(new IllegalArgumentException))(i => Retry(retries = 1)(() => Integer.parseInt(i)))

      result_of_flatmap shouldBe a[Failure]
    }

    it("should not flatMap a failed function failed value", EXO_3_6) {
      val result_of_flatmap = flatMap(Failure(new IllegalArgumentException))(i => Retry(retries = 1)(() => throw new IllegalArgumentException))

      result_of_flatmap shouldBe a[Failure]
    }

    it("should flatten Success(Success) to Success", EXO_3_6) {
      val result_of_flatten = flatten(Success(Success(1)))

      result_of_flatten shouldBe Success(1)
    }

    it("should flatten Success(Failure) to Failure", EXO_3_6) {
      val result_of_flatten = flatten(Success(Failure(new IllegalArgumentException)))

      result_of_flatten shouldBe a[Failure]
    }

    it("should flatten Failure(Failure) to Failure", EXO_3_6) {
      val result_of_flatten = flatten(Failure(new IllegalArgumentException))

      result_of_flatten shouldBe a[Failure]
    }

    it("should be able to use apply2 over 2 successful values", EXO_3_6) {
      val result_of_flatten = apply2(Success(1), Success(2))(_ + _)

      result_of_flatten shouldBe Success(3)
    }

    it("should be able to use apply2 over Success,Failure values", EXO_3_6) {
      val result_of_flatten = apply2(Success(1), Failure(new IllegalArgumentException): Retry[Int])(_ + _)

      result_of_flatten shouldBe a[Failure]
    }

    it("should be able to use apply2 over Failure,Success values", EXO_3_6) {
      val result_of_flatten = apply2(Success(1), Failure(new IllegalArgumentException): Retry[Int])(_ + _)

      result_of_flatten shouldBe a[Failure]
    }

    it("should be able to use apply2 over Failure,Failure values", EXO_3_6) {
      val result_of_flatten = apply2(Failure(new IllegalArgumentException): Retry[Int], Failure(new IllegalArgumentException): Retry[Int])(_ + _)

      result_of_flatten shouldBe a[Failure]
    }

    it("should be used in a for comprehension", EXO_3_7) {

      def retryParsing(s: String): Retry[Int] = Retry(retries = 1)(() => Integer.parseInt(s))

      val result_of_for = for {
        i <- retryParsing("1")
        j <- retryParsing("2")
      } yield i + j

      result_of_for shouldBe Success(3)
    }

    it("should be used in a for comprehension with filters", EXO_3_7) {

      def retryParsing(s: String): Retry[Int] = Retry(retries = 1)(() => Integer.parseInt(s))

      val result_of_for = for {
        i <- retryParsing("1") if i < 1
        j <- retryParsing("2")
      } yield i + j

      result_of_for shouldBe a[Failure]
    }

  }
}

class RetryMonadLawSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {

  import RetryMonadLawSpec._
  import Monade.retryMonade._

  property("right identity law of List Monad instance", EXO_3_8) {

    forAll(retryOf(Gen.identifier)) { fa =>

      fa shouldBe flatMap(fa)(a => point(a))
    }

  }

  property("left identity law of List Monad instance", EXO_3_8) {

    forAll(Gen.identifier) { a =>
      val f: (String) => Retry[String] = (s: String) => Success(s + "-after-flatMap")

      f(a) shouldBe flatMap(point(a))(f)
    }

  }

  property("associative flatMap", EXO_3_8) {

    val f: (String) => Retry[List[Char]] = (s: String) => Retry(retries = 1)(() => List(s.toCharArray))
    val g: (List[Char]) => Retry[List[Int]] = (chars: List[Char]) => Retry(retries = 1)(() => Monade.listMonade.map(chars)(_.toInt))

    forAll(retryOf(Gen.identifier)) { fa =>

      val result_of_flatMap_of_flatMap = flatMap(flatMap(fa)(f))(g)
      val result_of_nested_flatMap = flatMap(fa)((a: String) => flatMap(f(a))(g))

      result_of_flatMap_of_flatMap shouldBe result_of_nested_flatMap

    }
  }

  property("associative flatMap with randomly failing f", EXO_3_8) {

    val f: (String) => Retry[List[Char]] = (s: String) =>
      if (Gen.choose(0, 1).sample.get == 0) {
        Failure(new RuntimeException("Bad luck"))
      } else {
        Success(List(s.toCharArray))
      }
    val g: (List[Char]) => Retry[List[Int]] = (chars: List[Char]) => Retry(retries = 1)(() => Monade.listMonade.map(chars)(_.toInt))

    forAll(retryOf(Gen.identifier)) { fa =>

      val result_of_flatMap_of_flatMap = flatMap(flatMap(fa)(f))(g)
      val result_of_nested_flatMap = flatMap(fa)((a: String) => flatMap(f(a))(g))

      result_of_flatMap_of_flatMap match {
        case Success(t, tries) => result_of_flatMap_of_flatMap shouldBe result_of_nested_flatMap
        case f: Failure => result_of_nested_flatMap shouldBe a[Failure]
      }
    }
  }

}

object RetryMonadLawSpec {

  def retryOf[A](inner: Gen[A]) = for {
    success_or_failure <- Gen.choose[Int](0, 1)
    i <- inner
  } yield {
    success_or_failure match {
      case 0 => Failure(new RuntimeException("Bad luck"))
      case 1 => Success(i)
    }
  }

}