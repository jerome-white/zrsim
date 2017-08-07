#!/bin/bash

#
# defaults
#
workers=20
memory=480
duration=6:00:00

while getopts "w:m:d:p:t:h" OPTION; do
    case $OPTION in
	w) workers=$OPTARG ;;
	m) memory=$OPTARG ;;
	d) duration=$OPTARG ;;
	p) posting=$OPTARG ;;
	t) trees=$OPTARG ;; # trees=$SCRATCH/zrt/wsj/2017_0719_184233/trees
        h)
            exit
            ;;
        *) exit 1 ;;
    esac
done

module try-load jdk/1.8.0_111
module try-load apache-ant/1.9.8
module try-load pbzip2/intel/1.1.13

ant clean compile || exit 1

for i in $trees/*; do
    echo -n "$i "
    
    job=`mktemp`
    ngrams=`printf "%02.f" $i`
    pseudoterms=`dirname $i`/pseudoterms

    cat <<EOF > $job
#!/bin/bash

java generate.MakeTerms \
    -Xmx`printf "%0.f" $(bc -l <<< "$memory * .95")`g \
    -XX:+UseParallelGC \
    -XX:ParallelGCThreads=`expr $workers / 2` \
    -classpath bin \
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
done > generate.jobs
