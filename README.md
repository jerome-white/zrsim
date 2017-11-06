# Zero Resource Term Discovery Simulator

Given a collection of documents, in which some of those documents are
queries, produces "term" output similar to what [ZRTools](https://github.com/arenjansen/ZRTools) might if
the collection were audio.

## Run

0. Compilation

   The current implementation of the simulator is written in Java. As
   such, it must first be compiled:

   ```bash
   $> ant clean compile
   ```

   See [build.xml](build.xml) for details.

1. N-gram extraction

   Text documents must first be parsed into variable length n-grams;
   [src/exec/NGramExtractor.java](src/exec/NGramExtractor.java) manages this process. It produces
   a single CSV file with the following format:

   > document,n-gram,offset

   where *offset* is the position within the *document* from where
   *n-gram* starts. The class can be run directly from the command
   line as follows:

   ```bash
   $> java -classpath bin exec.NGramExtractor a b c d e f
   ```

   where

   a. Path to the top level directory containing the documents. The
      program assumes all documents are in the same directory.

   b. Minimum n-gram length

   c. Maximum n-gram length

   d. File to which the output should be written. The output format is
      CSV.

   e. Number of CPU cores available to the process.

   f. Temporary directory that can be used as scratch space. On most
      UNIX systems, `$TMPDIR` is a suitable candidate.

2. Term generation

   Terms are generated based on the CSV file previously produced. To
   do so, equivalent n-grams are grouped and given a "name." Documents
   are then reproduced using these names;
   [src/exec/TermGenerator.java](src/exec/TermGenerator.java) manages this process. For every
   document in the original collection, a corresponding CSV document
   is produced:

   > term,n-gram,start,end

   where *term* is the name of that n-gram group. Columns n-gram and
   start correspond to n-gram and offset from in the extraction
   output. Where start denotes the start of the n-gram within the
   document, end denotes its end. While redundant, it is maintained
   for compatibility with older processing tools.

   Again, the term generator can be run directly from the command
   line:

   ```bash
   $> java -classpath bin exec.TermGenerator a b c
   ```

    where

    a. File containing extraction output.

    b. Number of CPU cores available to the process.

    c. Directory to where output files should go.

For both n-gram extraction and term generation there are convenience
scripts that take care of running the underlying Java programs and
archiving their output:

```bash
$> ./scripts/extract.sh -h
...
$> ./scripts/generate.sh -h
...
```

These scripts assume the user is deploying simulator within a
SLURM-based cluster computing environment. As such, many of its
arguments will be fed directly to sbatch, and many of the variables it
assumes may not be appropriate for your environment.

## References

1. "[Simulating Zero-Resource Spoken Term
   Discovery](https://doi.org/10.1145/3132847.3133160)"

   <pre>
   @inproceedings{white-cikm-2017,
    author = {White, Jerome and Oard, Douglas~W.},
    title = {Simulating Zero-Resource Spoken Term Discovery},
    booktitle = {International Conference on Information and Knowledge Management},
    year = {2017},
    pages = {},
    publisher = {ACM},
    doi = {10.1145/3132847.3133160},
   }
   </pre>

   Reproduction: The results in this publication were produced using
   an early Python-based version of the simulator. That version can
   found in a [separate repository](https://github.com/jerome-white/pyzrt); and in particular its [CIKM
   release]().
