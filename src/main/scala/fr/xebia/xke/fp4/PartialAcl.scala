package fr.xebia.xke.fp4

object PartialAcl {

  private lazy val adminPassThrough: Restriction = {
    case AdminUser => true
  }

  def lift[Resource](acl: PartialAcl[Resource]): Acl[Resource] = {
    val f: (Resource) => User => Boolean = (resource: Resource) => {
      val resourceRestriction: Restriction = acl(resource)
      val restrictionWithAdmin: Restriction = resourceRestriction.orElse(adminPassThrough)
      restrictionWithAdmin.lift.andThen(_.getOrElse(false))
    }
    flip(f)
  }

  private def flip[A, B, C](f: A => B => C): (B => A => C) =
    (b: B) => (a: A) => f(a)(b)

}