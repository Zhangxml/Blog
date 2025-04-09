#!/bin/bash
 
today=`date +%y%m%d_%T`
echo $today

echo dumpsys activity "$today"
adb shell dumpsys activity > "$today"_activity.txt

echo dumpsys window "$today"
adb shell dumpsys window windows > "$today"_window.txt

echo dumpsys SurfaceFlinger "$today"
adb shell dumpsys SurfaceFlinger > "$today"_SurfaceFlinger.txt

adb shell screencap -p /data/local/tmp/screen.png
adb pull  /data/local/tmp/screen.png  ./"$today"_screen.png