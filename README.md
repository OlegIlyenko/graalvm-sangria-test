
This repo is an attempt to reproduce the issue with GraalVM CE 1.0.0-rc1 with results in `java.lang.InternalError` coming from `AtomicReferenceFieldUpdater`:

https://github.com/oracle/graal/issues/406

Prerequisites:

* [GraalVM CE 1.0.0-rc1](https://www.graalvm.org/downloads/)
* [Sbt](https://www.scala-sbt.org/download.html)
* [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (for sbt)

I prepared a script [create-native-image.sh](https://github.com/OlegIlyenko/graalvm-sangria-test/blob/master/create-native-image.sh) that you can just run to do all the steps that reproduce the problem (it compiles and packages Sbt project, executes native-image and runs the resulting executable):

```bash
chmod a+x create-native-image.sh
./create-native-image.sh
```

The reflection use (by scala's `TrieMap`) is configured in [reflection.json](https://github.com/OlegIlyenko/graalvm-sangria-test/blob/master/reflection.json). 

The image compilation is successful, but following exception is thrown:

```
Exception in thread "main" java.lang.reflect.InvocationTargetException
	at java.lang.Throwable.<init>(Throwable.java:310)
	at java.lang.Exception.<init>(Exception.java:102)
	at java.lang.ReflectiveOperationException.<init>(ReflectiveOperationException.java:89)
	at java.lang.reflect.InvocationTargetException.<init>(InvocationTargetException.java:72)
	at com.oracle.svm.reflect.proxies.Proxy_1_Main_main.invoke(Unknown Source)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at com.oracle.svm.core.JavaMainWrapper.run(JavaMainWrapper.java:199)
	at Lcom/oracle/svm/core/code/CEntryPointCallStubs;.com_002eoracle_002esvm_002ecore_002eJavaMainWrapper_002erun_0028int_002corg_002egraalvm_002enativeimage_002ec_002etype_002eCCharPointerPointer_0029(generated:0)
Caused by: java.lang.InternalError
	at java.lang.Throwable.<init>(Throwable.java:250)
	at java.lang.Error.<init>(Error.java:58)
	at java.lang.VirtualMachineError.<init>(VirtualMachineError.java:43)
	at java.lang.InternalError.<init>(InternalError.java:42)
	at sun.reflect.Reflection.ensureMemberAccess(Reflection.java:94)
	at sun.reflect.misc.ReflectUtil.ensureMemberAccess(ReflectUtil.java:103)
	at java.util.concurrent.atomic.AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl.<init>(AtomicReferenceFieldUpdater.java:327)
	at java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdater.java:110)
	at scala.collection.concurrent.TrieMap.<init>(TrieMap.scala:647)
	at scala.collection.concurrent.TrieMap.<init>(TrieMap.scala:652)
	at scala.collection.concurrent.TrieMap$.empty(TrieMap.scala:981)
	at scala.collection.concurrent.TrieMap$.empty(TrieMap.scala:976)
	at scala.collection.generic.MutableMapFactory.newBuilder(MutableMapFactory.scala:30)
	at scala.collection.generic.GenMapFactory.apply(GenMapFactory.scala:48)
	at sangria.validation.SchemaBasedDocumentAnalyzer.<init>(SchemaBasedDocumentAnalyzer.scala:17)
	at sangria.validation.ValidationContext.<init>(QueryValidator.scala:136)
	at sangria.validation.RuleBasedQueryValidator.validateQuery(QueryValidator.scala:61)
	at sangria.execution.Executor.$anonfun$execute$1(Executor.scala:79)
	at sangria.execution.Executor$$Lambda$461/1872166244.apply(Unknown Source)
	at sangria.execution.TimeMeasurement$.measure(TimeMeasurement.scala:8)
	at sangria.execution.Executor.execute(Executor.scala:79)
	at sangria.execution.Executor$.execute(Executor.scala:199)
	at Main$.main(Main.scala:17)
	at Main.main(Main.scala)
	... 4 more
```

Here is the full image compilation output:

```
Executing [
/home/tenshi/graal/graalvm-1.0.0-rc1/bin/java \
-Xbootclasspath/a:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/boot/graaljs-scriptengine.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/boot/graal-sdk.jar \
-cp \
/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/svm/builder/svm.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/svm/builder/objectfile.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/svm/builder/pointsto.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/jvmci/graal.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/jvmci/jvmci-api.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/jvmci/jvmci-hotspot.jar \
-server \
-d64 \
-noverify \
-XX:+UnlockExperimentalVMOptions \
-XX:+EnableJVMCI \
-XX:-UseJVMCIClassLoader \
-XX:+UseJVMCICompiler \
-Dgraal.CompileGraalWithC1Only=false \
-XX:CICompilerCount=2 \
-Dgraal.VerifyGraalGraphs=false \
-Dgraal.VerifyGraalGraphEdges=false \
-Dgraal.VerifyGraalPhasesSize=false \
-Dgraal.VerifyPhases=false \
-Dgraal.EagerSnippets=true \
-Xss10m \
-Duser.country=US \
-Duser.language=en \
-Dsubstratevm.version=68c7c1073a86a3d541ffb82434acc664f3096079:substratevm \
-Dgraalvm.version=1.0.0-rc1 \
-Dorg.graalvm.version=1.0.0-rc1 \
-Dcom.oracle.graalvm.isaot=true \
-Djvmci.class.path.append=/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/jvmci/graal.jar \
-Xmx3349266432 \
-Xms1g \
com.oracle.svm.hosted.NativeImageGeneratorRunner \
-imagecp \
/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/boot/graaljs-scriptengine.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/boot/graal-sdk.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/svm/builder/svm.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/svm/builder/objectfile.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/svm/builder/pointsto.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/jvmci/graal.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/jvmci/jvmci-api.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/jvmci/jvmci-hotspot.jar:/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/svm/library-support.jar:/media/sf_share/foo/lib_managed/bundles/com.chuusai/shapeless_2.12/shapeless_2.12-2.3.2.jar:/media/sf_share/foo/lib_managed/bundles/org.scala-lang.modules/scala-xml_2.12/scala-xml_2.12-1.0.6.jar:/media/sf_share/foo/lib_managed/jars/io.circe/circe-core_2.12/circe-core_2.12-0.9.1.jar:/media/sf_share/foo/lib_managed/jars/io.circe/circe-numbers_2.12/circe-numbers_2.12-0.9.1.jar:/media/sf_share/foo/lib_managed/jars/jline/jline/jline-2.14.5.jar:/media/sf_share/foo/lib_managed/jars/org.parboiled/parboiled_2.12/parboiled_2.12-2.1.4.jar:/media/sf_share/foo/lib_managed/jars/org.sangria-graphql/macro-visit_2.12/macro-visit_2.12-0.1.1.jar:/media/sf_share/foo/lib_managed/jars/org.sangria-graphql/sangria-circe_2.12/sangria-circe_2.12-1.2.1.jar:/media/sf_share/foo/lib_managed/jars/org.sangria-graphql/sangria-marshalling-api_2.12/sangria-marshalling-api_2.12-1.0.1.jar:/media/sf_share/foo/lib_managed/jars/org.sangria-graphql/sangria-streaming-api_2.12/sangria-streaming-api_2.12-1.0.0.jar:/media/sf_share/foo/lib_managed/jars/org.sangria-graphql/sangria_2.12/sangria_2.12-1.4.0.jar:/media/sf_share/foo/lib_managed/jars/org.scala-lang/scala-compiler/scala-compiler-2.12.4.jar:/media/sf_share/foo/lib_managed/jars/org.scala-lang/scala-library/scala-library-2.12.4.jar:/media/sf_share/foo/lib_managed/jars/org.scala-lang/scala-reflect/scala-reflect-2.12.4.jar:/media/sf_share/foo/lib_managed/jars/org.typelevel/cats-core_2.12/cats-core_2.12-1.0.1.jar:/media/sf_share/foo/lib_managed/jars/org.typelevel/cats-kernel_2.12/cats-kernel_2.12-1.0.1.jar:/media/sf_share/foo/lib_managed/jars/org.typelevel/cats-macros_2.12/cats-macros_2.12-1.0.1.jar:/media/sf_share/foo/lib_managed/jars/org.typelevel/machinist_2.12/machinist_2.12-0.6.2.jar:/media/sf_share/foo/lib_managed/jars/org.typelevel/macro-compat_2.12/macro-compat_2.12-1.1.1.jar:/media/sf_share/foo/target/scala-2.12/graalvm-sangria-test_2.12-1.jar \
-H:Path=/media/sf_share/foo \
-H:Name=helloworld \
-H:Class=Main \
-H:+ReportUnsupportedElementsAtRuntime \
-H:CLibraryPath=/home/tenshi/graal/graalvm-1.0.0-rc1/jre/lib/svm/clibraries/linux-amd64 \
-H:ReflectionConfigurationFiles=/media/sf_share/foo/reflection.json
]
   classlist:  19,912.83 ms
       (cap):   1,658.35 ms
       setup:   3,449.34 ms
  (typeflow):  17,175.36 ms
   (objects):   6,524.74 ms
  (features):     123.91 ms
    analysis:  24,559.09 ms
    universe:     916.51 ms
     (parse):   4,907.61 ms
    (inline):   3,365.90 ms
   (compile):  24,655.58 ms
     compile:  34,107.23 ms
       image:   2,648.22 ms
       write:     857.81 ms
     [total]:  86,616.39 ms
```