OPENNI = lib/org.openni.jar
NITE = lib/com.primesense.NITE.jar
ONI = res/play.oni

build:
	javac -d bin -sourcepath src -cp $(OPENNI):$(NITE) src/recognition/OnlineRecognizer.java

run: build
	java -cp bin:lib/org.openni.jar:lib/com.primesense.NITE.jar recognition/OnlineRecognizer $(ONI)

clean:
	rm bin/* -r

