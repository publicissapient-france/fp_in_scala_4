package fr.xebia.xke.fp4

import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CompositionSpec extends FunSpec with Matchers {

  markup( """##Objective
            |
            |Functions are meant to be small, testable, side-effect free and reusable.
            |
            |In this section, we'll study function composition. 
            |It's the fusion of a function *f* of type `A ⇨ B` and a function *g* of type `B ⇨ C` to a brand new function *h* of type `A ⇨ C`.
            |
            |*f* and *g* are independent and can be consistent and reusable.
            |
            |There are *two* combinators on Functions:
            |
            |* `andThen`: combines `A ⇨ B` with `B ⇨ C` to give `A ⇨ C`
            |* `compose`: combines `B ⇨ C` with `A ⇨ B` to give `A ⇨ C` 
            |
            |Fusions are usefull to build large functions from smallers ones.
            |They are also usefull to replace multiple calls to a *map* combinator and optimize call.
            |For *map* on *List*, it avoids to create a new iteration loop, on *Future* it avoid to post a new asynchronous call to a new *Thread*.
            |
            |With type safety, this allow programmers to build secured pipeline of data processing which well tested and easily extended.
            |
            |##Tests
          """.stripMargin)

  describe("function composition") {

    val f: Int => Int = i => i + 10
    describe("function f of type Int => Int") {

      it("should add 10 to an int") {
        f(1) shouldBe 11
      }
    }

    val g: Int => String = i => i.toString
    describe("function g of type Int => String") {

      it("should make toString of int") {
        g(1) shouldBe "1"
      }
    }

    describe("f andThen g") {
      val h = f andThen g
      it("must be the equivalent to successive call of f and g with intermediate results") {
        val r1 = f(1)
        val r2 = g(r1)

        r2 shouldBe h(1)
      }

      it("must be the equivalent to successive call of f and g on Lists") {
        List(1, 2, 3).map(f).map(g) shouldBe List(1, 2, 3).map(f andThen g)
      }
    }

    describe("g compose f") {
      val h = g compose f
      it("should add 10 and the make toString") {
        h(1) shouldBe "11"
      }

      it("must be the equivalent to successive call of f and g") {
        List(1, 2, 3).map(f).map(g) shouldBe List(1, 2, 3).map(f andThen g)

        import scala.concurrent.ExecutionContext.Implicits.global
        val f1 = Future(1).map(f).map(g)
        val f2 = Future(1).map(f andThen g)

        val (r1, r2) = Await.result(for {
          result1 <- f1
          result2 <- f2
        } yield (result1, result2), atMost = 1.second)

        r1 shouldBe r2
      }
    }

    describe("g andThen f") {
      it("should not type check") {
        "g andThen f" shouldNot typeCheck
        "g andThen f" shouldNot compile
      }
    }
  }


}