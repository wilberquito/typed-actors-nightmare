/*
* This programs shows an example of how actors can communicate between them.
*/

package edu.udg.pda
package actors.greet

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class SaludadorPartial extends Actor {
  var salutacions = 0

  def receive: Receive = {

    // PartialFunction needs to define the methods `apply` and `isDefinedAt`
    new PartialFunction[Any, Unit] {
      override def apply(message: Any): Unit = message match {
        case HolaCadena(nom, c) =>
          println(s"Soc en " + self.path.name + s" hola $nom" + ". Vaig a saludar a en " + c.path.name)
          salutacions += 1
          c ! Hola(self.path.name)
        case Hola(nom) =>
          println(s"Soc en " + self.path.name + s" hola $nom" + " FI SALUTACIONS")
          salutacions += 1
        case Adeu() =>
          println(s"Soc en " + self.path.name + ": Adeu si t'en vas... He rebut " + salutacions + " salutacions!")
      }

      def isDefinedAt(message: Any): Boolean =
        message.isInstanceOf[Message]
    }
  }
}

object SalutacionsActorsPartial extends App {
  val actorSystem: ActorSystem = ActorSystem("sistema")

  val fulano: ActorRef = actorSystem.actorOf(Props[SaludadorPartial](), name = "fulanito")
  val mengano: ActorRef = actorSystem.actorOf(Props[SaludadorPartial](), name = "menganito")

  fulano ! HolaCadena("Main", mengano)
  mengano ! HolaCadena("Main", fulano)

  println("MAIN: Ja he enviat les salutacions encadenades... i esperare 1 segon abans de dir adeu...")

  mengano ! Chao(fulano.toString) // Unhandled message

  Thread.sleep(1000) // what if we comment this line?

  fulano ! Adeu()
  mengano ! Adeu()

  // Program is not finished because the parent actor, the actor system has not been terminated
  // > actorSystem.terminate()
}






