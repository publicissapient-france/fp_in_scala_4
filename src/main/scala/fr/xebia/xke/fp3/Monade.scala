package fr.xebia.xke.fp3

import fr.xebia.xke._
import fr.xebia.xke.fp2.Applicative

import scala.language.higherKinds

trait Monade[F[_]] extends Applicative[F] {

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  // pour rappel, ceci est aussi abstrait dans Applicative
  // def point[A](a: => A): List[A] = ???

  //TODO EXO2
  override def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(a => point(f(a)))

  //TODO EXO2
  override def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = flatMap(fa)(a => map(f)(g => g(a)))

  //TODO EXO3
  def flatten[A](ffa: F[F[A]]): F[A] = flatMap(ffa)(fa => fa)

}

object Monade {

  //TODO EXO1
  val listMonade = new Monade[List] {

    override def flatMap[A, B](fa: List[A])(f: (A) => List[B]): List[B] = fa match {
      case Cons(a, tail) => f(a).concat(flatMap(tail)(f))
        case Nil => Nil
      }

    override def point[A](a: A): List[A] = List(a)

  }

  //TODO EXO6
  val retryMonade = new Monade[Retry] {

    override def flatMap[A, B](fa: Retry[A])(f: (A) => Retry[B]): Retry[B] = fa match {
      case Success(a, tries) => f(a)

      case f@Failure(t) => f

    }

    override def point[A](a: A): Retry[A] = Success(a)

  }
}