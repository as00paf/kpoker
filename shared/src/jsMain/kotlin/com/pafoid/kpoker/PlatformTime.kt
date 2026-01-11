package com.pafoid.kpoker

import kotlin.js.Date

actual fun getCurrentTimeMillis(): Long = Date.now().toLong()
