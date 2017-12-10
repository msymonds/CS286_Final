# CS286_Final

This project examines the decrypted zodiac 408 cipher text as
a starting point to solving the zodiac 340 cipher.
A digraph is generated from 1M characters read in from
the Brown corpus. A second digraph is generated from
the number-mapped plain text of the decrypted z408 cipher.
The distance between the 2 digraphs is measured and a score
is generated as a benchmark.

A variant of Jakobsen's algorithm is then used to iterate through
column-permutations of the z408 text, generating a new digraph, and 
scoring against the brown digraph. The algorithm runs swapping
adjacent columns, then every 2, then every 3, etc... until either 
a score is found that is lower than the current best score, in which case
that becomes the new best score, the permutation is kept, and the swapping
process starts from the beginning, or until the first
and last columns are swaped and none of the swaps produce a
distance measurements from their digraphs lower than the best score 
which ends the run.

The scoring formula is calculated from a choice of 2 methods:
1. by summing the absolute value of the differences
taken from each corresponding pair of indices of the 2 digraphs.
2. A PCA model is trained at the beginning from the digraph of 
the brown corpus, then the digraph generated from each column-
permutation is scored against that model.

In each case, the lowest score at the end of the run is determined
to be the best candidate. If running this algorithm with random restarts,
The best score and order persists across restarts.

The output vectors of these tests will be examined in an attempt to find
a solution to the column-swapping problem for the z408 cipher and, by 
extension, the z340 cipher.
