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

log=generate.jobs
rmlogs $log

for i in $root/trees/*; do
    ngrams=`basename $i .csv`
    job=`mktemp`
    pseudoterms=$root/pseudoterms

    echo -n "$ngrams "
    cat <<EOF > $job
#!/bin/bash

output=\$SLURM_JOBTMP/$ngrams

mkdir --parents \$output
java `jargs $memory $workers` exec.TermGenerator \
    $i $workers \$output

mkdir --parents $pseudoterms
tar \
    --create \
    --use-compress-prog=pbzip2 \
    --file=$pseudoterms/$ngrams.tar.bz \
    --directory=\$SLURM_JOBTMP \
    $ngrams
EOF

    sbatch \
	--mem=${memory}G \
	--time=$duration \
	--nodes=1 \
	--cpus-per-task=$workers \
	--workdir=`pwd` \
	--job-name=ptgen-$ngrams \
	$job
done > $log
