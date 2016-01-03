package fr.xebia.xke.fp4

import org.scalatest.{FunSpec, Matchers}

class CurryingSpec extends FunSpec with Matchers {

  markup(
    """##Currification
      |We are going to explore function currification. This is one core principal of function compositions.
      |
      |We are going to code an `add` function. It's a simple function that takes 2 `Integer` and returns the sum. 
      |It is composed of 2 input arguments. We say that this function has an *arity* of 2.
      |
      |Currification of functions is a process that change a function of arity n greater than 1 to n equivalent functions of 1 argument.
      |
      |For `add` example, we pass from `(Int,Int) ⇨ Int` to `Int ⇨ Int ⇨ Int`. The curried function takes 1 `Int` as input and returns another function that takes another `Int` and finally returns an `Int`.
      |
      |We call *higher order function* a function that returns another function.
      |
      |###Tests
    """.stripMargin)

  describe("an add function with 2 arguments for integers") {

    def add(x: Int, y: Int): Int = x + y

    it("should add x with y", EXO_4_2) {
      add(1, 2) shouldBe 3
      List(1, 2, 3).map(x => add(1, x)) shouldBe List(2, 3, 4)
    }

    it("it can be manually partially applied on x", EXO_4_2) {
      val add_one: Int => Int = (x: Int) => add(x, 1)

      add_one(2) shouldBe 3
      List(1, 2, 3).map(add_one) shouldBe List(2, 3, 4)
    }
  }

  describe("an add function as higher order function") {

    def add(x: Int): Int => Int = (y: Int) => x + y

    it("should add x with y", EXO_4_2) {
      add(1)(2) shouldBe 3
      List(1, 2, 3).map(x => add(1)(x)) shouldBe List(2, 3, 4)
    }

    it("it can be manually partially applied on x", EXO_4_2) {
      val add_one: Int => Int = (x: Int) => add(1)(x)

      add_one(2) shouldBe 3
      List(1, 2, 3).map(add_one) shouldBe List(2, 3, 4)
    }
  }

  def curry[A, B, C](f: ((A, B) => C)): A => B => C = (a: A) => (b: B) => f(a, b)

  def uncurry[A, B, C](f: (A => B => C)): (A, B) => C = (a: A, b: B) => f(a)(b)

  describe("the curry function") {

    it("it transform any function (A,B) => C into A => B => C", EXO_4_2) {
      def add(x: Int, y: Int): Int = x + y
      val yet_another_curried_add_one = curry(add)(1)

      yet_another_curried_add_one(2) shouldBe 3

    }
  }

  describe("the uncurry function") {

    it("it transform any function A => B => C into (A,B) => C", EXO_4_2) {
      def add(x: Int)(y: Int): Int = x + y
      val yet_another_uncurried_add = uncurry(add)

      yet_another_uncurried_add(1, 2) shouldBe 3

    }
  }

  markup(
    """##Partial application
      |
      |The advantage of curried function is that you can apply the different argument at different stage of your program.
      |
      |For example, you have a `List[Int]` and you want to `map` a function over it, you need a function of type `Int ⇨ A`.
      |
      |`add` is of type `Int ⇨ Int ⇨ Int`. If we apply only one argument of type `Int`, we have a function of type `Int ⇨ Int`. Thus it can be mapped on a list of `Int`.
      |
      |###Tests
    """.stripMargin)

  describe("a curried add function") {

    def add(x: Int)(y: Int): Int = x + y

    it("should add x with y", EXO_4_3) {
      add(1)(2) shouldBe 3
      List(1, 2, 3).map(x => add(1)(x)) shouldBe List(2, 3, 4)
    }

    it("it can be manually partially applied on x", EXO_4_3) {
      val add_one: Int => Int = add(1)

      add_one(2) shouldBe 3
      List(1, 2, 3).map(add_one) shouldBe List(2, 3, 4)
    }

    it("it can be uncurried", EXO_4_3) {
      val add_uncurried: (Int, Int) => Int = uncurry(add)

      add_uncurried(1, 2) shouldBe 3
    }

    it("it can be uncurried and curried", EXO_4_3) {
      val add_uncurried: (Int, Int) => Int = uncurry(add)
      val add_re_curried: Int => Int => Int = curry(add_uncurried)

      add_re_curried(1)(2) shouldBe 3
    }
  }

}
