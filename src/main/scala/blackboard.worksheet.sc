import edu.udg.pda.sorting.ImperativeSortingAlgorithms.quicksort
import edu.udg.pda.sorting.FunctionalSortingAlgorithms.quicksortf

println("hola")

2+2

// Sorting an array. Mutating its content...
val xs: Array[Int] = Array(4, 2, 3, 5, 6, 10, 1, 15, 25, 7)
quicksort(xs)
println(xs.mkString(","))

// Generates a sorted array `zs` from the unsorted array `ys`
val ys: Array[Int] = Array(4, 2, 3, 5, 6, 10, 1, 15, 25, 7)
val zs = quicksortf(xs)
println(zs.mkString(","))
