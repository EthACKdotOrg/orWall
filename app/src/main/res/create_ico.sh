#!/bin/sh
convert $1 -resize 144x drawable-xxhdpi/$(basename $1)
convert $1 -resize 96x drawable-xhdpi/$(basename $1)
convert $1 -resize 72x drawable-hdpi/$(basename $1)
convert $1 -resize 48x drawable-mdpi/$(basename $1)
