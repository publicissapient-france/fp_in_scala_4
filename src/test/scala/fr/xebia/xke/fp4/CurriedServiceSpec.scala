package fr.xebia.xke.fp4

import org.scalatest.{Matchers, FunSpec}

class CurriedServiceSpec extends FunSpec with Matchers {

  import PartialFunctionSpec.listApplicative

  markup(
    """
      |##Objective
      |We are going to see currification in action in a real-world code. 
      |Because we can partially applied a curried function, we are going to use this feature for dependency injection.
      |
      |We have a *repository* of entities. We have a *RestService* that uses a *repository* and provide a lookup method with visibility checks (ACL).
      |When a user tries to find an element from the *repository*, the *RestService* checks the user visibility before sending data.
      |For dependency injection, instead of injecting a *repository* in the *service*, it is the *service* function that requires the *repository* through an higher order function.
      |
      |##Types
      |###Bank
      |A *bank* is a domain element at the node in a hierarchy. It has a *name*.
      |
      |###Merchant
      |A *merchant* is a domain element which is a leaf in the hierarchy. It has a *name* and is attached to a *bank*.
      |
      |###ResourceCheck[Resource]
      |This is an alias for a function of type `Resource ⇨ Boolean`. It tells to the service if a resource loaded from the repository is visible in the current context or not.
    """.stripMargin)

  val bankRepo = new Repository[BankId, Bank] {}

  val bnpp = Bank(BankId(1), "BNPP")
  val bnpAdmin = BankUser("BNPPAdmin", bnpp.id)
  bankRepo add (bnpp.id -> bnpp)

  val socgen = Bank(BankId(2), "SOCGEN")
  val sgAdmin = BankUser("SOCGENAdmin", socgen.id)
  bankRepo add (socgen.id -> socgen)

  val merchantRepo = new Repository[MerchantId, Merchant] {}
  val mcdo = Merchant(MerchantId(1), "MCDO", bnpp.id)
  val mcdoAdmin = MerchantUser("Ronald", mcdo.id)
  merchantRepo add (mcdo.id -> mcdo)

  val sephora = Merchant(MerchantId(2), "SEPHORA", bnpp.id)
  val sephoraAdmin = MerchantUser("Christopher", sephora.id)
  merchantRepo add (sephora.id -> sephora)

  val starbucks = Merchant(MerchantId(3), "STARBUCKS", socgen.id)
  merchantRepo add (starbucks.id -> starbucks)

  val users: List[User] = List(bnpAdmin, sgAdmin, mcdoAdmin, sephoraAdmin)
  val bankIds: List[BankId] = List(bnpp, socgen).map(_.id)
  val merchantIds: List[MerchantId] = List(mcdo, sephora, starbucks).map(_.id)


  val passThrough: ResourceCheck[Bank] = _ => true

  markup(
    """`RestService#findBank` is a function of type `Repository[BankId, Bank] ⇨ ResourceCheck[Bank] ⇨ BankId ⇨ Option[Bank]`.
      |We can apply value `repo` to "inject" this value in the function.
      |We now have a function of type `ResourceCheck[Bank] ⇨ BankId ⇨ Option[Bank]`.
      |
      |Then we can do the same for `ResourceCheck` to be able to use the `findBank` function.
      |
      |##Tests
    """.stripMargin)

  describe("findBank function") {

    describe("with a bankRepo") {

      lazy val fWithRepo: (ResourceCheck[Bank]) => (BankId) => Option[Bank] = ??? //TODO EXO4

      describe("with a passThrough checker") {

        lazy val fWithPassThrough: (BankId) => Option[Bank] = ??? //TODO EXO4

        it("should returns all banks of the repo", EXO_4_4) {

          fWithPassThrough(bnpp.id) shouldBe Some(bnpp)
          fWithPassThrough(socgen.id) shouldBe Some(socgen)
          fWithPassThrough(BankId(3)) shouldBe None
        }
      }


      describe("with a checker only for BNPP") {

        val bnppCheck: ResourceCheck[Bank] = (bank: Bank) => bank.id == bnpp.id

        lazy val fForBnp: (BankId) => Option[Bank] = ??? //TODO EXO4

        it("should only returns BNPP", EXO_4_4) {
          fForBnp(bnpp.id) shouldBe Some(bnpp)
          fForBnp(socgen.id) shouldBe None
          fForBnp(BankId(3)) shouldBe None
        }
      }
    }
  }

