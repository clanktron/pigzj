PROGRAM=Main
SRC=$(shell find . -name '*.java')
TARGET_DIR=./target
TEST_DIR=./workdir
INPUT=input-long
INPUT_FILE=$(TEST_DIR)/$(INPUT)
OUTPUT_FILE=$(TEST_DIR)/output-pigzj-multi-$(INPUT)

compile:
	javac $(SRC) -d $(TARGET_DIR)

run: compile
	java -cp $(TARGET_DIR) $(PROGRAM) < $(INPUT_FILE) > $(OUTPUT_FILE)

test: run
	./test.sh pigzj

clean:
	rm -r $(TARGET_DIR)/*
