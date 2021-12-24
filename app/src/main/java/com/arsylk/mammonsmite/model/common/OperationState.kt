package com.arsylk.mammonsmite.model.common

import androidx.annotation.FloatRange

data class OperationProgress(
    @FloatRange(from = MIN.toDouble(), to = MAX.toDouble())
    val progress: Float,
) {
    val percentage: Float = (progress / MAX)

    fun startAt(@FloatRange(from = MIN.toDouble(), to = MAX.toDouble()) start: Float) =
        copy(progress = start + (percentage * (MAX - start)) )

    fun endAt(@FloatRange(from = MIN.toDouble(), to = MAX.toDouble()) end: Float) =
        copy(progress = percentage * end)

    fun between(
        @FloatRange(from = MIN.toDouble(), to = MAX.toDouble()) start: Float,
        @FloatRange(from = MIN.toDouble(), to = MAX.toDouble()) end: Float,
    ) =
        copy(progress = start  + (percentage * (end - start)) )

    companion object {
        const val MIN: Float = 0.0f
        const val MAX: Float = 100.0f

        val Initial = OperationProgress(MIN)
        val Finished = OperationProgress(MAX)
    }
}

sealed class OperationStateResult<T: Any?> {
    class Initial<T: Any?> : OperationStateResult<T>()
    data class InProgress<T: Any?>(
        val current: Int,
        val max: Int,
    ) : OperationStateResult<T>() {
        constructor(
            @FloatRange(from = OperationProgress.MIN.toDouble(), to = OperationProgress.MAX.toDouble())
            progress: Float
        ) : this(progress.toInt(), OperationProgress.MAX.toInt())
    }
    sealed class Finished<T: Any?> : OperationStateResult<T>()
    data class Success<T: Any?>(val result: T) : Finished<T>()
    data class Failure<T: Any?>(val t: Throwable) : Finished<T>()

    fun asOperationProgress(): OperationProgress = when (this) {
        is Initial -> OperationProgress.Initial
        is InProgress -> OperationProgress((current.toFloat() / max.toFloat()) * 100.0f)
        is Success -> OperationProgress.Finished
        is Failure -> OperationProgress.Finished
    }
}