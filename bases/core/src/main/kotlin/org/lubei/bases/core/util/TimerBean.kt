package org.lubei.bases.core.util

import com.codahale.metrics.Snapshot
import com.codahale.metrics.Timer
import java.util.concurrent.TimeUnit

/**
 * Timer输出到JSON的辅助类
 *
 * Timer的字段非常多，该类抽取概要字段供输出到JSON
 */
class TimerBean(timer: Timer, snapshot: Snapshot = timer.snapshot) {
    companion object {
        val durationFactor = 1.0 / TimeUnit.MILLISECONDS.toNanos(1L);
        val rateFactor = TimeUnit.SECONDS.toSeconds(1L);

        fun convertDuration(duration: Double): Long = (duration * durationFactor).toLong()
        fun convertDuration(duration: Long): Long = (duration * durationFactor).toLong()
        fun convertRate(rate: Double): Long = (rate * rateFactor).toLong()
        fun build(timer: Timer) = TimerBean(timer)
    }

    val count = timer.count
    val max = convertDuration(snapshot.max)
    val mean = convertDuration(snapshot.mean)
    val min = convertDuration(snapshot.min)
    val stdDev = convertDuration(snapshot.stdDev)
    val p50 = convertDuration(snapshot.median)
    val p75 = convertDuration(snapshot.get75thPercentile())
    val p95 = convertDuration(snapshot.get95thPercentile())
    val p98 = convertDuration(snapshot.get98thPercentile())
    val p99 = convertDuration(snapshot.get99thPercentile())
    val p999 = convertDuration(snapshot.get999thPercentile())
    val rate = convertRate(timer.meanRate)
    val rate1m = convertRate(timer.oneMinuteRate)
    val rate5m = convertRate(timer.fiveMinuteRate)
    val rate15m = convertRate(timer.fifteenMinuteRate)
}