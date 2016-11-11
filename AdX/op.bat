mvn compile
mvn jar:jar
jarsigner -storepass tomerg target\adx-1.3.0.jar MyCert
copy /Y target\adx-1.3.0.jar adx-server\lib
copy /Y target\adx-1.3.0.jar adx-server\public_html\code
