package edu.udg.pda
package primes.complex

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.*
import scala.language.postfixOps
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import akka.util.Timeout

import scala.language.postfixOps

trait ActorMessage

case class Count(group: Seq[Int]) extends ActorMessage

case class Agg(n: Int) extends ActorMessage

case class Done() extends ActorMessage

case class Start() extends ActorMessage

case class Query() extends ActorMessage

case class Answer(n: Int) extends ActorMessage

case class AnswerRange(a: Int, b: Int, n: Int) extends ActorMessage


class AggActor extends Actor with ActorLogging {
  var nPrimes = 0

  def receive: Receive = {
    case Agg(n) => nPrimes += n
    case Query() => sender() ! Answer(nPrimes)
  }
}

class CounterActor(val register: ActorRef) extends Actor with ActorLogging  {
  def isPrime(n: Int): Boolean =
    new java.math.BigInteger("" + n).isProbablePrime(20)

  def receive: Receive = {
    case Count(group: Seq[Int]) =>
      val nPrime = group count isPrime
      register ! Agg(nPrime)
      sender() ! Done()
  }
}

class Master(val nCounters: Int) extends Actor with ActorLogging {
  // Actor ref to accumulate the number of primes
  private val aggActor: ActorRef =
    context.actorOf(Props(new AggActor), "counterRegister")

  // Set of actors ref that actually count the primes in a chunk
  private val counters: IndexedSeq[ActorRef] =
    for (i <- 1 to nCounters) yield context.actorOf(Props(new CounterActor(aggActor)), s"primeCounter-$i")
  private val countersKeys          = (for (i <- 1 to nCounters) yield s"primeCounter-$i").toSet
  private var countersLeft          = counters.length

  // Chunk of groups
  private val max: Int              = 10000000                     // 10 millions
  private val groupSize: Int        = (max / nCounters) / 10       // Chunks split in 10 groups
  private val groups: Seq[Range]    = ((1 to max) grouped groupSize).toSeq // Group of numbers
  private var groupsLeft: Int       = groups.length

  private var replyTo: ActorRef     = null // non-actor, from non-actor code
  private var nPrimes: Int          = -1

  override def preStart(): Unit =  {
    log.info("Watching children actors...")
    // Monitoring actors
    context.watch(aggActor)
    for (counter <- counters) context.watch(counter)

  }

  def receive: Receive = {
    case Start() => {
      replyTo  = sender()
      log.info(s"Starting master actor - groups left: $groupsLeft")
      // Sending to counters the corresponding chunk
      for ((g, i) <- groups zipWithIndex) counters(i % nCounters) ! Count(g)
    }
    case Done() => {
      log.info(s"Done - groups left: $groupsLeft")
      groupsLeft -= 1
      if (groupsLeft == 0) {
          log.info(s"Poising counters - counters left: $countersLeft")
          for (counter <- counters) counter ! PoisonPill
      }
    }
    case Terminated(actor) if actor == aggActor => {
      log.info("Replying to Main")
      replyTo ! AnswerRange(1, max, nPrimes)
    }
    case Terminated(actor) if countersKeys contains actor.path.name => {
      countersLeft -= 1
      log.info(s"Poising counters - counters left: $countersLeft")
      if (countersLeft == 0) {

        // Thread awaiting
        log.info(s"Querying number of primes")
        implicit val timeout: Timeout = Timeout(5.seconds)

        // Use ? to get a Future
        val future: Future[Any] = aggActor ? Query()
        val result: Answer = Await.result(future, timeout.duration).asInstanceOf[Answer]
        nPrimes = result.n

        log.info(s"Poising register")
        aggActor ! PoisonPill
      }
    }
  }

}

object Main {
  def main(args: Array[String]): Unit = {

    val system: ActorSystem = ActorSystem("prime-system")
    val master: ActorRef = system.actorOf(Props(new Master(10)), "master")

    implicit val timeout: Timeout = Timeout(30.seconds)


    val t0: Long = System.nanoTime

    // Use ? to get a Future
    val future: Future[Any] = master ? Start()
    val result: AnswerRange = Await.result(future, timeout.duration).asInstanceOf[AnswerRange]

    println(s"Main got reply: $result")
    println(s"It took ${ (System.nanoTime - t0) / 1e9d + " seconds" }")

    system.terminate()
  }
}
