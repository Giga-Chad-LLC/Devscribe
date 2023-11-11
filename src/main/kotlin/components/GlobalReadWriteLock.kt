package components

import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class GlobalReadWriteLock private constructor() {
    private val readWriteLock: ReadWriteLock = ReentrantReadWriteLock()

    private object SingletonHelper {
        val INSTANCE = GlobalReadWriteLock()
    }
    companion object {
        fun getInstance(): GlobalReadWriteLock {
            return SingletonHelper.INSTANCE
        }
    }

    fun lockRead() = readWriteLock.readLock().lock()
    fun unlockRead() = readWriteLock.readLock().unlock()

    fun lockWrite() = readWriteLock.writeLock().lock()
    fun unlockWrite() = readWriteLock.writeLock().unlock()
}