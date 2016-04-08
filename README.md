# gcwfa
Garbage Collection Workload for Android
= GCW for Android

GCW for Android is a GC intensive workload for android GC performance evaluation and analysis. it is different from the Java GCBench. and it is based on real-world applications. We studied memory allocation behavior of many real android applications, and developed GCW for Android based on the investigation result, and take it as one of the important workloads that help on GC analysis.

Real applications allocate varied size objects, out study show allocated object size distribution has almost same trend in most real android applications. Allocated objects have different lifetime in real applications, and their lifetime seems to have relationship to their size. And more and more real applications allocate objects parallel in multiple threads. The object size distribution, object lifetime and multi-threading allocation behavior have significant impact to the efficiency of design of runtime memory management and GC. GCW for Android is developed based on data collection and analysis of many typical android application in multiple categories. It tries to mimic object size distribution, object lifetime and the relationship between lifetime and size, multi-threading allocation behavior in real applications.

For more details see https://software.intel.com/en-us/android/articles/garbage-collection-workload-for-android

== Metrics

* The Primary metric of GCW for Android is total execution time.
  It includes object allocation time, GC relevant time in mutator thread, which can reflect GC efficiency and object allocation throughput.

* Other metrics include heap footprint and bytes allocated.
  Heap footprint is the size of private dirty pages consumed by zygote heap, reported by android.os.Debug.getMemInfo().
  Bytes allocated is reported as (java.lang.Runtime.getRuntime().totalMemory() - java.lang.Runtime.getRuntime().freeMemory()).

* The workload also reports total time and pause time of each GC activity.
  These data are gotten from Logcat, so only GC cycle whose pause time or total time is larger than threshold is reported.

== Requirements

In order to use GCW for Android, you will need to have an android device with android version equal or later than 4.0(SDK API level >= 14). Previous version of android was not guaranteed to sucessfully run this workload.

=== Build in Eclipse:
* The minimum android SDK version should be larger than 10.
* Start eclipse and click File -> import menu, the import dialog will pop up.
* Choose General -> Existing Projects into Workspace, and click next.
* Set the root directory to the path to .project file, GCW for Android will show up, select it and click finish.
* Now GCW for Android project is imported into eclipse, and eclipse will build this project automatically.


