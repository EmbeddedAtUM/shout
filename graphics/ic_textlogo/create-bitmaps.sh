#!/bin/sh

FILENAME_BASE=ic_textlogo

export_bitmap() {
    # $1 is filename base
    # $2 is dpi (ldpi, mdpi, etc.)
    # $3 is resolution (36. 48, etc.)
    BASE=$1
    DPI=$2
    RES=$3

    INFILE=$BASE.svg
    OUTFILE=${BASE}-${DPI}.png

    inkscape --export-png=$OUTFILE --export-height=$RES $INFILE
}

IFS_OLD=$IFS
IFS=','
for r in ldpi,36 mdpi,48, hdpi,72, xhdpi,96; do
    set -- $r  # read tuple as $1, $2, etc, splitting on $IFS
    export_bitmap $FILENAME_BASE $1 $2
done
IFS=$OLD_IFS
