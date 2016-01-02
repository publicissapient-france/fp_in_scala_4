package fr.xebia.xke.fp4

trait RestService {


  val findBank: Repository[BankId, Bank] => ResourceCheck[Bank] => BankId => Option[Bank] = repo => check => bankId =>
    repo.find(bankId).filter(check)

  val findMerchant: Repository[MerchantId, Merchant] => ResourceCheck[Merchant] => MerchantId => Option[Merchant] = repo => check => merchantId =>
    repo.find(merchantId).filter(check)
}

object RestService extends RestService