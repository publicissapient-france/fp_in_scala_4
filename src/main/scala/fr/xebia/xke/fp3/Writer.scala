package fr.xebia.xke.fp3

import java.time.Instant

import play.api.libs.json.{Json, JsArray}


case class StringWriter[A](value: A, log: String) {

  def map[B](f: A => B): StringWriter[B] = StringWriter.stringWriterMonad.map(this)(f)

  def flatMap[B](f: A => StringWriter[B]): StringWriter[B] = StringWriter.stringWriterMonad.flatMap(this)(f)

}


object StringWriter {

  private def now = Instant.now()

  def apply[A](a: A): StringWriter[A] = stringWriterMonad.point(a)

  def startWith[A](a: A): StringWriter[A] = apply(a)

  def alterTo[A](newValue: A, cause: String): StringWriter[A] = StringWriter(newValue, s"$now: changing to: $newValue | ** $cause")

  lazy val stringWriterMonad = new Monade[StringWriter] {

    override def flatMap[A, B](a: StringWriter[A])(f: (A) => StringWriter[B]): StringWriter[B] = {
      val b = f(a.value)
      b.copy(log = a.log + "\n" + b.log)
    }

    override def point[A](a: A): StringWriter[A] = StringWriter(a, s"$now: setting to: $a")
  }
}

case class JsonWriter[A](value: A, log: JsArray) {

  def map[B](f: A => B): JsonWriter[B] = JsonWriter.jsonWriterMonad.map(this)(f)

  def flatMap[B](f: A => JsonWriter[B]): JsonWriter[B] = JsonWriter.jsonWriterMonad.flatMap(this)(f)

}


object JsonWriter {

  private def now = Instant.now()

  def apply[A](a: A): JsonWriter[A] = jsonWriterMonad.point(a)

  def startWith[A](a: A): JsonWriter[A] = apply(a)

  def alterTo[A](newValue: A, cause: String): JsonWriter[A] = JsonWriter(newValue, JsArray(
    Seq(Json.obj(
      "time" -> now.toString,
      "value" -> newValue.toString,
      "message" -> s"replacing value"))
  ))


  lazy val jsonWriterMonad = new Monade[JsonWriter] {

    override def flatMap[A, B](a: JsonWriter[A])(f: (A) => JsonWriter[B]): JsonWriter[B] = {
      val b = f(a.value)
      b.copy(log = a.log ++ b.log)
    }

    override def point[A](a: A): JsonWriter[A] = JsonWriter(a, JsArray(
      Seq(Json.obj(
        "time" -> now.toString,
        "value" -> a.toString,
        "message" -> s"setting value")))
    )
  }
}