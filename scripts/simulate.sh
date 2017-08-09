#!/bin/bash

source `dirname $BASH_SOURCE`/library.sh || exit 1
mload

#
# defaults
#
workers=20
min_ngram=4
memory=480
duration=6:00:00

while getopts "n:m:s:w:r:d:t:h" OPTION; do
    case $OPTION in
	n) min_ngram=$OPTARG ;;
	m) max_ngram=$OPTARG ;;
	s) starting_ngram=$OPTARG ;;
	w) workers=$OPTARG ;;
	r) memory=$OPTARG ;;
	t) duration=$OPTARG ;;
	d) root=$OPTARG ;; # $SCRATCH/zrt/wsj/2017_0719_184233
        h)
            exit
            ;;
        *) exit 1 ;;
    esac
done

if [ ! $starting_ngram ]; then
    starting_ngram=$min_ngram
fi

log=simulate.jobs
rmlogs $log

for i in `seq $starting_ngram $max_ngram`; do
    output=$root/trees/`printf "%02.f" $i`.csv
    if [ -e $output ]; then
	continue
    fi
    echo -n "$i "
    job=`mktemp`

    cat <<EOF > $job
#!/bin/bash

tar \
    --extract \
    --bzip \
    --directory=\$SLURM_JOBTMP \
    --file=$SCRATCH/zrt/corpus.tar.bz

java `jargs $memory $workers` simulate.Simulator \
    \$SLURM_JOBTMP/corpus $min_ngram $i $output $workers

EOF

    sbatch \
	--mem=${memory}G \
	--time=$duration \
	--mail-type=END,FAIL \
	--mail-user=jsw7@nyu.edu \
	--nodes=1 \
	--cpus-per-task=$workers \
	--workdir=`pwd` \
	--job-name=zrsim-$i \
	$job
done > $log
