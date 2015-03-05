#!/bin/sh

FILENAME_BASE=ic_menu_pin_rotated_white
RESOURCES_DIR=../../project/shout/src/main/res

install_bitmap() {
    # $1 is filename base
    # $2 is dpi (ldpi, mdpi, etc)
    # $3 is resources dir
    BASE=$1
    DPI=$2
    RESDIR=$3

    INFILE=$BASE-$DPI.png
    OUTFILE=$RESDIR/drawable-$DPI/$BASE.png
    
    CMD="cp $INFILE $OUTFILE"
    echo $CMD
    $CMD
}

for DPI in ldpi mdpi hdpi xhdpi; do
    install_bitmap $FILENAME_BASE $DPI $RESOURCES_DIR
done
