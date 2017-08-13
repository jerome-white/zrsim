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
rmlogs $log

for i in $root/trees/*; do
    ngrams=`basename $i .csv`
    job=`mktemp`
    pseudoterms=$root/pseudoterms/$ngrams

    echo -n "$ngrams "
    cat <<EOF > $job
#!/bin/bash

output=\$SLURM_JOBTMP/$ngrams

mkdir --parents \$output
java `jargs $memory $workers` exec.MakeTerms \
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
	--mail-type=END,FAIL \
	--mail-user=jsw7@nyu.edu \
	--nodes=1 \
	--cpus-per-task=$workers \
	--workdir=`pwd` \
	--job-name=ptgen-$ngrams \
	$job
    exit
done > $log
