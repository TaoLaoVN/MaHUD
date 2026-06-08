package com.cpumonitor.core.testing

import com.cpumonitor.core.common.dispatcher.DispatchersProvider
import kotlinx.coroutines.CoroutineDispatcher

class TestDispatchersProvider(
    override val io: CoroutineDispatcher,
    override val default: CoroutineDispatcher = io,
    override val main: CoroutineDispatcher = io,
) : DispatchersProvider
