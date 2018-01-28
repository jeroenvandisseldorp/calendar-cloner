#!/bin/bash

SRCDIR="CalendarCloner"
SRCPKG="com.dizzl.android.CalendarCloner"
SRCNAME="Calendar Cloner"
DSTDIR="CalendarCloner FREE"
DSTPKG="com.dizzl.android.CalendarClonerFree"
DSTNAME="Calendar Cloner FREE"
SRCSRCDIR="$SRCDIR/src/`echo $SRCPKG | sed 's/\./\//g'`"
DSTSRCDIR="$DSTDIR/src/`echo $DSTPKG | sed 's/\./\//g'`"

function copyfiles {
	TOPDIR=`pwd`
	cd "$3"
	rm $2
	cd "$TOPDIR"
	cd "$1"
	for F in `ls -1 $2`; do
		echo "Creating $F"
		cat "$F" | sed "s/$SRCPKG/$DSTPKG/g" | sed "s/$SRCNAME/$DSTNAME/g" > "$TOPDIR/$3/$F"
	done
	cd "$TOPDIR"
}

copyfiles "$SRCSRCDIR" '*.java' "$DSTSRCDIR"
copyfiles "$SRCDIR/res/anim" '*' "$DSTDIR/res/anim"
copyfiles "$SRCDIR/res/drawable-hdpi" '*' "$DSTDIR/res/drawable-hdpi"
copyfiles "$SRCDIR/res/drawable-ldpi" '*' "$DSTDIR/res/drawable-ldpi"
copyfiles "$SRCDIR/res/drawable-mdpi" '*' "$DSTDIR/res/drawable-mdpi"
copyfiles "$SRCDIR/res/drawable-xhdpi" '*' "$DSTDIR/res/drawable-xhdpi"
copyfiles "$SRCDIR/res/layout" '*' "$DSTDIR/res/layout"
#copyfiles "$SRCDIR/res/layout-land" '*' "$DSTDIR/res/layout-land"
#copyfiles "$SRCDIR/res/menu" '*' "$DSTDIR/res/menu"
#copyfiles "$SRCDIR/res/raw" '*' "$DSTDIR/res/raw"
copyfiles "$SRCDIR/res/values" '*' "$DSTDIR/res/values"
copyfiles "$SRCDIR/res/values-nl" '*' "$DSTDIR/res/values-nl"
copyfiles "$SRCDIR/res/values-v11" '*' "$DSTDIR/res/values-v11"
copyfiles "$SRCDIR/res/values-v14" '*' "$DSTDIR/res/values-v14"
copyfiles "$SRCDIR/res/xml" '*' "$DSTDIR/res/xml"
copyfiles "$SRCDIR" "AndroidManifest.xml" "$DSTDIR"
