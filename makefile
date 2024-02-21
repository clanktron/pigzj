PROGRAM=Pigzj
SRC=$(shell find . -name '*.java')
TARGET_DIR=./target
INPUT=input
INPUT_FILE=./input
OUTPUT_FILE=./output
TEST_FILE=./test

compile:
	javac $(SRC) -d $(TARGET_DIR)

run: compile
	java -cp $(TARGET_DIR) $(PROGRAM) < $(INPUT_FILE) > $(OUTPUT_FILE)

test: run
	gzip -d < $(OUTPUT_FILE) > $(TEST_FILE) && if diff $(INPUT) $(TEST_FILE); then echo "success"; else echo "fail"; fi

clean:
	rm -rf $(TARGET_DIR)/* $(OUTPUT_FILE) $(TEST_FILE)
