/*
* This programs shows an example of how actors can communicate between them.
*/
package edu.udg.pda
package actors.greet

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class Saludador extends Actor {
  var salutacions = 0

  // Type `Receive` is actually sinonym of `PartialFunction[Any, Unit]`
  def receive: Receive = {
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
}

object SalutacionsActors extends App {
  val actorSystem: ActorSystem = ActorSystem("sistema")

  val fulano: ActorRef = actorSystem.actorOf(Props[Saludador](), name = "fulanito")
  val mengano: ActorRef = actorSystem.actorOf(Props[Saludador](), name = "menganito")

  fulano ! HolaCadena("Main", mengano)
  mengano ! HolaCadena("Main", fulano)

  mengano ! Chao(fulano.toString) // Unhandled message...

  println("MAIN: Ja he enviat les salutacions encadenades... i esperare 1 segon abans de dir adeu...")

  Thread.sleep(1000) // what if we comment this line?

  fulano ! Adeu
  mengano ! Adeu

  // Program is not finished because the parent actor, the actor system has not been terminated
  // > actorSystem.terminate()
}
