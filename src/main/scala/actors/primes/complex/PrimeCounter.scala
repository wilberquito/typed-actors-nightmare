package primes.complex

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import akka.routing.{Broadcast, RoundRobinPool}
import scala.language.postfixOps

case class Compute(data: Seq[Int])
case class Done(count: Int)
case object Processed

class Summarizer extends Actor {
  var nPrimes = 0

  def receive: Receive = {
    case Done(np) => nPrimes += np
  }

  override def postStop(): Unit = {
    println("Hi ha " + nPrimes + " primers")
  }
}

class Counter(accumulator: ActorRef) extends Actor {
  var nPrimes = 0

  def isPrime(n: Int): Boolean =
    new java.math.BigInteger("" + n).isProbablePrime(20)

  def receive: Receive = {
    case Compute(data: Seq[Int]) =>
      nPrimes += data.count(isPrime)
      sender() ! Processed
  }

  override def postStop(): Unit = {
    accumulator ! Done(nPrimes)
  }
}

class Generator(nCounters: Int, counter: ActorRef) extends Actor  {
  private val max: Int              = 10000000
  private val groupSize: Int        = max / nCounters / 10
  private val groups: Seq[Range]    = (2 to max).grouped(groupSize).toSeq
  private var waitingProcessed: Int = groups.length

  override def preStart(): Unit = {
    for (g <- groups) counter ! Compute(g)
  }

  def receive: Receive = {
    case Processed => updateWaiting(-1)
  }

  private def updateWaiting(n: Int): Unit = {
    waitingProcessed += n
    if (waitingProcessed <= 0) context.stop(self) // stops the current actor
  }
}

class Master extends Actor {
  private val t0: Long = System.nanoTime
  private val nCounters = 10

  private val accumulator: ActorRef = context.actorOf(Props(new Summarizer), "accumulator")
  private val counters: ActorRef    = context.actorOf(Props(new Counter(accumulator)).withRouter(RoundRobinPool(nCounters)), "counters")
  private val generator: ActorRef   = context.actorOf(Props(new Generator(nCounters, counters)), "generator")

  override def preStart(): Unit =  {
    context.watch(accumulator) // monitor actor's terminate state
    context.watch(counters)
    context.watch(generator)
  }

  def receive: Receive = {
    case Terminated(`generator`)    => counters ! Broadcast(PoisonPill) // send a kill broadcast message
    case Terminated(`counters`)     => accumulator ! PoisonPill // send a kill message
    case Terminated(`accumulator`)  => context.system.terminate() // terminate the system
  }

  override def postStop(): Unit = {
    println("S'ha trigat: " + (System.nanoTime - t0) / 1e9d + " segons")
  }
}

object PrimeCounterApp extends App  {
  val system = ActorSystem("primer-counter-app")
  system.actorOf(Props[Master](), "master")

  // KEY POINTS:
  // > Routing
  // > Broadcast
  // > PoisonPill
  // > context.stop(self)
  // > context.system.terminate()

  println("tot enviat, esperant... a veure si triga en PACO")
}
