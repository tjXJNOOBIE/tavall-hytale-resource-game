# Android App Module

Future Kotlin/Android inspection and control shell.

The Android app should consume middleware APIs/projections and must not own canonical gameplay state.
Its commands and view actions must enter the server through `FrontendCommandIngressHandler`, then the canonical control pipeline.
