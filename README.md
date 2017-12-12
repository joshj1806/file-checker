# file-checker
 File-checker application returns duplicated files in the system. 
 It leverages parallelization using JVM folk/join or Spark.

# Design approach
 "Comparing checksums of files using a hash table and parallel
 computation"
 
 The application's execution flow is described below.
 1. Building a tree of file objects under the given directory.
 2. Calculating checksums of each file using parallel
  computation (JVM folk/join or Spark). The checksum and the file path consist
  a pair to be stored in the hash table. The checksums are used as the
  keys of the hash table. 
 3. If there are the same checksums in the hash table,
  printing out the file paths of the conflicted checksums.
  
 The application's complexity.
 Step 1 requires O(logN) time complexity to build a tree where N is number of files.
 Step 2 requires, O(N/c) time complexity. Calculation is linearly
 increased with the number of files where 'c' is the number of the CPU cores.
 Step 3 requires constant time.
 Therefore, overall time complexity of system is O(N/c * logN).
 
# Expected Inputs/Outputs
 Inputs: "directory name" e.g "/" for root.
 Outputs: Lists of duplicates files.

 e.g [("/Users/josh/test/f1.txt" "/Users/josh/test/sub-test/f1-copy.txt")\n
 ("/Users/josh/test/f2.txt" "/Users/josh/test/sub-test/f2-copy.txt")]

# Assumptions and Known limitations
 Soft links are not considered as a 'real' files.
 The system requires JRE.
 'Fold', parallel reducing stage is not leverage Folk/Join
 yet. (Future improvement) 
 
# Summary on how to run the test client and its pre-requisites
 1. Install JDK on a test machine.
 2. Install 'leiningen' to build the application.
 3. Steps to build is described below.
   $ lein compile
   $ lein uberjar
   $ java -jar file-checker-standalone.jar "/"
   
## License

Copyright © 2017 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
