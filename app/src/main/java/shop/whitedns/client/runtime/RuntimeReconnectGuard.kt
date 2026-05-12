package shop.whitedns.client.runtime

class RuntimeReconnectGuard(
    private val maxFailures: Int,
    private val windowMillis: Long,
) {
    private val failureTimes = ArrayDeque<Long>()

    fun recordFailure(nowMillis: Long = System.currentTimeMillis()): Boolean {
        prune(nowMillis)
        failureTimes.addLast(nowMillis)
        return failureTimes.size <= maxFailures
    }

    fun reset() {
        failureTimes.clear()
    }

    fun failureCount(nowMillis: Long = System.currentTimeMillis()): Int {
        prune(nowMillis)
        return failureTimes.size
    }

    private fun prune(nowMillis: Long) {
        while (failureTimes.isNotEmpty() && nowMillis - failureTimes.first() > windowMillis) {
            failureTimes.removeFirst()
        }
    }
}
