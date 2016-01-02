package fr.xebia.xke.fp4

import org.scalatest.{Matchers, FunSpec}

class PartialFunctionSpec extends FunSpec with Matchers {

  import PartialFunctionSpec.listApplicative

  markup(
    """
      |##Objective
      |We are going improve our *ACL* system. We've seen some repetition for common case (false by default and admin always true).
      |
      |We can also see that the specific part defines a subset of the input argument values (*User*).
      |
      |There is a tool for that, *PartialFunction*. *PartialFunction* are different from *partially applied function*. 
      |In functional programing, there is no exception. 
      |If a function takes a *Int* as an input parameter, it must returns a value for *all* possible values of *Int*.
      |A *PartialFunction* is explicitly defined on a *subset* of the values of its input argument.
      |
      |*PartialFunction* contains additional methods on the top of classic `apply` Function method:
      |
      |* `isDefinedAt`: it returns `true` if the given argument is in the defined space of the function. If it's `false`, the call of `apply` will throw an exception. 
      |* `orElse`: it delegates to another *PartialFunction* its execution when it the given argument to `apply` is not in its defined space.
      |* `lift`: it transforms a `PartialFunction[A,B]` to a function of type `A => Option[B]`.
      |
      |##Types
      |###Restriction 
      |It is a `PartialFunction[User, Boolean]`. That means that it returns a value only for some values of type `User`.
      |  
      |###PartialAcl[Resource]
      |It is an alias for `Resource ⇨ PartialFunction[User, Boolean]`
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

  markup(
    """Let's first play with PartialFunction with `Restriction` type.
      |
      |##Tests
    """.stripMargin)

  describe("an acl is composed of restrictions") {

    they("can be defined only on a restricted space of users") {
      val simpleRestriction: Restriction = {
        case AdminUser => true
        case BankUser("bob", _) => true
      }

      simpleRestriction.isDefinedAt(AdminUser) shouldBe true
      simpleRestriction.isDefinedAt(BankUser("bob", BankId(1))) shouldBe true
      simpleRestriction.isDefinedAt(BankUser("tom", BankId(1))) shouldBe false
    }

    they("are composable") {
      val adminRestriction: Restriction = {
        case AdminUser => true
      }

      val bobRestriction: Restriction = {
        case BankUser("bob", _) => true
      }

      adminRestriction.isDefinedAt(AdminUser) shouldBe true
      adminRestriction.isDefinedAt(BankUser("bob", BankId(1))) shouldBe false

      bobRestriction.isDefinedAt(AdminUser) shouldBe false
      bobRestriction.isDefinedAt(BankUser("bob", BankId(1))) shouldBe true

      val composedRestriction = adminRestriction.orElse(bobRestriction)
      composedRestriction.isDefinedAt(AdminUser) shouldBe true
      composedRestriction.isDefinedAt(BankUser("bob", BankId(1))) shouldBe true
    }

    they("throw a MatchError when a value out of definition spaced is applied") {
      a[MatchError] shouldBe thrownBy {
        val adminRestriction: Restriction = {
          case AdminUser => true
        }

        adminRestriction(BankUser("bob", BankId(1)))
      }

    }

    they("can be lifted to a full function") {
      val adminRestriction: Restriction = {
        case AdminUser => true
      }

      val liftedRestriction: (User) => Option[Boolean] = adminRestriction.lift
      liftedRestriction(AdminUser) shouldBe Some(true)
      liftedRestriction(BankUser("bob", BankId(1))) shouldBe None
    }

  }

  markup(
    """Let's play with `PartialAcl` and lift that to an `Acl`.
      |
      |##Note
      |For some convenience, you'll see that there is an inversion of types.
      |`type Acl[Resource]        = User ⇨ Resource ⇨ Boolean`
      |`type PartialAcl[Resource] = Resource ⇨ User ⇨ Boolean`
      |
      |We'll have to flip the two first arguments at a moment!
      |
      |##Tests
    """.stripMargin)
  describe("a PartialAcl") {

    it("is composed of restriction") {
      val partialAcl: PartialAcl[Bank] = (bank: Bank) => {
        case BankUser(name, parentId) if parentId == bank.id => true
      }

      val resource = Bank(BankId(1), "BNPP")
      partialAcl(resource).isDefinedAt(AdminUser) shouldBe false
      partialAcl(resource).isDefinedAt(BankUser("admin", BankId(1))) shouldBe true
      partialAcl(resource).isDefinedAt(BankUser("admin", BankId(2))) shouldBe false
    }

    it("can be lifted to a full function with default behavior") {
      val partialAcl: PartialAcl[Bank] = (bank: Bank) => {
        case BankUser(name, parentId) if parentId == bank.id => true
      }

      val acl: Acl[Bank] = PartialAcl.lift(partialAcl)

      val resource = Bank(BankId(1), "BNPP")
      acl(AdminUser)(resource) shouldBe true
      acl(BankUser("admin", BankId(1)))(resource) shouldBe true
      acl(BankUser("admin", BankId(2)))(resource) shouldBe false
    }
  }

  markup(
    """Let's plug everything with our *RestService*.
      |
      |##Tests
    """.stripMargin)

  describe("bank ACL") {

    lazy val bankAcl: PartialAcl[Bank] = bank => {
      case BankUser(_, parentId) if parentId == bank.id => true
    }

    it("should give access to an attached bank admin") {
      bankAcl(bnpp)(bnpAdmin) shouldBe true
    }

    it("should not give access to a bank admin attached to another bank") {
      bankAcl(bnpp).isDefinedAt(sgAdmin) shouldBe false
    }

    it("should not give access to a merchant user") {
      bankAcl(bnpp).isDefinedAt(sephoraAdmin) shouldBe false
    }

    it("should delegate global admin") {
      bankAcl(bnpp).isDefinedAt(AdminUser) shouldBe false
    }

    it("can be used with RestService#findBank") {
      val findBank: (User) => (BankId) => Option[Bank] = PartialAcl.lift(bankAcl) andThen RestService.findBank(bankRepo)

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

    lazy val merchantAcl: PartialAcl[Merchant] = merchant => {
      case BankUser(_, parentId) if parentId == merchant.parent => true
      case MerchantUser(_, parentId) if parentId == merchant.id => true
    }

    it("should give access to an attached bank admin") {
      merchantAcl(mcdo)(bnpAdmin) shouldBe true
    }

    it("should not give access to a bank admin attached to another bank") {
      merchantAcl(mcdo).isDefinedAt(sgAdmin) shouldBe false
    }

    it("should give access to attached merchant user") {
      merchantAcl(mcdo)(mcdoAdmin) shouldBe true
    }

    it("should not give access to a merchant admin attached to another merchant") {
      merchantAcl(mcdo).isDefinedAt(sephoraAdmin) shouldBe false
    }

    it("should delegate global admin") {
      merchantAcl(mcdo).isDefinedAt(AdminUser) shouldBe false
    }

    it("can be used with RestService#findMerchant") {
      val findMerchant = PartialAcl.lift(merchantAcl) andThen RestService.findMerchant(merchantRepo)

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


object PartialFunctionSpec {

  import fr.xebia.xke.fp2.Applicative

  val listApplicative = new Applicative[List] {
    override def point[A](a: A): List[A] = List(a)

    override def ap[A, B](fa: List[A])(f: List[(A) => B]): List[B] = fa.flatMap(a => f.map(g => g(a)))
  }
}