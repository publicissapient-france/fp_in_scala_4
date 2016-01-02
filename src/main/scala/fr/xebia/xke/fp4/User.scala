package fr.xebia.xke.fp4

sealed trait User

case class BankUser(name: String, parent: BankId) extends User {
  override def toString = name
}

case class MerchantUser(name: String, parent: MerchantId) extends User {
  override def toString = name
}

case object AdminUser extends User