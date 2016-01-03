package fr.xebia.xke.fp4

object PartialAcl {

  private lazy val adminPassThrough: Restriction = {
    case AdminUser => true
  }

  def lift[Resource](acl: PartialAcl[Resource]): Acl[Resource] = {
    ??? //TODO EXO7
  }

  private def flip[A, B, C](f: A => B => C): (B => A => C) =
    ??? //TODO EXO7

}