package edu.udg.pda
package actors.greet

import akka.actor.ActorRef

trait Message

case class HolaCadena(nom: String, cont: ActorRef) extends Message

case class Hola(nom: String) extends Message

case class Adeu() extends Message

case class Chao(nom: String) // Not instance of `Message`

