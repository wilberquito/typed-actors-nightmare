package edu.udg.pda
package sorting

object ImperativeSortingAlgorithms {
    private def _swap(i: Int, j: Int, xs: Array[Int]): Unit = {
      val t = xs(i)
      xs(i) = xs(j)
      xs(j) = t
    }

    private def _quicksort(l: Int, r: Int, xs: Array[Int]): Unit = {
      val pivot = xs((l + r) / 2)
      var i = l
      var j = r
      while (i <= j) {
        while (xs(i) < pivot) i += 1
        while (xs(j) > pivot) j -= 1
        if (i <= j) {
          _swap(i, j, xs)
          i += 1
          j -= 1
        }
      }
      if (l < j) _quicksort(l, j, xs)
      if (i < r) _quicksort(i, r, xs)
    }

    def quicksort(xs: Array[Int]): Unit = {
      _quicksort(0, xs.length - 1, xs)
    }
}
