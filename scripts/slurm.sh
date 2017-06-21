#!/bin/bash

min_ngram=4
while getopts "n:m:h" OPTION; do
    case $OPTION in
	n) min_ngram=$OPTARG ;;
	m) max_ngram=$OPTARG ;;
        h)
            exit
            ;;
        *) exit 1 ;;
    esac
done

module try-load jdk
module try-load apache-ant
ant clean compile

trees=$SCRATCH/zrt/wsj/2017_0615_175330/trees
# rm --recursive --force $trees
# mkdir $trees
rm --force slurm-*

for i in `seq $min_ngram $max_ngram`; do
    output=$trees/`printf "%02.f" $i`.csv
    if [ -e $output ]; then
	continue
    fi
    echo -n "$i "
    job=`mktemp`

    cat <<EOF > $job
#!/bin/bash

# module try-load jdk
# module try-load apache-ant

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
EOF

    sbatch \
	--mem=240G \
	--time=24:00:00 \
	--mail-type=END,FAIL \
	--mail-user=jsw7@nyu.edu \
	--nodes=1 \
	--cpus-per-task=20 \
	--workdir=$HOME/src/zrsim \
	--job-name=zrsim-$i \
	$job
done > jobs
