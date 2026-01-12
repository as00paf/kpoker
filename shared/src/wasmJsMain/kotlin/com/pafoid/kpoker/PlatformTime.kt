package com.pafoid.kpoker

import kotlin.time.TimeSource

actual fun getCurrentTimeMillis(): Long = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
