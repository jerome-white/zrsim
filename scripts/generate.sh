#!/bin/bash

source `dirname $BASH_SOURCE`/library.sh || exit 1
mload

#
# defaults
#
workers=20
memory=480
duration=6:00:00

while getopts "w:m:t:d:h" OPTION; do
    case $OPTION in
	w) workers=$OPTARG ;;
	r) memory=$OPTARG ;;
	t) duration=$OPTARG ;;
	d) root=$OPTARG ;; # $SCRATCH/zrt/wsj/2017_0719_184233
        h)
	    cat <<EOF
A convenience script for running the term generator within a
SLURM-based cluster.

Usage: $0 [options]
  -w Number of CPU cores to make available to the JVM.

  -r Amount of memory to make available to the JVM.

  -t Amount of time the JVM is allowed to run.

  -d Directory to which the output should go.

Options -r and -t should be specified in a format that SLURM can
understand. See the sbatch manpage for details.
EOF
            exit
            ;;
        *) exit 1 ;;
    esac
done

# log=generate.jobs
# rmlogs $log
sandbox=`mktemp --directory --tmpdir=$BEEGFS`

for i in $root/trees/*; do
    ngrams=`basename --suffix=.csv $i`

    output=$sandbox/$ngrams
    pseudoterms=$root/pseudoterms
    for j in $pseudoterms $output; do
	mkdir --parents $j
    done

    job=`mktemp`

    echo -n "$ngrams $job "
    cat <<EOF > $job
#!/bin/bash

java `jargs $memory $workers` exec.TermGenerator \
    $i $workers $output

tar \
    --create \
    --use-compress-prog=pbzip2 \
    --file=$pseudoterms/$ngrams.tar.bz \
    --directory=$sandbox \
    $ngrams

rm --recursive --force $output
EOF

    sbatch \
	--mem=${memory}G \
	--time=$duration \
	--nodes=1 \
	--cpus-per-task=$workers \
	--workdir=`pwd` \
	--job-name=ptgen-$ngrams \
	$job
done
