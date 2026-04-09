.PHONY: build test install deploy clean help

help:
	@echo "Available targets:"
	@echo "  make build   - Compile all modules"
	@echo "  make test    - Run tests"
	@echo "  make install - Install to local ~/.m2"
	@echo "  make deploy  - Build, test, deploy to GitLab Registry"
	@echo "  make clean   - Clean build artifacts"

build:
	mvn compile

test:
	mvn test

install:
	mvn install

deploy:
	mvn clean deploy

clean:
	mvn clean
