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
	m) memory=$OPTARG ;;
	t) duration=$OPTARG ;;
	d) root=$OPTARG ;; # $SCRATCH/zrt/wsj/2017_0719_184233
        h)
            exit
            ;;
        *) exit 1 ;;
    esac
done

log=generate.jobs
rmlog $log

for i in $root/trees/*; do
    echo -n "$i "
    
    job=`mktemp`
    ngrams=`printf "%02.f" $i`
    pseudoterms=`dirname $i`/pseudoterms

    cat <<EOF > $job
#!/bin/bash

java `jargs $memory $workers` generate.MakeTerms \
    $i $workers \$SLURM_JOBTMP/$ngrams

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
	--mail-type=END,FAIL \
	--mail-user=jsw7@nyu.edu \
	--nodes=1 \
	--cpus-per-task=$workers \
	--workdir=`pwd` \
	--job-name=ptgen-$i \
	$job
done > $log
