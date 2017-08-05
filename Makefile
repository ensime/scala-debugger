
.PHONY: \
	build build-scala-2.10 build-scala-2.11 build-scala-2.12 \
	test unit-test-all unit-test-scala-2.10 unit-test-scala-2.11 \
	unit-test-scala-2.12 it-test-all it-test-scala-2.10 it-test-scala-2.11 \
	it-test-scala-2.12 assembly assembly-scala-2.10 assembly-scala-2.11 \
	assembly-scala-2.12 build-docker push-docker docs serve-docs push-docs \
	stats stats-split

# =============================================================================
# = CONFIG SECTION
# =============================================================================

# Binaries
JAVA=$(shell which java 2> /dev/null)
SBT=$(shell which sbt 2> /dev/null)
CLOC=$(shell which cloc 2> /dev/null)
FIND=$(shell which find 2> /dev/null)
DOCKER=$(shell which docker 2> /dev/null)

# Scala config
SCALA_2.10_VERSION=2.10.6
SCALA_2.11_VERSION=2.11.8
SCALA_2.12_VERSION=2.12.1

SCALA_COMPILE=compile
SCALA_ASSEMBLY=assembly
SCALA_UNIT_TEST=test
SCALA_IT_TEST=it:test

SCALA_ASSEMBLY_PROJECTS=scalaDebuggerTool

# Cloc config
CLOC_IGNORE_FILE=.clocignore

# Docker config
DOCKER_IMAGE="chipsenkbeil/scala-debugger:latest"

# Call F_HEADER with text argument
DIVIDER=$(strip ================================================================================)
F_HEADER=\
		 $(info $(DIVIDER))\
		 $(info = $(1))\
		 $(info $(DIVIDER))

# =============================================================================
# = DEFAULT ENTRY
# =============================================================================
all: build

# =============================================================================
# = COMPILE SECTION
# =============================================================================
build: build-scala-2.10 build-scala-2.11 build-scala-2.12

build-scala-2.10:
	@$(SBT) '+++ $(SCALA_2.10_VERSION) $(SCALA_COMPILE)'

build-scala-2.11:
	@$(SBT) '+++ $(SCALA_2.11_VERSION) $(SCALA_COMPILE)'

build-scala-2.12:
	@$(SBT) '+++ $(SCALA_2.12_VERSION) $(SCALA_COMPILE)'

# =============================================================================
# = TEST SECTION
# =============================================================================
test: unit-test-all it-test-all

unit-test-all: unit-test-scala-2.10 unit-test-scala-2.11 unit-test-scala-2.12

unit-test-scala-2.10:
	@$(SBT) '+++$(SCALA_2.10_VERSION) $(SCALA_UNIT_TEST)'

unit-test-scala-2.11:
	@$(SBT) '+++$(SCALA_2.11_VERSION) $(SCALA_UNIT_TEST)'

unit-test-scala-2.12:
	@$(SBT) '+++$(SCALA_2.12_VERSION) $(SCALA_UNIT_TEST)'

it-test-all: it-test-scala-2.10 it-test-scala-2.11 it-test-scala-2.12

it-test-scala-2.10:
	@$(SBT) '+++$(SCALA_2.10_VERSION) $(SCALA_IT_TEST)'

it-test-scala-2.11:
	@$(SBT) '+++$(SCALA_2.11_VERSION) $(SCALA_IT_TEST)'

it-test-scala-2.12:
	@$(SBT) '+++$(SCALA_2.12_VERSION) $(SCALA_IT_TEST)'

# =============================================================================
# = ASSEMBLY SECTION
# =============================================================================
assembly: assembly-scala-2.10 assembly-scala-2.11 assembly-scala-2.12

assembly-scala-2.10:
	@$(foreach p,$(SCALA_ASSEMBLY_PROJECTS),$(SBT) '+++ $(SCALA_2.10_VERSION) $(p)/$(SCALA_ASSEMBLY)')

assembly-scala-2.11:
	@$(foreach p,$(SCALA_ASSEMBLY_PROJECTS),$(SBT) '+++ $(SCALA_2.11_VERSION) $(p)/$(SCALA_ASSEMBLY)')

assembly-scala-2.12:
	@$(foreach p,$(SCALA_ASSEMBLY_PROJECTS),$(SBT) '+++ $(SCALA_2.12_VERSION) $(p)/$(SCALA_ASSEMBLY)')

# =============================================================================
# = DOCKER SECTION
# =============================================================================
build-docker:
	@$(DOCKER) build -t $(DOCKER_IMAGE) .

push-docker:
	@$(DOCKER) login
	@$(DOCKER) push $(DOCKER_IMAGE)

# =============================================================================
# = DOCS SECTION
# =============================================================================
docs: 
	@$(SBT) generateSite

serve-docs: 
	@$(SBT) serveSite

push-docs: docs
	@$(SBT) publishSite

# =============================================================================
# = STATISTICS SECTION
# =============================================================================
stats:
	@$(info All Code)
	@$(CLOC) \
		--exclude-dir=$(shell tr '\n' ',' < $(CLOC_IGNORE_FILE)) \
		.

stats-split: stats-main stats-unit-test stats-it-test

stats-main:
	@$(call F_HEADER,Main Code)
	@$(CLOC) \
		--exclude-dir=$(shell tr '\n' ',' < $(CLOC_IGNORE_FILE)) \
		$(shell $(FIND) . -path "*/src/main" -type d -not -path "*/target/*")
stats-unit-test:
	@$(call F_HEADER,Unit Test Code)
	@$(CLOC) \
		--exclude-dir=$(shell tr '\n' ',' < $(CLOC_IGNORE_FILE)) \
		$(shell $(FIND) . -path "*/src/test" -type d -not -path "*/target/*")
stats-it-test:
	@$(call F_HEADER,Integration Test Code)
	@$(CLOC) \
		--exclude-dir=$(shell tr '\n' ',' < $(CLOC_IGNORE_FILE)) \
		$(shell $(FIND) . -path "*/src/it" -type d -not -path "*/target/*")
