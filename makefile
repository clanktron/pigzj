PROGRAM=Main
SRC=$(shell find . -name '*.java')
TARGET_DIR=./target
TEST_DIR=./test
INPUT_FILE=$(TEST_DIR)/input
OUTPUT_FILE=$(TEST_DIR)/output-pigzj-multi

compile:
	javac $(SRC) -d $(TARGET_DIR)

run: compile
	java -cp $(TARGET_DIR) $(PROGRAM) < $(INPUT_FILE) > $(OUTPUT_FILE)

test: run
	./test.sh pigzj

clean:
	rm -r $(TARGET_DIR)/*
