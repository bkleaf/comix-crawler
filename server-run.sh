rm -f nohup.out
nohup mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=product -Djava.security.egd=file:/dev/./urandom" &
sleep 2
tail -f nohup.out