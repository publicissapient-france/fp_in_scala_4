import org.scalatest.Matchers._

// 1
def add(x: Int, y: Int): Int = x + y
List(1, 2, 3).map(x => add(1, x)) shouldBe List(2, 3, 4)

// 2
val add_one: Int => Int = (x: Int) => add(x, 1)
List(1, 2, 3).map(add_one) shouldBe List(2, 3, 4)

// 3
// higher order function: a function that returns another function
def add2(x: Int): Int => Int = (y: Int) => x + y
List(1, 2, 3).map(add2(1)) shouldBe List(2, 3, 4)

// 4
val add2_one: Int => Int = add2(1)
List(1, 2, 3).map(add2_one) shouldBe List(2, 3, 4)

// 5 currying
def add_curried(x: Int)(y: Int) = x + y
List(1, 2, 3).map(add_curried(1)) shouldBe List(2, 3, 4)

// 6 partial application of a curried function
val add_curried_one: Int => Int = add_curried(1)
List(1, 2, 3).map(add_curried_one) shouldBe List(2, 3, 4)

// 7 scala auto-currying
val another_curried_add_one = (add _).curried(1)
List(1, 2, 3).map(another_curried_add_one) shouldBe List(2, 3, 4)

// 8 make your own curry function 
def curry[A, B, C](f: ((A, B) => C)): A => B => C = (a: A) => (b: B) => f(a, b)
val yet_another_curried_add_one = curry(add)(1)
List(1, 2, 3).map(yet_another_curried_add_one) shouldBe List(2, 3, 4)

// 9 application for dependency injection
type ResourceCheck[Resource] = Resource => Boolean

trait Repository[Id, Resource] {

  private var data = Map[Id, Resource]()

  def find(id: Id): Option[Resource] = {
    data.get(id)
  }

  def add(entry: (Id, Resource)): Unit = {
    data += entry

  }

}


val repo = new Repository[Int, String] {}

trait SomeService {
  def exists: Repository[Int, String] => ResourceCheck[String] => Int => Option[String] = { repo => checker => id =>
    repo.find(id).filter(checker)
  }
}

val service = new SomeService {}
repo.add(1 -> "1")
repo.add(2 -> "2")
val existsWithRepo = service.exists(repo)
val passThrough: ResourceCheck[String] = _ => true
val acceptOnlyOne: ResourceCheck[String] = _ == "1"
val f = existsWithRepo(passThrough)
f(0) shouldBe None
f(1) shouldBe Some("1")
f(2) shouldBe Some("2")
val g = existsWithRepo(acceptOnlyOne)
g(0) shouldBe None
g(1) shouldBe Some("1")
g(2) shouldBe None

//Injection of user but not seen by the Repository
case class User(attachedTo: String)

type ResourceCheckWithUser[Resource] =
User => ResourceCheck[Resource]
def restrictionToUser: ResourceCheckWithUser[String] =
  (user: User) => (resource: String) => user.attachedTo == resource
val user1 = User("1")
val user2 = User("2")
val h = existsWithRepo(restrictionToUser(user1))
h(0) shouldBe None
h(1) shouldBe Some("1")
h(2) shouldBe None
//use in other combinator
List(1, 2, 3).map(h)
val i = existsWithRepo(restrictionToUser(user2))
i(0) shouldBe None
i(1) shouldBe None
i(2) shouldBe Some("2")
List(1, 2, 3).map(i)

//A more complex(| realistic) case
object domain {

  case class BankId(value: Int)

  case class Bank(id: BankId, name: String) {
    override def toString = name
  }

  case class MerchantId(value: Int)

  case class Merchant(id: MerchantId, name: String, parent: BankId) {
    override def toString = name
  }

  sealed trait User

  case class BankUser(name: String, parent: BankId) extends User {
    override def toString = name
  }

  case class MerchantUser(name: String, parent: MerchantId) extends User {
    override def toString = name
  }

  case object AdminUser extends User

  type Acl[Resource] = User => ResourceCheck[Resource]
}

import domain._

val bankRepo = new Repository[BankId, Bank] {}
val merchantRepo = new Repository[MerchantId, Merchant] {}

trait RestService {

  private lazy val bankAcl: Acl[Bank] = user => bank => user match {
    case BankUser(_, parentId) => parentId == bank.id
    case MerchantUser(_, _) => false
    case AdminUser => true
    case _ => false
  }

  private lazy val merchantAcl: Acl[Merchant] = user => merchant => user match {
    case BankUser(_, parentId) => parentId == merchant.parent
    case MerchantUser(_, parentId) => parentId == merchant.id
    case AdminUser => true
    case _ => false
  }

  val findBank: Repository[BankId, Bank] => domain.User => BankId => Option[Bank] = repo => user => bankId =>
    repo.find(bankId).filter(bankAcl(user))
  val findMerchant: Repository[MerchantId, Merchant] => domain.User => MerchantId => Option[Merchant] = repo => user => merchantId =>
    repo.find(merchantId).filter(merchantAcl(user))
}

