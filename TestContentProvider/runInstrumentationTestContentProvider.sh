START_LOCATION=`pwd`
APP_LOCATION=/home/tomek/android/system/device/tj/common/src/apps/TestContentProvider

# Replace orginal ContentProvider
cp $APP_LOCATION/mock/src/com/test/testcontentprovider/TestContentProvider.java $APP_LOCATION/src/com/test/testcontentprovider/TestContentProvider.java

# Make and install TEST ContentProvider
make TestContentProvider showcommands
adb remount
adb sync

# Make instrumentation
rm -f out/target/product/generic/data/app/TestContentProvider.test.apk		# remove previous one
make TestContentProvider.test showcommands
adb install -r out/target/product/generic/data/app/TestContentProvider.test.apk
adb shell am instrument -w com.test.testcontentprovider.test/android.test.InstrumentationTestRunner

# Revert all changes concern TestContentProvider
cd $APP_LOCATION
#git reset --hard HEAD
#git clean -f
git checkout ./src/com/test/testcontentprovider/TestContentProvider.java

# Make and install TEST ContentProvider (revert orginal app)
cd $START_LOCATION
make TestContentProvider showcommands
adb remount
adb sync

