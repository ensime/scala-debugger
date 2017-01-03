# Release Notes

---

## Version 1.0.0 (2015-12-23)

- Added high-level pure profile
    - Implemented caching in pure profile traits
- Added high-level swappable profile used by `ScalaVirtualMachine` to
  give the appearance of one API but utilize different APIs underneath
  that can be decided upon programmatically
- Added pipelines to simulate stream of events in same fashion as
  Apache Spark streaming
- Added `JDIArgument` support in low-level and high-level APIs
    - Implemented filters and filter processing for events
    - Implemented data retrieval and data retrieval processing for events
    - Implemented property/filter configuring for requests
- Added dummy implementations of low-level APIs
    - Implemented utilization of dummy APIs to set desired requests
      before virtual machine is started
- Added profile manager to organize available profiles
- Completed missing low-level request management
    - Added low-level access watchpoint request management
    - Added low-level class prepare request management
    - Added low-level class unload request management
    - Added low-level exception request management
    - Added low-level modification watchpoint request management
    - Added low-level monitor contended entered request management
    - Added low-level monitor contended enter request management
    - Added low-level monitor waited request management
    - Added low-level monitor wait request management
    - Added low-level method entry request management
    - Added low-level method exit request management
    - Added low-level thread death request management
    - Added low-level thread start request management
    - Added low-level vm death request management

---

## Version senkbeil (2015-07-11)

!!! note "Note:"
    Released under `org.senkbeil` as version `1.0.0`.

    This was the only version released using the `org.senkbeil` name and is
    incompatible with any other release.

- Added low-level breakpoint creation and event handling
- Added low-level step creation and event handling
- Added wrappers around stack frames, thread references, reference types,
  and values

