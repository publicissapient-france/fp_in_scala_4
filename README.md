#Functions

Functions are meant to be small, testable, side-effect free and reusable. 

We'll study some core principles that enable to build large programs from smalls functions:
 
* function composition
* curried function
* partially applied function
* partial function


You can launch exercises with the following command. 
It will run specific unit tests and display an html reports. All explanation are given in this report. 
Once all tests are green, pass to the next one.

```
    sbt exo1
```

#CompositionSpec
##Function composition
In this section, we'll study function composition. 
It's the fusion of a function *f* of type `A ⇨ B` and a function *g* of type `B ⇨ C` to a brand new function *h* of type `A ⇨ C`.

*f* and *g* are independent and can be consistent and reusable.

There are *two* combinators on Functions:

* `andThen`: combines `A ⇨ B` with `B ⇨ C` to give `A ⇨ C`
* `compose`: combines `B ⇨ C` with `A ⇨ B` to give `A ⇨ C` 

Fusions are usefull to build large functions from smallers ones.
They are also usefull to replace multiple calls to a *map* combinator and optimize call.
For *map* on *List*, it avoids to create a new iteration loop, on *Future* it avoid to post a new asynchronous call to a new *Thread*.

With type safety, this allow programmers to build secured pipeline of data processing which well tested and easily extended.

###Tests

```
    sbt exo1
```

#CurryingSpec
##Currification
We are going to explore function currification. This is one core principal of function compositions.

We are going to code an `add` function. It's a simple function that takes 2 `Integer` and returns the sum. 
It is composed of 2 input arguments. We say that this function has an *arity* of 2.

Currification of functions is a process that change a function of arity n greater than 1 to n equivalent functions of 1 argument.

For `add` example, we pass from `(Int,Int) ⇨ Int` to `Int ⇨ Int ⇨ Int`. The curried function takes 1 `Int` as input and returns another function that takes another `Int` and finally returns an `Int`.

We call *higher order function* a function that returns another function.

###Tests

```
    sbt exo2
```


##Partial application

The advantage of curried function is that you can apply the different argument at different stage of your program.

For example, you have a `List[Int]` and you want to `map` a function over it, you need a function of type `Int ⇨ A`.

`add` is of type `Int ⇨ Int ⇨ Int`. If we apply only one argument of type `Int`, we have a function of type `Int ⇨ Int`. Thus it can be mapped on a list of `Int`.

###Tests

```
    sbt exo3
```

#CurriedServiceSpec
##Currification for dependency injection
We are going to see currification in action in a real-world code. 
Because we can partially applied a curried function, we are going to use this feature for dependency injection.

We have a *repository* of entities. We have a *RestService* that uses a *repository* and provide a lookup method with visibility checks (ACL).
When a user tries to find an element from the *repository*, the *RestService* checks the user visibility before sending data.
For dependency injection, instead of injecting a *repository* in the *service*, it is the *service* function that requires the *repository* through an higher order function.

##Types
###Bank
A *bank* is a domain element at the node in a hierarchy. It has a *name*.

###Merchant
A *merchant* is a domain element which is a leaf in the hierarchy. It has a *name* and is attached to a *bank*.

###ResourceCheck[Resource]
This is an alias for a function of type `Resource ⇨ Boolean`. It tells to the service if a resource loaded from the repository is visible in the current context or not.
    
`RestService#findBank` is a function of type `Repository[BankId, Bank] ⇨ ResourceCheck[Bank] ⇨ BankId ⇨ Option[Bank]`.
We can apply value `repo` to "inject" this value in the function.
We now have a function of type `ResourceCheck[Bank] ⇨ BankId ⇨ Option[Bank]`.

Then we can do the same for `ResourceCheck` to be able to use the `findBank` function.

###Tests

```
    sbt exo4
```

##Extension
Our service requires a function of type `Resource ⇨ Boolean` to check the visibility of an element. Here we want to add the control depending on the visibility of a user.
Thanks to partial application, we can add this notion of `User` without changing the service signature.

##Types
###User
A user can be of type:

* `BankUser`: this user is attached to a *bank* and can see his own *bank* or *every merchants* attached to this *bank*.
* `MerchantUser`: this user is attached to a *merchant* and can only see his own *merchant*.
* `AdminUser`: this user can see every *banks* and every *merchants*.

###Acl
We can combine type aliases. Here `Acl[Resource]` is a function of type `User ⇨ Resource ⇨ Boolean`.
`Acl` is a sort of *factory* of `ResourceCheck` thanks to partial application of the `User`.

Without modifying the service we can add the user as a dependency and injecting it by partial application before using it in the service.

###Note
We previously have seen that functions can be used with existing others, for example *map*.
There is an implementation of our *Applicative* type class to provide magic method to in one call test every combination of users and banks/merchants!
But `apply2` is not a curried function but a function of type `(A,B) ⇨ C`. Try to use it first.
Next, use `fpair2` which is curried ;-)

###Tests

```
    sbt exo5
```

#PartialFunctionSpec
We are going improve our *ACL* system. We've seen some repetition for common case (false by default and admin always true).

We can also see that the specific part defines a subset of the input argument values (*User*).

There is a tool for that, *PartialFunction*. *PartialFunction* are different from *partially applied function*.
In functional programing, there is no exception.
If a function takes a *Int* as an input parameter, it must returns a value for *all* possible values of *Int*.
A *PartialFunction* is explicitly defined on a *subset* of the values of its input argument.

*PartialFunction* contains additional methods on the top of classic `apply` Function method:

* `isDefinedAt`: it returns `true` if the given argument is in the defined space of the function. If it's `false`, the call of `apply` will throw an exception.
* `orElse`: it delegates to another *PartialFunction* its execution when it the given argument to `apply` is not in its defined space.
* `lift`: it transforms a `PartialFunction[A,B]` to a function of type `A => Option[B]`.

##Types
###Restriction
It is a `PartialFunction[User, Boolean]`. That means that it returns a value only for some values of type `User`.

###PartialAcl[Resource]
It is an alias for `Resource ⇨ PartialFunction[User, Boolean]`

##Note
For some convenience, you'll see that there is an inversion of types.

    type Acl[Resource]        = User ⇨ Resource ⇨ Boolean
    type PartialAcl[Resource] = Resource ⇨ User ⇨ Boolean

We'll have to flip the two first arguments at a moment!

###Tests

```
    sbt exo6
    sbt exo7
    sbt exo8
    sbt exo9
```