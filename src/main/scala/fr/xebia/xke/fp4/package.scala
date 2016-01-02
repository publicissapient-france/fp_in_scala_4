package fr.xebia.xke

package object fp4 {

  type ResourceCheck[Resource] = Resource => Boolean

  type Acl[Resource] = User => ResourceCheck[Resource]

  type Restriction = PartialFunction[User, Boolean]
  
  type PartialAcl[Resource] = Resource => Restriction

}
