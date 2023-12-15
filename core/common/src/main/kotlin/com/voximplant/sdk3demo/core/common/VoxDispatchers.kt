package com.voximplant.sdk3demo.core.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val voxDispatcher: VoxDispatchers)

enum class VoxDispatchers {
    Default,
    IO,
}
