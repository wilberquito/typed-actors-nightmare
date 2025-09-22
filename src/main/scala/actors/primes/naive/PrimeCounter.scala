package edu.udg.pda
package primes.naive

import scala.language.postfixOps

/*
* The program counts how many primes there are in a given range of integers.
*/

object PrimeCounter extends App {
  val max = 10000000

  def isPrime(n: Int): Boolean = new java.math.BigInteger("" + n).isProbablePrime(20)

  println(s"Anem a comptar primers, de 2 a $max")

  val t0: Long = System.nanoTime
  val nPrimes: Int = (2 to max) count isPrime

  println(s"Hi ha $nPrimes primers")
  println("S'ha trigat: " + (System.nanoTime - t0) / 1e9d + " segons")
}