  markup(
    """
      |##Objectives
      |Our service requires a function of type `Resource ⇨ Boolean` to check the visibility of an element. Here we want to add the control depending on the visibility of a user.
      |Thanks to partial application, we can add this notion of `User` without changing the service signature.
      |
      |##Types
      |###User
      |A user can be of type:
      |
      |* `BankUser`: this user is attached to a *bank* and can see his own *bank* or *every merchants* attached to this *bank*.
      |* `MerchantUser`: this user is attached to a *merchant* and can only see his own *merchant*.
      |* `AdminUser`: this user can see every *banks* and every *merchants*.
      |
      |###Acl
      |We can combine type aliases. Here `Acl[Resource]` is a function of type `User ⇨ Resource ⇨ Boolean`.
      |`Acl` is a sort of *factory* of `ResourceCheck` thanks to partial application of the `User`.
      |
      |Without modifying the service we can add the user as a dependency and injecting it by partial application before using it in the service.
      |
      |###Note
      |We previously have seen that functions can be used with existing others, for example *map*.
      |There is an implementation of our *Applicative* type class to provide magic method to in one call test every combination of users and banks/merchants!
      |But `apply2` is not a curried function but a function of type `(A,B) ⇨ C`. Try to use it first.
      |Next, use `fpair2` which is curried ;-)
      |
      |##Tests
    """.stripMargin)

  describe("the extension of ResourceCheck with User") {

    describe("bank ACL") {

      lazy val bankAcl: Acl[Bank] = ??? //TODO EXO5

      it("should give access to an attached bank admin", EXO_4_5) {
        bankAcl(bnpAdmin)(bnpp) shouldBe true
      }

      it("should not give access to a bank admin attached to another bank", EXO_4_5) {
        bankAcl(sgAdmin)(bnpp) shouldBe false
      }

      it("should not give access to a merchant user", EXO_4_5) {
        bankAcl(sephoraAdmin)(bnpp) shouldBe false
      }

      it("should give access to a global admin", EXO_4_5) {
        bankAcl(AdminUser)(bnpp) shouldBe true
      }

      it("can be used with RestService#findBank", EXO_4_5) {
        val findBank: (User) => (BankId) => Option[Bank] = ??? //TODO EXO5
        val curriedFindBank: (User, BankId) => Option[Bank] = ??? //TODO EXO5
        listApplicative.apply2(users, bankIds)(curriedFindBank) shouldBe List(
          Some(bnpp), //bnpAdmin
          None, //socgenAdmin
          None, //mcdoAdmin
          None, //sephoraAdmin
          None, //bnpAdmin
          Some(socgen), //socgenAdmin
          None, //mcdoAdmin
          None) //sephoraAdmin

        listApplicative.fpair2(users)(bankIds)(findBank) shouldBe List(
          (bnpAdmin, BankId(1), Some(bnpp)),
          (sgAdmin, BankId(1), None),
          (mcdoAdmin, BankId(1), None),
          (sephoraAdmin, BankId(1), None),
          (bnpAdmin, BankId(2), None),
          (sgAdmin, BankId(2), Some(socgen)),
          (mcdoAdmin, BankId(2), None),
          (sephoraAdmin, BankId(2), None)
        )
      }
    }

    describe("merchant ACL") {

      lazy val merchantAcl: Acl[Merchant] = ??? //TODO EXO5

      it("should give access to an attached bank admin", EXO_4_5) {
        merchantAcl(bnpAdmin)(mcdo) shouldBe true
      }

      it("should not give access to a bank admin attached to another bank", EXO_4_5) {
        merchantAcl(sgAdmin)(mcdo) shouldBe false
      }

      it("should give access to attached merchant user", EXO_4_5) {
        merchantAcl(mcdoAdmin)(mcdo) shouldBe true
      }

      it("should not give access to a merchant admin attached to another merchant", EXO_4_5) {
        merchantAcl(sephoraAdmin)(mcdo) shouldBe false
      }

      it("should give access to a global admin", EXO_4_5) {
        merchantAcl(AdminUser)(mcdo) shouldBe true
      }

      it("can be used with RestService#findMerchant", EXO_4_5) {
        val findMerchant: (User) => (MerchantId) => Option[Merchant] = ??? //TODO EXO5
        val curriedFindMerchant: (User, MerchantId) => Option[Merchant] = ??? //TODO EXO5
        listApplicative.apply2(users, merchantIds)(curriedFindMerchant) shouldBe List(
          Some(mcdo), //bnpAdmin
          None, //socgen
          Some(mcdo), //mcdoAdmin
          None, //sephoraAdmin
          Some(sephora), //bnpAdmin
          None, //socgen
          None, //mcdoAdmin
          Some(sephora), //sephoraAdmin
          None, //bnpAdmin
          Some(starbucks), //socgenAdmin
          None, //mcdoAdmin
          None //sephoraAdmin
        )

        listApplicative.fpair2(users)(merchantIds)(findMerchant) shouldBe List(
          (bnpAdmin, MerchantId(1), Some(mcdo)),
          (sgAdmin, MerchantId(1), None),
          (mcdoAdmin, MerchantId(1), Some(mcdo)),
          (sephoraAdmin, MerchantId(1), None),
          (bnpAdmin, MerchantId(2), Some(sephora)),
          (sgAdmin, MerchantId(2), None),
          (mcdoAdmin, MerchantId(2), None),
          (sephoraAdmin, MerchantId(2), Some(sephora)),
          (bnpAdmin, MerchantId(3), None),
          (sgAdmin, MerchantId(3), Some(starbucks)),
          (mcdoAdmin, MerchantId(3), None),
          (sephoraAdmin, MerchantId(3), None)
        )
      }
    }
  }
}