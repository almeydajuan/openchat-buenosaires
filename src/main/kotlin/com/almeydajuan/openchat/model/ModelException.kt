package com.almeydajuan.openchat.model

import java.lang.RuntimeException

class ModelException(val reason: String) : RuntimeException(reason)