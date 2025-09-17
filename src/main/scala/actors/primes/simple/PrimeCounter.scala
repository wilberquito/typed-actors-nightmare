package primes.simple

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import scala.language.postfixOps

case class Compute(data: Seq[Int], summarizer: ActorRef)
case class Count(n: Int, t: Long)
case class Done(count: Int)

class PrimeCounter extends Actor {
  def isPrime(n: Int): Boolean = new java.math.BigInteger("" + n).isProbablePrime(20)

  def receive: Receive = {
    case Compute(data, summarizer) =>
      val nPrimes = data.count(isPrime)
      summarizer ! Done(nPrimes)
  }
}

class Summarizer extends Actor {
  private var nPrimes       = 0
  private var waitingGroups = 0
  private var t0: Long      = 0

  private def updateCount(n: Int): Unit = {
    waitingGroups += n
    if (waitingGroups == 0) {
      println("Hi ha " + nPrimes + " primers")
      println("S'ha trigat: " + (System.nanoTime - t0) / 1e9d + " segons")
      context.system.terminate()
    }
  }

  def receive: Receive = {
    case Count(n, t) =>
      updateCount(n)
      t0 = t
    case Done(np) =>
      nPrimes += np
      updateCount(-1)
  }
}

object PrimersActors extends App {
  val max = 10000000
  val nActors = 10
  val groupSize = max / nActors / 10
  val groups = ((2 to max) grouped groupSize).zipWithIndex.toSeq

  val actorSystem: ActorSystem = ActorSystem("prime-counter-app")

  val seqPrimeCounterActors: IndexedSeq[ActorRef] =
    for (i <- 0 until nActors) yield actorSystem.actorOf(Props[PrimeCounter](), "counter" + i)

  val summarizerActor: ActorRef = actorSystem.actorOf(Props[Summarizer](), "summarizer")

  val t0: Long = System.nanoTime // denotes the computation start

  summarizerActor ! Count(groups.length, t0)

  for ((g, i) <- groups) {
    val primeCounterActor: ActorRef = seqPrimeCounterActors(i % seqPrimeCounterActors.length)
    primeCounterActor ! Compute(g, summarizerActor)
  }

  println("tot enviat, esperant... a veure si triga en PACO")

  // Why this example successfully close the actor system?
}