object RestService extends RestService

import play.api.libs.functional.syntax._

val bnpp = Bank(BankId(1), "BNPP")
val socgen = Bank(BankId(2), "SOCGEN")
val mcdo = Merchant(MerchantId(1), "MCDO", bnpp.id)
val sephora = Merchant(MerchantId(2), "SEPHORA", bnpp.id)
val starbucks = Merchant(MerchantId(3), "STARBUCKS", socgen.id)
bankRepo add (bnpp.id -> bnpp)
bankRepo add (socgen.id -> socgen)
merchantRepo add (mcdo.id -> mcdo)
merchantRepo add (sephora.id -> sephora)
merchantRepo add (starbucks.id -> starbucks)
val bnpAdmin = BankUser("BNPPAdmin", bnpp.id)
val sgAdmin = BankUser("SOCGENAdmin", socgen.id)
val mcdoAdmin = MerchantUser("Ronald", mcdo.id)
val sephoraAdmin = MerchantUser("Christopher", sephora.id)
val findBank = RestService.findBank(bankRepo)
val findMerchant = RestService.findMerchant(merchantRepo)
import fr.xebia.xke.fp2.Applicative
val listApplicative = new Applicative[List] {
  override def point[A](a: A): List[A] = List(a)

  override def ap[A, B](fa: List[A])(f: List[(A) => B]): List[B] = fa.flatMap(a => f.map(g => g(a)))
}
val users: List[domain.User] = List(bnpAdmin, sgAdmin, mcdoAdmin, sephoraAdmin)
val bankIds: List[domain.BankId] = List(bnpp, socgen).map(_.id)
val merchantIds: List[domain.MerchantId] = List(mcdo, sephora, starbucks).map(_.id)
//this line is necessary because our version is not curried!
val k: (domain.User, BankId) => Option[Bank] = Function.uncurried(findBank)
listApplicative.apply2(users, bankIds)(k) shouldBe List(
  Some(bnpp), //bnpAdmin
  None, //socgenAdmin
  None, //mcdoAdmin
  None, //sephoraAdmin
  None, //bnpAdmin
  Some(socgen), //socgenAdmin
  None, //mcdoAdmin
  None) //sephoraAdmin
val l: (domain.User, MerchantId) => Option[Merchant] = Function.uncurried(findMerchant)
listApplicative.apply2(users, merchantIds)(l) shouldBe List(
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
//FPair 2 is better and curried
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
//acl can be improved with partialFunction
type Restriction = PartialFunction[domain.User, Boolean]
type PartialAcl[Resource] = Resource => Restriction
type Acl[Resource] = domain.User => Resource => Boolean
object PartialAcl {

  private lazy val adminPassThrough: Restriction = {
    case AdminUser => true
  }

  def toAcl[Resource](acl: PartialAcl[Resource]): Acl[Resource] = {
    val f: (Resource) => (domain.User) => Boolean = (resource: Resource) => {
      val resourceRestriction: Restriction = acl(resource)
      val restrictionWithAdmin: Restriction = resourceRestriction.orElse(adminPassThrough)
      restrictionWithAdmin.lift.andThen(_.getOrElse(false))
    }
    flip(f)
  }

  private def flip[A, B, C](f: A => B => C): (B => A => C) =
    (b: B) => (a: A) => f(a)(b)

}

trait RestService2 {

  private lazy val bankAcl: PartialAcl[Bank] = (bank: Bank) => {
    case BankUser(_, parentId) if bank.id == parentId => true
  }
  private lazy val merchantAcl: PartialAcl[Merchant] = (merchant: Merchant) => {
    case BankUser(_, parentId) if parentId == merchant.parent => true
    case MerchantUser(_, parentId) if parentId == merchant.id => true
  }
  val findBank: Repository[BankId, Bank] => domain.User => BankId => Option[Bank] = repo => user => bankId =>
    repo.find(bankId).filter(PartialAcl.toAcl(bankAcl)(user))
  val findMerchant: Repository[MerchantId, Merchant] => domain.User => MerchantId => Option[Merchant] = repo => user => merchantId =>
    repo.find(merchantId).filter(PartialAcl.toAcl(merchantAcl)(user))
}

object RestService2 extends RestService2

val findBank2 = RestService2.findBank(bankRepo)
val findMerchant2 = RestService2.findMerchant(merchantRepo)
//FPair 2 is better is curried
listApplicative.fpair2(users)(bankIds)(findBank2) shouldBe List(
  (bnpAdmin, BankId(1), Some(bnpp)),
  (sgAdmin, BankId(1), None),
  (mcdoAdmin, BankId(1), None),
  (sephoraAdmin, BankId(1), None),
  (bnpAdmin, BankId(2), None),
  (sgAdmin, BankId(2), Some(socgen)),
  (mcdoAdmin, BankId(2), None),
  (sephoraAdmin, BankId(2), None)
)

listApplicative.fpair2(users)(merchantIds)(findMerchant2) shouldBe List(
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

println("done")