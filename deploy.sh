export JAVA_HOME=/home/louis/.jdks/corretto-17.0.3/
echo "Building jar"
./gradlew shadowJar
echo "Uploading jar to the server"
scp -C -P 50152 /home/louis/IdeaProjects/Speculator/build/libs/Speculator-0.0.1-all.jar ubuntu@37.187.200.9:/home/ubuntu/app.jar
echo "Uploading kill script"
scp -C -P 50152 /home/louis/IdeaProjects/Speculator/kill-app.sh ubuntu@37.187.200.9:/home/ubuntu/kill-app.sh
echo "Shutting down current app"
ssh -f -p 50152 ubuntu@37.187.200.9 bash kill-app.sh
sleep 3
echo "Starting app with uploaded jar"
ssh -f -p 50152 ubuntu@37.187.200.9 "java -jar /home/ubuntu/app.jar > /var/log/app.log"
echo "Done"
