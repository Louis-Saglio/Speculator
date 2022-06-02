kill $(ps x | grep "java -jar /home/ubuntu/app.jar" | grep -v grep | awk '{print $1}' | tr '\n' ' ')
