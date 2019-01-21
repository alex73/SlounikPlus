#!/bin/sh

for i in *.svg; do
    convert -background none $i -resize 18x18 `echo $i | sed -r 's/\.svg/\.png/'`
done
