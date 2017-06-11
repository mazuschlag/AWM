BIN = ./bin/
SRC = src
LIB = ./lib/system-hook-2.5.jar
PACKAGES = AWMs

JFLAGS = -g -d $(BIN) -sourcepath $(SRC) -classpath $(LIB)
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	./src/AWMs/Client.java \
	./src/AWMs/ClientConnect.java \
	./src/AWMs/Server.java \
	./src/AWMs/ServerConnect.java \
	./src/AWMs/ConnectFlag.java \
	
	
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) $(BIN)$(PACKAGES)/*