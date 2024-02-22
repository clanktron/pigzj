PROGRAM=Pigzj
SRC=$(shell find . -name '*.java')
TARGET_DIR=./target
INPUT_FILE=./input-large
OUTPUT_FILE=./output-large
TEST_FILE=./test-large
JAR=hw3.jar
README=README.txt

compile:
	javac $(SRC) -d $(TARGET_DIR)

run: compile
	java -cp $(TARGET_DIR) $(PROGRAM) < $(INPUT_FILE) > $(OUTPUT_FILE)

test: run
	gzip -d < $(OUTPUT_FILE) > $(TEST_FILE) && if diff $(INPUT_FILE) $(TEST_FILE); then echo "success"; else echo "fail"; fi

jar:
	cd src && jar cf ../$(JAR) * ../$(README)

clean:
	rm -rf $(TARGET_DIR)/* $(OUTPUT_FILE) $(TEST_FILE)
