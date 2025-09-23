package edu.udg.pda
package sorting

object FunctionalSortingAlgorithms {
  def quicksortf(xs: Array[Int]): Array[Int] =
    if xs.length <= 1 then xs
    else
      val pivot = xs(xs.length / 2)
      Array.concat(
        quicksortf(xs filter (x => pivot > x)), // define the whole lambda expression
        xs filter (_ == pivot), // use of lambda shorthand expression
        quicksortf(xs filter (pivot < _)) // use of lambda shorthand expression
      )
}
