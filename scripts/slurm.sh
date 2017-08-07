#!/bin/bash

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
	d) duration=$OPTARG ;;
	t) trees=$OPTARG ;; # trees=$SCRATCH/zrt/wsj/2017_0719_184233/trees
        h)
            exit
            ;;
        *) exit 1 ;;
    esac
done

module try-load jdk/1.8.0_111
module try-load apache-ant/1.9.8
ant clean compile || exit 1

# rm --recursive --force $trees
# mkdir $trees
rm --force slurm-*

if [ ! $starting_ngram ]; then
    starting_ngram=$min_ngram
fi

for i in `seq $starting_ngram $max_ngram`; do
    output=$trees/`printf "%02.f" $i`.csv
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

ant slurm \
    -Dmin_ngram=$min_ngram \
    -Dmax_ngram=$i \
    -Dcorpus=\$SLURM_JOBTMP/corpus \
    -Doutput=$output
    -Dworkers=$workers
EOF

    sbatch \
	--mem=${memory}G \
	--time=$duration \
	--mail-type=END,FAIL \
	--mail-user=jsw7@nyu.edu \
	--nodes=1 \
	--cpus-per-task=$workers \
	--workdir=`pwd` \
	--job-name=zrcomp-$i \
	$job
done > jobs
