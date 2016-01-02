package fr.xebia.xke.fp4

case class BankId(value: Int)

case class Bank(id: BankId, name: String) {
  override def toString = name
}

case class MerchantId(value: Int)

case class Merchant(id: MerchantId, name: String, parent: BankId) {
  override def toString = name
}

