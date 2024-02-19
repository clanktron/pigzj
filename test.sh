#!/bin/sh
CLI="${1:-gzip}"
INPUT=input
OUTPUT=output-"$CLI"
TEST_FILE=test-"$CLI"

echo "Compressed result:"
cat "$OUTPUT" && echo
# "$CLI" < "$INPUT" > "$OUTPUT" && \
gzip -d < "$OUTPUT" > "$TEST_FILE" && \
if diff "$INPUT" "$TEST_FILE"; then
    echo "success"
else
    echo "fail"
fi
