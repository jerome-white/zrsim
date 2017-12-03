#!/bin/bash

mload() {
    module try-load jdk/1.8.0_111
    module try-load apache-ant/1.9.8
    module try-load pbzip2/intel/1.1.13
    
    # ant clean compile || exit 1
}

rmlogs() {
    if [ -e $1 ]; then
	for i in $(rev $1 | cut --delimiter=' ' --fields=1 | rev); do
	    rm --force slurm-${i}.out
	done
    fi
}

jargs() {
    args=(
	Xmx$(printf "%0.f" $(bc -l <<< "$1 * .95"))g
	XX:+UseParallelGC
	XX:ParallelGCThreads=$(expr $2 / 2)
    )
    
    echo "-$(sed -e's/ / -/g' <<< ${args[@]}) -classpath bin"
}
