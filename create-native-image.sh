rm -rf lib_managed
rm -rf helloworld

sbt clean package

cp="$(find "." -name '*.jar' | xargs echo | tr ' ' ':')"

echo "classpath is $cp"

native-image --verbose -cp "$cp" -H:Name=helloworld -H:Class=Main  -H:+ReportUnsupportedElementsAtRuntime -H:ReflectionConfigurationFiles=reflection.json

chmod a+x helloworld

./helloworld