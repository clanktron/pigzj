PROGRAM=Pigzj
SRC=$(shell find . -name '*.java')
OUTPUT_FILE=./output-pigzj

compile:
	javac $(SRC)

run: compile
	java $(PROGRAM) < input > $(OUTPUT_FILE)

test: run
	./test.sh pigzj
