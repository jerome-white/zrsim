#!/bin/bash

tmp=`mktemp`
cat <<EOF > $tmp
(defun emacs-format-function ()
   "Format the whole buffer."
   (c-set-style "java")
   (indent-region (point-min) (point-max) nil)
   (untabify (point-min) (point-max))
   (delete-trailing-whitespace)
   (save-buffer)
)
EOF

for i in `find . -name '*.java'`; do
    echo "emacs -batch $i -l $tmp -f emacs-format-function"
done | parallel --no-notice

rm $tmp
