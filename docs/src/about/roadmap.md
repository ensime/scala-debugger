---
weight: 1
---
# Roadmap

Contains the current roadmap for the next significant release to the
Scala debugger API and tooling. The versioning is `MAJOR.MINOR.REVISION`
where a major release indicates breaking API changes, a minor release
indicates new features, and a revision update indicates bug fixes.

---

## Version 1.2.0

- Add expression evaluator
- Improve Scala debug profiles to handle more scenarios

## Version 1.1.0

- Add JDB implementation using Scala Debugger API (SDB)
- Add SBT plugin providing JDB-like interface
- Add basic Scala profile to support name demangling
- Add freeze support to serialize/deserialize debug information
- Improve stability of library

## Version 1.0.0

- Create high-level profile system (only Java profile needed now)
- Create low-level managers for JDI requests
- Add support for pending requests to be applied as soon as possible
- Architect "extra argument" system
- Create documentation website

*[JDI]: Java Debugger Interface
[wiki]: https://github.com/ensime/scala-debugger/wiki
