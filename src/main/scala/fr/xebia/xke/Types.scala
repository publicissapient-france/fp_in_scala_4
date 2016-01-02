package fr.xebia.xke

sealed trait List[+A] {

  def concat[B >: A](right: List[B]): List[B] = this match {

    case Nil => right
    case Cons(a, Nil) => Cons(a, right)
    case Cons(a, tail) => Cons(a, tail.concat(right))

  }

}

case object Nil extends List[Nothing]

case class Cons[A](a: A, t: List[A]) extends List[A]

object List {
  def apply[A](elts: A*): List[A] = if (elts.isEmpty) Nil else Cons(elts.head, List.apply(elts.tail: _*))

  def apply[A](elts: Array[A]): List[A] = List.apply(elts: _*)

}