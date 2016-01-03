package fr.xebia.xke.fp3

sealed trait Retry[+T] {

  def flatMap[B](f: (T => Retry[B])): Retry[B] = Monade.retryMonade.flatMap(this)(f)

  def map[B](f: (T => B)): Retry[B] = Monade.retryMonade.map(this)(f)

  def filter(predicate: (T => Boolean)): Retry[T] = this.flatMap { t =>
    if (predicate(t)) {
      this
    } else {
      Failure(new IllegalArgumentException(s"$t does not match"))
    }
  }

}

case class Failure(t: Throwable) extends Retry[Nothing]

case class Success[T](t: T, tries: Int = 0) extends Retry[T]

object Retry {

  def apply[T](retries: Int, tries: Int = 0)(t: () => T): Retry[T] =
    if (tries < retries) {


      try {
        Success(t(), tries)
      } catch {
        case throwable: Throwable =>
          if ((tries + 1) < retries) {
            Retry(retries, tries + 1)(t)
          }
          else {
            Failure(throwable)
          }
      }

    } else {
      Failure(new IllegalArgumentException(s"Retries must be > 0"))
    }

}