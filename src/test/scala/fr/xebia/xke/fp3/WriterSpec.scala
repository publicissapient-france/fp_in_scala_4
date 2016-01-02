package fr.xebia.xke.fp3

import org.scalatest.{FunSpec, Matchers}

class WriterSpec extends FunSpec with Matchers {

  describe("A writer with string logs") {

    import fr.xebia.xke.fp3.StringWriter._

    it("should start with an empty log", EXO_3_10) {

      val computation = StringWriter.startWith(1)

      computation.log should include("setting to: 1")
      computation.value shouldBe 1
    }

    it("should store log ", EXO_3_10) {

      val computation: StringWriter[Int] = for {
        start <- StringWriter.startWith(1)
        next <- alterTo(start + 1, "adding 1")
        next_next <- alterTo(next + 5, "adding 5")
      } yield next_next

      info(computation.value.toString)
      computation.log.split("\n").foreach(info(_))

      computation.value shouldBe 7
      computation.log.split("\n") should have length 4 // 1 start + 2 flatmap + 1 final map
    }
  }

  describe("A writer with json logs") {

    import fr.xebia.xke.fp3.JsonWriter._

    it("should start with an empty log", EXO_3_10) {

      val computation = JsonWriter.startWith(1)

      computation.log.value should have length 1
      computation.value shouldBe 1
    }

    it("should store log ", EXO_3_10) {

      val computation: JsonWriter[Int] = for {
        start <- JsonWriter.startWith(1)
        next <- alterTo(start + 1, "adding 1")
        next_next <- alterTo(next + 5, "adding 5")
      } yield next_next

      info(computation.value.toString)
      info(computation.log.toString())

      computation.value shouldBe 7
      computation.log.value should have length 4 // 1 start + 2 flatmap + 1 final map
    }
  }


}